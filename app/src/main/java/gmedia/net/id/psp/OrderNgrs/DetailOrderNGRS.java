package gmedia.net.id.psp.OrderNgrs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.ApiVolley;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.Utils.FormatItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailOrderNGRS extends AppCompatActivity implements LocationListener {

    private EditText edtNamaReseller, edtNominal, edtHarga, edtKeterangan;
    private Button btnProses;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private RadioGroup rgCrBayar;
    private RadioButton rbTunai;
    private RadioButton rbTransfer;
    private RadioButton rbDeposit;
    private LinearLayout llTransfer;
    private EditText edtBank, edtRekening;
    private String nikMKIOS = "", kode = "";
    private String nomorRS, namaRS, levelRS, noUpline, pinRS, pinUpline;
    private final String TAG = "TAG";
    private String hargaTcash = "0";
    private String nonota = "";

    // Location
    private double latitude, longitude;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location;
    private final int REQUEST_PERMISSION_COARSE_LOCATION=2;
    private final int REQUEST_PERMISSION_FINE_LOCATION=3;
    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private LinearLayout llNonota;
    private EditText edtNonota;
    private EditText edtJarak;
    private ImageView ivRefreshPosition;

    private String jarak = "",range = "", latitudeOutlet = "", longitudeOutlet = "";
    private Button btnMapsOutlet;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationSettingsRequest mLocationSettingsRequest;
    private SettingsClient mSettingsClient;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Boolean mRequestingLocationUpdates;
    private Location mCurrentLocation;
    private PspPrinter printer;
    private Context context;
    private Activity activity;
    private Button btnCetakDetail;
    private String tglTransaksi;
    private String namaSales = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_ngrs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Order NGRS");

        // getLocation update by google
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        context = this;
        activity = this;
        printer = new PspPrinter(context);
        printer.startService();

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MockLocChecker checker = new MockLocChecker(activity);
    }

   /* @Override
    protected void onDestroy() {
        printer.stopService();
        super.onDestroy();
    }*/

    private void initUI() {

        llNonota = (LinearLayout) findViewById(R.id.ll_nonota);
        edtNonota = (EditText) findViewById(R.id.edt_nonota);
        edtNamaReseller = (EditText) findViewById(R.id.edt_nama_reseller);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        edtNominal = (EditText) findViewById(R.id.edt_nominal);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        rgCrBayar = (RadioGroup) findViewById(R.id.rg_crbayar);
        rbTunai = (RadioButton) findViewById(R.id.rb_tunai);
        rbTransfer = (RadioButton) findViewById(R.id.rb_transfer);
        rbDeposit = (RadioButton) findViewById(R.id.rb_deposit);
        llTransfer = (LinearLayout) findViewById(R.id.ll_transfer);
        edtBank = (EditText) findViewById(R.id.edt_bank);
        edtRekening = (EditText) findViewById(R.id.edt_rekening);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        btnProses = (Button) findViewById(R.id.btn_proses);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_outlet);
        btnCetakDetail = (Button) findViewById(R.id.btn_cetak);

        session = new SessionManager(activity);
        nikMKIOS = session.getUserInfo(SessionManager.TAG_NIK);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kode = bundle.getString("kode", "");
            nonota = bundle.getString("nonota", "");

            if(!nonota.isEmpty()){

                namaSales = bundle.getString("namasales",session.getUser());
                edtNominal.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtNominal.setFocusable(false);
                btnProses.setEnabled(false);
                btnProses.setVisibility(View.GONE);
                btnCetakDetail.setVisibility(View.VISIBLE);
                edtNonota.setText(nonota);
                edtKeterangan.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtKeterangan.setFocusable(false);
                tglTransaksi = bundle.getString("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));

                getDetailOrder();
            }else{

                if(kode != null && kode.length() > 0){

                    llNonota.setVisibility(View.GONE);
                    getRSDetail();

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    setCriteria();
                    latitude = 0;
                    longitude = 0;
                    location = new Location("set");
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    //location = getLocation();
                    updateAllLocation();

                    initEvent();
                }
            }
        }
    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                onLocationChanged(mCurrentLocation);
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.

        isUpdateLocation = true;
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        isUpdateLocation = false;
                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location clocation) {

                                        if (clocation != null) {

                                            onLocationChanged(clocation);
                                        }else{
                                            location = getLocation();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                                //refreshMode = false;
                        }

                        //get Location
                        isUpdateLocation = false;
                        location = getLocation();
                    }
                });
    }

    private void updateAllLocation(){
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHECK_SETTINGS){

            if(resultCode == Activity.RESULT_CANCELED){

                mRequestingLocationUpdates = false;
            }else if(resultCode == Activity.RESULT_OK){

                startLocationUpdates();
            }

        }
    }

    private void getDetailOrder() {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nonota", nonota);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(activity, jBody, "POST", ServerURL.getDetailPenjualanNgrs, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan saat memuat data, harap ulangi";

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            namaRS = jo.getString("nama");
                            edtNamaReseller.setText(namaRS);
                            edtNominal.setText(jo.getString("value_tcash"));
                            hargaTcash = jo.getString("hargatcash");
                            edtHarga.setText(iv.ChangeToRupiahFormat(hargaTcash));
                            edtKeterangan.setText(jo.getString("pesan"));
                            break;
                        }
                    }else{
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }

                    pbProses.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbProses.setVisibility(View.GONE);

                }

                setCetakORder();
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void setCetakORder() {

        btnCetakDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!nonota.isEmpty()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View viewDialog = inflater.inflate(R.layout.dialog_cetak, null);
                    builder.setView(viewDialog);
                    builder.setCancelable(false);

                    final Button btnTutup = (Button) viewDialog.findViewById(R.id.btn_tutup);
                    final Button btnCetak = (Button) viewDialog.findViewById(R.id.btn_cetak);

                    final AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    List<Item> items = new ArrayList<>();
                    items.add(new Item("Tcash", "-", iv.parseNullDouble(hargaTcash)));

                    Calendar date = Calendar.getInstance();
                    final Transaksi transaksi = new Transaksi(namaRS, namaSales, nonota, date.getTime(), items, iv.ChangeFormatDateString(tglTransaksi, FormatItem.formatTimestamp, FormatItem.formatDateDisplay2));

                    btnTutup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view2) {

                            if(alert != null){

                                try {

                                    alert.dismiss();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            /*Intent intent = new Intent(DetailTcashOrder.this, ActOrderTcash.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();*/
                        }
                    });

                    btnCetak.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(!printer.bluetoothAdapter.isEnabled()) {

                                printer.dialogBluetooth.show();
                                Toast.makeText(context, "Hidupkan bluetooth anda kemudian klik cetak kembali", Toast.LENGTH_LONG).show();
                            }else{

                                if(printer.isPrinterReady()){

                                    printer.print(transaksi);

                                }else{

                                    Toast.makeText(context, "Harap pilih device printer telebih dahulu", Toast.LENGTH_LONG).show();
                                    printer.showDevices();
                                }
                            }
                        }
                    });

                    try {
                        alert.show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private boolean isOnLocation(Location detectedLocation){

        boolean hasil = false;

        if(jarak != "" && range != "" && latitudeOutlet != "" && longitudeOutlet != "" && detectedLocation != null){

            double latOutlet = iv.parseNullDouble(latitudeOutlet);
            double longOutlet = iv.parseNullDouble(longitudeOutlet);

            double detectedJarak = (6371 * Math.acos(Math.sin(Math.toRadians(latOutlet)) * Math.sin(Math.toRadians(detectedLocation.getLatitude())) + Math.cos(Math.toRadians(longOutlet - detectedLocation.getLongitude())) * Math.cos(Math.toRadians(latOutlet)) * Math.cos(Math.toRadians(detectedLocation.getLatitude()))));
            double rangeDouble = iv.parseNullDouble(range);

            if(detectedJarak <= rangeDouble) hasil = true;
        }

        return hasil;
    }

    public Location getLocation() {
        isUpdateLocation = false;
        try {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
                Toast.makeText(activity, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        }
                        isUpdateLocation = false;
                        return null;
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");

                    if (locationManager != null) {
                        if(jarak != ""){ // Jarak sudah terdeteksi

                            Location locationBuffer = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if(isOnLocation(locationBuffer)){

                                location = locationBuffer;
                            }

                        }else{
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                        if (location != null) {
                            //onLocationChanged(location);
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");

                    if (locationManager != null) {

                        if(jarak != ""){ // Jarak sudah terdeteksi

                            Location locationBuffer = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if(isOnLocation(locationBuffer)){

                                location = locationBuffer;
                            }

                        }else{
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }

                        if (location != null) {
                            //onLocationChanged(location);
                        }
                    }
                }else{
                    //Toast.makeText(context, "Turn on your GPS for better accuracy", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        isUpdateLocation = false;
        if(location != null){
            onLocationChanged(location);
        }

        return location;
    }

    public void setCriteria() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        provider = locationManager.getBestProvider(criteria, true);
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void getRSDetail() {

        isLoading(true);
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        ApiVolley request = new ApiVolley(activity, new JSONObject(), "GET", ServerURL.getReseller+nik+"/"+kode, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            nomorRS = jo.getString("nomor");
                            namaRS = jo.getString("nama");
                            levelRS = jo.getString("level");
                            noUpline = jo.getString("nomor_upline");
                            pinRS = jo.getString("pin");
                            pinUpline = jo.getString("pin_upline");

                            edtNamaReseller.setText(namaRS);
                            break;
                        }
                    }

                    pbProses.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbProses.setVisibility(View.GONE);
                }
                isLoading(false);
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
                isLoading(false);
            }
        });
    }

    private void saveData() {

        final ProgressDialog progressDialog = new ProgressDialog(activity, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnProses.setEnabled(false);
        isLoading(true);

        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jData = new JSONObject();

        try {
            jData.put("status", "PENDING");
            jData.put("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
            jData.put("kode_cv", nik);
            jData.put("kode", kode);
            jData.put("nomor", nomorRS);
            jData.put("nama", namaRS);
            jData.put("level", levelRS);
            jData.put("nomor_upline", noUpline);
            jData.put("pin", pinRS);
            jData.put("pin_upline", pinUpline);
            jData.put("pesan", edtKeterangan.getText().toString());
            jData.put("crbayar", "ISI");
            jData.put("value_tcash", edtNominal.getText().toString());
            jData.put("hargatcash", hargaTcash);
            jData.put("total", hargaTcash);
            jData.put("keterangan", "-");
            jData.put("keterangan_order", "NGRS " + edtNominal.getText().toString());
            jData.put("flag", "1");
            jData.put("order_format", "*800*500#");
            jData.put("status_transaksi", "INJECK");
            jData.put("flag_injek", "SMS");
            jData.put("proses", "1");
            jData.put("nonota", "");
            jData.put("cluster", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version = pInfo.versionName;

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("version", version);
            jBody.put("data", jData);
            jBody.put("kode", kode);
            jBody.put("nomor", nomorRS);
            jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
            jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ServerURL.saveNGRS, method = "POST";

        ApiVolley request = new ApiVolley(activity, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                btnProses.setEnabled(true);
                progressDialog.dismiss();

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String superMessage = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        String nonota = response.getJSONObject("response").getString("nobukti");
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();

                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View viewDialog = inflater.inflate(R.layout.dialog_cetak, null);
                        builder.setView(viewDialog);
                        builder.setCancelable(false);

                        final Button btnTutup = (Button) viewDialog.findViewById(R.id.btn_tutup);
                        final Button btnCetak = (Button) viewDialog.findViewById(R.id.btn_cetak);

                        final AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                        List<Item> items = new ArrayList<>();
                        items.add(new Item("NGRS ", "-", iv.parseNullDouble(hargaTcash)));

                        Calendar date = Calendar.getInstance();
                        final Transaksi transaksi = new Transaksi(namaRS, session.getUser(), nonota, date.getTime(), items, iv.getCurrentDate(FormatItem.formatDateDisplay2));

                        btnTutup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view2) {

                                if(alert != null){

                                    try {

                                        alert.dismiss();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }

                                Intent intent = new Intent(activity, ActOrderNGRS.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });

                        btnCetak.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(!printer.bluetoothAdapter.isEnabled()) {

                                    printer.dialogBluetooth.show();
                                    Toast.makeText(context, "Hidupkan bluetooth anda kemudian klik cetak kembali", Toast.LENGTH_LONG).show();
                                }else{

                                    if(printer.isPrinterReady()){

                                        printer.print(transaksi);

                                    }else{

                                        Toast.makeText(context, "Harap pilih device printer telebih dahulu", Toast.LENGTH_LONG).show();
                                        printer.showDevices();
                                    }
                                }
                            }
                        });

                        try {
                            alert.show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(activity, superMessage, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
                isLoading(false);
                btnProses.setEnabled(true);
                progressDialog.dismiss();
            }
        });
    }

    private void initEvent() {

        rgCrBayar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == rbTransfer.getId()){
                    llTransfer.setVisibility(View.VISIBLE);
                }else{
                    llTransfer.setVisibility(View.GONE);
                }
            }
        });

        edtNominal.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                final String currentValue = editable.toString();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(currentValue.equals(edtNominal.getText().toString())){
                            getHarga();
                        }
                    }
                }, 1000);
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validate before save
                if(iv.parseNullLong(edtNominal.getText().toString()) == 0){
                    edtNominal.setText("");
                    hargaTcash = "0";
                    updateHarga(hargaTcash);
                    edtNominal.setError("Nominal tidak boleh kosong");
                    edtNominal.requestFocus();
                    return;
                }else{
                    edtNominal.setError(null);
                }

                if(hargaTcash.equals("0")){
                    Toast.makeText(activity, "Harga belum termuat, harap ulangi proses", Toast.LENGTH_LONG).show();
                    return;
                }

                if(jarak.equals("")){

                    Toast.makeText(activity, "Mohon tunggu hinggan posisi diketahui / tekan refresh pada bagian jarak", Toast.LENGTH_LONG).show();
                    edtJarak.requestFocus();
                    return;
                }

                if(location == null){

                    Toast.makeText(activity, "Harap tunggu hingga posisi diketahui", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog builder = new AlertDialog.Builder(activity)
                        .setTitle("Konfimasi")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Apakah anda yakin ingin memproses order ini ? (" +iv.ChangeToCurrencyFormat(edtNominal.getText().toString())+")")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveData();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        ivRefreshPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isUpdateLocation){
                    updateAllLocation();
                }
            }
        });

        btnMapsOutlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(latitudeOutlet != "" && longitudeOutlet != ""){

                    Intent intent = new Intent(activity, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latitudeOutlet);
                    intent.putExtra("long_outlet", longitudeOutlet);
                    intent.putExtra("nama", namaRS);

                    startActivity(intent);
                }else{

                    Toast.makeText(activity, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateHarga(String harga){
        hargaTcash = harga;
        edtHarga.setText(iv.ChangeToRupiahFormat(harga));
    }

    private void isLoading(boolean loading){

        if(loading){
            edtNominal.setEnabled(false);
            btnProses.setEnabled(false);
        }else{
            edtNominal.setEnabled(true);
            btnProses.setEnabled(true);
        }

    }

    private void getHarga(){

        if(iv.parseNullLong(edtNominal.getText().toString()) <= 0){

            updateHarga("0");
            return;
        }

        isLoading(true);
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kode", kode);
            jBody.put("harga", edtNominal.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(activity, jBody, "POST", ServerURL.getHargaNGRS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String harga = response.getJSONObject("response").getString("harga");
                        updateHarga(harga);
                    }else{

                        Toast.makeText(activity, "Data harga tidak termuat",Toast.LENGTH_LONG).show();
                    }

                    pbProses.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbProses.setVisibility(View.GONE);
                    updateHarga("0");
                }

                isLoading(false);
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
                updateHarga("0");
                isLoading(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    @Override
    public void onLocationChanged(Location clocation) {

        if(jarak != ""){

            if(isOnLocation(clocation)){

                this.location = clocation;
                this.latitude = location.getLatitude();
                this.longitude = location.getLongitude();
            }

        }else{

            this.location = clocation;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }

        if(!isUpdateLocation){
            getJarak();
        }
    }

    private boolean isUpdateLocation = false;

    private void getJarak() {

        isUpdateLocation = true;
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kode", kode);
            jBody.put("kdcus", "");
            jBody.put("lat", iv.doubleToStringFull(latitude));
            jBody.put("long", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(activity, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            range = jo.getString("range");
                            jarak = jo.getString("jarak");
                            latitudeOutlet = jo.getString("latitude");
                            longitudeOutlet = jo.getString("longitude");
                            String pesan = jo.getString("pesan");
                            String keteranganJarak = "";
                            if(iv.parseNullDouble(jo.getString("jarak")) <= 6371){
                                if(iv.parseNullDouble(jo.getString("jarak")) <= 1){
                                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "2") + " m";
                                }else{
                                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "2") + " km";
                                }

                                if(iv.parseNullDouble(jarak) > iv.parseNullDouble(range)){

                                    keteranganJarak = "<font color='#ec1c25'>"+keteranganJarak+"</font>";
                                }

                            }else{


                                keteranganJarak = "<font color='#ec1c25'>Lokasi outlet tidak diketahui</font>";
                            }

                            edtJarak.setText(Html.fromHtml(pesan + keteranganJarak));

                            break;
                        }
                    }

                    isUpdateLocation = false;
                    pbProses.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    isUpdateLocation = false;
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {
                isUpdateLocation = false;
                pbProses.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
