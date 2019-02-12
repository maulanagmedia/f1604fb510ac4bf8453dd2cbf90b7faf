package com.leonardus.irfan.bluetoothprinter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinter {
    /*
        BLUETOOTH PRINTER
        Library untuk menggunakan bluetooth printer. Langkah menggunakan :
        1. Buat objek BluetoothPrinter dengan menggunakan keyword new dengan parameter input context
            (ex : btPrint = new BluetoothPrinter(this))
        2. panggil method startService untuk menginisialisasi object bluetooth printer
            (ex : btnPrint.startService())
        3. panggil method showDevices untuk melakukan koneksi dengan device bluetooth printer
            (ex : btPrint.showDevices())
        4. panggil method print dengan parameter input pesan untuk mencetak pesan di device
            (ex : btPrint.print("Test Printing"))
        5. panggil method stopService untuk mengakhiri koneksi, saran : gunakan di method onDestroy Activity
            (ex : btPrint.stopService())
    */


    private final UUID BLUETOOTH_PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    Context context;

    public BluetoothAdapter bluetoothAdapter;
    public static BluetoothSocket socket;
    public static BluetoothDevice bluetoothDevice;
    public static OutputStream outputStream;
    private InputStream inputStream;
    private ProgressBar progressbar;
    private Button btn_devices;

    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private Dialog dialogDevices;
    public Dialog dialogBluetooth;
    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<String> deviceAdapter;
    private List<String> listDevicesData = new ArrayList<>();
    private List<BluetoothDevice> listDevices = new ArrayList<>();

    private ArrayAdapter<String> discoveredAdapter;
    private List<String> listDiscoveredData = new ArrayList<>();
    private List<BluetoothDevice> listDiscovered = new ArrayList<>();

    public BluetoothPrinter(Context context){
        this.context = context;
    }

    public void startService(){
        //Inisialisasi UI
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bluetooth tidak menyala");
        builder.setMessage("Bluetooth anda tidak menyala. Nyalakan bluetooth sekarang?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Meminta user menyalakan bluetooth
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                BluetoothPrinter.this.context.startActivity(intentOpenBluetoothSettings);
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialogBluetooth = builder.create();

        //Inisialisasi Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(context, "Adapter Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show();
        }
        /*if(!bluetoothAdapter.isEnabled()) {
            dialogBluetooth.show();
        }*/

        //init dialog UI
        dialogDevices = new Dialog(context);
        dialogDevices.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDevices.setContentView(R.layout.popup_devices);
        ListView list_devices = dialogDevices.findViewById(R.id.list_devices);
        ListView list_discovered = dialogDevices.findViewById(R.id.list_discovered);
        btn_devices = dialogDevices.findViewById(R.id.btn_devices);
        progressbar = dialogDevices.findViewById(R.id.progressbar);

        deviceAdapter = new ArrayAdapter<>(context, R.layout.item_devices, R.id.txt_device, listDevicesData);
        list_devices.setAdapter(deviceAdapter);
        list_devices.setOnItemClickListener(new DeviceClicked());

        discoveredAdapter = new ArrayAdapter<>(context, R.layout.item_devices, R.id.txt_device, listDiscoveredData);
        list_discovered.setAdapter(discoveredAdapter);
        list_discovered.setOnItemClickListener(new DiscoveredClicked());

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(broadcastReceiver, filter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        listDiscovered.add(device);
                        listDiscoveredData.add(device.getName() + "\n" + device.getAddress());
                        discoveredAdapter.notifyDataSetChanged();
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressbar.setVisibility(View.GONE);
                    btn_devices.setText(R.string.cari_device);
                    //Toast.makeText(context, "Pencarian Device Selesai", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btn_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isDiscovering()) {

                    progressbar.setVisibility(View.GONE);
                    bluetoothAdapter.cancelDiscovery();
                    btn_devices.setText(R.string.cari_device);
                    //mBluetoothAdapter.startDiscovery();
                }
                else {
                    btn_devices.setText(R.string.berhenti);
                    progressbar.setVisibility(View.VISIBLE);
                    bluetoothAdapter.startDiscovery();
                    context.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                }
            }
        });
    }

    public void showDevices(){
        if(!bluetoothAdapter.isEnabled()) {
            dialogBluetooth.show();
            return;
        }

        listDiscovered.clear();
        listDiscoveredData.clear();
        discoveredAdapter.notifyDataSetChanged();

        initDevices();
        dialogDevices.show();
    }

    public void stopService(){
        try {
            closeBT();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
            context.unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createBond(BluetoothDevice device)throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        if(!returnValue){
            Toast.makeText(context, "Pairing device gagal!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initDevices(){
        listDevices.clear();
        listDevicesData.clear();

        final Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        Object[] objectList = paired.toArray();

        if(objectList.length > 0) {
            for (Object device : objectList) {
                BluetoothDevice bluetooth = (BluetoothDevice) device;
                if(bluetooth.getUuids()[0].getUuid().equals(BLUETOOTH_PRINTER_UUID)){
                    listDevicesData.add(bluetooth.getName() + "\n" + bluetooth.getAddress());
                    listDevices.add(bluetooth);
                }
            }
        }

        deviceAdapter.notifyDataSetChanged();
    }

    private void connectBluetooth() throws IOException {

        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_PRINTER_UUID);
            socket.connect();
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            beginListenForData();

            Toast.makeText(context, "Device Bluetooth Printer tersambung", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

            bluetoothDevice = null;
            Toast.makeText(context, "Device Bluetooth Printer gagal tersambung", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void beginListenForData(){
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this will send text data to be printed by the bluetooth printer
    public void print(String msg){
        if(bluetoothDevice == null){
            Toast.makeText(context, "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            msg += "\n";
            outputStream.write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // close the connection to bluetooth printer.
    private void closeBT() throws IOException {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DeviceClicked implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice selected_device = listDevices.get(position);
            try {

                if(selected_device.getUuids()[0].getUuid().equals(BLUETOOTH_PRINTER_UUID)){
                    try{
                        bluetoothDevice = selected_device;
                        connectBluetooth();
                        dialogDevices.dismiss();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(context, "Device bukan Device Printer, coba Bluetooth lain", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class DiscoveredClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Pairing dengan Device
            bluetoothAdapter.cancelDiscovery();
            progressbar.setVisibility(View.GONE);
            btn_devices.setText(R.string.cari_device);

            BluetoothDevice device = listDiscovered.get(position);

            try {
                createBond(device);
                dialogDevices.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
