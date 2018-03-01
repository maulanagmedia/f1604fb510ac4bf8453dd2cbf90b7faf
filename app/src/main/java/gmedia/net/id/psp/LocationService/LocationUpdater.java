package gmedia.net.id.psp.LocationService;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import gmedia.net.id.psp.TambahCustomer.DetailCustomer;
import gmedia.net.id.psp.Utils.ServerURL;

public class LocationUpdater extends Service implements LocationListener {

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
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 5 meters
    private static final long MIN_TIME_BW_UPDATES = 1; // 5 minute
    private String TAG = "locationUpdater";
    private String address0 = "";
    private SessionManager session;
    private static String nik = "", kdarea = "";
    private ItemValidation iv = new ItemValidation();
    private static Timer timer;
    private int timerTime = 1000 * 60 * 15; // 15 minute refresh
    private boolean onUpdate = false;
    private static String imei1 = "", imei2 = "";
    private FusedLocationProviderClient mFusedLocationClient;

    public LocationUpdater() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {

        //super.onCreate();
        //Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
        context = this;
        session = new SessionManager(context);
        nik = session.getUserInfo(SessionManager.TAG_UID);
        kdarea = session.getUserInfo(SessionManager.TAG_AREA);
        ArrayList<String>imeiList = iv.getIMEI(context);

        if(imeiList.size() > 1){ // dual sim

            imei1 = imeiList.get(0);
            imei2 = imeiList.get(1);
        }else if(imeiList.size() == 1){ // single sim

            imei1 = imeiList.get(0);
        }

        //Log.d(TAG, "onCreate: "+imeiList.get(0)+" "+imeiList.get(1));
        onUpdate = false;

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocation();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //super.onStart(intent, startId);
        //Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

    }

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        try {
            location = getLocation();
        }catch (Exception e){
            e.printStackTrace();
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{permissionName}, permissionRequestCode);
    }

    public Location getLocation() {

        onUpdate = true;
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
                Toast.makeText(context, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        }
                        onUpdate = false;
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
                            //onLocationChanged(location);
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

        /*mFusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location clocation) {
                        // Got last known location. In some rare situations this can be null.
                        if (clocation != null) {
                            Log.d(TAG, "onSuccess: googleloc "+ clocation.getLongitude());
                            location = clocation;
                        }
                    }
                });*/

        if(location != null){
            onLocationChanged(location);
        }

        onUpdate = false;
        timer = new Timer();
        timer.scheduleAtFixedRate(new mainTask(), 1000, timerTime);
        return location;
    }

    @Override
    public void onDestroy() {
        if(timer != null){
            timer.cancel();
        }
        super.onDestroy();
    }

    private void saveLocation(){

        onUpdate = true;
        JSONObject jData = new JSONObject();

        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;

        if(session.isLoggedIn()){
            try {
                jData.put("nik", nik);
                jData.put("keterangan", "LOG_UPDATE , " + deviceMan + " "+ deviceName);
                jData.put("latitude", iv.doubleToStringFull(latitude));
                jData.put("longitude", iv.doubleToStringFull(longitude));
                jData.put("state", address0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String method = "POST";
            JSONObject jBody = new JSONObject();
            try {
                jBody.put("data", jData);
                jBody.put("imei1", imei1);
                jBody.put("imei2", imei2);
                jBody.put("kdarea", kdarea);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ApiVolley request = new ApiVolley(context, jBody, method, ServerURL.logLocation, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
                @Override
                public void onSuccess(String result) {

                    try {

                        JSONObject response = new JSONObject(result);
                        String status = response.getJSONObject("metadata").getString("status");

                        String message = "";
                        if(iv.parseNullInteger(status) == 200){

                            message = response.getJSONObject("response").getString("message");
                        }else{
                            message = response.getJSONObject("metadata").getString("message");
                        }

                        Log.d(TAG, "onSuccess: " + message);

                    } catch (JSONException e) {
                        Log.d(TAG, "onSuccess: " + e.toString());
                    }

                    onUpdate = false;
                }

                @Override
                public void onError(String result) {

                    Log.d(TAG, "onError: " + result);
                    onUpdate = false;
                }
            });
        }
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            Log.d(TAG, "onLocationChanged: " +String.valueOf(latitude)+" , "+ String.valueOf(longitude));
            if(imei1 != null && imei1.length() > 0){ // nik ada
                if(latitude != 0 && longitude != 0 && !onUpdate){ // lat long tidak kosong
                    saveLocation();
                }
            }else if(nik == null){
                //onDestroy();
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(final Location location) {
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        //get state
        /*new Thread(new Runnable(){
            public void run(){
                address0=getAddress(location);
            }
        }).start();*/

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
        //return "Address not available";
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

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}