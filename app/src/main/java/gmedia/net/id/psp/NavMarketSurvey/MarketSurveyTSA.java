package gmedia.net.id.psp.NavMarketSurvey;

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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.Adapter.AutocompleteAdapter;
import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class MarketSurveyTSA extends AppCompatActivity implements LocationListener {

    private Context context;
    private SessionManager session;
    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private AppBarLayout appBarLayout;
    private CustomMapView mvMap;
    private GoogleMap googleMap;
    private String title;
    private CollapsingToolbarLayout collapsingToolbar;
    private ItemValidation iv = new ItemValidation();

    // Location
    private double latitude, longitude;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location;
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 3;
    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private TextView tvTitle;
    private String TAG = "DetailCustomer";
    private String address0 = "";
    private Button btnResetPosition;
    private boolean refreshMode = false;
    private EditText edtNama;
    private Button btnSimpan;

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
    private String idSurvey = "", latitudeString = "", longitudeString = "", state = "";
    private boolean editMode = false;
    private ProgressBar pbProses;
    private ProgressDialog progressDialog;

    private AutoCompleteTextView actvPOI;
    private EditText edtAlamat, edtKartuUtama, edtKartuKedua, edtPulsa, edtData;
    private Spinner spKartuUtama, spKartuKedua, spPulsa, spData;
    private List<OptionItem> listProvider;
    private List<CustomItem> listPOI;
    private String lastKdcus = "", lastCus = "", latitudePOI = "", longitudePOI = "", lastRadius = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_survey_ts);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // getLocation update by google
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        context = this;

        initUI();
    }

    private void initUI(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnResetPosition = (Button) findViewById(R.id.btn_reset);

        actvPOI = (AutoCompleteTextView) findViewById(R.id.actv_poi);
        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat);
        edtKartuUtama = (EditText) findViewById(R.id.edt_kartu_utama);
        edtKartuKedua = (EditText) findViewById(R.id.edt_kartu_kedua);
        edtPulsa = (EditText) findViewById(R.id.edt_pulsa);
        edtData = (EditText) findViewById(R.id.edt_data);
        spKartuUtama = (Spinner) findViewById(R.id.sp_kartu_utama);
        spKartuKedua = (Spinner) findViewById(R.id.sp_kartu_kedua);
        spPulsa = (Spinner) findViewById(R.id.sp_pulsa);
        spData = (Spinner) findViewById(R.id.sp_data);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        session = new SessionManager(context);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        title = "Detail Market Survey";

        initCollapsingToolbar();

        editMode = false;

        initLocation();

        initEvent();
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //validate Before Save
                if(actvPOI.getText().toString().length() == 0){

                    actvPOI.setError("Mohon pilih POI terlebih dahulu");
                    actvPOI.requestFocus();
                    return;
                }else{
                    actvPOI.setError(null);
                }

                if(!actvPOI.getText().toString().equals(lastCus)){

                    actvPOI.setError("POI tidak benar, harap cek kembali");
                    actvPOI.requestFocus();
                    return;
                }else{
                    actvPOI.setError(null);
                }

                if(edtNama.getText().toString().length() == 0){

                    edtNama.setError("Nama harap diisi");
                    edtNama.requestFocus();
                    return;
                }else{
                    edtNama.setError(null);
                }

                if(edtAlamat.getText().toString().length() == 0){

                    edtAlamat.setError("Alamat harap diisi");
                    edtAlamat.requestFocus();
                    return;
                }else{
                    edtAlamat.setError(null);
                }

                AlertDialog konfirmasi = new AlertDialog.Builder(MarketSurveyTSA.this)
                        .setTitle("Konfirmasi")
                        .setMessage( (editMode) ? "Apakah anda yakin ingin menyimpan perubahan ?" : "Apakah anda yakin ingin menyimpan data survey ?")
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
    }

    private void saveData() {

        progressDialog = new ProgressDialog(MarketSurveyTSA.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        String nik = session.getUserInfo(SessionManager.TAG_UID);
        String provKU = "";
        String provKK = "";
        String provPulsa = "";
        String provData = "";
        OptionItem provKartuUtama = (OptionItem) spKartuUtama.getSelectedItem();
        if(provKartuUtama != null) provKU = provKartuUtama.getValue();

        OptionItem provKartuKedua = (OptionItem) spKartuKedua.getSelectedItem();
        if(provKartuKedua != null) provKK = provKartuKedua.getValue();

        OptionItem provPulsaItem = (OptionItem) spPulsa.getSelectedItem();
        if(provPulsaItem != null) provPulsa = provPulsaItem.getValue();

        OptionItem provDataItem = (OptionItem) spData.getSelectedItem();
        if(provDataItem != null) provData = provDataItem.getValue();

        JSONObject jDataSurvey = new JSONObject();

        try {
            jDataSurvey.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jDataSurvey.put("kdcus",lastKdcus);
            jDataSurvey.put("nama", edtNama.getText().toString());
            jDataSurvey.put("alamat", edtAlamat.getText().toString());
            jDataSurvey.put("kartu_utama", edtKartuUtama.getText().toString());
            jDataSurvey.put("prov_kartu_utama", provKU);
            jDataSurvey.put("kartu_kedua", edtKartuKedua.getText().toString());
            jDataSurvey.put("prov_kartu_kedua", provKK);
            jDataSurvey.put("jml_pulsa", edtPulsa.getText().toString());
            jDataSurvey.put("prov_pulsa", provPulsa);
            jDataSurvey.put("jml_data", edtData.getText().toString());
            jDataSurvey.put("prov_data", provData);
            jDataSurvey.put("latitude", latitudeString);
            jDataSurvey.put("longitude", longitudeString);
            jDataSurvey.put("state", state);
            jDataSurvey.put("kodearea", session.getUserInfo(SessionManager.TAG_AREA));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String method = "POST";
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("data", jDataSurvey);
            jBody.put("flag", "TSA");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MarketSurveyTSA.this, jBody, method, ServerURL.saveMarketSurvey, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    progressDialog.dismiss();

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(MarketSurveyTSA.this, message, Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(MarketSurveyTSA.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(MarketSurveyTSA.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                    btnSimpan.setEnabled(true);
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(MarketSurveyTSA.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();

        /*if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }*/

    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarketSurveyTSA.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(MarketSurveyTSA.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location clocation) {

                                        if (clocation != null) {

                                            location = clocation;
                                            refreshMode = true;
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
                                    rae.startResolutionForResult(MarketSurveyTSA.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MarketSurveyTSA.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                                //refreshMode = false;
                        }

                        //get Location
                        location = getLocation();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_SETTINGS){

            if(resultCode == Activity.RESULT_CANCELED){

                mRequestingLocationUpdates = false;
            }else if(resultCode == Activity.RESULT_OK){

                startLocationUpdates();
            }

        }
    }

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        refreshMode = true;

        //getProvider();
        getDataOutlet();

        btnResetPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refreshMode = true;
                //location = getLocation();
                updateAllLocation();
            }
        });
    }

    private void getDataOutlet() {

        pbProses.setVisibility(View.VISIBLE);
        String area = session.getUserInfo(SessionManager.TAG_AREA);
        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("area", area);
            jBody.put("flag", "DS");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCustomerPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbProses.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listPOI = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listPOI.add(new CustomItem(jo.getString("kdcus"),
                                    jo.getString("nama"),
                                    jo.getString("alamat"),
                                    jo.getString("latitude"),
                                    jo.getString("longitude"),
                                    jo.getString("toleransi_jarak")));

                        }
                    }

                    setAutocomplete(listPOI);
                    getProvider();

                } catch (JSONException e) {
                    e.printStackTrace();
                    setAutocomplete(null);
                }
            }

            @Override
            public void onError(String result) {

                setAutocomplete(null);
                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void setAutocomplete(List<CustomItem> listItem) {

        actvPOI.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            AutocompleteAdapter adapterACTV = new AutocompleteAdapter(context, listItem, "");
            actvPOI.setAdapter(adapterACTV);
            actvPOI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    lastKdcus = item.getItem1();
                    lastCus = item.getItem2();
                    String alamat =item.getItem3();

                    //actvPoi.setText(lastCus);
                    //if(actvPOI.getText().length() > 0) actvPOI.setSelection(actvPOI.getText().length());
                    latitudePOI = item.getItem4();
                    longitudePOI = item.getItem5();
                    lastRadius = item.getItem6();
                }
            });
        }
    }

    private void getProvider() {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        ApiVolley request = new ApiVolley(context, jBody, "GET", ServerURL.getProvider, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbProses.setVisibility(View.GONE);

                try {

                    JSONObject responseAPI = new JSONObject(result);
                    String status = responseAPI.getJSONObject("metadata").getString("status");
                    listProvider = new ArrayList<>();

                    if(iv.parseNullDouble(status) == 200){

                        JSONArray jsonArray = responseAPI.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listProvider.add(new OptionItem(jo.getString("id_provider"), jo.getString("nama")));
                        }

                        spKartuUtama.setAdapter(null);
                        spKartuKedua.setAdapter(null);
                        spPulsa.setAdapter(null);
                        spData.setAdapter(null);

                        ArrayAdapter adapterKU = new ArrayAdapter(context, R.layout.layout_simple_list, listProvider);
                        spKartuUtama.setAdapter(adapterKU);
                        spKartuUtama.setSelection(0);

                        ArrayAdapter adapterKK = new ArrayAdapter(context, R.layout.layout_simple_list, listProvider);
                        spKartuKedua.setAdapter(adapterKK);
                        spKartuKedua.setSelection(0);

                        ArrayAdapter adapterP = new ArrayAdapter(context, R.layout.layout_simple_list, listProvider);
                        spPulsa.setAdapter(adapterP);
                        spPulsa.setSelection(0);

                        ArrayAdapter adapterD = new ArrayAdapter(context, R.layout.layout_simple_list, listProvider);
                        spData.setAdapter(adapterD);
                        spData.setSelection(0);

                    }

                    getDataSurvey();

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void getDataSurvey() {

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            idSurvey = bundle.getString("id", "");
            if(idSurvey.length() > 0){

                editMode = true;

                btnSimpan.setEnabled(false);

                getSurveyMarket();

            }else{

                //location = getLocation();
                updateAllLocation();
            }
        }else{
            //location = getLocation();
            updateAllLocation();
        }
    }

    private void getSurveyMarket() {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("id", idSurvey);
            jBody.put("flag", "TSA");
            jBody.put("nik", "");
            jBody.put("keyword", "");
            jBody.put("date1", "");
            jBody.put("date2", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getMarketSurvey, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbProses.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);

                            lastKdcus = jo.getString("kdcus");
                            lastCus = jo.getString("customer");
                            actvPOI.setText(lastCus);

                            //actvPoi.setText(lastCus);
                            //if(actvPOI.getText().length() > 0) actvPOI.setSelection(actvPOI.getText().length());
                            latitudePOI = jo.getString("latitude_poi");
                            longitudePOI = jo.getString("longitude_poi");
                            lastRadius = jo.getString("toleransi_jarak");

                            latitudeString = jo.getString("latitude");
                            longitudeString = jo.getString("longitude");
                            latitude = iv.parseNullDouble(latitudeString);
                            longitude = iv.parseNullDouble(longitudeString);
                            edtNama.setText(jo.getString("nama"));
                            edtAlamat.setText(jo.getString("alamat"));
                            edtKartuUtama.setText(jo.getString("kartu_utama"));
                            edtKartuKedua.setText(jo.getString("kartu_kedua"));
                            edtPulsa.setText(jo.getString("jml_pulsa"));
                            edtData.setText(jo.getString("jml_pulsa"));

                            String kartuUtama = jo.getString("prov_kartu_utama");
                            String kartuKedua = jo.getString("prov_kartu_kedua");
                            String jmlPulsa = jo.getString("prov_pulsa");
                            String jmlData = jo.getString("prov_data");

                            int positionUtama = 0;
                            int positionKedua = 0;
                            int positionPulsa = 0;
                            int positionData = 0;
                            for (int x = 0; x < listProvider.size(); x++){

                                if(listProvider.get(x).getValue().equals(kartuUtama)) positionUtama = x;
                                if(listProvider.get(x).getValue().equals(kartuKedua)) positionKedua = x;
                                if(listProvider.get(x).getValue().equals(jmlPulsa)) positionPulsa = x;
                                if(listProvider.get(x).getValue().equals(jmlData)) positionData = x;
                            }

                            spKartuUtama.setSelection(positionUtama);
                            spKartuKedua.setSelection(positionKedua);
                            spPulsa.setSelection(positionPulsa);
                            spData.setSelection(positionData);

                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            refreshMode = true;
                            onLocationChanged(location);
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void updateAllLocation(){
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    public Location getLocation() {

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
                Toast.makeText(MarketSurveyTSA.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(MarketSurveyTSA.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarketSurveyTSA.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MarketSurveyTSA.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(MarketSurveyTSA.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        }
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

        if(location != null && location.getLongitude() != 0 && location.getLatitude() != 0){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MarketSurveyTSA.this);
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
        ActivityCompat.requestPermissions(MarketSurveyTSA.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void initCollapsingToolbar() {
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.ctl_main);
        collapsingToolbar.setTitle(" ");
        tvToolbarTitle.setText(" ");
        appBarLayout = (AppBarLayout) findViewById(R.id.abl_main);
        appBarLayout.setExpanded(true);

        // hiding & showing the tvTitle when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (appBarLayout.getTotalScrollRange() + verticalOffset <= 30) {
                    tvToolbarTitle.setText(title);
                    isShow = true;
                } else if (isShow) {

                    tvToolbarTitle.setText(" ");
                    isShow = false;
                }
            }
        });
    }

    private void setPointMap(){

        mvMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .draggable(true)
                        .position(new LatLng(latitude, longitude)));

                if (ActivityCompat.checkSelfPermission(MarketSurveyTSA.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarketSurveyTSA.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MarketSurveyTSA.this, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(MarketSurveyTSA.this);
                LatLng position = new LatLng(latitude, longitude);
                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                updateKeterangan(position);

                /*googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {

                        LatLng position = marker.getPosition();
                        updateKeterangan(position);
                        Log.d(TAG, "onMarkerDragEnd: " + position.latitude +" "+ position.longitude);
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .anchor(0.0f, 1.0f)
                                .draggable(true)
                                .position(latLng));
                        updateKeterangan(latLng);
                        Log.d(TAG, "onMarkerDragEnd: " + latLng.latitude +" "+ latLng.longitude);
                    }
                });*/
            }
        });
    }

    private void updateKeterangan(LatLng position){

        latitude = position.latitude;
        longitude = position.longitude;

        //get address
        new Thread(new Runnable(){
            public void run(){
                address0 = getAddress(location);
            }
        }).start();

        latitudeString = iv.doubleToStringFull(latitude);
        longitudeString = iv.doubleToStringFull(longitude);
        state = address0;
    }

    private String getAddress(Location location)
    {
        List<Address> addresses;
        try{
            addresses = new Geocoder(this, Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    @Override
    public void onLocationChanged(Location location) {

        if(refreshMode){
            refreshMode = false;
            this.location = location;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            setPointMap();
        }
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