package gmedia.net.id.psp.NavEvent;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.material.snackbar.Snackbar;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.Adapter.AutocompleteAdapter;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.NavHome.NavHome;
import gmedia.net.id.psp.OrderDirectSelling.Adapter.ListBalasanInjectAdapter;
import gmedia.net.id.psp.OrderDirectSelling.Adapter.ListBarangEUAdapter;
import gmedia.net.id.psp.OrderDirectSelling.DetailDSPerdana;
import gmedia.net.id.psp.OrderDirectSelling.DetailInjectPulsa;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailEventManual extends AppCompatActivity implements LocationListener {

    private Context context;
    private static ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private EditText edtNomor;
    private AutoCompleteTextView actvNama;
    private EditText edtAlamat;
    private LinearLayout btnSimpan, llBeliPulsa, llBeliPerdana;
    private ProgressBar pbLoading;
    private List<HashMap<String,String >> masterList;
    private SimpleAdapter adapter;
    private String nomor = "", lokasi = "", lastKdcus = "", lastCus = "", selectedKdcus = "", latitudePOI = "", longitudePOI = "", lastRadius = "";
    private LinearLayout llNomor, llLokasi;
    private AutoCompleteTextView actvPoi;
    private LinearLayout llPOI;
    private List<CustomItem> listPOI;
    private DialogBox dialog;
    private boolean isProses = false;
    private ListBarangEUAdapter adapterBarang;
    private List<CustomItem> listBarang;
    public boolean isActive = false;
    private ListView lvBarang;
    private static String radius = "", nama = "", alamat = "", kdcus = "", flagRadius = "", poiName = "";

    private LinearLayout llNonota;
    private EditText edtNonota;
    private static EditText edtHarga;
    private static List<CustomItem> listBalasan;
    private static ListBalasanInjectAdapter balasanAdapter;
    private Button btnProses;
    private static final String TAG = "DetailInject";
    private static ProgressBar pbProses;
    private static LinearLayout llNominal;
    private static EditText edtNominal;
    private static String flagOrder = "", selectedHarga = "";
    private static CustomItem selectedItemOrder;
    private String currentString = "";
    private LinearLayout llJarak;
    private EditText edtJarak;
    private ImageView ivRefreshPosition;
    private Button btnMapsOutlet;
    private static String lastKodebrg = "", lastFlagOrder = "", lastSN = "", lastSuccessBalasan = "", lastSuccessSender = "";

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
    private static String jarak = "";
    private String range = "";
    private String latitudeEvent = "";
    private String longitudeEvent = "";
    private boolean isPOI = false;

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
    private Activity activity;
    private boolean isOnSaving = false;
    private String transactionID = "";
    private EditText edtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event_manual);

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

        transactionID = iv.getCurrentDate(FormatItem.formatDateTimePure);


        setTitle("Penjualan DDS");
        context = this;
        activity = this;

        initUI();
    }

    private void initUI() {

        llNomor = (LinearLayout) findViewById(R.id.ll_nomor);
        llLokasi = (LinearLayout) findViewById(R.id.ll_lokasi);
        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        actvPoi = (AutoCompleteTextView) findViewById(R.id.actv_poi);
        actvNama = (AutoCompleteTextView) findViewById(R.id.actv_nama);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat);
        btnSimpan = (LinearLayout) findViewById(R.id.simpan);
        llPOI = (LinearLayout) findViewById(R.id.ll_poi);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        llBeliPulsa = (LinearLayout) findViewById(R.id.ll_beli_pulsa);
        edtTotal = (EditText) findViewById(R.id.edt_total);

        llNonota = (LinearLayout) findViewById(R.id.ll_nonota);
        edtNonota = (EditText) findViewById(R.id.edt_nonota);
        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);
        llNominal = (LinearLayout) findViewById(R.id.ll_nominal);
        edtNominal = (EditText) findViewById(R.id.edt_nominal);
        lvBarang = (ListView) findViewById(R.id.lv_barang);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        llJarak = (LinearLayout) findViewById(R.id.ll_jarak);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_event);
        isOnSaving = false;

        currentString = "";

        edtTotal.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(!editable.toString().equals(currentString)){

                    String cleanString = editable.toString().replaceAll("[,.]", "");
                    edtTotal.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    currentString = formatted;
                    edtTotal.setText(formatted);
                    edtTotal.setSelection(formatted.length());
                    edtTotal.addTextChangedListener(this);
                }
            }
        });

        dialog = new DialogBox(context);

        session = new SessionManager(context);
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            nomor = bundle.getString("nomor","");
            lokasi = bundle.getString("lokasi","");

            if(nomor.length() > 0){

                llNomor.setVisibility(View.VISIBLE);
                llLokasi.setVisibility(View.VISIBLE);

                edtNomor.setText(nomor);
            }

        }

        setTitle("Detail Direct Selling");
        llPOI.setVisibility(View.VISIBLE);

        initEvent();

        getDataOutlet();

        initLocationManual();

        getBarang();
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
        ActivityCompat.requestPermissions(DetailEventManual.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void updateAllLocation(){
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    private void getBarang() {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getBarangUE, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading(false);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listBarang = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listBarang.add(new CustomItem(
                                    jo.getString("kodebrg"),
                                    jo.getString("namabrg"),
                                    jo.getString("hargajual"),
                                    "0",
                                    jo.getString("flag"),
                                    jo.getString("pin"),
                                    jo.getString("format"),
                                    jo.getString("balasan")));
                            //1. kdbrg
                            //2. namabrg
                            //3. hargajual
                            //4. flag dipilih
                            //5. flag jenis order MK, BL, TC
                            //6. pin rs
                            //7. format inject
                            //8. balasan
                            //break;
                        }
                    }

                    setTableBarang(listBarang);

                } catch (JSONException e) {
                    e.printStackTrace();
                    showDialog(3, "Terjadi kesalahan saat mengambil data barang, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                showDialog(3, "Terjadi kesalahan saat memuat data barang, harap ulangi");
            }
        });
    }

    private void setTableBarang(List<CustomItem> listBarang) {

        lvBarang.setAdapter(null);
        if(listBarang != null){

            adapterBarang = new ListBarangEUAdapter((Activity) context, listBarang);
            lvBarang.setAdapter(adapterBarang);
            if(listBarang.size() > 0) setSelectedItem(listBarang.get(0));

            /*lvBarang.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    adapterBarang.setSelected(i);
                }
            });*/
        }
    }

    public static void setSelectedItem(CustomItem item){

        //1. kdbrg
        //2. namabrg
        //3. hargajual
        //4. flag dipilih
        //5. flag jenis order MK, BL, TC, BL2
        //6. pin rs

        selectedItemOrder = item;

        flagOrder = item.getItem5();

        llNominal.setVisibility(View.GONE);

        if(iv.parseNullDouble(item.getItem3()) > 0){ // ada harganya
            edtNominal.setText(selectedItemOrder.getItem3());
            edtHarga.setVisibility(View.VISIBLE);
            //hitungHarga();
        }else{
            //llNominal.setVisibility(View.VISIBLE);
            edtNominal.setText("1");
            edtHarga.setVisibility(View.GONE);
        }

        if(flagOrder.equals("BL2")){ // Bulk reguler

            edtNominal.setText("");
            edtHarga.setVisibility(View.VISIBLE);
        }
    }

    private void isLoading(boolean status){

        isProses = status;

        if(pbLoading != null){
            if(status){
                pbLoading.setVisibility(View.VISIBLE);
            }else{
                pbLoading.setVisibility(View.GONE);
            }
        }
    }

    private void getDataOutlet() {

        pbLoading.setVisibility(View.VISIBLE);
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

                pbLoading.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();
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

                    //getAutocompleteEvent(masterList);
                    setAutocomplete(listPOI);

                } catch (JSONException e) {
                    e.printStackTrace();
                    //getAutocompleteEvent(null);
                    setAutocomplete(null);
                }
            }

            @Override
            public void onError(String result) {

                //getAutocompleteEvent(null);
                setAutocomplete(null);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void getAutocompleteEvent(List<HashMap<String, String>> listItem) {

        String[] from = {"nama"};
        int[] to = new int[] { android.R.id.text1 };

        actvPoi.setAdapter(null);
        if(listItem != null){

            adapter = new SimpleAdapter(context, listItem, android.R.layout.simple_list_item_1,
                    from, to);
            actvPoi.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actvPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    HashMap<String, String> customer = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                    lastKdcus = customer.get("kdcus");
                    lastCus = customer.get("nama");
                    String alamat = customer.get("alamat");

                    actvPoi.setText(lastCus);
                    if(actvPoi.getText().length() > 0) actvPoi.setSelection(actvPoi.getText().length());
                    latitudePOI = customer.get("latitude");
                    longitudePOI = customer.get("longitude");
                    lastRadius = customer.get("radius");

                    if(lastKdcus.length() == 0){
                        llJarak.setVisibility(View.GONE);
                        btnMapsOutlet.setVisibility(View.GONE);
                        isPOI = false;
                    }else{ //POI
                        llJarak.setVisibility(View.VISIBLE);
                        btnMapsOutlet.setVisibility(View.VISIBLE);
                        isPOI = true;
                    }

                    //edtAlamat.setText(alamat);
                }
            });
        }
    }

    private void setAutocomplete(List<CustomItem> listItem) {

        actvPoi.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            AutocompleteAdapter adapterACTV = new AutocompleteAdapter(context, listItem, "");
            actvPoi.setAdapter(adapterACTV);
            actvPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    lastKdcus = item.getItem1();
                    lastCus = item.getItem2();
                    String alamat =item.getItem3();

                    //actvPoi.setText(lastCus);
                    //if(actvPoi.getText().length() > 0) actvPoi.setSelection(actvPoi.getText().length());
                    latitudePOI = item.getItem4();
                    longitudePOI = item.getItem5();
                    lastRadius = item.getItem6();

                    if(lastKdcus.length() == 0){
                        llJarak.setVisibility(View.GONE);
                        btnMapsOutlet.setVisibility(View.GONE);
                        isPOI = false;
                    }else{ //POI
                        llJarak.setVisibility(View.VISIBLE);
                        btnMapsOutlet.setVisibility(View.VISIBLE);
                        isPOI = true;
                    }

                    if(actvPoi.getText().toString().length() > 0) {
                        selectedKdcus = actvPoi.getText().toString().equals(lastCus) ? lastKdcus: "";
                    }else{
                        selectedKdcus = "";
                    }

                    kdcus = lastKdcus;
                    nama = actvNama.getText().toString();
                    alamat = edtAlamat.getText().toString();
                    radius = lastRadius;
                    poiName = lastCus;
                }
            });
        }
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });

        llBeliPulsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                redirectToDetail(true);
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(actvPoi.getText().length() > 0 && !lastCus.equals(actvPoi.getText().toString())){
                    actvPoi.setError("POI tidak ditemukan");
                    actvPoi.requestFocus();
                    return;

                }

                if(actvPoi.getText().toString().length() > 0) {
                    selectedKdcus = actvPoi.getText().toString().equals(lastCus) ? lastKdcus: "";
                }else{
                    selectedKdcus = "";
                }

                if(actvNama.getText().toString().length() == 0){

                    actvNama.setError("Nama harap diisi");
                    actvNama.requestFocus();
                    return;
                }else{
                    actvNama.setError(null);
                    //selectedKdcus = actvNama.getText().toString().equals(lastCus) ? lastKdcus: "";
                }

                if(edtAlamat.getText().toString().length() == 0){

                    edtAlamat.setError("Alamat harap diisi");
                    edtAlamat.requestFocus();
                    return;
                }else{

                    edtAlamat.setError(null);
                }

                kdcus = lastKdcus;
                nama = actvNama.getText().toString();
                alamat = edtAlamat.getText().toString();
                radius = lastRadius;
                poiName = lastCus;

                if(!isOnLocation(location) && flagRadius.equals("1")){ // wajib tapi tidak dilokasi

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

                //validasi
                if(edtNomor.getText().toString().length() == 0){

                    edtNomor.setError("Nomor harap diisi");
                    edtNomor.requestFocus();
                    return;
                }else{
                    edtNomor.setError(null);
                }

                if(isProses){

                    Toast.makeText(context, "Harap tunggu hingga proses selesai", Toast.LENGTH_LONG).show();
                    return;
                }

                //1. kdbrg
                //2. namabrg
                //3. hargajual
                //4. flag dipilih
                //5. flag jenis order MK, BL, TC
                //6. pin rs
                //7. format inject

                lastFlagOrder = selectedItemOrder.getItem5();
                lastKodebrg = selectedItemOrder.getItem1();

                //Toast.makeText(context, generalizePhoneNumber(edtNomor.getText().toString()), Toast.LENGTH_LONG).show();

                final String nominal = edtTotal.getText().toString().replaceAll("[,.]", "");
                if(nominal.isEmpty()){

                    edtTotal.setError("Nominal harap diisi");
                    edtTotal.requestFocus();
                    return;
                }else if(iv.parseNullDouble(nominal) == 0){

                    edtTotal.setError("Nominal harap lebih dari 0");
                    edtTotal.requestFocus();
                    return;
                }else{
                    edtTotal.setError(null);
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Apakah anda yakin ingin memproses order?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                lastSN = "";
                                selectedHarga = nominal;
                                edtNominal.setText(nominal);

                                if(isOnSaving){
                                    Toast.makeText(context, "Harap tunggu hingga proses selesai dan ulangi menyimpan", Toast.LENGTH_LONG).show();
                                    return;
                                }

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

        edtNominal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                hitungHarga();
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

                String latLokasi = "", longLokasi = "", namaTitle = "";

                if (isPOI){
                    latLokasi = latitudePOI;
                    longLokasi = longitudePOI;
                    namaTitle = poiName;
                }else{
                    latLokasi = latitudeEvent;
                    longLokasi = longitudeEvent;
                    namaTitle = "Lokasi Event";
                }

                if(!latLokasi.equals("") && !longLokasi.equals("")){

                    Intent intent = new Intent(context, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latLokasi);
                    intent.putExtra("long_outlet", longLokasi);
                    intent.putExtra("nama", namaTitle);

                    startActivity(intent);
                }else{

                    Toast.makeText(context, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void hitungHarga(){

        selectedHarga = edtNominal.getText().toString();
        edtHarga.setText(iv.ChangeToRupiahFormat(selectedHarga));
    }

    private void simpanData() {

        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject body = new JSONObject();
        try {
            body.put("kdcus", lastKdcus);
            body.put("nama", actvNama.getText().toString());
            body.put("alamat", edtAlamat.getText().toString());
            body.put("nik", nik);

        }catch (JSONException e){
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, body, "POST", ServerURL.saveJualPOIManual, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                pbLoading.setVisibility(View.GONE);

                try {
                    Log.d("Response", result);
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message;
                    if(iv.parseNullInteger(status) == 200){
                        message = response.getJSONObject("metadata").getString("message");
                    } else {
                        message = response.getJSONObject("metadata").getString("message");
                    }
                    Log.d("Sukses", message);
                    Toast.makeText(DetailEventManual.this, message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DetailEventManual.this, DetailEventManual.class));
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("respon", e.getMessage());
                    finish();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void redirectToDetail(boolean isPulsa){

        //Validasi

        if(actvPoi.getText().length() > 0 && !lastCus.equals(actvPoi.getText().toString())){
            actvPoi.setError("POI tidak ditemukan");
            actvPoi.requestFocus();
            return;

        }

        if(actvPoi.getText().toString().length() > 0) {
            selectedKdcus = actvPoi.getText().toString().equals(lastCus) ? lastKdcus: "";
        }else{
            selectedKdcus = "";
        }

        if(actvNama.getText().toString().length() == 0){

            actvNama.setError("Nama harap diisi");
            actvNama.requestFocus();
            return;
        }else{
            actvNama.setError(null);
            //selectedKdcus = actvNama.getText().toString().equals(lastCus) ? lastKdcus: "";
        }

        if(edtAlamat.getText().toString().length() == 0){

            edtAlamat.setError("Alamat harap diisi");
            edtAlamat.requestFocus();
            return;
        }else{

            edtAlamat.setError(null);
        }

        kdcus = lastKdcus;
        nama = actvNama.getText().toString();
        alamat = edtAlamat.getText().toString();
        radius = lastRadius;
        poiName = lastCus;
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

        Intent intent = new Intent(context, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    private void showDialog(int state, String message){

        if(state == 1){

            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_success, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final androidx.appcompat.app.AlertDialog alert = builder.create();
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
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_failed, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);

            final androidx.appcompat.app.AlertDialog alert = builder.create();
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

            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View viewDialog = inflater.inflate(R.layout.layout_warning, null);
            builder.setView(viewDialog);
            builder.setCancelable(false);

            final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
            tvText1.setText(message);
            final Button btnOK = (Button) viewDialog.findViewById(R.id.btn_ok);
            btnOK.setText("Ulangi Proses");

            final androidx.appcompat.app.AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {

                    if(alert != null){

                        try {

                            alert.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    getBarang();
                }
            });

            try {
                alert.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
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
                                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
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
                                    rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
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

        if (isPOI){
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

                keteranganJarak = "<font color='#ec1c25'>Lokasi event tidak diketahui</font>";
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

    private void saveData() {

        isOnSaving = true;
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

        JSONArray jData = new JSONArray();

        //1. kdbrg
        //2. namabrg
        //3. hargajual
        //4. flag dipilih
        //5. flag jenis order MK, BL, TC
        //6. pin rs
        //7. format inject
        //break;

        JSONObject jDataDetail = new JSONObject();
        try {
            jDataDetail.put("nobukti","");
            jDataDetail.put("nik", nik);
            jDataDetail.put("kodebrg", lastKodebrg);
            jDataDetail.put("ccid", "");
            jDataDetail.put("harga", selectedHarga);
            jDataDetail.put("jumlah", "1");
            jDataDetail.put("total", selectedHarga);
            jDataDetail.put("kdcus", kdcus);
            jDataDetail.put("nama", nama);
            jDataDetail.put("alamat", alamat);
            jDataDetail.put("nomor", generalizePhoneNumber(edtNomor.getText().toString()));
            jDataDetail.put("nomor_event", nomor);
            jDataDetail.put("jarak", jarak);
            jDataDetail.put("sn", lastSN);
            jDataDetail.put("transaction_id", String.valueOf(transactionID));
            jDataDetail.put("manual", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jData.put(jDataDetail);

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

                isOnSaving = false;
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
                    showDialog(4, "Laporan tidak masuk, harap tekan ulangi proses");
                }
            }

            @Override
            public void onError(String result) {

                isOnSaving = false;
                isLoading(false);

                try {

                    progressDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }

                //Toast.makeText(context, "Terjadi kesalahan saat menyimpan data, harap ulangi kembali", Toast.LENGTH_LONG).show();
                showDialog(4, "Laporan tidak masuk, harap tekan ulangi proses");
            }
        });
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

    private static String generalizePhoneNumber(String number){

        String result = number;

        if(number.length() > 3){

            if(number.startsWith("+62")){

                result = number.replace("+62", "0");
            }else if(number.startsWith("62")){

                result = "0" + number.substring(2);
            }else if(number.startsWith("8")){

                result = "0" + number;
            }
        }

        return result;
    }
}
