package gmedia.net.id.psp.OrderDirectSelling;

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.OrderDirectSelling.Adapter.ListCCIDAllAdapter;
import gmedia.net.id.psp.OrderDirectSelling.Adapter.ListCCIDDSAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDCBAdapter;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailDSPerdana extends AppCompatActivity implements LocationListener{

    private static Context context;
    private static SessionManager session;
    private static ItemValidation iv = new ItemValidation();
    private static final String TAG = "DSPerdana";

    // Location
    private double latitude, longitude;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private static Location location;
    private final int REQUEST_PERMISSION_COARSE_LOCATION=2;
    private final int REQUEST_PERMISSION_FINE_LOCATION=3;
    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private EditText edtJarak;
    private ImageView ivRefreshPosition;
    private static String jarak = "";
    private String range = "";
    private String latitudeEvent = "";
    private String longitudeEvent = "";
    private Button btnMapsOutlet, btnProses;

    private boolean isUpdateLocation = false;
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
    private boolean editMode = false;
    private static String nomor = "", radius = "", nama = "", alamat = "", kdcus = "", flagRadius ="", latitudePOI = "", longitudePOI = "", poiName = "";
    private boolean isEvent = false, isPOI = false;
    private static ProgressBar pbProses;

    private static boolean isProses = false;
    private Button btnAmbilCCIDList, btnScanCCID;
    private EditText edtCCID, edtNamaBrg, edtHargaBrg;
    private static ListView lvPerdana;
    private static EditText edtTotalCCID, edtTotalHarga;
    private static List<CustomItem> listBarang;
    private static ListCCIDDSAdapter adapter;
    private LinearLayout llJarak;
    private List<OptionItem> listCCID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_dsperdana);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        setTitle("Direct Selling Perdana");
        context = this;
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MockLocChecker checker = new MockLocChecker(DetailDSPerdana.this);
    }

    private void initUI() {

        btnAmbilCCIDList = (Button) findViewById(R.id.btn_ambil_ccid_list);
        btnScanCCID = (Button) findViewById(R.id.btn_scan_ccid);
        edtCCID = (EditText) findViewById(R.id.edt_ccid);
        edtNamaBrg = (EditText) findViewById(R.id.edt_nama_barang_scan);
        edtHargaBrg = (EditText) findViewById(R.id.edt_harga_scan);
        lvPerdana = (ListView) findViewById(R.id.lv_perdana);
        edtTotalCCID = (EditText) findViewById(R.id.edt_total_ccid);
        edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
        pbProses = (ProgressBar) findViewById(R.id.pb_loading);
        llJarak = (LinearLayout) findViewById(R.id.ll_jarak);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_event);
        btnProses = (Button) findViewById(R.id.btn_proses);
        isEvent = false;
        session = new SessionManager(context);

        nomor = "";
        radius = "";
        flagRadius = "";
        nama = "";
        alamat = "";
        kdcus = "";
        jarak = "";

        listBarang = new ArrayList<>();
        adapter = new ListCCIDDSAdapter((Activity) context, listBarang);
        lvPerdana.setAdapter(adapter);
        initLocationManual();
        initEvent();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            nomor = bundle.getString("nomor","");
            kdcus = bundle.getString("kdcus", "");
            nama = bundle.getString("nama", "");
            alamat = bundle.getString("alamat", "");
            latitudePOI =  bundle.getString("lat_poi", "");
            longitudePOI =  bundle.getString("long_poi", "");
            radius = bundle.getString("radius", "");
            poiName = bundle.getString("poi", "");

            if(nomor.length() > 0){

                //edtNomor.setText(nomor);
                isEvent = true;
                latitudeEvent = bundle.getString("lat", "");
                longitudeEvent = bundle.getString("long", "");
                flagRadius = bundle.getString("flag_radius", "");
            }
        }

        if(!isEvent){

            if(kdcus.length() == 0){
                llJarak.setVisibility(View.GONE);
                btnMapsOutlet.setVisibility(View.GONE);
                isPOI = false;
            }else{ // POI
                llJarak.setVisibility(View.VISIBLE);
                btnMapsOutlet.setVisibility(View.VISIBLE);
                isPOI = true;
            }
        }else{

            btnMapsOutlet.setText("Peta Event");
        }
    }

    private void initEvent() {

        btnAmbilCCIDList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getBarangByNik();
            }
        });

        btnScanCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edtCCID.setText("");
                edtNamaBrg.setText("");
                edtHargaBrg.setText("");
                openScanBarcode();
            }
        });

        lvPerdana.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                //1. kdbrg
                //2. namabrg
                //3. ccid
                //4. harga
                //5. jumlah
                //6. nomor surat jalan
                //7. id surat jalam

                edtCCID.setText(item.getItem3());
                edtNamaBrg.setText(item.getItem2());
                edtHargaBrg.setText(iv.ChangeToRupiahFormat(item.getItem4()));
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //validasi
                if(adapter == null ){

                    Toast.makeText(context, "Silahkan masukkan minimal satu barang", Toast.LENGTH_LONG).show();
                    return;
                }

                if(adapter.getDataList().size() == 0){

                    Toast.makeText(context, "Silahkan masukkan minimal satu barang", Toast.LENGTH_LONG).show();
                    return;
                }

                if(isEvent && !isOnLocation(location) && flagRadius.equals("1")){ // wajib tapi tidak dilokasi

                    Snackbar.make(findViewById(android.R.id.content), "Posisi anda jauh dari lokasi event, mohon menuju " + radius + " km dari lokasi event",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                    return;
                }

                if(isPOI && !isOnLocation(location)){ // wajib tapi tidak dilokasi

                    Snackbar.make(findViewById(android.R.id.content), "Posisi anda jauh dari " +poiName+ ", mohon menuju " + radius + " km dari lokasi",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                    return;
                }

                String message = "Apakah anda yakin ingin memproses data?\n\n";
                // if(editMode) message = "Apakah anda yakin ingin mengubah "+noBukti+" ?\n\n";
                message += ("Total " + String.valueOf(adapter.getDataList().size())+" CCID");
                AlertDialog builder = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage(message)
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

                String latLokasi = "", longLokasi = "";

                if(isEvent){
                    latLokasi = latitudeEvent;
                    longLokasi = longitudeEvent;
                }else if (isPOI){
                    latLokasi = latitudePOI;
                    longLokasi = longitudePOI;
                }else{
                    latLokasi = latitudeEvent;
                    longLokasi = longitudeEvent;
                }

                if(!latLokasi.equals("")&& !longLokasi.equals("")){

                    Intent intent = new Intent(context, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latLokasi);
                    intent.putExtra("long_outlet", longLokasi);
                    intent.putExtra("nama", poiName);

                    startActivity(intent);
                }else{

                    Toast.makeText(context, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showListCCID() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailDSPerdana.this, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_ccid, null);
        builder.setView(view);

        final AutoCompleteTextView actvCCIDBA = (AutoCompleteTextView) view.findViewById(R.id.actv_ccid);
        final ListView lvCCIDBA = (ListView) view.findViewById(R.id.lv_ccid);

        final List<OptionItem> lastData = new ArrayList<>(listCCID);

        lastData.add(0,new OptionItem("0","","all","","","","", false));
        final ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, lastData);
        lvCCIDBA.setAdapter(adapterB);

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
                    ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, lastData);
                    lvCCIDBA.setAdapter(adapterB);
                }
            }
        });

        //1. kdbrg
        //2. namabrg
        //3. ccid
        //4. harga
        //5. jumlah
        //6. nomor surat jalan
        //7. id surat jalan
        actvCCIDBA.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<OptionItem> items = new ArrayList<OptionItem>();
                    String keyword = actvCCIDBA.getText().toString().trim().toUpperCase();

                    for (OptionItem item: lastData){

                        if(item.getAtt3().toUpperCase().contains(keyword)) items.add(item);
                    }

                    ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, items);
                    lvCCIDBA.setAdapter(adapterB);
                    iv.hideSoftKey(DetailDSPerdana.this);
                    return true;
                }

                return false;
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ListCCIDAllAdapter adapterSelected = (ListCCIDAllAdapter) lvCCIDBA.getAdapter();
                List<OptionItem> optionItems = adapterSelected.getItems();

                adapter.clearData();
                int x = 0;
                for(OptionItem item: optionItems){

                    if(item.isSelected()){

                        adapter.addData(
                                new CustomItem(

                                        item.getAtt1(),
                                        item.getAtt2(),
                                        item.getAtt3(),
                                        item.getAtt4(),
                                        item.getAtt5(),
                                        item.getAtt6(),
                                        item.getAtt7()
                                )
                        );
                    }
                    x++;
                }

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

    private static void saveData() {

        isLoading(true);
        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version = pInfo.versionName;

        //1. kdbrg
        //2. namabrg
        //3. ccid
        //4. harga
        //5. jumlah
        //6. nomor surat jalan
        //7. id surat jalam

        JSONArray jData = new JSONArray();
        for(CustomItem item: adapter.getDataList()){

            JSONObject jDataDetail = new JSONObject();
            try {
                jDataDetail.put("nobukti","");
                jDataDetail.put("nik", nik);
                jDataDetail.put("kodebrg", item.getItem1());
                jDataDetail.put("ccid", item.getItem3());
                jDataDetail.put("harga", item.getItem4());
                jDataDetail.put("jumlah", item.getItem5());
                jDataDetail.put("total", item.getItem4());
                jDataDetail.put("kdcus", kdcus);
                jDataDetail.put("nama", nama);
                jDataDetail.put("alamat", alamat);
                jDataDetail.put("nomor", "");
                jDataDetail.put("nomor_event", nomor);
                jDataDetail.put("jarak", jarak);
                jDataDetail.put("no_surat_jalan", item.getItem6());
                jDataDetail.put("id_surat_jalan", item.getItem7());
                jDataDetail.put("transaction_id", "0");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jData.put(jDataDetail);
        }

        try {
            jBody.put("version", version);
            jBody.put("data", jData);
            jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
            jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
            jBody.put("radius", radius);
            jBody.put("flag_radius", flagRadius);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ServerURL.saveDirectSelling, method = "POST";
        /*if(editMode){
            try {
                jBody.put("nonota", nonota);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            method = "PUT";
        }*/

        ApiVolley request = new ApiVolley(context, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                progressDialog.dismiss();

                String superMessage = "Terjadi kesalahan saat menyimpan data, harap ulangi";
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    superMessage = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(context, PenjualanHariIni.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ((Activity)context).startActivity(intent);
                        ((Activity)context).finish();
                    }else{
                        //Toast.makeText(DetailOrderPulsa.this, superMessage, Toast.LENGTH_LONG).show();
                        showDialog(2,superMessage);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, superMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);
                progressDialog.dismiss();
                Toast.makeText(context, "Terjadi kesalahan saat menyimpan data, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    private void openScanBarcode() {

        Collection<String> ONE_D_CODE_TYPES =
                list("CODE_128B","QR_CODE");

        IntentIntegrator integrator = new IntentIntegrator(DetailDSPerdana.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    private void initLocationManual() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        //location = getLocation();
        updateAllLocation();
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
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailDSPerdana.this, new OnSuccessListener<Location>() {
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
                                    rae.startResolutionForResult(DetailDSPerdana.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                                //refreshMode = false;
                        }

                        //get Location
                        isUpdateLocation = false;
                        location = getLocation();
                    }
                });
    }

    private boolean isOnLocation(Location detectedLocation){

        String latLokasi = "", longLokasi = "";

        if(isEvent){
            latLokasi = latitudeEvent;
            longLokasi = longitudeEvent;
        }else if (isPOI){
            latLokasi = latitudePOI;
            longLokasi = longitudePOI;
        }else{
            latLokasi = latitudeEvent;
            longLokasi = longitudeEvent;
        }

        boolean hasil = false;

        if(!radius.equals("") && !latLokasi.equals("") && !longLokasi.equals("") && detectedLocation != null){

            double latOutlet = iv.parseNullDouble(latLokasi);
            double longOutlet = iv.parseNullDouble(longLokasi);

            double detectedJarak = (6371 * Math.acos(Math.sin(Math.toRadians(latOutlet)) * Math.sin(Math.toRadians(detectedLocation.getLatitude())) + Math.cos(Math.toRadians(longOutlet - detectedLocation.getLongitude())) * Math.cos(Math.toRadians(latOutlet)) * Math.cos(Math.toRadians(detectedLocation.getLatitude()))));
            double rangeDouble = iv.parseNullDouble(range);


            range = radius;
            jarak = iv.doubleToStringFull(detectedJarak);
            String pesan = "Jarak saat ini dengan lokasi adalah ";
            String keteranganJarak = "";
            if(iv.parseNullDouble(radius) <= 6371){
                if(iv.parseNullDouble(jarak) <= 1){
                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak) * 1000, "2") + " m";
                }else{
                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak), "2") + " km";
                }

                if(iv.parseNullDouble(jarak) > iv.parseNullDouble(range)){

                    keteranganJarak = "<font color='#ec1c25'>"+keteranganJarak+"</font>";
                }

            }else{

                keteranganJarak = "<font color='#ec1c25'>Lokasi outlet tidak diketahui</font>";
            }

            edtJarak.setText(Html.fromHtml(pesan + keteranganJarak));

            if(detectedJarak <= rangeDouble) {

                hasil = true;
            }else{

            }
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
                Toast.makeText(context, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailDSPerdana.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailDSPerdana.this,
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

                            Location locationBuffer = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if(locationBuffer != null){
                                location = locationBuffer;
                            }

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
                            Location locationBuffer = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if(locationBuffer != null){

                                location = locationBuffer;
                            }
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
        ActivityCompat.requestPermissions(DetailDSPerdana.this,
                new String[]{permissionName}, permissionRequestCode);
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

        }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {

                    Log.d(TAG, "onActivityResult: Scan failed ");
                } else {

                    getBarang(result.getContents());
                    //getBarang("0650000032698023");
                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void getBarangByNik() {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("ccid", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getBarangByCCID, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listCCID = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            boolean flag = false;
                            if(adapter != null && adapter.getDataList().size() > 0){
                                for(CustomItem item: adapter.getDataList()){
                                    if(item.getItem3().equals(jo.getString("ccid"))){
                                        flag = true;
                                    }
                                }
                            }

                            //1. kdbrg
                            //2. namabrg
                            //3. ccid
                            //4. harga
                            //5. jumlah
                            //6. nomor surat jalan
                            //7. id surat jalan

                            listCCID.add(new OptionItem(
                                    jo.getString("kodebrg"),
                                    jo.getString("namabrg"),
                                    jo.getString("ccid"),
                                    jo.getString("harga"),
                                    jo.getString("jumlah"),
                                    jo.getString("nobukti"),
                                    jo.getString("id"),
                                    flag
                            ));
                        }

                        if(items.length() == 0){
                            showDialog(2, "Tidak ada barang di surat jalan");
                        }else{

                            showListCCID();
                        }
                    }else{
                        showDialog(2, "Barang tidak ditemukan di surat jalan hari ini");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showDialog(2, "Terjadi kesalahan saat memuat data, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                showDialog(2, "Terjadi kesalahan saat memuat data, harap ulangi");
            }
        });
    }

    private void getBarang(final String ccid) {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("ccid", ccid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getBarangByCCID, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            adapter.addData(new CustomItem(jo.getString("kodebrg"),
                                    jo.getString("namabrg"),
                                    jo.getString("ccid"),
                                    jo.getString("harga"),
                                    jo.getString("jumlah"),
                                    jo.getString("nobukti"),
                                    jo.getString("id")));

                            edtCCID.setText(jo.getString("ccid"));
                            edtNamaBrg.setText(jo.getString("namabrg"));
                            edtHargaBrg.setText(jo.getString("harga"));
                            //1. kdbrg
                            //2. namabrg
                            //3. ccid
                            //4. harga
                            //5. jumlah
                            //6. nomor surat jalan
                            //7. id surat jalan
                            break;
                        }

                        if(items.length() == 0){
                            showDialog(2, "Barang "+ ccid +" tidak ditemukan di surat jalan hari ini");
                        }
                    }else{
                        showDialog(2, "Barang tidak ditemukan di surat jalan hari ini");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showDialog(2, "Terjadi kesalahan saat memuat data, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                showDialog(2, "Terjadi kesalahan saat memuat data, harap ulangi");
            }
        });
    }

    private static void isLoading(boolean status){

        isProses = status;

        if(pbProses != null){
            if(status){
                pbProses.setVisibility(View.VISIBLE);
            }else{
                pbProses.setVisibility(View.GONE);
            }
        }
    }

    public static void updateTotal(){

        if(adapter != null && edtTotalCCID != null && edtTotalHarga != null){

            List<CustomItem> barangSelected = adapter.getDataList();
            edtTotalCCID.setText(String.valueOf(barangSelected.size()));
            double total = 0;
            for(CustomItem item : barangSelected){

                total+= iv.parseNullDouble(item.getItem4());
            }

            edtTotalHarga.setText(iv.ChangeToRupiahFormat(total));
        }
    }

    private static void showDialog(int state, String message){

        if(state == 1){

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
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

                    if(alert != null) alert.dismiss();
                }
            });

            alert.show();
        }else if(state == 2){ // failed
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
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

                    if(alert != null) alert.dismiss();
                }
            });

            alert.show();
        }else if(state == 3){

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
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

                    if(alert != null) alert.dismiss();
                    //getBarang();
                }
            });

            try {
                alert.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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
            isOnLocation(clocation);
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
