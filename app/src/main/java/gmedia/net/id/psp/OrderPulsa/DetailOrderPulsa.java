package gmedia.net.id.psp.OrderPulsa;

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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailOrderPulsa extends AppCompatActivity implements LocationListener{

    private EditText edtNonota, edtNamaRS, edtS5, edtS10, edtS20, edtS25, edtS50, edtS100, edtBulk, edtTotal, edtKeterangan;
    private TextView tvS5, tvS10, tvS20, tvS25, tvS50, tvS100, tvBulk;
    private Button btnProses;
    private String kodeRS = "", nomorRS = "", namaRS = "", levelRS = "", noUpline = "", pinRS = "", pinUpline = "";
    private String hargaS5 = "", hargaS10 = "", hargaS20 = "", hargaS25 = "", hargaS50 = "", hargaS100 = "", hargaDiskonBulk = "";
    private String nonota = "", flag = "";
    private ProgressBar pbProses;
    private boolean isProses = false;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private TextView tvHargaS5, tvHargaS10, tvHargaS20, tvHargaS25, tvHargaS50, tvHargaS100;
    private final String TAG = "DetailOrderPulsa";
    private boolean editMode = false;
    private ScrollView hsvPulsa;
    private Context context;

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
    private EditText edtJarak;
    private ImageView ivRefreshPosition;
    private String jarak = "",range = "", latitudeOutlet = "", longitudeOutlet = "";
    private Button btnMapsOutlet;
    private String kodeCV = "", tglTransaksi = "";

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
    private Button btnCetakDetail;
    private String namaSales = "", namaOutlet = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_pulsa);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Order Pulsa");

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
        MockLocChecker checker = new MockLocChecker(DetailOrderPulsa.this);
    }

    /*@Override
    protected void onDestroy() {
        printer.stopService();
        super.onDestroy();
    }*/

    private void initUI() {

        hsvPulsa = (ScrollView) findViewById(R.id.hsv_pulsa);
        llNonota = (LinearLayout) findViewById(R.id.ll_nonota);
        edtNonota = (EditText) findViewById(R.id.edt_nonota);
        edtNamaRS = (EditText) findViewById(R.id.edt_nama_reseller);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        edtS5 = (EditText) findViewById(R.id.edt_s5);
        edtS10 = (EditText) findViewById(R.id.edt_s10);
        edtS20 = (EditText) findViewById(R.id.edt_s20);
        edtS25 = (EditText) findViewById(R.id.edt_s25);
        edtS50 = (EditText) findViewById(R.id.edt_s50);
        edtS100 = (EditText) findViewById(R.id.edt_s100);
        edtBulk = (EditText) findViewById(R.id.edt_bulk);
        tvS5 = (TextView) findViewById(R.id.tv_s5);
        tvS10 = (TextView) findViewById(R.id.tv_s10);
        tvS20 = (TextView) findViewById(R.id.tv_s20);
        tvS25 = (TextView) findViewById(R.id.tv_s25);
        tvS50 = (TextView) findViewById(R.id.tv_s50);
        tvS100 = (TextView) findViewById(R.id.tv_s100);
        tvBulk = (TextView) findViewById(R.id.tv_bulk);
        tvHargaS5 = (TextView) findViewById(R.id.tv_hargaS5);
        tvHargaS10 = (TextView) findViewById(R.id.tv_hargaS10);
        tvHargaS20 = (TextView) findViewById(R.id.tv_hargaS20);
        tvHargaS25 = (TextView) findViewById(R.id.tv_hargaS25);
        tvHargaS50 = (TextView) findViewById(R.id.tv_hargaS50);
        tvHargaS100 = (TextView) findViewById(R.id.tv_hargaS100);
        edtTotal = (EditText) findViewById(R.id.edt_total);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        btnProses = (Button) findViewById(R.id.btn_proses);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        btnCetakDetail = (Button) findViewById(R.id.btn_cetak);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_outlet);

        isProses = false;
        session = new SessionManager(DetailOrderPulsa.this);
        editMode = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kodeRS = bundle.getString("koders", "");
            nonota = bundle.getString("nonota", "");

            if(!nonota.isEmpty()){

                kodeCV = bundle.getString("kode_cv","");
                namaSales = bundle.getString("namasales",session.getUser());
                namaOutlet = bundle.getString("namaoutlet","");
                tglTransaksi = bundle.getString("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
                editMode = true;
                flag = bundle.getString("flag");
                edtNonota.setText(nonota);
                btnProses.setEnabled(false);
                btnProses.setVisibility(View.GONE);
                btnCetakDetail.setVisibility(View.VISIBLE);
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
            }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            setCriteria();
            latitude = 0;
            longitude = 0;
            location = new Location("set");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            //location = getLocation();
            updateAllLocation();

            getRSDetail();
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
                        if (ActivityCompat.checkSelfPermission(DetailOrderPulsa.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailOrderPulsa.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailOrderPulsa.this, new OnSuccessListener<Location>() {
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
                                    rae.startResolutionForResult(DetailOrderPulsa.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(DetailOrderPulsa.this, errorMessage, Toast.LENGTH_LONG).show();
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
                Toast.makeText(DetailOrderPulsa.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailOrderPulsa.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailOrderPulsa.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailOrderPulsa.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailOrderPulsa.this,
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
                            //Changed(location);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPulsa.this);
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
        ActivityCompat.requestPermissions(DetailOrderPulsa.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void getRSDetail() {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        if(kodeCV.length() > 0) nik = kodeCV;
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, new JSONObject(), "GET", ServerURL.getReseller+nik+"/"+kodeRS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
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
                            hargaS5 = jo.getString("harga5");
                            hargaS10 = jo.getString("harga10");
                            hargaS20 = jo.getString("harga20");
                            hargaS25 = jo.getString("harga25");
                            hargaS50 = jo.getString("harga50");
                            hargaS100 = jo.getString("harga100");
                            hargaDiskonBulk = jo.getString("hbulk");

                            tvHargaS5.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS5)));
                            tvHargaS10.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS10)));
                            tvHargaS20.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS20)));
                            tvHargaS25.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS25)));
                            tvHargaS50.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS50)));
                            tvHargaS100.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS100)));
                            hitungTotal();

                            edtNamaRS.setText(namaRS);

                            if(editMode){
                                getDetailMkios();
                            }else{
                                //getNonota();
                                llNonota.setVisibility(View.GONE);
                                nonota = "";
                            }
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showDialog(3, "Terjadi kesalahan saat memuat data reseller, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                showDialog(3, "Terjadi kesalahan saat memuat data reseller, harap ulangi");
            }
        });
    }

    private void getDetailMkios() {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        if(kodeCV.length()>0) nik = kodeCV;
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nonota", nonota);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, jBody, "POST", ServerURL.getMkios+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String pesan = "", S5 = "0", S10 = "0", S20 = "0", S25 = "0", S50 = "0", S100 = "0", valueBulk = "0";
                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){
                            JSONObject jo = items.getJSONObject(i);

                            kodeRS = jo.getString("kode");
                            nomorRS = jo.getString("nomor");
                            namaRS = (jo.getString("nama").isEmpty() ? namaOutlet : jo.getString("nama"));
                            levelRS = jo.getString("level");
                            noUpline = jo.getString("nomor_upline");
                            pinRS = jo.getString("pin");
                            pinUpline = jo.getString("pin_upline");
                            pesan = jo.getString("pesan");
                            if(!jo.getString("S5").equals("0") && !jo.getString("S5").equals("")) S5 = jo.getString("S5");
                            if(!jo.getString("S10").equals("0") && !jo.getString("S10").equals("")) S10 = jo.getString("S10");
                            if(!jo.getString("S20").equals("0") && !jo.getString("S20").equals("")) S20 = jo.getString("S20");
                            if(!jo.getString("S25").equals("0") && !jo.getString("S25").equals("")) S25 = jo.getString("S25");
                            if(!jo.getString("S50").equals("0") && !jo.getString("S50").equals("")) S50 = jo.getString("S50");
                            if(!jo.getString("S100").equals("0") && !jo.getString("S100").equals("")) S100 = jo.getString("S100");
                            if(!jo.getString("value_bulk").equals("0") && !jo.getString("value_bulk").equals("")) valueBulk = jo.getString("value_bulk");
                            totalHarga += iv.parseNullDouble(jo.getString("total"));
                        }

                        edtS5.setText(S5);
                        edtS10.setText(S10);
                        edtS20.setText(S20);
                        edtS25.setText(S25);
                        edtS50.setText(S50);
                        edtS100.setText(S100);
                        edtBulk.setText(valueBulk);
                        edtKeterangan.setText(pesan);
                        hitungTotal();
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

    private void showDialog(int state, String message){

        if(state == 1){

            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPulsa.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_success, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null) {

                        try {
                            alert.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });

            try {
                alert.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(state == 2){ // failed
            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPulsa.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_failed, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null) {

                        try {
                            alert.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });

            try {
                alert.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(state == 3){

            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPulsa.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_warning, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);
            btnOK.setText("Ulangi Proses");

            final AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null) {
                        try {
                            alert.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    getRSDetail();
                }
            });

            try {
                alert.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void getNonota() {

        isLoading(true);
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, new JSONObject(), "GET", ServerURL.getMKIOSNonota +nomorRS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        nonota = response.getJSONObject("response").getString("nonota");
                        edtNonota.setText(nonota);
                    }

                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);
            }
        });
    }

    private void initEvent() {

        setTextWatcherHarga(edtS5);
        setTextWatcherHarga(edtS10);
        setTextWatcherHarga(edtS20);
        setTextWatcherHarga(edtS25);
        setTextWatcherHarga(edtS50);
        setTextWatcherHarga(edtS100);
        setTextWatcherHarga(edtBulk);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = "Apakah anda yakin ingin memproses order?\n\n";
                if(editMode) message = "Apakah anda yakin ingin mengubah "+nonota+" ?\n\n";

                int maxLength = edtS5.getText().toString().length();
                if(maxLength < edtS10.getText().toString().length()) maxLength =  edtS10.getText().toString().length();
                if(maxLength < edtS20.getText().toString().length()) maxLength =  edtS20.getText().toString().length();
                if(maxLength < edtS25.getText().toString().length()) maxLength =  edtS25.getText().toString().length();
                if(maxLength < edtS50.getText().toString().length()) maxLength =  edtS50.getText().toString().length();
                if(maxLength < edtS100.getText().toString().length()) maxLength =  edtS100.getText().toString().length();
                if(maxLength < edtBulk.getText().toString().length()) maxLength =  edtBulk.getText().toString().length();

                String vS5 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS5.getText().toString())), maxLength);
                String vS10 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS10.getText().toString())), maxLength);
                String vS20 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS20.getText().toString())), maxLength);
                String vS25 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS25.getText().toString())), maxLength);
                String vS50 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS50.getText().toString())), maxLength);
                String vS100 = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtS100.getText().toString())), maxLength);
                String vSBULK = addWhiteSpace(String.valueOf(iv.parseNullInteger(edtBulk.getText().toString())), maxLength);

                message += (" \tS5\t\t\t\t\t"+ String.format("%15s", vS5) +"\n");
                message += (" \tS10\t\t\t\t"+ String.format("%15s", vS10) +"\n");
                message += (" \tS20\t\t\t\t"+ String.format("%15s", vS20) +"\n");
                message += (" \tS25\t\t\t\t"+ String.format("%15s", vS25) +"\n");
                message += (" \tS50\t\t\t\t"+ String.format("%15s", vS50)+"\n");
                message += (" \tS100\t\t\t"+ String.format("%15s", vS100) +"\n");
                message += (" \tBULK\t\t\t"+ String.format("%15s", iv.ChangeToRupiahFormat(iv.parseNullDouble(vSBULK)))+"\n");

                AlertDialog builder = new AlertDialog.Builder(DetailOrderPulsa.this)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage(message)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                validasiBeforeSave();
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

                    Intent intent = new Intent(DetailOrderPulsa.this, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latitudeOutlet);
                    intent.putExtra("long_outlet", longitudeOutlet);
                    intent.putExtra("nama", namaRS);

                    startActivity(intent);
                }else{

                    Toast.makeText(DetailOrderPulsa.this, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnCetakDetail.setOnClickListener(new View.OnClickListener() {
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

                    if(!edtS5.getText().toString().equals("0")) items.add(new Item("5k @"+tvHargaS5.getText().toString(), edtS5.getText().toString(), total5));
                    if(!edtS10.getText().toString().equals("0")) items.add(new Item("10k @"+tvHargaS10.getText().toString(), edtS10.getText().toString(), total10));
                    if(!edtS20.getText().toString().equals("0")) items.add(new Item("20k @"+tvHargaS20.getText().toString(), edtS20.getText().toString(), total20));
                    if(!edtS25.getText().toString().equals("0")) items.add(new Item("25k @"+tvHargaS25.getText().toString(), edtS25.getText().toString(), total25));
                    if(!edtS50.getText().toString().equals("0")) items.add(new Item("50k @"+tvHargaS50.getText().toString(), edtS50.getText().toString(), total50));
                    if(!edtS100.getText().toString().equals("0")) items.add(new Item("100k @"+tvHargaS100.getText().toString(), edtS100.getText().toString(), total100));
                    if(!edtBulk.getText().toString().equals("0")) items.add(new Item("Bulk", "-", totalBulk));

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

    private String addWhiteSpace(String awal, int max){

        int gap = max - awal.length();

        for(int i = 0; i < 0; i++){
            awal = "\t"+awal;
        }

        return awal;
    }

    private void validasiBeforeSave() {

        if(isProses){
            Snackbar.make(findViewById(android.R.id.content), "Tunggu hingga proses selesai", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(editMode && !flag.equals("1")){
            Snackbar.make(findViewById(android.R.id.content), "Order sudah di proses, tidak dapat diubah", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(editMode){
            if(nonota == null || nonota.equals("")){
                Snackbar.make(findViewById(android.R.id.content), "Tidak dapat membuat no nota", Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        if(kodeRS.equals("")){
            Snackbar.make(findViewById(android.R.id.content), "Reseller tidak ditemukan", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(jarak.equals("")){

            Snackbar.make(findViewById(android.R.id.content), "Mohon tunggu hinggan posisi diketahui / tekan refresh pada bagian jarak", Snackbar.LENGTH_LONG).show();
            edtJarak.requestFocus();
            return;
        }

        if(edtS5.getText().toString().equals("") && edtS10.getText().toString().equals("") && edtS20.getText().toString().equals("") && edtS25.getText().toString().equals("") && edtS50.getText().toString().equals("") && edtS100.getText().toString().equals("") && edtBulk.getText().toString().equals("")){

            Snackbar.make(findViewById(android.R.id.content), "Harap isi minimal salah satu order", Snackbar.LENGTH_LONG).show();
            edtS5.requestFocus();
            return;
        }

        if(totalHarga <= 0){

            Snackbar.make(findViewById(android.R.id.content), "Tunggu hingga harga terisi dan ulangi proses input denom", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(location == null){

            Toast.makeText(DetailOrderPulsa.this, "Harap tunggu hingga posisi diketahui", Toast.LENGTH_LONG).show();
            return;
        }

        bagiOrder();
    }

    private JSONArray jsonArray = new JSONArray();

    private void bagiOrder() {

        List<CustomItem> orderList = new ArrayList<>();

        if(iv.parseNullDouble(edtS5.getText().toString()) > 0){
            orderList.add(new CustomItem("V5","5", edtS5.getText().toString(), iv.doubleToStringRound(total5)));
        }

        if(iv.parseNullDouble(edtS10.getText().toString()) > 0){
            orderList.add(new CustomItem("V10","10", edtS10.getText().toString(), iv.doubleToStringRound(total10)));
        }

        if(iv.parseNullDouble(edtS20.getText().toString()) > 0){
            orderList.add(new CustomItem("V20","20", edtS20.getText().toString(), iv.doubleToStringRound(total20)));
        }

        if(iv.parseNullDouble(edtS25.getText().toString()) > 0){
            orderList.add(new CustomItem("V25","25", edtS25.getText().toString(), iv.doubleToStringRound(total25)));
        }

        if(iv.parseNullDouble(edtS50.getText().toString()) > 0){
            orderList.add(new CustomItem("V50","50", edtS50.getText().toString(), iv.doubleToStringRound(total50)));
        }

        if(iv.parseNullDouble(edtS100.getText().toString()) > 0){
            orderList.add(new CustomItem("V100","100", edtS100.getText().toString(), iv.doubleToStringRound(total100)));
        }

        jsonArray = new JSONArray();
        int x = 1, i = 0;
        int clusterX = 1;
        String keteranganOrder = "", orderFormat = "";
        int banyakCluster = orderList.size() / 3;
        int sisaList = orderList.size() % 3;
        double total = 0;
        boolean akhir = false;
        SortedMap<String,String> dataInsert = new TreeMap<>();

        for(CustomItem item :orderList){

            //keteranganOrder = keteranganOrder + ((x == 1) ? item.getItem1(): ", " + item.getItem1()) + "=" + item.getItem3();
            keteranganOrder = keteranganOrder + item.getItem1() + "=" + item.getItem3() + ((i == (orderList.size() - 1) && iv.parseNullDouble(edtBulk.getText().toString()) == 0) ? "" : ",");
            orderFormat = orderFormat + item.getItem3()+ "*" + item.getItem2()+"*";
            total += iv.parseNullDouble(item.getItem4());
            dataInsert.put(item.getItem2(), item.getItem3());

            if(x == 3 && clusterX <= banyakCluster){

                if(sisaList == 0 && iv.parseNullDouble(edtBulk.getText().toString()) > 0) akhir = true;

                pushJsonData(dataInsert, keteranganOrder, orderFormat, iv.doubleToStringRound(total));
                x = 0;
                clusterX +=1 ;
                total = 0;
                dataInsert = new TreeMap<>();
                keteranganOrder = "";
                orderFormat = "";
            }else if(x == sisaList && clusterX > banyakCluster){

                if(iv.parseNullDouble(edtBulk.getText().toString()) > 0) akhir = true;
                pushJsonData(dataInsert, keteranganOrder, orderFormat, iv.doubleToStringRound(total));
            }

            x++;
            i++;
        }

        if(iv.parseNullDouble(edtBulk.getText().toString()) > 0){

            double hasilBulk = iv.parseNullDouble(edtBulk.getText().toString()) / 1000;
            dataInsert = new TreeMap<>();
            dataInsert.put("bulk", edtBulk.getText().toString());
            pushJsonData(dataInsert, "Bulk "+ edtBulk.getText().toString(), "0" + iv.doubleToStringRound(hasilBulk)+"*", iv.doubleToStringRound(totalBulk));
        }

        saveData(orderList);
    }

    private void pushJsonData(SortedMap<String, String> data, final String keteranganOrder, String orderFormat, String total) {

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("status", "PENDING");
            jBody.put("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
            jBody.put("kode_cv", session.getUserDetails().get(SessionManager.TAG_NIK));
            jBody.put("kode", kodeRS);
            jBody.put("nomor", nomorRS);
            jBody.put("nama", namaRS);
            jBody.put("level", levelRS);
            jBody.put("nomor_upline", noUpline);
            jBody.put("pin", pinRS);
            jBody.put("pin_upline", pinUpline);
            jBody.put("pesan", edtKeterangan.getText().toString());
            jBody.put("crbayar", "ISI");
            jBody.put("S5", (data.get("5") == null) ? "": data.get("5"));
            jBody.put("S10", (data.get("10") == null) ? "": data.get("10"));
            jBody.put("S20", (data.get("20") == null) ? "": data.get("20"));
            jBody.put("S25", (data.get("25") == null) ? "": data.get("25"));
            jBody.put("S50", (data.get("50") == null) ? "": data.get("50"));
            jBody.put("S100", (data.get("100") == null) ? "": data.get("100"));
            jBody.put("value_bulk", (data.get("bulk") == null) ? "": data.get("bulk"));
            jBody.put("hargaS5", hargaS5);
            jBody.put("hargaS10", hargaS10);
            jBody.put("hargaS20", hargaS20);
            jBody.put("hargaS25", hargaS25);
            jBody.put("hargaS50", hargaS50);
            jBody.put("hargaS100", hargaS100);
            jBody.put("hargabulk", (data.get("bulk") == null) ? "": iv.doubleToStringRound(totalBulk));
            jBody.put("total", total);
            jBody.put("keterangan", "-");
            jBody.put("keterangan_order", keteranganOrder);
            jBody.put("flag", "2");
            jBody.put("order_format", orderFormat);
            jBody.put("status_transaksi", "INJECK");
            jBody.put("flag_injek", "SMS");
            jBody.put("proses", "1");
            jBody.put("nonota", nonota);
            jBody.put("cluster", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jBody);
    }

    private void saveData(final List<CustomItem> orderList) {

        isLoading(true);
        final ProgressDialog progressDialog = new ProgressDialog(DetailOrderPulsa.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jBody = new JSONObject();

        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version = pInfo.versionName;

        try {
            jBody.put("version", version);
            jBody.put("data", jsonArray);
            jBody.put("kode", kodeRS);
            jBody.put("nomor", nomorRS);
            jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
            jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String url = ServerURL.saveMKIOS, method = "POST";
        if(editMode){
            try {
                jBody.put("nonota", nonota);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            method = "PUT";
        }

        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {
                    progressDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }

                String superMessage = "Terjadi kesalahan saat menyimpan data, harap ulangi";
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    superMessage = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        String nobukti = response.getJSONObject("response").getString("nobukti");
                        String message = response.getJSONObject("response").getString("message");
                        if(editMode) message = "Order "+ nonota+ " berhasil diubah";

                        Toast.makeText(DetailOrderPulsa.this, message, Toast.LENGTH_LONG).show();

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
                        for(CustomItem item :orderList){

                            items.add(new Item("Denom "+item.getItem2(), item.getItem3(), iv.parseNullDouble(item.getItem4())));
                        }

                        if(iv.parseNullDouble(edtBulk.getText().toString()) > 0){

                            items.add(new Item("Bulk", "-", totalBulk));
                        }

                        Calendar date = Calendar.getInstance();
                        final Transaksi transaksi = new Transaksi(namaRS, session.getUser(), nobukti, date.getTime(), items, iv.getCurrentDate(FormatItem.formatDateDisplay2));

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
                                Intent intent = new Intent(DetailOrderPulsa.this, PenjualanHariIni.class);
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
                        //Toast.makeText(DetailOrderPulsa.this, superMessage, Toast.LENGTH_LONG).show();
                        showDialog(2,superMessage);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailOrderPulsa.this, superMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);

                try {
                    progressDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }

                Toast.makeText(DetailOrderPulsa.this, "Terjadi kesalahan saat menyimpan data, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTextWatcherHarga(EditText edt){

        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                hitungTotal();
            }
        });
    }

    private  double total5, total10, total20, total25, total50, total100, totalBulk, totalHarga;

    private void hitungTotal(){

        double jumlah5 = iv.parseNullDouble(edtS5.getText().toString());
        double jumlah10 = iv.parseNullDouble(edtS10.getText().toString());
        double jumlah20 = iv.parseNullDouble(edtS20.getText().toString());
        double jumlah25 = iv.parseNullDouble(edtS25.getText().toString());
        double jumlah50 = iv.parseNullDouble(edtS50.getText().toString());
        double jumlah100 = iv.parseNullDouble(edtS100.getText().toString());
        double jumlahBulk = iv.parseNullDouble(edtBulk.getText().toString());

        double harga5 = iv.parseNullDouble(hargaS5);
        double harga10 = iv.parseNullDouble(hargaS10);
        double harga20 = iv.parseNullDouble(hargaS20);
        double harga25 = iv.parseNullDouble(hargaS25);
        double harga50 = iv.parseNullDouble(hargaS50);
        double harga100 = iv.parseNullDouble(hargaS100);
        double diskonBulk = iv.parseNullDouble(hargaDiskonBulk);

        total5 = jumlah5 * harga5;
        total10 = jumlah10 * harga10;
        total20 = jumlah20 * harga20;
        total25 = jumlah25 * harga25;
        total50 = jumlah50 * harga50;
        total100 = jumlah100 * harga100;
        totalBulk = jumlahBulk - (jumlahBulk * diskonBulk / 100);

        tvS5.setText(iv.ChangeToRupiahFormat(total5));
        tvS10.setText(iv.ChangeToRupiahFormat(total10));
        tvS20.setText(iv.ChangeToRupiahFormat(total20));
        tvS25.setText(iv.ChangeToRupiahFormat(total25));
        tvS50.setText(iv.ChangeToRupiahFormat(total50));
        tvS100.setText(iv.ChangeToRupiahFormat(total100));
        tvBulk.setText(iv.ChangeToRupiahFormat(totalBulk));

        totalHarga = total5 + total10 + total20 + total25 + total50 + total100 + totalBulk;

        edtTotal.setText(iv.ChangeToRupiahFormat(totalHarga));
    }

    private void isLoading(boolean status){

        isProses = status;
        if(status){
            pbProses.setVisibility(View.VISIBLE);
        }else{
            pbProses.setVisibility(View.GONE);
        }
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
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kode", kodeRS);
            jBody.put("kdcus", "");
            jBody.put("lat", iv.doubleToStringFull(latitude));
            jBody.put("long", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isUpdateLocation = false;
                pbProses.setVisibility(View.GONE);
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
                pbProses.setVisibility(View.GONE);
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
