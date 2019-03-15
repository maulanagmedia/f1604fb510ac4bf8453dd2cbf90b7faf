package gmedia.net.id.psp.NavVerifikasiOutlet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maulana.custommodul.ApiVolley;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.BuildConfig;
import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.Adapter.PhotosAdapter;
import gmedia.net.id.psp.TambahCustomer.Model.AreaModel;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailVerifikasiOutlet extends AppCompatActivity  implements LocationListener {

    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private AppBarLayout appBarLayout;
    private CustomMapView mvMap;
    private GoogleMap googleMap;
    private String title;
    private CollapsingToolbarLayout collapsingToolbar;
    private ItemValidation iv = new ItemValidation();

    // Location
    private double latitude, longitude, latitudeCurrent, longitudeCurrent;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location, locationCurrent;
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
    private Button btnResetPosition;
    private boolean refreshMode = false;
    private EditText edtNama, edtAlamat, edtKota, edtTelepon, edtNoHP, edtBank, edtRekening, edtCP, edtLatitude, edtLongitude, edtState;
    private Spinner spArea;
    private Button btnSimpan;
    private List<AreaModel> listArea;
    private SessionManager session;
    private RecyclerView rvPhoto;
    private ImageButton ibAddPhoto;

    //Upload Handler
    private static int RESULT_OK = -1;
    private static int PICK_IMAGE_REQUEST = 1212;
    private ImageUtils iu = new ImageUtils();
    private Bitmap bitmap;
    private List<Bitmap> photoList;
    private PhotosAdapter adapter;
    private ProgressDialog progressDialog;
    private EditText edtEmail;
    private String kdcus = "";
    private boolean editMode = false;
    private String kdArea = "";
    private String statusAktif = "";
    private LinearLayout llUploadKTP;
    private RadioGroup rgPersetujuan;
    private boolean statusCustomer = true;
    private boolean uploadKTP = true;
    private Bitmap bitmapKTP;
    private ImageView ivKTP;
    private boolean isVerifikasi = true, approved = true;

    public DetailVerifikasiOutlet() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_verifikasi_outlet);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MockLocChecker checker = new MockLocChecker(DetailVerifikasiOutlet.this);
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnResetPosition = (Button) findViewById(R.id.btn_reset);

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
        llUploadKTP = (LinearLayout) findViewById(R.id.ll_upload_ktp);
        rgPersetujuan = (RadioGroup) findViewById(R.id.rg_persetujuan);
        ivKTP = (ImageView) findViewById(R.id.iv_ktp);

        session = new SessionManager(DetailVerifikasiOutlet.this);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(DetailVerifikasiOutlet.this);
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
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        adapter = new PhotosAdapter(DetailVerifikasiOutlet.this, photoList);
        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(adapter);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        title = "Detail Verifikasi Outlet";

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            isVerifikasi = bundle.getBoolean("isverifikasi", true);
            if(!isVerifikasi){
                title = "Detail Aktivasi Outlet";
                btnSimpan.setText(" Simpan Data Aktivasi ");
            }
        }

        //tvTitle.setText(title);
        initCollapsingToolbar();

        initLocation();

        initEvent();
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

                if(uploadKTP){
                    bitmapKTP = bitmap;
                    ivKTP.setImageBitmap(bitmap);
                }else{
                    photoList.add(bitmap);
                    adapter.notifyDataSetChanged();
                }
            }

        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            /*try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(photoFromCameraURI));
                bitmap = scaleDown(bitmap, 380, true);

                if(bitmap != null){

                    if(uploadKTP){
                        bitmapKTP = bitmap;
                        ivKTP.setImageBitmap(bitmap);
                    }else{
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
                Toast.makeText(DetailVerifikasiOutlet.this, "Gambar tidak termuat, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
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

                uploadKTP = false;
                loadChooserDialog();
            }
        });

        rgPersetujuan.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                if(i == R.id.rb_setuju){

                    statusCustomer = true;
                }else if(i == R.id.rb_tolak){

                    statusCustomer = false;
                }
            }
        });

        llUploadKTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadKTP = true;
                loadChooserDialog();
            }
        });
    }

    private void loadChooserDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailVerifikasiOutlet.this);
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

        if(PermissionUtils.hasPermissions(DetailVerifikasiOutlet.this,Manifest.permission.CAMERA)){

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

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(DetailVerifikasiOutlet.this)
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

    private void validateBeforeSave() {

        if(editMode && iv.parseNullInteger(statusAktif) != 2 && isVerifikasi){

            Toast.makeText(DetailVerifikasiOutlet.this, "Outlet telah dirposes, tidak dapat diubah", Toast.LENGTH_LONG).show();
            return;
        }

        if(editMode && iv.parseNullInteger(statusAktif) != 3 && !isVerifikasi){

            Toast.makeText(DetailVerifikasiOutlet.this, "Outlet telah dirposes, tidak dapat diubah", Toast.LENGTH_LONG).show();
            return;
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

        if(edtTelepon.getText().toString().length() == 0){

            edtTelepon.setError("Nomor Reseller harap diisi");
            edtTelepon.requestFocus();
            return;
        }else{
            edtTelepon.setError(null);
        }

        if(edtCP.getText().toString().length() == 0){

            edtCP.setError("Contact Person harap diisi");
            edtCP.requestFocus();
            return;
        }else{
            edtCP.setError(null);
        }

        /*
        if(edtNoHP.getText().toString().length() == 0){

            edtNoHP.setError("Nama harap diisi");
            edtNoHP.requestFocus();
            return;
        }else{
            edtNoHP.setError(null);
        }

        if(spArea.getCount() == 0){

            Toast.makeText(DetailCustomer.this, "Tidak ada Area yang terpilih", Toast.LENGTH_LONG).show();
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
        }*/
        String message = "Anda yakin ingin memverifikasi "+ edtNama.getText().toString()+" ?";
        if(!isVerifikasi) message = "Anda yakin ingin mengaktivasi "+ edtNama.getText().toString()+" ?";
        approved = true;
        if(!statusCustomer){
            message = "Anda yakin ingin menolak "+ edtNama.getText().toString()+" ?";
            approved = false;
        }
        AlertDialog alert = new AlertDialog.Builder(DetailVerifikasiOutlet.this)
                .setTitle("Konfirmasi")
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
                })
                .show();
    }

    private void saveData() {

        progressDialog = new ProgressDialog(DetailVerifikasiOutlet.this, R.style.AppTheme_Login_Dialog);
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
            jDataCustomer.put("status", (statusCustomer) ? ((isVerifikasi) ? "3" : "4") : "0");
            jDataCustomer.put("contact_person", edtCP.getText().toString());

            AreaModel area = (AreaModel) spArea.getSelectedItem();
            jDataCustomer.put("kodearea", area.getValue());
            jDataCustomer.put("useru", session.getUserInfo(SessionManager.TAG_UID));
            jDataCustomer.put("useru_tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
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
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdcus", kdcus);
            jBody.put("data_customer", jDataCustomer);
            jBody.put("data_location", jDataLocation);
            jBody.put("data_images", jDataImages);
            jBody.put("lat_admin", iv.doubleToStringFull(latitudeCurrent));
            jBody.put("long_admin", iv.doubleToStringFull(longitudeCurrent));
            jBody.put("data_ktp", (bitmapKTP != null) ? ImageUtils.convert(bitmapKTP) : "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailVerifikasiOutlet.this, jBody, "PUT", ServerURL.verifikasiOutlet, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    progressDialog.dismiss();

                    if(iv.parseNullInteger(status) == 200){

                        if(!isVerifikasi && approved){

                            saveTransaksiAktivasi();
                        }else{
                            String message = response.getJSONObject("response").getString("message");
                            Toast.makeText(DetailVerifikasiOutlet.this, message, Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(DetailVerifikasiOutlet.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(DetailVerifikasiOutlet.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(DetailVerifikasiOutlet.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    private void saveTransaksiAktivasi() {

        JSONArray jsonArray = new JSONArray();
        JSONObject jBody1 = new JSONObject();
        try {
            jBody1.put("status", "PENDING");
            jBody1.put("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
            jBody1.put("kode_cv", session.getUserDetails().get(SessionManager.TAG_NIK));
            jBody1.put("kode", "");
            jBody1.put("nomor", edtTelepon.getText().toString());
            jBody1.put("nama", edtNama.getText().toString());
            jBody1.put("level", "RS");
            jBody1.put("nomor_upline", "");
            jBody1.put("pin", "");
            jBody1.put("pin_upline", "");
            jBody1.put("pesan", "AKTIVASI_RS RS "+ edtNama.getText().toString());
            jBody1.put("crbayar", "-");
            jBody1.put("keterangan", "-");
            jBody1.put("keterangan_order", "");
            jBody1.put("flag", "1");
            jBody1.put("order_format", "*772*1*"+edtTelepon.getText().toString()+"#");
            jBody1.put("status_transaksi", "AKTIVASI_RS");
            jBody1.put("flag_injek", "SMS");
            jBody1.put("proses", "1");
            jBody1.put("nonota", "");
            jBody1.put("cluster", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jBody2 = new JSONObject();
        try {
            jBody2.put("status", "PENDING");
            jBody2.put("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
            jBody2.put("kode_cv", session.getUserDetails().get(SessionManager.TAG_NIK));
            jBody2.put("kode", edtTelepon.getText().toString());
            jBody2.put("nomor", edtTelepon.getText().toString());
            jBody2.put("nama", edtNama.getText().toString());
            jBody2.put("level", "RS");
            jBody2.put("nomor_upline", "");
            jBody2.put("pin", "");
            jBody2.put("pin_upline", "");
            jBody2.put("pesan", "AKTIVASI_RS RS "+ edtNama.getText().toString());
            jBody2.put("crbayar", "-");
            jBody2.put("keterangan", "-");
            jBody2.put("keterangan_order", "");
            jBody2.put("flag", "1");
            jBody2.put("order_format", "1");
            jBody2.put("status_transaksi", "AKTIVASI_RS1");
            jBody2.put("flag_injek", "SMS");
            jBody2.put("proses", "1");
            jBody2.put("nonota", "");
            jBody2.put("cluster", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jBody2);

        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jsonArray);
            jBody.put("kode", edtTelepon.getText().toString());
            jBody.put("nomor", edtTelepon.getText().toString());
            jBody.put("kdcus", kdcus);
            jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
            jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
            jBody.put("lat_admin", iv.doubleToStringFull(latitudeCurrent));
            jBody.put("long_admin", iv.doubleToStringFull(longitudeCurrent));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialog = new ProgressDialog(DetailVerifikasiOutlet.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Injecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        ApiVolley request = new ApiVolley(DetailVerifikasiOutlet.this, jBody, "POST", ServerURL.aktivasiOutlet, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String superMessage = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailVerifikasiOutlet.this, message, Toast.LENGTH_LONG).show();
                        onBackPressed();

                    }else{
                        Toast.makeText(DetailVerifikasiOutlet.this, superMessage, Toast.LENGTH_LONG).show();
                    }

                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                progressDialog.dismiss();
            }
        });
    }

    private void getDataArea() {

        ApiVolley request = new ApiVolley(DetailVerifikasiOutlet.this, new JSONObject(), "GET", ServerURL.getArea, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listArea = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        String area = session.getUserInfo(SessionManager.TAG_AREA);

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            if(jo.getString("kode_kota").equals(area)) listArea.add(new AreaModel(jo.getString("kode_kota"), jo.getString("omo")));
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

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        location = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        latitudeCurrent = 0;
        longitudeCurrent = 0;
        locationCurrent = new Location("set1");
        locationCurrent.setLatitude(latitudeCurrent);
        locationCurrent.setLongitude(longitudeCurrent);

        refreshMode = true;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kdcus = bundle.getString("kdcus");
            isVerifikasi = bundle.getBoolean("isverifikasi", true);
            if(kdcus != null && kdcus.length() > 0){

                editMode = true;
                getDataCustomer();
                getPhotos();

            }else{
                location = getLocation();
                getDataArea();
            }
        }else{
            location = getLocation();
            getDataArea();
        }

        btnResetPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //refreshMode = true;
                location = getLocation();
            }
        });
    }

    private void getPhotos() {

        ApiVolley request = new ApiVolley(DetailVerifikasiOutlet.this, new JSONObject(), "GET", ServerURL.getImages+kdcus, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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

    private class AsyncGettingKTPBitmap extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            System.out.println("doInBackground");

            bitmapKTP = null;

            bitmapKTP = downloadImage(params[0]);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ivKTP.setImageBitmap(bitmapKTP);
                }
            });
            return bitmapKTP;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            System.out.println("bitmap" + bitmap);

        }
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

        ApiVolley request = new ApiVolley(DetailVerifikasiOutlet.this, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            String urlKTP = jo.getString("ktp");
                            if(urlKTP.length() > 0){
                                new AsyncGettingKTPBitmap().execute(urlKTP);
                            }

                            if(jo.getString("latitude").length() > 0){
                                latitude = iv.parseNullDouble(jo.getString("latitude"));
                                longitude = iv.parseNullDouble(jo.getString("longitude"));
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                refreshMode = true;
                                onLocationChanged(location);
                                getLocation();
                            }

                            statusAktif = jo.getString("status");
                            if(iv.parseNullInteger(statusAktif) == 2 && isVerifikasi){

                                btnSimpan.setEnabled(true);
                            }else if(iv.parseNullInteger(statusAktif) == 3 && !isVerifikasi){

                                btnSimpan.setEnabled(true);
                            }else{
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
                Toast.makeText(DetailVerifikasiOutlet.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailVerifikasiOutlet.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailVerifikasiOutlet.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailVerifikasiOutlet.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailVerifikasiOutlet.this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailVerifikasiOutlet.this);
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
        ActivityCompat.requestPermissions(DetailVerifikasiOutlet.this,
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

    private void callGoogleMap(String latitude, String longitude){

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ","+longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(DetailVerifikasiOutlet.this, "Cannot find google map, Please install latest google map.",
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
                googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .snippet(edtNama.getText().toString())
                        .position(new LatLng(latitude, longitude)));

                googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .snippet("Anda")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person_point))
                        .position(new LatLng(latitudeCurrent, longitudeCurrent)));

                if (ActivityCompat.checkSelfPermission(DetailVerifikasiOutlet.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailVerifikasiOutlet.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DetailVerifikasiOutlet.this, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(DetailVerifikasiOutlet.this);
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
                });*/

                /*googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
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

        edtLatitude.setText(iv.doubleToStringFull(latitude));
        edtLongitude.setText(iv.doubleToStringFull(longitude));
        edtState.setText(address0);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    @Override
    public void onLocationChanged(Location location) {

        latitudeCurrent = location.getLatitude();
        longitudeCurrent = location.getLongitude();

        //refreshMode = false;
        if(refreshMode){

            refreshMode = false;
            this.location = location;
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            setPointMap();
        }else{
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
