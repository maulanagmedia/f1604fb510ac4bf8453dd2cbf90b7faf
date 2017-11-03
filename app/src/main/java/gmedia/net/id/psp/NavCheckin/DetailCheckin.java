package gmedia.net.id.psp.NavCheckin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.Adapter.PhotosAdapter;
import gmedia.net.id.psp.TambahCustomer.Model.AreaModel;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailCheckin extends AppCompatActivity implements LocationListener {

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
    private boolean refreshMode = false;
    private EditText edtNama, edtAlamat, edtKota, edtTelepon, edtNoHP, edtBank, edtRekening, edtCP, edtLatitude, edtLongitude, edtState;
    private Spinner spArea;
    private Button btnSimpan;
    private List<AreaModel> listArea;
    private SessionManager session;
    private RecyclerView rvPhoto, rvPhotoUpload;
    private ImageButton ibAddPhoto;

    //Upload Handler
    private static int RESULT_OK = -1;
    private static int PICK_IMAGE_REQUEST = 1212;
    private ImageUtils iu = new ImageUtils();
    private Bitmap bitmap;
    private List<Bitmap> photoList, photoUploadList;
    private PhotosAdapter adapter, adapterUpload;
    private ProgressDialog progressDialog;
    private EditText edtEmail;
    private String kdcus = "";
    private boolean editMode = false;
    private String kdArea = "";
    private String statusAktif = "";

    public DetailCheckin() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_checkin);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initUI();
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat);
        edtKota = (EditText) findViewById(R.id.edt_kota);
        edtTelepon = (EditText) findViewById(R.id.edt_telepon);
        edtNoHP = (EditText) findViewById(R.id.edt_nohp);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtBank = (EditText) findViewById(R.id.edt_bank);
        edtRekening = (EditText) findViewById(R.id.edt_rekening);
        edtCP = (EditText) findViewById(R.id.edt_contant_person);
        spArea = (Spinner) findViewById(R.id.sp_area);
        edtLatitude = (EditText) findViewById(R.id.edt_latitude);
        edtLongitude = (EditText) findViewById(R.id.edt_longitude);
        edtState = (EditText) findViewById(R.id.edt_state);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_photo);
        ibAddPhoto = (ImageButton) findViewById(R.id.ib_add_photo);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);

        session = new SessionManager(DetailCheckin.this);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(DetailCheckin.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        photoList = new ArrayList<>();
        photoUploadList = new ArrayList<>();
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);

        adapter = new PhotosAdapter(DetailCheckin.this, photoList);
        adapterUpload = new PhotosAdapter(DetailCheckin.this, photoUploadList);

        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(adapter);

        rvPhotoUpload.setLayoutManager(layoutManager);
        rvPhotoUpload.setAdapter(adapterUpload);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        title = "Detail Customer";
        //tvTitle.setText(title);
        initCollapsingToolbar();

        initLocation();

        initEvent();
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

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        refreshMode = true;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kdcus = bundle.getString("kdcus");
            if(kdcus != null && kdcus.length() > 0){

                editMode = true;
                getDataCustomer();
                getPhotos();
            }
        }

        location = getLocation();
        getDataArea();
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
                Toast.makeText(DetailCheckin.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailCheckin.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailCheckin.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailCheckin.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailCheckin.this,
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
                            onLocationChanged(location);
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    location=null;
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                onLocationChanged(location);
                            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCheckin.this);
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
        ActivityCompat.requestPermissions(DetailCheckin.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateBeforeSave();
            }
        });

        ibAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadChooserDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            /*File file = new File(String.valueOf(filePath));
            long length = file.length();
            length = length/1024; //in KB*/

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(
                        filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            bitmap = scaleDown(bitmap, 380, true);

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

            try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(photoFromCameraURI));
                bitmap = scaleDown(bitmap, 380, true);

                if(bitmap != null){

                    photoList.add(bitmap);
                    adapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadChooserDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailCheckin.this);
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

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private String photoFromCameraURI;

    private void openCamera(){

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
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
        photoFromCameraURI = "file:" + image.getAbsolutePath();
        return image;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    private void validateBeforeSave() {

        if(editMode && iv.parseNullInteger(statusAktif) != 2){

            Toast.makeText(DetailCheckin.this, "Outlet telah dirposes, tidak dapat diubah", Toast.LENGTH_LONG).show();
            return;
        }

        if(edtNama.getText().toString().length() == 0){

            edtNama.setError("Nama harap diisi");
            edtNama.requestFocus();
            return;
        }else{
            edtNama.setError(null);
        }

        if(edtTelepon.getText().toString().length() == 0){

            edtTelepon.setError("No Telepon harap diisi");
            edtTelepon.requestFocus();
            return;
        }else{
            edtTelepon.setError(null);
        }

        if(edtNoHP.getText().toString().length() == 0){

            edtNoHP.setError("Nama harap diisi");
            edtNoHP.requestFocus();
            return;
        }else{
            edtNoHP.setError(null);
        }

        if(spArea.getCount() == 0){

            Toast.makeText(DetailCheckin.this, "Tidak ada Area yang terpilih", Toast.LENGTH_LONG).show();
            return;
        }

        if(edtBank.getText().toString().length() > 0){

            if(edtRekening.getText().toString().length() == 0){

                edtRekening.setError("No Rekening harap diisi");
                edtRekening.requestFocus();
                return;
            }else{
                edtRekening.setError(null);
            }
        }

        saveData();
    }

    private void saveData() {

        progressDialog = new ProgressDialog(DetailCheckin.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        JSONObject jDataCustomer = new JSONObject();

        try {
            jDataCustomer.put("nama", edtNama.getText().toString());
            jDataCustomer.put("alamat", edtAlamat.getText().toString());
            jDataCustomer.put("kota", edtKota.getText().toString());
            jDataCustomer.put("notelp", edtTelepon.getText().toString());
            jDataCustomer.put("nohp", edtNoHP.getText().toString());
            jDataCustomer.put("email", edtEmail.getText().toString());
            jDataCustomer.put("bank", edtBank.getText().toString());
            jDataCustomer.put("norekening", edtRekening.getText().toString());
            jDataCustomer.put("status", "2");
            jDataCustomer.put("contact_person", edtCP.getText().toString());

            jDataCustomer.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            AreaModel area = (AreaModel) spArea.getSelectedItem();
            jDataCustomer.put("kodearea", area.getValue());
            jDataCustomer.put("tglmasuk", iv.getCurrentDate(FormatItem.formatDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jDataLocation = new JSONObject();

        try {
            jDataLocation.put("kdcus", "");
            jDataLocation.put("latitude", iv.doubleToStringFull(latitude));
            jDataLocation.put("longitude", iv.doubleToStringFull(longitude));
            jDataLocation.put("city", edtState.getText().toString());
            jDataLocation.put("state", "");
            jDataLocation.put("country", "");
        } catch (JSONException e) {
            e.printStackTrace();
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

        String method = "POST";
        JSONObject jBody = new JSONObject();
        try {
            if(editMode) {
                method = "PUT";
                jBody.put("kdcus", kdcus);
            }
            jBody.put("data_customer", jDataCustomer);
            jBody.put("data_location", jDataLocation);
            jBody.put("data_images", jDataImages);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailCheckin.this, jBody, method, ServerURL.saveCustomer, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    progressDialog.dismiss();

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailCheckin.this, message, Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(DetailCheckin.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(DetailCheckin.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(DetailCheckin.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
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

        ApiVolley request = new ApiVolley(DetailCheckin.this, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            edtAlamat.setText(jo.getString("alamat"));
                            edtKota.setText(jo.getString("kota"));
                            edtTelepon.setText(jo.getString("notelp"));
                            edtNoHP.setText(jo.getString("nohp"));
                            edtEmail.setText(jo.getString("email"));
                            edtBank.setText(jo.getString("bank"));
                            edtRekening.setText(jo.getString("norekening"));
                            edtCP.setText(jo.getString("contact_person"));
                            kdArea = jo.getString("kodearea");
                            edtLatitude.setText(jo.getString("latitude"));
                            edtLongitude.setText(jo.getString("longitude"));
                            edtState.setText(jo.getString("state"));

                            if(jo.getString("latitude").length() > 0){
                                latitude = iv.parseNullDouble(jo.getString("latitude"));
                                longitude = iv.parseNullDouble(jo.getString("longitude"));
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                refreshMode = true;
                                onLocationChanged(location);
                            }

                            statusAktif = jo.getString("status");
                            if(iv.parseNullInteger(statusAktif) != 2){
                                btnSimpan.setEnabled(false);
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getDataArea();
            }

            @Override
            public void onError(String result) {

                getDataArea();
            }
        });
    }

    private void getDataArea() {

        ApiVolley request = new ApiVolley(DetailCheckin.this, new JSONObject(), "GET", ServerURL.getArea, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listArea = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listArea.add(new AreaModel(jo.getString("kode_kota"), jo.getString("omo")));
                        }

                        setSPAreaEntry();
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

    private void setSPAreaEntry() {

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, listArea);
        spArea.setAdapter(adapter);

        if(listArea.size() > 0 && editMode && kdArea.length() > 0){

            int x = 0;
            for(AreaModel areaItem : listArea){

                if(kdArea.equals(areaItem.getValue())){
                    spArea.setSelection(x);
                    break;
                }
                x++;
            }
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

    private void getPhotos() {

        ApiVolley request = new ApiVolley(DetailCheckin.this, new JSONObject(), "GET", ServerURL.getImages+kdcus, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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

    public static  Bitmap downloadImage(String url) {
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

    public static  InputStream getHttpConnection(String urlString)  throws IOException {

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

    @Override
    public void onLocationChanged(Location location) {

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
