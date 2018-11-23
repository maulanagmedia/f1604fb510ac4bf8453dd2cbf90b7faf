package com.leonardus.irfan.bluetoothprinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PspPrinter extends BluetoothPrinter {
    /*
        BLUETOOTH PRINTER PSP
        Library untuk menggunakan bluetooth printer. Langkah menggunakan :
        1. Buat objek BluetoothPrinter dengan menggunakan keyword new dengan parameter input context
            (ex : btPrint = new BluetoothPrinter(this))
        2. panggil method startService untuk menginisialisasi object bluetooth printer
            (ex : btnPrint.startService())
        3. panggil method showDevices untuk melakukan koneksi dengan device bluetooth printer
            (ex : btPrint.showDevices())
        4. panggil method print dengan parameter transaksi untuk mencetak nota di device
            (ex :
            Calendar date = Calendar.getInstance();
            List<Item> listTransaksi = new ArrayList<>();
            listTransaksi.add(new Item("Denom 5k", 20, 5500));
            listTransaksi.add(new Item("Denom 25k", 10, 20500));
            listTransaksi.add(new Item("Denom 100k", 5, 97000));
            Transaksi t = new Transaksi("Yunma Jaya Cell", "Andi Kusworo", "PD001", date.getTime(), listTransaksi)
            btPrint.print(transaksi))
        5. panggil method stopService untuk mengakhiri koneksi, saran : gunakan di method onDestroy Activity
            (ex : btPrint.stopService())
    */

    public PspPrinter(Context context){
        super(context);
    }

    public boolean isPrinterReady(){

        return bluetoothDevice != null;
    }

    // this will send text data to be printed by the bluetooth printer
    public void print(Transaksi transaksi){
        final int NAMA_MAX = 15;
        final int JUMLAH_MAX = 4;
        final int HARGA_TOTAL_MAX = 11;

        if(bluetoothDevice == null){
            Toast.makeText(context, "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double jum = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            //PROSES CETAK HEADER
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.ALIGN_CENTER);
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.psp_header);
            byte[] bmp_byte = PrintFormatter.decodeBitmap(bmp);
            if(bmp_byte != null){
                outputStream.write(bmp_byte);
            }
            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);

            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write(String.format("Reseller    :  %s\n", transaksi.getOutlet()).getBytes());
            outputStream.write(String.format("Sales       :  %s\n", transaksi.getSales()).getBytes());
            outputStream.write(String.format("No. Nota    :  %s\n", transaksi.getNo_nota()).getBytes());
            outputStream.write(String.format("Tgl Nota    :  %s\n", transaksi.getTglTransaksi()).getBytes());

            outputStream.write(PrintFormatter.NEW_LINE);

            //PROSES CETAK TRANSAKSI
            outputStream.write("--------------------------------\n".getBytes());
            outputStream.write(PrintFormatter.ALIGN_LEFT);
            outputStream.write("Nama Barang    Jumlah      Total\n".getBytes());
            outputStream.write("--------------------------------\n".getBytes());
            outputStream.write(PrintFormatter.ALIGN_LEFT);

            List<Item> listItem = transaksi.getListItem();
            for(int i = 0; i < listItem.size(); i++){
                Item t =  listItem.get(i);
                String nama = t.getNama();
                String jumlah = t.getJumlah();
                String harga_total = RupiahFormatter.getRupiah(t.getHarga()/**t.getJumlah()*/);

                int n = 1;
                if(nama.length() > NAMA_MAX){
                    n = Math.max((int)Math.ceil((double)nama.length()/(double)NAMA_MAX), n);
                }
                if(jumlah.length() > JUMLAH_MAX){
                    n = Math.max((int)Math.ceil((double)jumlah.length()/(double)JUMLAH_MAX), n);
                }
                if(harga_total.length() > HARGA_TOTAL_MAX){
                    n = Math.max((int)Math.ceil((double)harga_total.length()/(double)HARGA_TOTAL_MAX), n);
                }

                String[] nama_array = leftAligned(nama, NAMA_MAX, n);
                String[] jumlah_array = leftAligned(jumlah, JUMLAH_MAX, n);
                String[] harga_total_array = rightAligned(harga_total, HARGA_TOTAL_MAX, n);

                for(int j = 0; j < n; j++){
                    outputStream.write(String.format(Locale.getDefault(), "%s %s %s\n", nama_array[j], jumlah_array[j], harga_total_array[j]).getBytes());
                }

                jum += t.getHarga()/**t.getJumlah()*/;
            }

            transaksi.setTunai(jum); //tunai selalu sama dengan jumlah
            String jum_string, tunai_string;
            //String kembali_string;
            jum_string = RupiahFormatter.getRupiah(jum);
            tunai_string = RupiahFormatter.getRupiah(transaksi.getTunai());
            //kembali_string = RupiahFormatter.getRupiah(transaksi.getTunai() - jum);

            outputStream.write(PrintFormatter.ALIGN_RIGHT);
            outputStream.write("----------".getBytes());
            outputStream.write("\nTOTAL :  ".getBytes());
            outputStream.write(jum_string.getBytes());
            outputStream.write("\nTUNAI :  ".getBytes());
            outputStream.write(tunai_string.getBytes());
            /*outputStream.write("\nKEMBALI : ".getBytes());
            for(int i = 0; i < character_size - kembali_string.length(); i++){
                outputStream.write(" ".getBytes());
            }
            outputStream.write(kembali_string.getBytes());*/

            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);

            //PROSES CETAK FOOTER
            outputStream.write(PrintFormatter.ALIGN_CENTER);
            outputStream.write("Terima Kasih\n".getBytes());

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(transaksi.getTglNota());

            outputStream.write(String.format("%s\n", currentDateandTime).getBytes());
            outputStream.write("==============================\n".getBytes());
            outputStream.write(PrintFormatter.DEFAULT_STYLE);
            outputStream.write(PrintFormatter.NEW_LINE);
            outputStream.write(PrintFormatter.NEW_LINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] leftAligned(String s, int max_length, int n){
        //Mencetak transaksi secara rata kiri
        String[] result = new String[n];
        int counter = 0;
        for(int i = 0; i < n; i++){
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < max_length; j++){
                if(counter < s.length()){
                    builder.append(s.charAt(counter));
                    counter++;
                }
                else{
                    builder.append(" ");
                }
            }
            result[i] = builder.toString();
            System.out.println(result[i]);
        }
        return result;
    }

    private String[] rightAligned(String s, int max_length, int n){
        //Mencekak transaksi secara rata kanan
        String[] result = new String[n];
        int counter = 0;
        for(int i = 0; i < n; i++) {
            StringBuilder builder = new StringBuilder();
            if (counter >= s.length()) {
                for (int j = 0; j < max_length; j++) {
                    builder.append(" ");
                }
            } else if (s.length() - i * max_length < max_length) {
                int pad = max_length - (s.length() - i * max_length);
                for (int j = 0; j < max_length; j++) {
                    if (j < pad) {
                        builder.append(" ");
                    } else {
                        builder.append(s.charAt(counter));
                        counter++;
                    }
                }
            } else {
                for (int j = 0; j < max_length; j++) {
                    builder.append(s.charAt(counter));
                    counter++;
                }
            }
            result[i] = builder.toString();
            System.out.println(result[i]);
        }
        return result;
    }
}
