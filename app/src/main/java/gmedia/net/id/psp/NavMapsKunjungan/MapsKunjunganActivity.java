package gmedia.net.id.psp.NavMapsKunjungan;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.DetailCustomer;
import gmedia.net.id.psp.Utils.ServerURL;

public class MapsKunjunganActivity extends AppCompatActivity implements LocationListener {

    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private CustomMapView mvMap;
    private GoogleMap googleMap;

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
    private String TAG = "DetailCustomer";
    private String address0 = "";
    private ProgressBar pbLoading;
    private boolean refreshMode = false;
    private boolean isMain = true;

    private List<CustomItem> listKunjungan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_kunjungan);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            isMain = bundle.getBoolean("main", true);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Maps Kunjungan");

        initUI();
    }

    private void initUI() {

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        session = new SessionManager(MapsKunjunganActivity.this);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(MapsKunjunganActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initLocation();
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
        location = getLocation();

        /*btnResetPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refreshMode = true;
                location = getLocation();
            }
        });*/
    }

    private void getDetailKunjungan() {

        listKunjungan = new ArrayList<>();
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", "");
            jBody.put("keyword", "");
            jBody.put("start", "0");
            jBody.put("count", "1000");
            jBody.put("latitude", iv.doubleToStringFull(latitude));
            jBody.put("longitude", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(MapsKunjunganActivity.this, jBody, "POST", ServerURL.getCustomerKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listKunjungan = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listKunjungan.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"),jo.getString("alamat"), jo.getString("latitude"), jo.getString("longitude")));
                        }
                    }

                    setPointMap();
                    pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                pbLoading.setVisibility(View.GONE);
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
                Toast.makeText(MapsKunjunganActivity.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(MapsKunjunganActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsKunjunganActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsKunjunganActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsKunjunganActivity.this,
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

                        Location bufferLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (bufferLocation != null) {
                            location = bufferLocation;
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

        if(location != null){
            onLocationChanged(location);
        }
        return location;
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsKunjunganActivity.this);
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
        ActivityCompat.requestPermissions(MapsKunjunganActivity.this,
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

        if(isMain){
            Intent intent = new Intent(MapsKunjunganActivity.this, MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }else{

            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        if(refreshMode){
            refreshMode = false;
            this.location = location;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            getDetailKunjungan();
        }
    }

    private void callGoogleMap(String latitude, String longitude){

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ","+longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(MapsKunjunganActivity.this, "Cannot find google map, Please install latest google map.",
                    Toast.LENGTH_LONG).show();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
            startActivity(browserIntent);
        }
    }

    private void setPointMap(){

        mvMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                googleMap.clear();

                if(listKunjungan != null && listKunjungan.size() > 0){

                    int x = 0;
                    for(CustomItem item: listKunjungan){

                        if(x == 0){
                            latitude = iv.parseNullDouble(item.getItem4());
                            longitude = iv.parseNullDouble(item.getItem5());
                        }

                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .anchor(0.0f, 1.0f)
                                .draggable(true)
                                .snippet(item.getItem3())
                                .title(item.getItem2())
                                .position(new LatLng(iv.parseNullDouble(item.getItem4()), iv.parseNullDouble(item.getItem5()))));
                        marker.setTag(x);
                        x++;
                    }

                    /*googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            int position = (int)(marker.getTag());
                            //Using position get Value from arraylist
                            return false;
                        }
                    });*/
                }


                if (ActivityCompat.checkSelfPermission(MapsKunjunganActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsKunjunganActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MapsKunjunganActivity.this, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(MapsKunjunganActivity.this);
                LatLng position = new LatLng(latitude, longitude);
                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
                        Log.d(TAG, "onMarkerDragEnd: " + latLng.latitude +" "+ latLng.longitude);
                    }
                });*/
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
