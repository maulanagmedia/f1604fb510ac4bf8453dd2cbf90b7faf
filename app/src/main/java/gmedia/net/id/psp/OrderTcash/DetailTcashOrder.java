package gmedia.net.id.psp.OrderTcash;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailTcashOrder extends AppCompatActivity implements LocationListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tcash_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Tcash Order");

        initUI();
    }

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

        session = new SessionManager(DetailTcashOrder.this);
        nikMKIOS = session.getUserInfo(SessionManager.TAG_NIK);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kode = bundle.getString("kode");
            nonota = bundle.getString("nonota");

            if(nonota != null && nonota.length() >0){

                edtNominal.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtNominal.setFocusable(false);
                btnProses.setEnabled(false);
                edtNonota.setText(nonota);
                edtKeterangan.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtKeterangan.setFocusable(false);
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
                    location = getLocation();

                    initEvent();
                }
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
        ApiVolley request = new ApiVolley(DetailTcashOrder.this, jBody, "POST", ServerURL.getDetailPenjualanTcash, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtNamaReseller.setText(jo.getString("nama"));
                            edtNominal.setText(jo.getString("value_tcash"));
                            edtHarga.setText(iv.ChangeToRupiahFormat(jo.getString("hargatcash")));
                            edtKeterangan.setText(jo.getString("pesan"));
                            break;
                        }
                    }

                    pbProses.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
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
                Toast.makeText(DetailTcashOrder.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailTcashOrder.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailTcashOrder.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailTcashOrder.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailTcashOrder.this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailTcashOrder.this);
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
        ActivityCompat.requestPermissions(DetailTcashOrder.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void getRSDetail() {

        isLoading(true);
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        ApiVolley request = new ApiVolley(DetailTcashOrder.this, new JSONObject(), "GET", ServerURL.getReseller+nik+"/"+kode, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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

        final ProgressDialog progressDialog = new ProgressDialog(DetailTcashOrder.this, R.style.AppTheme_Login_Dialog);
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
            jData.put("keterangan_order", "Tcash " + edtNominal.getText().toString());
            jData.put("flag", "1");
            jData.put("order_format", "*800*200#");
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

        String url = ServerURL.saveTcash, method = "POST";

        ApiVolley request = new ApiVolley(DetailTcashOrder.this, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                        Toast.makeText(DetailTcashOrder.this, message, Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(DetailTcashOrder.this, ActOrderTcash.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(DetailTcashOrder.this, superMessage, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
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
                    Toast.makeText(DetailTcashOrder.this, "Harga belum termuat, harap ulangi proses", Toast.LENGTH_LONG).show();
                    return;
                }

                if(jarak.equals("")){

                    Toast.makeText(DetailTcashOrder.this, "Mohon tunggu hinggan posisi diketahui / tekan refresh pada bagian jarak", Toast.LENGTH_LONG).show();
                    edtJarak.requestFocus();
                    return;
                }

                if(location == null){

                    Toast.makeText(DetailTcashOrder.this, "Harap tunggu hingga posisi diketahui", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog builder = new AlertDialog.Builder(DetailTcashOrder.this)
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

                if(!isUpdateLocation)location = getLocation();
            }
        });

        btnMapsOutlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(latitudeOutlet != "" && longitudeOutlet != ""){

                    Intent intent = new Intent(DetailTcashOrder.this, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latitudeOutlet);
                    intent.putExtra("long_outlet", longitudeOutlet);
                    intent.putExtra("nama", namaRS);

                    startActivity(intent);
                }else{

                    Toast.makeText(DetailTcashOrder.this, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
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
        ApiVolley request = new ApiVolley(DetailTcashOrder.this, jBody, "POST", ServerURL.getTcashHarga, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String harga = response.getJSONObject("response").getString("harga");
                        updateHarga(harga);
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

        ApiVolley request = new ApiVolley(DetailTcashOrder.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
