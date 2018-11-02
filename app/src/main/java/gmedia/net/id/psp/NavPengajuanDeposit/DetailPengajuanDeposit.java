package gmedia.net.id.psp.NavPengajuanDeposit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListPengajuanDepositAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailPengajuanDeposit extends AppCompatActivity implements LocationListener {

    private View footerList;
    private ListView lvDeposit;
    private ProgressBar pbLoading;
    private static ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private Context context;
    private List<CustomItem> listPengajuan, moreData;
    private static ListPengajuanDepositAdapter adapterDeposit;
    private int start = 0, count = 40;
    private String keyword = "";
    private boolean isLoading = false;
    private String TAG = "test";
    private String nik = "";
    private String kdcus = "", nama = "", flag = "";
    private static TextView tvTotal;
    private Button btnTolak, btnTerima;
    private static String total = "0";
    public HashMap<String, List<CustomItem>> listCCID = new HashMap<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pengajuan_deposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;

        setTitle("Pengajuan Deposit");

        initLocation();
        initUI();

        listCCID = new HashMap<>();
    }

    private void initLocation() {

        // getLocation update by google
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        updateAllLocation();
        //location = getLocation();
    }

    private void initUI() {

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        lvDeposit = (ListView) findViewById(R.id.lv_deposit);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        btnTolak = (Button) findViewById(R.id.btn_tolak);
        btnTerima = (Button) findViewById(R.id.btn_terima);
        session = new SessionManager(context);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);
        total = "0";

        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps_outlet);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            kdcus = bundle.getString("kdcus", "");
            nama = bundle.getString("nama", "");
            flag = bundle.getString("flag", "");

            setTitle("Pengajuan");
            if(flag.equals("2")) setTitle("Pembelian Perdana");
            getSupportActionBar().setSubtitle("a/n " + nama);

            isLoading = false;
            getDataPengajuan();

            initEvent();
        }
    }

    private void initEvent() {

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapterDeposit == null || adapterDeposit.getItems().size() == 0){

                    Toast.makeText(context, "Data masih kosong, harap diisi", Toast.LENGTH_LONG).show();
                    return;
                }

                showDialogTolak();
            }
        });

        btnTerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapterDeposit == null || adapterDeposit.getItems().size() == 0){

                    Toast.makeText(context, "Data masih kosong, harap diisi", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean isSelected = false;
                for(CustomItem item:adapterDeposit.getItems()){

                    if(item.getItem8().equals("1")){
                        isSelected = true;
                        break;
                    }
                }

                if(jarak.equals("")){

                    Snackbar.make(findViewById(android.R.id.content), "Mohon tunggu hinggan posisi diketahui / tekan refresh pada bagian jarak", Snackbar.LENGTH_LONG).show();
                    edtJarak.requestFocus();
                    return;
                }

                if(!isSelected){

                    Toast.makeText(context, "Harap pilih minimal satu item untuk disetujui", Toast.LENGTH_LONG).show();
                    return;
                }

                final AlertDialog alertTerima = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyetujui deposit ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                saveData("2", "");
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
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

                    Intent intent = new Intent(context, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latitudeOutlet);
                    intent.putExtra("long_outlet", longitudeOutlet);
                    intent.putExtra("nama", nama);

                    startActivity(intent);
                }else{

                    Toast.makeText(context, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getDataPengajuan() {

        isLoading = true;
        start = 0;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
            jBody.put("keyword", keyword);
            jBody.put("flag", flag);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDetail, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    listPengajuan = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listPengajuan.add(new CustomItem(
                                    jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl") +" "+ jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan"),
                                    "0"
                            ));

                        }
                    }

                    setAdapter(listPengajuan);
                } catch (JSONException e) {
                    setAdapter(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                setAdapter(null);
            }
        });
    }

    private void setAdapter(List<CustomItem> listItem) {

        lvDeposit.setAdapter(null);
        if(listItem != null){

            adapterDeposit = new ListPengajuanDepositAdapter((Activity) context, listItem, flag);
            lvDeposit.addFooterView(footerList);
            lvDeposit.setAdapter(adapterDeposit);
            lvDeposit.removeFooterView(footerList);

            lvDeposit.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int countMerchant = lvDeposit.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvDeposit.getLastVisiblePosition() >= countMerchant - threshold && !isLoading) {

                            isLoading = true;
                            lvDeposit.addFooterView(footerList);
                            start += count;
                            getMoreData();
                            Log.i(TAG, "onScroll: last ");
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });

            /*lvDeposit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    if(item.getItem6().equals("1")) showDialog(item);
                }
            });*/
        }
    }

    private void getMoreData() {

        isLoading = true;
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
            jBody.put("keyword", keyword);
            jBody.put("flag", flag);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDetail, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                lvDeposit.removeFooterView(footerList);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    moreData = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreData.add(new CustomItem(jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl") +" "+ jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan"),
                                    "0"
                            ));

                        }

                        if(adapterDeposit != null) adapterDeposit.addMoreData(moreData);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvDeposit.removeFooterView(footerList);
            }
        });
    }

    private void showDialog(final CustomItem item){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View viewDialog = inflater.inflate(R.layout.dialog_pengajuan_deposit, null);
        builder.setView(viewDialog);

        final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
        final TextView tvText2 = (TextView) viewDialog.findViewById(R.id.tv_text2);
        final Button btnBatal = (Button) viewDialog.findViewById(R.id.btn_batal);
        final Button btnTolak = (Button) viewDialog.findViewById(R.id.btn_tolak);
        final Button btnSetujui = (Button) viewDialog.findViewById(R.id.btn_setuju);

        tvText1.setText(item.getItem2());
        tvText2.setText("Mengajukan deposit sebesar "+iv.ChangeToRupiahFormat(item.getItem3()));

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog alertTolak = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menolak pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                saveData(item.getItem1(), "9");
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
                            }
                        })
                        .show();
            }
        });

        btnSetujui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {

                final AlertDialog alertSetujui = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyutujui pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                saveData(item.getItem1(), "2");
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
                            }
                        })
                        .show();
            }
        });

        alert.show();
    }

    private void showDialogTolak(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View viewDialog = inflater.inflate(R.layout.dialog_message, null);
        builder.setView(viewDialog);

        final EditText edtMessage = (EditText) viewDialog.findViewById(R.id.edt_message);
        final Button btnBatal = (Button) viewDialog.findViewById(R.id.btn_batal);
        final Button btnOk = (Button) viewDialog.findViewById(R.id.btn_ok);

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog alertTolak = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menolak pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(edtMessage.getText().toString().isEmpty()){

                                    edtMessage.setError("Alasan penolakan harap diisi");
                                    edtMessage.requestFocus();
                                    return;
                                }else{
                                    edtMessage.setError(null);
                                }

                                alert.dismiss();
                                saveData( "9", edtMessage.getText().toString());
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
                            }
                        })
                        .show();
            }
        });

        alert.show();
    }

    public static void updateHarga(){

        double totalDouble = 0;
        if(adapterDeposit != null){

            for(CustomItem item:adapterDeposit.getItems()){

                if(item.getItem8().equals("1")){

                    totalDouble += iv.parseNullDouble(item.getItem3());
                }
            }
        }

        total = iv.doubleToString(totalDouble);
        tvTotal.setText(iv.ChangeToRupiahFormat(totalDouble));
    }

    private void saveData(String type, String alasan) {

        isLoading = true;
        final ProgressDialog progressDialog = new ProgressDialog(DetailPengajuanDeposit.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if(adapterDeposit != null && adapterDeposit.getItems().size() > 0){

            JSONArray jArray = new JSONArray();

            for(CustomItem item : adapterDeposit.getItems()){

                if(item.getItem8().equals("1")){

                    if(flag.equals("2")){

                        List<CustomItem> selectedItem = listCCID.get(item.getItem1());
                        for(CustomItem item2: selectedItem){
                            JSONObject jDataDetail = new JSONObject();
                            try {
                                jDataDetail.put("nobukti","");
                                jDataDetail.put("nik", nik);
                                jDataDetail.put("kodebrg", item2.getItem1());
                                jDataDetail.put("ccid", item2.getItem3());
                                jDataDetail.put("harga", item2.getItem4());
                                jDataDetail.put("jumlah", item2.getItem5());
                                jDataDetail.put("total", item2.getItem4());
                                jDataDetail.put("kdcus", kdcus);
                                jDataDetail.put("nama", nama);
                                jDataDetail.put("alamat", "");
                                jDataDetail.put("nomor", "");
                                jDataDetail.put("nomor_event", "");
                                jDataDetail.put("jarak", "");
                                jDataDetail.put("no_surat_jalan", item2.getItem6());
                                jDataDetail.put("id_surat_jalan", item2.getItem7());
                                jDataDetail.put("transaction_id", "0");
                                jDataDetail.put("is_rs", "1");
                                jDataDetail.put("id_transaksi", item.getItem1());
                                jDataDetail.put("keterangan", alasan);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            jArray.put(jDataDetail);
                        }
                    }else{

                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("id", item.getItem1());
                            jo.put("nik", nik);
                            jo.put("apv", type);
                            jo.put("keterangan", alasan);

                            jArray.put(jo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            JSONObject jData = new JSONObject();

            String saveUrl = ServerURL.savePengajuanDeposite;

            if(flag.equals("2")){

                saveUrl = ServerURL.savePengajuanDepositePerdana;

                try {
                    jData.put("data", jArray);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                try {
                    jData.put("approval", jArray);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            PackageInfo pInfo = null;
            String version = "";

            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            version = pInfo.versionName;

            try {
                jData.put("version", version);
                jData.put("kdcus", kdcus);
                jData.put("nik", session.getNikGA());
                jData.put("latitude", iv.doubleToStringFull(location.getLatitude()));
                jData.put("longitude", iv.doubleToStringFull(location.getLongitude()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ApiVolley request = new ApiVolley(context, jData, "POST", saveUrl, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
                @Override
                public void onSuccess(String result) {

                    String message = "Terjadi kesalahan saat memproses data, silahkan ulangi kembali";
                    isLoading = false;
                    progressDialog.dismiss();

                    try {

                        JSONObject response = new JSONObject(result);
                        String status = response.getJSONObject("metadata").getString("status");
                        message = response.getJSONObject("metadata").getString("message");

                        if(iv.parseNullInteger(status) == 200){

                            progressDialog.dismiss();
                            message = response.getJSONObject("response").getString("message");
                            getDataPengajuan();

                        }

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String result) {

                    Snackbar.make(findViewById(android.R.id.content), "Terjadi kesalahan saat memproses data, harap ulangi kembali", Snackbar.LENGTH_LONG).show();
                    isLoading = false;
                    progressDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(context, "Barang masih kosong", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 9 && data != null && resultCode == RESULT_OK){
            String jsonItems = data.getStringExtra("data");

            Type typeList = new TypeToken<List<CustomItem>>(){}.getType();
            Gson gson = new Gson();
            List<CustomItem> selectedCCID = gson.fromJson(jsonItems, typeList);
            String id = data.getStringExtra("id");
            listCCID.put(id, selectedCCID);

            if(selectedCCID == null || selectedCCID.size() == 0){

                adapterDeposit.updateStatus(id, "0");

            }

            Log.d(TAG, "onActivityResult: ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                start = 0;
                keyword = queryText;
                iv.hideSoftKey(context);
                getDataPengajuan();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : "";
                if(newText.length() == 0){

                    start = 0;
                    keyword = "";
                    getDataPengajuan();
                }

                return true;
            }
        });

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        return super.onCreateOptionsMenu(menu);
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

    //region : location

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
                        if (ActivityCompat.checkSelfPermission(DetailPengajuanDeposit.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailPengajuanDeposit.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailPengajuanDeposit.this, new OnSuccessListener<Location>() {
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
                                    rae.startResolutionForResult(DetailPengajuanDeposit.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(DetailPengajuanDeposit.this, errorMessage, Toast.LENGTH_LONG).show();
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
                Toast.makeText(DetailPengajuanDeposit.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailPengajuanDeposit.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailPengajuanDeposit.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailPengajuanDeposit.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailPengajuanDeposit.this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailPengajuanDeposit.this);
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
        ActivityCompat.requestPermissions(DetailPengajuanDeposit.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private boolean isUpdateLocation = false;

    private void getJarak() {

        isUpdateLocation = true;
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kode", "");
            jBody.put("kdcus", kdcus);
            jBody.put("lat", iv.doubleToStringFull(latitude));
            jBody.put("long", iv.doubleToStringFull(longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailPengajuanDeposit.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
    //endregion
}
