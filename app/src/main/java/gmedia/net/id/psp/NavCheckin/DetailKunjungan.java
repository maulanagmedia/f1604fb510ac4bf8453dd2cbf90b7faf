package gmedia.net.id.psp.NavCheckin;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailKunjungan extends AppCompatActivity implements LocationListener {

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
    private TextView tvTitle;
    private String TAG = "DetailCheckin";
    private String address0 = "";

    private EditText edtNama, edtJarak, edtKeterangan;
    private Button btnSimpan;
    private String kdcus = "";
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private ProgressDialog progressDialog;
    private Geocoder geocoder;
    private boolean showMode = false;
    private Context context;
    private ProgressBar pbLoading;
    private String nikDetail = "", timestampDetail = "", idKunjungan = "";
    private boolean flag = true;
    private LinearLayout llJarak, llJarak1;
    private EditText edtJarak1;
    private ImageView ivRefreshJarak;
    private boolean isLocationRefresh = false;

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
    private RadioGroup rgKondisiOutlet;
    private RadioButton rbBuka, rbTutup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kunjungan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        context = this;
        setTitle("Rincian Kunjungan");

        // getLocation update by google
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        initUI();
    }

    private void initUI() {

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        rgKondisiOutlet = (RadioGroup) findViewById(R.id.rg_kondisi_outlet);
        rbBuka = (RadioButton) findViewById(R.id.rb_buka);
        rbTutup = (RadioButton) findViewById(R.id.rb_tutup);

        llJarak = (LinearLayout) findViewById(R.id.ll_jarak);
        llJarak1 = (LinearLayout) findViewById(R.id.ll_jarak_1);

        edtJarak1 = (EditText) findViewById(R.id.edt_jarak_1);
        ivRefreshJarak = (ImageView) findViewById(R.id.iv_refresh_position);
        session = new SessionManager(DetailKunjungan.this);
        flag = true;
        isLocationRefresh = false;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kdcus = bundle.getString("kdcus");
            nikDetail = bundle.getString("nik");
            timestampDetail = bundle.getString("timestamp");

            if(nikDetail != null && nikDetail.length() > 0){
                showMode = true;
                getDetailKunjungan(nikDetail, timestampDetail, kdcus);
                flag = bundle.getBoolean("flag", false);

                if(nikDetail.equals(session.getUserInfo(SessionManager.TAG_UID))){ //kunjungan sendiri

                    idKunjungan = bundle.getString("id");
                    btnSimpan.setEnabled(true);
                }else{ // lihat kunjungan sales
                    btnSimpan.setEnabled(false);
                    String namaSales = bundle.getString("nama");
                    if(namaSales != null && namaSales.length() > 0){
                        getSupportActionBar().setSubtitle("a/n "+ namaSales);
                    }
                }
            }else{

                showMode = false;
                btnSimpan.setEnabled(true);
                if(kdcus != null && kdcus.length() > 0){

                    getDataCustomer();
                }
            }

            if(showMode){

                llJarak.setVisibility(View.VISIBLE);
                llJarak1.setVisibility(View.GONE);
            }else{

                llJarak.setVisibility(View.GONE);
                llJarak1.setVisibility(View.VISIBLE);
            }
        }

        initLocation();
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

        isLocationRefresh = true;
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        isLocationRefresh = false;
                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(DetailKunjungan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailKunjungan.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailKunjungan.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location clocation) {

                                        if (clocation != null) {

                                            location = clocation;
                                            onLocationChanged(location);
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
                                    rae.startResolutionForResult(DetailKunjungan.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(DetailKunjungan.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                                //refreshMode = false;
                        }

                        //get Location
                        isLocationRefresh = false;
                        location = getLocation();
                    }
                });
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

    private void updateAllLocation(){
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    private void initLocation() {

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

    private void getDetailKunjungan(String nik, String timestamp, String kdcus) {

        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("timestamp", timestamp);
            jBody.put("kdcus", kdcus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getDetailKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtNama.setText(jo.getString("nama"));
                            if(iv.parseNullDouble(jo.getString("jarak")) <= 6371){
                                if(iv.parseNullDouble(jo.getString("jarak")) <= 1){
                                    edtJarak.setText(iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "2") + " m");
                                }else{
                                    edtJarak.setText(iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "2") + " km");
                                }
                            }else{
                                edtJarak.setText("Jarak outlet tidak diketahui");
                            }

                            edtJarak1.setText("Posisi anda dengan outlet adalah: " + edtJarak.getText().toString());

                            edtKeterangan.setText(jo.getString("keterangan"));

                            if(jo.getString("is_open").equals("1")){

                                rbBuka.setChecked(true);
                            }else{
                                rbTutup.setChecked(true);
                            }
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {

                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateBeforeSave();
            }
        });

        ivRefreshJarak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!showMode){
                    if(!isLocationRefresh){
                        //location = getLocation();
                        updateAllLocation();
                    }
                }
            }
        });
    }

    private void validateBeforeSave() {

        /*if( edtKeterangan.getText().length() == 0){

            edtKeterangan.setError("Keterangan Kunjungan harap diisi");
            edtKeterangan.requestFocus();
            return;
        }else{
            edtKeterangan.setError(null);
        }*/

        saveData();
    }

    private void saveData() {

        progressDialog = new ProgressDialog(DetailKunjungan.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject jDataLocation = new JSONObject();

        try {
            jDataLocation.put("kdcus", kdcus);
            jDataLocation.put("latitude", iv.doubleToStringFull(latitude));
            jDataLocation.put("longitude", iv.doubleToStringFull(longitude));
            jDataLocation.put("keterangan", edtKeterangan.getText().toString());
            jDataLocation.put("nik", nik);
            jDataLocation.put("state", address0);
            jDataLocation.put("is_open", rbBuka.isChecked() ? "1":"0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jDataImages = new JSONArray();

        String method = "POST";
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jDataLocation);
            jBody.put("data_images", jDataImages);
            if(showMode){ // edit kunjungan
                method = "PUT";
                JSONObject jUpdate = new JSONObject();
                jUpdate.put("id", idKunjungan);
                jUpdate.put("kdcus", kdcus);
                jUpdate.put("nik", nik);
                jUpdate.put("timestamp", timestampDetail);
                jBody.put("data_update", jUpdate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailKunjungan.this, jBody, method, ServerURL.saveCheckin, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailKunjungan.this, message, Toast.LENGTH_LONG).show();
                        if(flag){
                            Intent intent = new Intent(DetailKunjungan.this, ActKunjungan.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else{
                            onBackPressed();
                        }
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(DetailKunjungan.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(DetailKunjungan.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(DetailKunjungan.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    private void getDataCustomer() {

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailKunjungan.this, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtNama.setText(jo.getString("nama"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void getJarak() {

        isLocationRefresh = true;
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdcus", kdcus);
            jBody.put("lat", iv.doubleToStringFull(latitude));
            jBody.put("long", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailKunjungan.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            String range = jo.getString("range");
                            String jarak = jo.getString("jarak");
                            String pesan = jo.getString("pesan");
                            String keteranganJarak = "";

                            if(iv.parseNullDouble(jo.getString("jarak")) <= 6371){
                                if(iv.parseNullDouble(jo.getString("jarak")) <= 1){
                                    edtJarak.setText(iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "2") + " m");
                                }else{
                                    edtJarak.setText(iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "2") + " km");
                                }
                            }else{
                                edtJarak.setText("Jarak outlet tidak diketahui");
                            }

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

                            edtJarak1.setText(Html.fromHtml(pesan + keteranganJarak));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pbLoading.setVisibility(View.GONE);
                isLocationRefresh = false;
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                isLocationRefresh = false;
            }
        });
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

    public Location getLocation() {

        isLocationRefresh = true;

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
                Toast.makeText(DetailKunjungan.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailKunjungan.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailKunjungan.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailKunjungan.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailKunjungan.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        }
                        isLocationRefresh = false;
                        return null;
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
                        Location bufferLocation = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (bufferLocation != null) {

                            location = bufferLocation;
                        }
                    }
                }else{
                    //Toast.makeText(context, "Turn on your GPS for better accuracy", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        isLocationRefresh = false;
        if(location != null){
            onLocationChanged(location);
        }

        return location;
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailKunjungan.this);
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
        ActivityCompat.requestPermissions(DetailKunjungan.this,
                new String[]{permissionName}, permissionRequestCode);
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

        this.location = clocation;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        //get address
        new Thread(new Runnable(){
            public void run(){
                address0 = getAddress(location);
            }
        }).start();

        if(!showMode){
            if(!isLocationRefresh){
                getJarak();
            }
        }
    }

    private String getAddress(Location location)
    {
        List<Address> addresses;
        try{
            addresses = new Geocoder(this,Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            return findAddress(addresses);
        }
        catch (Exception e) {

            return "";

        }
    }

    private String findAddress(List<Address> addresses)
    {
        String address="";
        if(addresses!=null)
        {
            for(int i=0 ; i < addresses.size() ; i++){

                Address addre = addresses.get(i);
                String street = addre.getAddressLine(0);
                if(null == street)
                    street="";

                String city = addre.getLocality();
                if(city == null) city = "";

                String state=addre.getAdminArea();
                if(state == null) state="";

                String country = addre.getCountryName();
                if(country == null) country = "";

                address = street+", "+city+", "+state+", "+country;
            }
            return address;
        }
        return address;
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
