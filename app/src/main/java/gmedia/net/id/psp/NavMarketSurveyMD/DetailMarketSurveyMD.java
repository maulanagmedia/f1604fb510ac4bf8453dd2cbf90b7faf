package gmedia.net.id.psp.NavMarketSurveyMD;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.PermissionUtils;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.Adapter.AutocompleteAdapter;
import gmedia.net.id.psp.BuildConfig;
import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.MapsOutletActivity;
import gmedia.net.id.psp.NavMarketSurvey.DetailMarketSurvey;
import gmedia.net.id.psp.NavMarketSurveyMD.Adapter.ListDisplayMDAdapter;
import gmedia.net.id.psp.NavMarketSurveyMD.Adapter.ListPostmatMDAdapter;
import gmedia.net.id.psp.NavMarketSurveyMD.Adapter.ListRekomendasiMDAdapter;
import gmedia.net.id.psp.NavPOL.Adapter.PhotosAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailMarketSurveyMD extends AppCompatActivity implements LocationListener{

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
    private String TAG = "MarketSurveyMD";
    private String address0 = "";
    private Button btnResetPosition;
    private boolean refreshMode = false;
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
    private List<CustomItem> listPOI;
    private AutoCompleteTextView actvPOI;
    private String lastKdcus = "", lastCus = "", latitudePOI = "", longitudePOI = "", lastRadius = "";
    private RecyclerView rvPhoto;
    private ImageButton ibAddPhoto;

    //Upload Handler
    private static int RESULT_OK = -1;
    private static int PICK_IMAGE_REQUEST = 1212;
    private ImageUtils iu = new ImageUtils();
    private Bitmap bitmap;
    private List<Bitmap> photoList;
    private PhotosAdapter adapter;

    private String kdcus = "";
    private String statusAktif = "";
    private ListView lvDisplay, lvRekomendasi, lvPostmat;
    private EditText edtBranding, edtEvaluasi, edtRekomendasiBranding;
    private List<CustomItem> listProvider, listRekomendasi, listPostmat;
    private ImageView ivAddRekomendasi, ivAddPostmat;
    private ListRekomendasiMDAdapter adapterRekomendasi;
    private ListPostmatMDAdapter adapterPostmat;
    private LinearLayout llJarak;
    private EditText edtJarak;
    private ImageView ivRefreshPosition;
    private Button btnMapsOutlet;
    private String jarak = "";
    private String idKunjungan = "";
    private ListDisplayMDAdapter adapterDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_market_survey_md);

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
        lvDisplay = (ListView) findViewById(R.id.lv_display);
        edtBranding = (EditText) findViewById(R.id.edt_branding);
        edtEvaluasi = (EditText) findViewById(R.id.edt_evaluasi);
        lvRekomendasi = (ListView) findViewById(R.id.lv_rekomendasi);
        edtRekomendasiBranding = (EditText) findViewById(R.id.edt_rekomendasi_branding);
        lvPostmat = (ListView) findViewById(R.id.lv_postmat);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_photo);
        ibAddPhoto = (ImageButton) findViewById(R.id.ib_add_photo);
        ivAddRekomendasi = (ImageView) findViewById(R.id.iv_add_rekomendasi);
        ivAddPostmat = (ImageView) findViewById(R.id.iv_add_postmat);

        llJarak = (LinearLayout) findViewById(R.id.ll_jarak);
        edtJarak = (EditText) findViewById(R.id.edt_jarak);
        ivRefreshPosition = (ImageView) findViewById(R.id.iv_refresh_position);
        btnMapsOutlet = (Button) findViewById(R.id.btn_maps);
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

        photoList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adapter = new PhotosAdapter(context, photoList);
        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(adapter);

        // Inisialisasi pager slider
        listRekomendasi = new ArrayList<>();
        adapterRekomendasi = new ListRekomendasiMDAdapter((Activity) context, listRekomendasi);
        lvRekomendasi.setAdapter(adapterRekomendasi);

        // Inisialisasi jenis postmat
        listPostmat = new ArrayList<>();
        adapterPostmat = new ListPostmatMDAdapter((Activity) context, listPostmat);
        lvPostmat.setAdapter(adapterPostmat);

        initEvent();
        initCollapsingToolbar();
        editMode = false;
        initLocation();
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //validasi
                if(!actvPOI.getText().toString().equals(lastCus) || actvPOI.getText().toString().isEmpty()){

                    actvPOI.setError("Harap pilih POI dari list");
                    actvPOI.requestFocus();
                    return;
                }else{
                    actvPOI.setError(null);
                }

                if(edtBranding.getText().toString().isEmpty()){

                    edtBranding.setError("Chip All Operator harap diisi");
                    edtBranding.requestFocus();
                    return;
                }else{
                    edtBranding.setError(null);
                }

                if(edtEvaluasi.getText().toString().isEmpty()){

                    edtEvaluasi.setError("Evaluasi harap diisi");
                    edtEvaluasi.requestFocus();
                    return;
                }else{
                    edtEvaluasi.setError(null);
                }

                if(edtRekomendasiBranding.getText().toString().isEmpty()){

                    edtRekomendasiBranding.setError("Rekomendasi Branding harap diisi");
                    edtRekomendasiBranding.requestFocus();
                    return;
                }else{
                    edtRekomendasiBranding.setError(null);
                }

                if(listRekomendasi.size() == 0){

                    Toast.makeText(context, "Harap tambahkan data rekomendasi", Toast.LENGTH_LONG).show();
                    return;
                }

                if(listPostmat.size() == 0){

                    Toast.makeText(context, "Harap tambahkan data postmat", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!isOnLocation(location)){
                    Toast.makeText(context, "Posisi anda diluar area yang ditentukan", Toast.LENGTH_LONG).show();
                    refreshMode = true;
                    updateAllLocation();
                    return;
                }

                AlertDialog konfirmasi = new AlertDialog.Builder(context)
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

        ibAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadChooserDialog();
            }
        });

        ivAddRekomendasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listRekomendasi.add(new CustomItem("",""));
                adapterRekomendasi = new ListRekomendasiMDAdapter((Activity) context, listRekomendasi);
                lvRekomendasi.setAdapter(adapterRekomendasi);
            }
        });

        ivAddPostmat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listPostmat.add(new CustomItem(""));
                adapterPostmat = new ListPostmatMDAdapter((Activity) context, listPostmat);
                lvPostmat.setAdapter(adapterPostmat);
            }
        });

        ivRefreshPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                refreshMode = true;
                updateAllLocation();
            }
        });

        btnMapsOutlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String latLokasi = "", longLokasi = "";

                latLokasi = latitudePOI;
                longLokasi = longitudePOI;

                if(!latLokasi.equals("")&& !longLokasi.equals("")){

                    Intent intent = new Intent(context, MapsOutletActivity.class);
                    intent.putExtra("lat", iv.doubleToStringFull(latitude));
                    intent.putExtra("long", iv.doubleToStringFull(longitude));
                    intent.putExtra("lat_outlet", latLokasi);
                    intent.putExtra("long_outlet", longLokasi);
                    intent.putExtra("nama", lastCus);

                    startActivity(intent);
                }else{

                    Toast.makeText(context, "Harap tunggu hingga proses pencarian lokasi selesai", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveData() {

        progressDialog = new ProgressDialog(context, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        String nik = session.getUserInfo(SessionManager.TAG_UID);

        JSONObject jDataSurvey = new JSONObject();

        boolean isEmpty = false;

        try {
            jDataSurvey.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jDataSurvey.put("kdcus",lastKdcus);
            jDataSurvey.put("branding", edtBranding.getText().toString());
            jDataSurvey.put("evaluasi", edtEvaluasi.getText().toString());
            jDataSurvey.put("rekomendasi_branding", edtRekomendasiBranding.getText().toString());
            jDataSurvey.put("latitude", latitudeString);
            jDataSurvey.put("longitude", longitudeString);
            jDataSurvey.put("state", state);
            jDataSurvey.put("kodearea", session.getUserInfo(SessionManager.TAG_AREA));

            List<CustomItem> dataDisplay = new ArrayList<>();
            try {

                dataDisplay = ((ListDisplayMDAdapter) lvDisplay.getAdapter()).getItems();
            }catch (Exception e){

                e.printStackTrace();
                Toast.makeText(context, "Data display tidak termuat, harap ulangi proses", Toast.LENGTH_LONG).show();
                getProvider();
                return;
            }

            for(CustomItem item : dataDisplay){
                jDataSurvey.put("unit_"+item.getItem5(), ((item.getItem3().equals("")) ? "0" : item.getItem3()));
                jDataSurvey.put("order_"+item.getItem5(), ((item.getItem4().equals("")) ? "0" : item.getItem4()));
                jDataSurvey.put("penjualan_"+item.getItem5(), ((item.getItem6().equals("")) ? "0" : item.getItem6()));
                jDataSurvey.put("recharge_"+item.getItem5(), ((item.getItem7().equals("")) ? "0" : item.getItem7()));
                jDataSurvey.put("unit_small_"+item.getItem5(), ((item.getItem8().equals("")) ? "0" : item.getItem8()));
                jDataSurvey.put("unit_medium_"+item.getItem5(), ((item.getItem9().equals("")) ? "0" : item.getItem9()));
                jDataSurvey.put("unit_high_"+item.getItem5(), ((item.getItem10().equals("")) ? "0" : item.getItem10()));
                jDataSurvey.put("penjualan_small_"+item.getItem5(), ((item.getItem11().equals("")) ? "0" : item.getItem11()));
                jDataSurvey.put("penjualan_medium_"+item.getItem5(), ((item.getItem12().equals("")) ? "0" : item.getItem12()));
                jDataSurvey.put("penjualan_high_"+item.getItem5(), ((item.getItem13().equals("")) ? "0" : item.getItem13()));

                if(item.getItem4().isEmpty() || item.getItem7().isEmpty() || item.getItem8().isEmpty() || item.getItem9().isEmpty()
                        || item.getItem10().isEmpty() || item.getItem11().isEmpty() || item.getItem12().isEmpty() || item.getItem13().isEmpty()){

                    isEmpty = true;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(isEmpty){

            Toast.makeText(context, "Jumlah Display Perdana tidak boleh ada yang kosong, minimal 0", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            btnSimpan.setEnabled(true);
            return;
        }

        JSONArray jDataImages = new JSONArray();

        for (Bitmap item : photoList){

            JSONObject jo = new JSONObject();
            try {
                jo.put("image", ImageUtils.convert(item));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jDataImages.put(jo);
        }

        JSONArray jDataRekomendasi = new JSONArray();
        for (CustomItem item : listRekomendasi){

            if(!item.getItem1().equals("") && !item.getItem2().equals("")){

                JSONObject jo = new JSONObject();
                try {
                    jo.put("namabrg", item.getItem1());
                    jo.put("unit", item.getItem2());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jDataRekomendasi.put(jo);
            }
        }

        if(jDataRekomendasi.length() == 0){

            Toast.makeText(context, "Harap lengkapi data rekomendasi", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            btnSimpan.setEnabled(true);
            return;
        }

        JSONArray jDataPostman = new JSONArray();
        for (CustomItem item : listPostmat){

            if(!item.getItem1().equals("")){

                JSONObject jo = new JSONObject();
                try {
                    jo.put("postmat", item.getItem1());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jDataPostman.put(jo);
            }
        }

        if(jDataPostman.length() == 0){

            Toast.makeText(context, "Harap lengkapi data postmat", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            btnSimpan.setEnabled(true);
            return;
        }

        String method = "POST";
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("data", jDataSurvey);
            jBody.put("flag", "MD");
            jBody.put("data_images", jDataImages);
            jBody.put("data_rekomendasi", jDataRekomendasi);
            jBody.put("data_postmat", jDataPostman);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, method, ServerURL.saveMarketSurvey, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                btnSimpan.setEnabled(true);
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    progressDialog.dismiss();

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                }

                //btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    private void loadChooserDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_chooser, null);
        builder.setView(view);

        final LinearLayout llBrowse= (LinearLayout) view.findViewById(R.id.ll_browse);
        final LinearLayout llCamera = (LinearLayout) view.findViewById(R.id.ll_camera);

        final AlertDialog alert = builder.create();

        llBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFileChooser();
                alert.dismiss();
            }
        });

        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openCamera();
                alert.dismiss();
            }
        });

        alert.show();
    }

    //region File Chooser

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private final int REQUEST_IMAGE_CAPTURE = 2;
    private String photoFromCameraURI;

    private void openCamera(){

        if(PermissionUtils.hasPermissions(context,Manifest.permission.CAMERA)){

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            /*if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                Uri photoURL = null;
                try {
                    photoURL = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            createImageFile());
                    photoFromCameraURI = photoURL.toString();
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i(TAG, "IOException");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }*/
        }else{

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Ijin dibutuhkan")
                    .setMessage("Ijin dibutuhkan untuk mengakses kamera, harap ubah ijin kamera ke \"diperbolehkan\"")
                    .setPositiveButton("Buka Ijin", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        //photoFromCameraURI = "file:" + image.getAbsolutePath();
        return image;
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
        MockLocChecker checker = new MockLocChecker(context);
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
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(DetailMarketSurveyMD.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(DetailMarketSurveyMD.this, new OnSuccessListener<Location>() {
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
                                    rae.startResolutionForResult(DetailMarketSurveyMD.this, REQUEST_CHECK_SETTINGS);
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

        }else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            /*File file = new File(String.valueOf(filePath));
            long length = file.length();
            length = length/1024; //in KB*/

            InputStream imageStream = null, copyStream = null;
            try {
                imageStream = getContentResolver().openInputStream(
                        filePath);
                copyStream = getContentResolver().openInputStream(
                        filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            //options.inDither = true;

            // Get bitmap dimensions before reading...
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(copyStream, null, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int largerSide = Math.max(width, height);
            options.inJustDecodeBounds = false; // This time it's for real!
            int sampleSize = 1; // Calculate your sampleSize here
            if(largerSide <= 1000){
                sampleSize = 1;
            }else if(largerSide > 1000 && largerSide <= 2000){
                sampleSize = 2;
            }else if(largerSide > 2000 && largerSide <= 3000){
                sampleSize = 3;
            }else if(largerSide > 3000 && largerSide <= 4000){
                sampleSize = 4;
            }else{
                sampleSize = 6;
            }
            options.inSampleSize = sampleSize;
            //options.inDither = true;

            Bitmap bmp = BitmapFactory.decodeStream(imageStream, null, options);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            bitmap = scaleDown(bitmap, 360, true);

            try {
                stream.close();
                stream = null;
            } catch (IOException e) {

                e.printStackTrace();
            }

            if(bitmap != null){

                photoList.add(bitmap);
                adapter.notifyDataSetChanged();
            }

        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            /*try {

                if(photoFromCameraURI != null){

                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(photoFromCameraURI));
                    //bitmap = (Bitmap) data.getExtras().get("data");

                    *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bitmap = rotateImage(bitmap, 90);
                    }*//*

                    bitmap = scaleDown(bitmap, 360, true);


                    if(bitmap != null){

                        photoList.add(bitmap);
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            if(data != null){
                bitmap = (Bitmap) data.getExtras().get("data");
                bitmap = scaleDown(bitmap, 360, true);
                if(bitmap != null){

                    photoList.add(bitmap);
                    adapter.notifyDataSetChanged();
                }
            }else{
                Toast.makeText(context, "Gambar tidak termuat, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }

        }
    }

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        try {
            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            int height = Math.round((float) ratio * realImage.getHeight());

            realImage = Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
        }catch (Exception e){
            e.printStackTrace();
        }

        return realImage;
    }

    private Bitmap rotateImage(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /*private class RotateTask extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<ImageView> imgInputView;
        private WeakReference<Bitmap> rotateBitmap;

        public RotateTask(ImageView imgInputView){
            this.imgInputView = new WeakReference<ImageView>(imgInputView);
        }

        @Override
        protected void onPreExecute() {
            //if you want to show progress dialog
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotateBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(rotated, 0, 0,rotated.getWidth(), rotated.getHeight(), matrix, true));
            return rotateBitmap.get();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //dismiss progress dialog
            imgInputView.get().setImageBitmap(result);
        }
    }*/

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
                            if(!jo.getString("field_name").equals("")){
                                listProvider.add(new CustomItem(
                                        jo.getString("id_provider"),
                                        jo.getString("nama"),
                                        "",
                                        "",
                                        jo.getString("field_name"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        jo.getString("color"),
                                        jo.getString("color_text")
                                        ));
                            }
                        }
                    }

                    setTableDisplay(listProvider);
                    initData();

                } catch (JSONException e) {

                    e.printStackTrace();
                    setTableDisplay(null);
                }
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
                setTableDisplay(null);
            }
        });
    }

    private void initData() {

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            idSurvey = bundle.getString("id", "");
            if(!idSurvey.isEmpty()){

                btnSimpan.setVisibility(View.GONE);
                btnSimpan.setEnabled(false);
                llJarak.setVisibility(View.GONE);
                getDataSurvey();
            }else{

                btnSimpan.setEnabled(true);
                refreshMode = true;
                updateAllLocation();
            }

        }else{

            btnSimpan.setEnabled(true);
            refreshMode = true;
            updateAllLocation();
        }
    }

    private void getDataSurvey() {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("id", idSurvey);
            jBody.put("flag", "MD");
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

                        JSONObject jSurvey = response.getJSONObject("response").getJSONObject("survey");
                        lastKdcus = jSurvey.getString("kdcus");
                        lastCus = jSurvey.getString("nama");
                        lastRadius = jSurvey.getString("toleransi_jarak");
                        actvPOI.setText(lastCus);

                        idKunjungan = jSurvey.getString("id_kunjungan");
                        latitudeString = jSurvey.getString("latitude");
                        longitudeString = jSurvey.getString("longitude");
                        latitudePOI = jSurvey.getString("latitude_poi");
                        longitudePOI = jSurvey.getString("longitude_poi");

                        for(CustomItem item: listProvider){

                            item.setItem3(jSurvey.getString("unit_"+item.getItem5()));
                            item.setItem4(jSurvey.getString("order_"+item.getItem5()));
                            item.setItem6(jSurvey.getString("penjualan_"+item.getItem5()));
                            item.setItem7(jSurvey.getString("recharge_"+item.getItem5()));
                            item.setItem8(jSurvey.getString("unit_small_"+item.getItem5()));
                            item.setItem9(jSurvey.getString("unit_medium_"+item.getItem5()));
                            item.setItem10(jSurvey.getString("unit_high_"+item.getItem5()));
                            item.setItem11(jSurvey.getString("penjualan_small_"+item.getItem5()));
                            item.setItem12(jSurvey.getString("penjualan_medium_"+item.getItem5()));
                            item.setItem13(jSurvey.getString("penjualan_high_"+item.getItem5()));
                        }

                        adapterDisplay.notifyDataSetChanged();

                        edtBranding.setText(jSurvey.getString("branding"));
                        edtEvaluasi.setText(jSurvey.getString("evaluasi"));
                        edtRekomendasiBranding.setText(jSurvey.getString("rekomendasi_branding"));

                        JSONArray jRekomendArray = response.getJSONObject("response").getJSONArray("rekomendasi");
                        for(int j = 0; j < jRekomendArray.length() ; j++){

                            JSONObject jRekomendasi = jRekomendArray.getJSONObject(j);
                            listRekomendasi.add(new CustomItem(jRekomendasi.getString("namabrg"),jRekomendasi.getString("unit")));
                        }
                        adapterRekomendasi.notifyDataSetChanged();

                        JSONArray jPostmatArray = response.getJSONObject("response").getJSONArray("postmat");
                        for(int j = 0; j < jPostmatArray.length() ; j++){

                            JSONObject jPostmat = jPostmatArray.getJSONObject(j);
                            listPostmat.add(new CustomItem(jPostmat.getString("postmat")));
                        }
                        adapterPostmat.notifyDataSetChanged();

                        latitude = iv.parseNullDouble(latitudeString);
                        longitude = iv.parseNullDouble(longitudeString);
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        refreshMode = true;
                        onLocationChanged(location);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getPhotos();
            }

            @Override
            public void onError(String result) {

                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void getPhotos() {

        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", ServerURL.getDetailKunjunganImg+idKunjungan, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");

                        for (int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            String image = jo.getString("image");
                            new AsyncGettingBitmapFromUrl().execute(image);
                        }
                    }

                } catch (JSONException e) {
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private class AsyncGettingBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... params) {

            System.out.println("doInBackground");

            Bitmap bitmap1 = null;

            bitmap1 = downloadImage(params[0]);
            photoList.add(bitmap1);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
            return bitmap1;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            System.out.println("bitmap" + bitmap);

        }
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream stream = null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        try {
            stream = getHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
            if(stream != null){
                stream.close();
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("downloadImage"+ e1.toString());
        }
        return bitmap;
    }

    private InputStream getHttpConnection(String urlString)  throws IOException {

        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("downloadImage" + ex.toString());
        }
        return stream;
    }

    private void setTableDisplay(List<CustomItem> listItem) {

        lvDisplay.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            adapterDisplay = new ListDisplayMDAdapter((Activity) context, listItem);
            lvDisplay.setAdapter(adapterDisplay);
        }
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

                    refreshMode = true;
                    updateAllLocation();
                }
            });
        }
    }

    private void updateAllLocation(){
        mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    private boolean isOnLocation(Location detectedLocation){

        String latLokasi = "", longLokasi = "";

        latLokasi = latitudePOI;
        longLokasi = longitudePOI;

        boolean hasil = false;

        if(!lastRadius.equals("") && !latLokasi.equals("") && !longLokasi.equals("") && detectedLocation != null){

            double latOutlet = iv.parseNullDouble(latLokasi);
            double longOutlet = iv.parseNullDouble(longLokasi);

            double detectedJarak = (6371 * Math.acos(Math.sin(Math.toRadians(latOutlet)) * Math.sin(Math.toRadians(detectedLocation.getLatitude())) + Math.cos(Math.toRadians(longOutlet - detectedLocation.getLongitude())) * Math.cos(Math.toRadians(latOutlet)) * Math.cos(Math.toRadians(detectedLocation.getLatitude()))));
            double rangeDouble = iv.parseNullDouble(lastRadius);

            jarak = iv.doubleToStringFull(detectedJarak);
            String pesan = "Jarak saat ini dengan lokasi adalah ";
            String keteranganJarak = "";
            if(iv.parseNullDouble(lastRadius) <= 6371){
                if(iv.parseNullDouble(jarak) <= 1){
                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak) * 1000, "2") + " m";
                }else{
                    keteranganJarak = iv.doubleToString(iv.parseNullDouble(jarak), "2") + " km";
                }

                if(iv.parseNullDouble(jarak) > iv.parseNullDouble(lastRadius)){

                    keteranganJarak = "<font color='#ec1c25'>"+keteranganJarak+"</font>";
                }

            }else{

                keteranganJarak = "<font color='#ec1c25'>Lokasi outlet tidak diketahui</font>";
            }

            edtJarak.setText(Html.fromHtml(pesan + keteranganJarak));

            if(detectedJarak <= rangeDouble) {

                hasil = true;
            }
        }

        return hasil;
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
                Toast.makeText(context, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailMarketSurveyMD.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailMarketSurveyMD.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailMarketSurveyMD.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailMarketSurveyMD.this,
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
                        Location cLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (cLocation != null && !isOnLocation(location)) {

                            location = cLocation;
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
                        if (bufferLocation != null && !isOnLocation(location)) {

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
        ActivityCompat.requestPermissions(DetailMarketSurveyMD.this,
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

                if (ActivityCompat.checkSelfPermission(DetailMarketSurveyMD.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailMarketSurveyMD.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(DetailMarketSurveyMD.this);
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
    public void onLocationChanged(Location clocation) {

        if(refreshMode){

            if(!jarak.equals("")){

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

            if(!editMode){
                isOnLocation(clocation);
            }

            refreshMode = false;
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
