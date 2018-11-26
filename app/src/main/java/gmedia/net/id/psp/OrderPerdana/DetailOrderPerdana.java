package gmedia.net.id.psp.OrderPerdana;

import android.Manifest;
import android.app.Activity;
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
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDCBAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListRentangCCIDAdapter;
import gmedia.net.id.psp.OrderPulsa.DetailOrderPulsa;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailOrderPerdana extends AppCompatActivity implements LocationListener{

    private EditText edtSuratJalan, edtCCID, edtNamaBarangScan, edtHargaScan;
    private ImageView ivSuratJalan, ivAddCCID;
    private static ItemValidation iv = new ItemValidation();
    private List<OptionItem> suratJalanList;
    private int SuratjalanSelected = 0;
    private final String TAG = "DetailOrderPerdana";
    private String suratJalan = "";
    private EditText edtNobukti;
    private static List<CustomItem> ccidList = new ArrayList<>();
    private static List<OptionItem> masterCCID = new ArrayList<>();
    private static ListView lvCCID;
    private EditText edtNamaBarang, edtHarga;
    private String noCus = "", namaCus = "", kdBrg = "", namaBrg = "", crBayar = "", tempo = "", kdGudang = "", hargaBrg = "";
    private SessionManager session;
    private LinearLayout llScanCCID;
    private ProgressBar pbLoading;
    private boolean isProses = false;
    private Button btnUbahHarga;
    private static EditText edtTotalHarga;
    private static double totalHarga = 0;
    private Button btnAmbilCCIDList, btnScanCCID;
    private boolean firstLoad = true;
    private String noBukti = "";
    private Button btnProses;
    private String notelpCus = "";
    private String noBa = "", status = "";
    private boolean editMode = false;
    private List<CustomItem> listCCIDLama;
    private Button btnRentangCCID;

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
    private LinearLayout llNobukti;
    private static EditText edtTotalCCID;
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

    private Context context;
    private PspPrinter printer;
    private Button btnCetakData;
    private String tglNota = "";
    private String namaSales = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_perdana);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Order Perdana");

        // getLocation update by google
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        context = this;
        printer = new PspPrinter(context);
        printer.startService();

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MockLocChecker checker = new MockLocChecker(DetailOrderPerdana.this);
    }

    @Override
    protected void onDestroy() {
        printer.startService();
        super.onDestroy();
    }

    private void initUI() {

        edtNamaBarang = (EditText) findViewById(R.id.edt_nama_barang);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        edtSuratJalan = (EditText) findViewById(R.id.edt_surat_jalan);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        ivSuratJalan = (ImageView) findViewById(R.id.iv_suran_jalan);
        llNobukti = (LinearLayout) findViewById(R.id.ll_nobukti);
        edtNobukti = (EditText) findViewById(R.id.edt_nobukti);
        edtCCID = (EditText) findViewById(R.id.edt_ccid);
        ivAddCCID = (ImageView) findViewById(R.id.iv_add_ccid);
        edtNamaBarangScan = (EditText) findViewById(R.id.edt_nama_barang_scan);
        edtHargaScan = (EditText) findViewById(R.id.edt_harga_scan);
        btnUbahHarga = (Button) findViewById(R.id.btn_ubah_harga);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_outlet);

        lvCCID = (ListView) findViewById(R.id.lv_perdana);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        btnAmbilCCIDList = (Button) findViewById(R.id.btn_ambil_ccid_list);
        btnScanCCID = (Button) findViewById(R.id.btn_scan_ccid);
        btnRentangCCID = (Button) findViewById(R.id.btn_rentang_ccid);

        llScanCCID = (LinearLayout) findViewById(R.id.ll_scan_ccid);

        edtTotalCCID = (EditText) findViewById(R.id.edt_total_ccid);
        edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);
        btnCetakData = (Button) findViewById(R.id.btn_cetak);

        isProses = false;
        totalHarga = 0;
        session = new SessionManager(DetailOrderPerdana.this);
        Bundle bundle = getIntent().getExtras();
        masterCCID = new ArrayList<>();
        ccidList = new ArrayList<>();
        if(bundle != null){

            suratJalan = bundle.getString("suratjalan","");
            noCus = bundle.getString("nocus","");
            namaCus = bundle.getString("namacus","");
            notelpCus = bundle.getString("notelpcus","");
            kdBrg = bundle.getString("kodebrg","");
            namaBrg = bundle.getString("namabrg","");
            crBayar = bundle.getString("crbayar","");
            tempo = bundle.getString("tempo","");
            kdGudang = bundle.getString("kdgudang","");
            hargaBrg = bundle.getString("harga","");
            noBa = bundle.getString("noba","");
            tglNota = bundle.getString("tglnota",iv.getCurrentDate(FormatItem.formatDate));
            tglNota = iv.ChangeFormatDateString(tglNota, FormatItem.formatDate, FormatItem.formatDateDisplay);

            edtNamaBarang.setText(namaBrg);
            edtHarga.setText(hargaBrg);
            edtSuratJalan.setText(suratJalan);

            ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this,ccidList);
            updateHargaTotal();
            lvCCID.setAdapter(adapter);

            noBukti = bundle.getString("nobukti");
            if(noBukti != null && noBukti.length() > 0 ){

                editMode = true;
                namaSales = bundle.getString("namasales",session.getUser());
                edtNobukti.setText(noBukti);
                status = bundle.getString("status");
                btnProses.setEnabled(false);
                btnProses.setVisibility(View.GONE);
                btnCetakData.setVisibility(View.VISIBLE);

                String jarak = bundle.getString("jarak");

                String keteranganJarak = "";
                if(jarak != null && iv.parseNullDouble(jarak) <= 6371 && !jarak.equals("Tidak diketahui")){
                    if(iv.parseNullDouble(jarak) <= 1){
                        keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak) * 1000, "2") + " m";
                    }else{
                        keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak), "2") + " km";
                    }

                    keteranganJarak = "Jarak sales dengan outlet saat order yaitu " + keteranganJarak;
                }else{


                    keteranganJarak = "<font color='#ec1c25'>Lokasi outlet tidak diketahui</font>";
                }

                edtJarak.setText(Html.fromHtml( keteranganJarak));
            }else{

                editMode = false;
                noBukti = "";
                llNobukti.setVisibility(View.GONE);
                //getNobukti();
            }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            setCriteria();
            latitude = 0;
            longitude = 0;
            location = new Location("set");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            updateAllLocation();
            //location = getLocation();

            getMasterCCID();
            initEvent();
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
                        if (ActivityCompat.checkSelfPermission(DetailOrderPerdana.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailOrderPerdana.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailOrderPerdana.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location clocation) {

                                        mRequestingLocationUpdates = true;
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
                                    rae.startResolutionForResult(DetailOrderPerdana.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(DetailOrderPerdana.this, errorMessage, Toast.LENGTH_LONG).show();
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

    private void setListViewBehaviour(){
        lvCCID.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        iv.setListViewHeightBasedOnChildren(lvCCID);
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

        isUpdateLocation = true;
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
                Toast.makeText(DetailOrderPerdana.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailOrderPerdana.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailOrderPerdana.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailOrderPerdana.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailOrderPerdana.this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPerdana.this);
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
        ActivityCompat.requestPermissions(DetailOrderPerdana.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void getNobukti() {

        isLoading(true);
        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, new JSONObject(), "GET", ServerURL.getPerdanaNonota +noCus, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        noBukti = response.getJSONObject("response").getString("nonota");
                        edtNobukti.setText(noBukti);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);
            }
        });
    }

    private void initEvent() {

        edtHarga.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                double hargaBaru = iv.parseNullDouble(edtHarga.getText().toString());
                if(hargaBaru < iv.parseNullDouble(hargaBrg)){
                   edtHarga.setError("Harga minimal "+iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)));
                }else{
                    edtHarga.setError(null);
                }
            }
        });

        edtHargaScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                double harga = iv.parseNullDouble(edtHargaScan.getText().toString());

                if(hargaBrg == null){
                    edtHargaScan.setError("Data harga belum termuat");
                }else{
                    if( harga < iv.parseNullDouble(hargaBrg)){
                        edtHargaScan.setError("Harga minimal "+ iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)));
                    }else{
                        edtHargaScan.setError(null);
                    }
                }
            }
        });

        btnUbahHarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtCCID.getText().length() == 0){

                    Snackbar.make(findViewById(android.R.id.content), "CCID masih kosong", Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                if(hargaBrg == null || masterCCID == null){
                    Snackbar.make(findViewById(android.R.id.content), "Harap tunggu semua data termuat", Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                double hargaDouble = iv.parseNullDouble(edtHargaScan.getText().toString());
                if(hargaDouble < iv.parseNullDouble(hargaBrg)){
                    Snackbar.make(findViewById(android.R.id.content), "Harga minimal " + iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)), Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                String ccid = edtCCID.getText().toString();
                String harga = edtHargaScan.getText().toString();

                int x = 0;
                for (OptionItem item: masterCCID){

                    if(item.getText().equals(ccid)){

                        masterCCID.get(x).setAtt2(harga);
                        break;
                    }
                    x++;
                }

                ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
                adapter.changeData(ccid, harga);
                updateHargaTotal();
                iv.hideSoftKey(DetailOrderPerdana.this);

                Toast.makeText(DetailOrderPerdana.this, "Data berhasil diubah", Toast.LENGTH_LONG).show();
            }
        });

        lvCCID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                edtCCID.setText(item.getItem2());
                edtNamaBarangScan.setText(item.getItem3());
                edtHargaScan.setText(item.getItem4());
                edtCCID.requestFocus();
                edtHargaScan.requestFocus();
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
                List<CustomItem> listItem = adapter.getDataList();

                String message = "Apakah anda yakin ingin memproses data?\n\n";
                if(editMode) message = "Apakah anda yakin ingin mengubah "+noBukti+" ?\n\n";
                message += ("Total " + String.valueOf(listItem.size())+" CCID");
                AlertDialog builder = new AlertDialog.Builder(DetailOrderPerdana.this)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage(message)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                validateSaveData();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
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

                    Intent intent = new Intent(DetailOrderPerdana.this, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latitudeOutlet);
                    intent.putExtra("long_outlet", longitudeOutlet);
                    intent.putExtra("nama", namaCus);

                    startActivity(intent);
                }else{

                    Toast.makeText(DetailOrderPerdana.this, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnCetakData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editMode){

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

                    HashMap<String, DataBufferCCID> hCCID = new HashMap<String, DataBufferCCID>();

                    for(CustomItem ccid: ccidList){

                        boolean isExist = false;
                        String key = ccid.getItem3() +" "+ iv.ChangeToCurrencyFormat(ccid.getItem4());

                        if(hCCID.containsKey(key)){
                            isExist = true;
                        }

                        if(isExist){

                            DataBufferCCID data = hCCID.get(key);
                            int newJml = data.getJml() + 1;
                            double newHarga = data.getHarga() + iv.parseNullDouble(ccid.getItem4());

                            hCCID.put(key, new DataBufferCCID(newJml, newHarga));
                        }else{

                            hCCID.put(key, new DataBufferCCID(1, iv.parseNullDouble(ccid.getItem4())));
                        }
                    }

                    for ( String key : hCCID.keySet() ) {
                        DataBufferCCID data = hCCID.get(key);
                        items.add(new Item(key, String.valueOf(data.getJml()), data.harga));
                    }

                    Calendar date = Calendar.getInstance();
                    final Transaksi transaksi = new Transaksi(namaCus, namaSales, noBukti, date.getTime(), items, tglNota);

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

                            /*//Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                            Intent intent = new Intent(DetailOrderPerdana.this, PenjualanHariIni.class);
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

    private class DataBufferCCID{

        private int jml;
        private double harga;

        public DataBufferCCID(int jml, double harga){

            this.jml = jml;
            this.harga = harga;
        }

        public int getJml(){
            return this.jml;
        }

        public double getHarga(){
            return this.harga;
        }

        public void setJml(int jml) {
            this.jml = jml;
        }

        public void setHarga(double harga) {
            this.harga = harga;
        }
    }

    private void validateSaveData() {

        if(isProses){

            Snackbar.make(findViewById(android.R.id.content), "Tunggu Hingga Proses Selesai", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(editMode && status.equals("3")){

            Snackbar.make(findViewById(android.R.id.content), "Tidak dapat diubah karena penjualan sudah diproses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(jarak.equals("")){

            Snackbar.make(findViewById(android.R.id.content), "Mohon tunggu hinggan posisi diketahui / tekan refresh pada bagian jarak", Snackbar.LENGTH_LONG).show();
            edtJarak.requestFocus();
            return;
        }

        if(iv.parseNullDouble(edtHarga.getText().toString()) < iv.parseNullDouble(hargaBrg)){

            Snackbar.make(findViewById(android.R.id.content), "Harga minimal "+ iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)), Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            edtHarga.requestFocus();
            return;
        }

        if(edtSuratJalan.getText().length() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Surat Jalan tidak termuat, harap ulangi proses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(editMode && edtNobukti.getText().length() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Nomor nota tidak termuat, harap ulangi proses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(totalHarga  <= 0 ){

            Snackbar.make(findViewById(android.R.id.content), "Total Harga masih kosong, periksa apakah data anda sudah benar", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(location == null){

            Toast.makeText(DetailOrderPerdana.this, "Harap tunggu hingga posisi diketahui", Toast.LENGTH_LONG).show();
            return;
        }

        ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
        List<CustomItem> listItem = adapter.getDataList();

        if(listItem == null || listItem.size() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Tidak ada CCID yang terpilih", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        saveData(listItem);
    }

    private void saveData(final List<CustomItem> listItem) {

        btnProses.setEnabled(false);
        // Jual D lama
        JSONArray jualDLama = new JSONArray();

        if(editMode){

            for(CustomItem item: listCCIDLama){
                JSONObject joLama = new JSONObject();

                try {
                    joLama.put("nobukti", noBukti);
                    joLama.put("kodebrg", item.getItem1());
                    joLama.put("ccid", item.getItem8());
                    joLama.put("harga", item.getItem4());
                    joLama.put("jumlah", "1");
                    joLama.put("hpp", item.getItem5());
                    joLama.put("nopengeluaran", suratJalan);
                    joLama.put("tgl_do", item.getItem6());
                    joLama.put("nodo", item.getItem7());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jualDLama.put(joLama);
            }
        }

        // JUAL D
        JSONArray jualD = new JSONArray();

        double totalHPP = 0;
        for(CustomItem item: listItem){

            JSONObject jo = new JSONObject();

            try {
                jo.put("nobukti", noBukti);
                jo.put("kodebrg", item.getItem1());
                jo.put("ccid", item.getItem2());
                jo.put("harga", item.getItem4());
                jo.put("jumlah", "1");
                jo.put("hpp", item.getItem5());
                jo.put("nopengeluaran", item.getItem8());
                jo.put("tgl_do", item.getItem6());
                jo.put("nodo", item.getItem7());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jualD.put(jo);
            totalHPP += iv.parseNullDouble(item.getItem5());
        }

        // JUAL H
        JSONObject jualH = new JSONObject();
        try {
            jualH.put("nobukti", noBukti);
            jualH.put("tgl", iv.getCurrentDate(FormatItem.formatDate));
            jualH.put("kdcus", noCus);
            jualH.put("tgltempo", (crBayar.equals("T")) ? iv.getCurrentDate(FormatItem.formatDate) : iv.sumDate(iv.getCurrentDate(FormatItem.formatDate), iv.parseNullInteger(tempo), FormatItem.formatDate));
            jualH.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jualH.put("total", iv.doubleToStringRound(totalHarga));
            jualH.put("totalhpp", iv.doubleToStringRound(totalHPP));
            //jualH.put("keterangan", namaCus + " | " + notelpCus + " | " + session.getUserInfo(SessionManager.TAG_FLAG) + " " + session.getUserInfo(SessionManager.TAG_NAMA));
            jualH.put("keterangan", suratJalan);
            jualH.put("userid", session.getUserInfo(SessionManager.TAG_USERNAME));
            jualH.put("status", "1");
            jualH.put("nomutasi", suratJalan);
            jualH.put("crbayar", crBayar);
            jualH.put("kode_lokasi", session.getUserInfo(SessionManager.TAG_AREA));
            jualH.put("no_ba", noBa);
            jualH.put("cluster", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Post Piurang
        JSONObject postPiutang = new JSONObject();
        try {
            postPiutang.put("nonota", noBukti);
            postPiutang.put("tgl", iv.getCurrentDate(FormatItem.formatDate));
            postPiutang.put("tgltempo", (crBayar.equals("T")) ? iv.getCurrentDate(FormatItem.formatDate) : iv.sumDate(iv.getCurrentDate(FormatItem.formatDate), iv.parseNullInteger(tempo), FormatItem.formatDate));
            postPiutang.put("kdcus", noCus);
            postPiutang.put("kdsales", session.getUserInfo(SessionManager.TAG_UID));
            postPiutang.put("piutang", iv.doubleToStringRound(totalHarga));
            postPiutang.put("keterangan", namaBrg);
            postPiutang.put("crbayar", crBayar);
            postPiutang.put("kode_lokasi", session.getUserInfo(SessionManager.TAG_AREA));
            postPiutang.put("jml", String.valueOf(listItem.size()));

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

        String method = (editMode) ? "PUT" : "POST";
        String url = ServerURL.savePerdana;
        JSONObject jBody = new JSONObject();
        try {

            if(editMode){
                jBody.put("nobukti", noBukti);
                jBody.put("jual_d_lama", jualDLama);
            }
            jBody.put("version", version);
            jBody.put("jual_d", jualD);
            jBody.put("jual_h", jualH);
            jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
            jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
            jBody.put("post_piutang", postPiutang);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        isLoading(true);

        final ProgressDialog progressDialog = new ProgressDialog(DetailOrderPerdana.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String superMessage = "Terjadi kesalahan saat menyimpan data, harap ulangi";
                isLoading(false);
                progressDialog.dismiss();

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    superMessage = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        String message = "Order Perdana berhasil disimpan";
                        String nonota = response.getJSONObject("response").getString("nobukti");
                        if(editMode) message = "Order "+ noBukti + " berhasil diubah";
                        Toast.makeText(DetailOrderPerdana.this, message, Toast.LENGTH_LONG).show();

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

                        items.add(new Item(namaBrg, String.valueOf(listItem.size()), totalHarga));

                        Calendar date = Calendar.getInstance();
                        final Transaksi transaksi = new Transaksi(namaCus, session.getUser(), nonota, date.getTime(), items, iv.getCurrentDate(FormatItem.formatDateDisplay2));

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

                                //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                                Intent intent = new Intent(DetailOrderPerdana.this, PenjualanHariIni.class);
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
                        Toast.makeText(DetailOrderPerdana.this, superMessage, Toast.LENGTH_LONG).show();
                    }

                    btnProses.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailOrderPerdana.this, superMessage, Toast.LENGTH_LONG).show();
                    btnProses.setEnabled(true);
                }
            }

            @Override
            public void onError(String result) {

                btnProses.setEnabled(true);
                isLoading(false);
                Toast.makeText(DetailOrderPerdana.this, "Terjadi kesalahan saat menyimpan data, harap ulangi", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void getMasterCCID(){

        masterCCID = new ArrayList<>();
        isLoading(true);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kodegudang", kdGudang);
            jBody.put("kodebrg", kdBrg);
            jBody.put("harga", hargaBrg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, "POST", ServerURL.getListCCIDPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterCCID = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterCCID.add(new OptionItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo"), jo.getString("nobukti") , false));
                        }
                    }

                    if(editMode){
                        getSelectedCCID();
                    }else{
                        setRadioButtonEvent();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
            }
        });
    }

    private void getSelectedCCID(){

        ccidList = new ArrayList<>();
        listCCIDLama = new ArrayList<>();
        isLoading(true);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nonota", noBukti);
            jBody.put("kodegudang", kdGudang);
            jBody.put("kodebrg", kdBrg);
            jBody.put("harga", hargaBrg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, "POST", ServerURL.getSelectedCCIDPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    ccidList = new ArrayList<>();
                    listCCIDLama = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            ccidList.add(new CustomItem(
                                    jo.getString("kodebrg")
                                    , jo.getString("ccid")
                                    , jo.getString("namabrg")
                                    , jo.getString("harga")
                                    , jo.getString("hpp")
                                    , jo.getString("tgl_do")
                                    , jo.getString("nodo")
                                    , jo.getString("nobukti")));

                            listCCIDLama.add(new CustomItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo"), jo.getString("nobukti")));
                            masterCCID.add(i, new OptionItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo"), jo.getString("nobukti") , true));
                        }
                    }

                    ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                    lvCCID.setAdapter(adapter);
                    updateHargaTotal();
                    setRadioButtonEvent();

                } catch (JSONException e) {
                    e.printStackTrace();
                    updateHargaTotal();
                    setRadioButtonEvent();
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                setRadioButtonEvent();
            }
        });
    }

    private void setRadioButtonEvent(){

        btnAmbilCCIDList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListCCID();
            }
        });

        btnScanCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edtCCID.setText("");
                edtNamaBarangScan.setText("");
                edtHargaScan.setText("");
                openScanBarcode();
            }
        });

        btnRentangCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadRentangCCID();
            }
        });
    }

    private void isLoading(boolean status){
        isProses = status;
        if(isProses){
            pbLoading.setVisibility(View.VISIBLE);
        }else{
            pbLoading.setVisibility(View.GONE);
        }
    }
    private List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    private void openScanBarcode() {

        Collection<String> ONE_D_CODE_TYPES =
                list("CODE_128","QR_CODE");
        IntentIntegrator integrator = new IntentIntegrator(DetailOrderPerdana.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHECK_SETTINGS){

            if(resultCode == Activity.RESULT_CANCELED){

                mRequestingLocationUpdates = false;
            }else if(resultCode == Activity.RESULT_OK){

                startLocationUpdates();
            }

        }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {

                    Log.d(TAG, "onActivityResult: Scan failed ");
                } else {

                    updateCCID(result.getContents());
                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void updateCCID(final String ccid){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String finalCCID = "";
                CustomItem barang = null;
                boolean found = false;
                try {

                    finalCCID = ccid.substring(5,21);
                    //finalCCID = "0640000028317945";

                    int x = 0;
                    for(OptionItem item: masterCCID){

                        if(item.getText().equals(finalCCID)){
                            barang = new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2(), item.getAtt3(), item.getAtt4(), item.getAtt5(), item.getAtt6());
                            found = true;
                            break;
                        }
                        x++;
                    }

                    //finalCCID = ccid;
                }catch (Exception e){
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), "CCID tidak terdeteksi, harap scan ulang",
                            Snackbar.LENGTH_LONG).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }

                if(found){

                    edtCCID.setText(finalCCID);
                    edtNamaBarangScan.setText(barang.getItem3());
                    edtHargaScan.setText(barang.getItem4());
                    if(finalCCID.length() > 0){
                        updateList(barang);
                    }
                }else{

                    Snackbar.make(findViewById(android.R.id.content), "CCID tidak terdeteksi, harap scan ulang",
                            Snackbar.LENGTH_LONG).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }
            }
        });
    }

    private void updateList(CustomItem itemToAdd) {

        boolean isExist = false;
        for(CustomItem item : ccidList){

            if(item.getItem2().equals(itemToAdd.getItem2())) isExist = true;
        }

        if(!isExist) {

            ccidList.add(0,itemToAdd);
            ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
            adapter.notifyDataSetChanged();
            updateHargaTotal();

            int x = 0;
            for(OptionItem item:masterCCID){

                if(item.getText().equals(itemToAdd.getItem2())){
                    masterCCID.get(x).setSelected(true);
                    //selectedOptionList[x+1] = true;
                }
                x++;
            }
        }else{

            Snackbar.make(findViewById(android.R.id.content), "CCID sudah ada", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
        }
    }

    public static void deleteSelectedCCID(String ccid){

        int x = 0;
        for(OptionItem item: masterCCID){

            if(ccid.equals(item.getText())){

                masterCCID.get(x).setSelected(false);
                break;
            }
            x++;
        }
        updateHargaTotal();
    }

    //region ============================================ Ambil dari list =====================================================
    private boolean[] selectedOptionList;
    private void showListCCID() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPerdana.this, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_ccid, null);
        builder.setView(view);

        final AutoCompleteTextView actvCCIDBA = (AutoCompleteTextView) view.findViewById(R.id.actv_ccid);
        final ListView lvCCIDBA = (ListView) view.findViewById(R.id.lv_ccid);

        selectedOptionList = new boolean[masterCCID.size()];
        int x = 0;
        for(OptionItem item: masterCCID){
            selectedOptionList[x] = item.isSelected();
            x++;
        }

        final List<OptionItem> lastData = new ArrayList<>(masterCCID);

        lastData.add(0,new OptionItem("0","all"));
        ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, lastData);
        lvCCIDBA.setAdapter(adapter);

        actvCCIDBA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(actvCCIDBA.getText().length() <= 0){

                    lvCCIDBA.setAdapter(null);
                    ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, lastData);
                    lvCCIDBA.setAdapter(adapter);
                }
            }
        });

        actvCCIDBA.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<OptionItem> items = new ArrayList<OptionItem>();
                    String keyword = actvCCIDBA.getText().toString().trim().toUpperCase();

                    for (OptionItem item: lastData){

                        if(item.getText().toUpperCase().contains(keyword)) items.add(item);
                    }

                    ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, items);
                    lvCCIDBA.setAdapter(adapter);
                    iv.hideSoftKey(DetailOrderPerdana.this);
                    return true;
                }

                return false;
            }
        });

        /*final CharSequence[] choiceList = new CharSequence[masterCCID.size()+1];
        selectedOptionList = new boolean[masterCCID.size()+1];
        choiceList[0] = "All";
        boolean checkAll = true;
        for(int x = 1; x <= masterCCID.size();x++){
            choiceList[x] = x +". "+ masterCCID.get(x-1).toString();
            selectedOptionList[x] = masterCCID.get(x-1).isSelected();
            if(!masterCCID.get(x-1).isSelected()) checkAll = masterCCID.get(x-1).isSelected();
        }
        selectedOptionList[0] = checkAll;

        builder.setMultiChoiceItems(choiceList, selectedOptionList, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                if(i == 0){

                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    ListView v = dialog.getListView();
                    int x = 1;
                    selectedOptionList[i] = b;
                    while(x < choiceList.length) {
                        v.setItemChecked(x, b);
                        //masterCCID.get(x-1).setSelected(b);
                        selectedOptionList[x] = b;
                        x++;
                    }
                }else{

                    //masterCCID.get(i-1).setSelected(b);
                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    ListView v = dialog.getListView();
                    int x = 1;
                    selectedOptionList[i] = b;
                    boolean checkALL = true;

                    while(x < choiceList.length) {
                        if(!selectedOptionList[x]) checkALL = false;
                        x++;
                    }
                    selectedOptionList[0] = checkALL;
                    v.setItemChecked(0, checkALL);
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int x = 1; x <= masterCCID.size();x++){
                    masterCCID.get(x-1).setSelected(selectedOptionList[x]);
                }

                ccidList = new ArrayList<CustomItem>();
                for(OptionItem item: masterCCID){

                    if(item.isSelected()){

                        ccidList.add(new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2()));
                    }
                }

                ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                lvCCID.setAdapter(null);
                lvCCID.setAdapter(adapter);
                updateHargaTotal();
            }
        });*/

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ListCCIDCBAdapter adapterSelected = (ListCCIDCBAdapter) lvCCIDBA.getAdapter();
                List<OptionItem> optionItems = adapterSelected.getItems();

                ccidList = new ArrayList<CustomItem>();

                int x = 0;
                for(OptionItem item: optionItems){

                    if(item.isSelected()){

                        ccidList.add(new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2(), item.getAtt3(), item.getAtt4(), item.getAtt5(), item.getAtt6()));
                    }

                    masterCCID.get(x).setSelected(item.isSelected());
                    selectedOptionList[x] = item.isSelected();
                    x++;
                }

                ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                lvCCID.setAdapter(null);
                lvCCID.setAdapter(adapter);
                updateHargaTotal();
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int x = 0;
                for(OptionItem item: masterCCID){

                    masterCCID.get(x).setSelected(selectedOptionList[x]);
                    x++;
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //endregion

    //region ================================================ range ccid ===========================================

    private boolean[] selectedRentang;
    private void loadRentangCCID(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPerdana.this, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_rentang_ccid, null);
        builder.setView(view);

        selectedRentang = new boolean[masterCCID.size()];
        final List<OptionItem> rentangCCIDList = new ArrayList<>();
        final EditText edtCCIDAwal = (EditText) view.findViewById(R.id.edt_ccid_awal);
        final EditText edtCCIDAkhir = (EditText) view.findViewById(R.id.edt_ccid_akhir);
        final EditText edtBanyakCCID = (EditText) view.findViewById(R.id.edt_banyak_ccid);
        final Button btnAmbilCCID = (Button) view.findViewById(R.id.btn_ambil_ccid);
        final ListView lvRentangCCID = (ListView) view.findViewById(R.id.lv_rentang_ccid);

        btnAmbilCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtCCIDAwal.getText().length() == 0){
                    edtCCIDAwal.setError("Harap di isi");
                    return;
                }else{
                    edtCCIDAwal.setError(null);
                }

                if(edtCCIDAkhir.getText().length() == 0){
                    edtCCIDAkhir.setError("Harap di isi");
                    return;
                }else{
                    edtCCIDAkhir.setError(null);
                }

                selectedRentang = new boolean[masterCCID.size()];
                for(int i = 0; i < masterCCID.size();i++) {
                    selectedRentang[i] = false;
                }

                long ccidAwal = iv.parseNullLong(edtCCIDAwal.getText().toString());
                long ccidAkhir = iv.parseNullLong(edtCCIDAkhir.getText().toString());
                List<OptionItem> selectedItems = new ArrayList<OptionItem>();

                for(int x = 0; x < masterCCID.size();x++){
                    long selectedCCID = iv.parseNullLong(masterCCID.get(x).getText());
                    if(selectedCCID >= ccidAwal && selectedCCID <= ccidAkhir){
                        selectedRentang[x] = true;
                        selectedItems.add(masterCCID.get(x));
                    }
                }

                edtBanyakCCID.setText(String.valueOf(selectedItems.size()));

                lvRentangCCID.setAdapter(null);
                if(selectedItems != null && selectedItems.size() > 0){
                    ListRentangCCIDAdapter adapter = new ListRentangCCIDAdapter(DetailOrderPerdana.this, selectedItems);
                    lvRentangCCID.setAdapter(adapter);
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int x = 0; x < selectedRentang.length; x++){

                    if(selectedRentang[x]){
                        OptionItem item = masterCCID.get(x);
                        masterCCID.get(x).setSelected(true);
                        ccidList.add(new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2(), item.getAtt3(), item.getAtt4(), item.getAtt5(), item.getAtt6()));
                    }
                }

                ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                lvCCID.setAdapter(null);
                lvCCID.setAdapter(adapter);
                updateHargaTotal();
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //endregion

    private static void updateHargaTotal(){

        ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();

        totalHarga = 0;
        long banyak = 0;

        if(adapter != null && adapter.getDataList() != null){
            final List<CustomItem> items = new ArrayList<>(adapter.getDataList());
            banyak = items.size();

            for(CustomItem item: items){

                totalHarga += iv.parseNullDouble(item.getItem4());
            }

        }

        edtTotalCCID.setText(String.valueOf(banyak));
        edtTotalHarga.setText(iv.ChangeToRupiahFormat(totalHarga));
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

        if(!isUpdateLocation && !editMode){
            getJarak();
        }
    }

    private boolean isUpdateLocation = false;

    private void getJarak() {

        isUpdateLocation = true;
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kode", "");
            jBody.put("kdcus", noCus);
            jBody.put("lat", iv.doubleToStringFull(latitude));
            jBody.put("long", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isUpdateLocation = false;
                pbLoading.setVisibility(View.GONE);

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


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isUpdateLocation = false;
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        //location = getLocation();
    }

    @Override
    public void onProviderEnabled(String s) {

        //location = getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
