package gmedia.net.id.psp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.RuntimePermissionsActivity;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.NotificationUtil.InitFirebaseSetting;
import gmedia.net.id.psp.Utils.ServerURL;

public class LoginScreen extends RuntimePermissionsActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private EditText edtUsername, edtPassword;
    private CheckBox cbRemeber;
    private Button btnLogin;
    private static final int REQUEST_PERMISSIONS = 20;
    private SessionManager session;
    private boolean visibleTapped;
    private ItemValidation iv = new ItemValidation();
    private String refreshToken = "";
    private String sim1 = "", sim2 = "";
    private Button btnChangeAPN;
    private String imei1 = "", imei2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_login_screen);

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.getBoolean("exit", false)) {
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }

        if (ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED /*|| ContextCompat.checkSelfPermission(
                LoginScreen.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                LoginScreen.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED*/) {

            LoginScreen.super.requestAppPermissions(new
                            String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.WRITE_SETTINGS,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WAKE_LOCK,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE/*,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS*/}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        InitFirebaseSetting.getFirebaseSetting(LoginScreen.this);

        refreshToken = FirebaseInstanceId.getInstance().getToken();
        initUI();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    private void initUI() {

        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        cbRemeber = (CheckBox) findViewById(R.id.cb_remeber_me);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnChangeAPN = (Button) findViewById(R.id.btn_change_apn);

        visibleTapped = true;
        session = new SessionManager(LoginScreen.this);

        if (session.isSaved()) {

            edtUsername.setText(session.getUserDetails().get(SessionManager.TAG_USERNAME));
            edtPassword.setText(session.getUserDetails().get(SessionManager.TAG_PASSWORD));
            cbRemeber.setChecked(true);
            login();
        }else{
            //cbRemeber.setChecked(false);
        }

        cbRemeber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                iv.hideSoftKey(LoginScreen.this);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validasiLogin();
            }
        });

        edtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int position = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (event.getX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[position].getBounds().width())) {

                        if (visibleTapped) {
                            edtPassword.setTransformationMethod(null);
                            edtPassword.setSelection(edtPassword.getText().length());
                            visibleTapped = false;
                        } else {
                            edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                            edtPassword.setSelection(edtPassword.getText().length());
                            visibleTapped = true;
                        }
                        return false;
                    }
                }

                return false;
            }
        });

        btnChangeAPN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*int identity = InsertAPN("Internet");
                SetPreferredAPN(identity);*/
                startActivityForResult(new Intent(android.provider.Settings.ACTION_APN_SETTINGS), 0);
            }
        });
    }

    //TODO: Change APN setting, still not working
    /*public int InsertAPN(String name){

        //Set the URIs and variables
        int id = -1;
        boolean existing = false;
        final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
        final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

        int permissionCheck = ContextCompat.checkSelfPermission(LoginScreen.this, Manifest.permission.WRITE_APN_SETTINGS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            //requesting permission
            ActivityCompat.requestPermissions(LoginScreen.this, new String[]{Manifest.permission.WRITE_APN_SETTINGS}, 1);
        } else {

            //Check if the specified APN is already in the APN table, if so skip the insertion
            Cursor parser = getContentResolver().query(APN_TABLE_URI, null, null, null, null);
            parser.moveToLast();
            while (parser.isBeforeFirst() == false){
                int index = parser.getColumnIndex("name");
                String n = parser.getString(index);
                if (n.equals(name)){
                    existing = true;
                    Toast.makeText(getApplicationContext(), "APN already configured.",Toast.LENGTH_SHORT).show();
                    break;
                }
                parser.moveToPrevious();
            }

            //if the entry doesn't already exist, insert it into the APN table
            if (!existing){

                //Initialize the Content Resolver and Content Provider
                ContentResolver resolver = this.getContentResolver();
                ContentValues values = new ContentValues();

                //Capture all the existing field values excluding name
                Cursor apu = getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
                apu.moveToFirst();
                int index;

                index = apu.getColumnIndex("apn");
                String apn = apu.getString(index);
                index = apu.getColumnIndex("type");
                String type = apu.getString(index);
                index = apu.getColumnIndex("proxy");
                String proxy = apu.getString(index);
                index = apu.getColumnIndex("port");
                String port = apu.getString(index);
                index = apu.getColumnIndex("user");
                String user = apu.getString(index);
                index = apu.getColumnIndex("password");
                String password = apu.getString(index);
                index = apu.getColumnIndex("server");
                String server = apu.getString(index);
                index = apu.getColumnIndex("mmsc");
                String mmsc = apu.getString(index);
                index = apu.getColumnIndex("mmsproxy");
                String mmsproxy = apu.getString(index);
                index = apu.getColumnIndex("mmsport");
                String mmsport = apu.getString(index);
                index = apu.getColumnIndex("mcc");
                String mcc = apu.getString(index);
                index = apu.getColumnIndex("mnc");
                String mnc = apu.getString(index);
                index = apu.getColumnIndex("numeric");
                String numeric = apu.getString(index);

                //Assign them to the ContentValue object
                values.put("name", name); //the method parameter
                values.put("apn", apn);
                values.put("type", type);
                values.put("proxy", proxy);
                values.put("port", port);
                values.put("user", user);
                values.put("password", password);
                values.put("server", server);
                values.put("mmsc", mmsc);
                values.put("mmsproxy", mmsproxy);
                values.put("mmsport", mmsport);
                values.put("mcc", mcc);
                values.put("mnc", mnc);
                values.put("numeric", numeric);

                //Actual insertion into table
                Cursor c = null;
                try{
                    Uri newRow = resolver.insert(APN_TABLE_URI, values);

                    if(newRow != null){
                        c = resolver.query(newRow, null, null, null, null);
                        int idindex = c.getColumnIndex("_id");
                        c.moveToFirst();
                        id = c.getShort(idindex);
                    }
                }
                catch(SQLException e){}
                if(c !=null ) c.close();
            }
        }


        return id;
    }

    //Takes the ID of the new record generated in InsertAPN and sets that particular record the default preferred APN configuration
    public boolean SetPreferredAPN(int id){

        //If the id is -1, that means the record was found in the APN table before insertion, thus, no action required
        if (id == -1){
            return false;
        }

        Uri.parse("content://telephony/carriers");
        final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

        boolean res = false;
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();

        values.put("apn_id", id);
        try{
            resolver.update(PREFERRED_APN_URI, values, null, null);
            Cursor c = resolver.query(PREFERRED_APN_URI, new String[]{"name", "apn"}, "_id="+id, null, null);
            if(c != null){
                res = true;
                c.close();
            }
        }
        catch (SQLException e){}
        return res;
    }*/

    private void validasiLogin() {

        if (edtUsername.getText().length() <= 0) {

            Snackbar.make(findViewById(android.R.id.content), "Username tidak boleh kosong",
                    Snackbar.LENGTH_LONG).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();

            return;
        }

        if (edtPassword.getText().length() <= 0) {

            Snackbar.make(findViewById(android.R.id.content), "Password tidak boleh kosong",
                    Snackbar.LENGTH_LONG).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();

            return;
        }

        login();
    }

    private void login() {

        final ProgressDialog progressDialog = new ProgressDialog(LoginScreen.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            }else{

                SubscriptionManager subscriptionManager = SubscriptionManager.from(LoginScreen.this);
                List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String mPhoneNumber = tMgr.getLine1Number();
                Log.d("sim", "login: " + mPhoneNumber);

                if(subsInfoList != null && subsInfoList.size() > 0){
                    Log.d("sim", "Current list = " + subsInfoList);

                    int x = 0;
                    for (SubscriptionInfo subscriptionInfo : subsInfoList) {

                        String number = subscriptionInfo.getNumber();

                        Log.d("sim", " Number is  " + number);
                        if(x == 0){
                            sim1 = number;
                        }else{
                            sim2 = number;
                        }

                        x++;
                    }
                }
            }
        }

        ArrayList<String> imeiList = iv.getIMEI(LoginScreen.this);

        if(imeiList.size() > 1){ // dual sim

            imei1 = imeiList.get(0);
            imei2 = imeiList.get(1);
        }else if(imeiList.size() == 1){ // single sim

            imei1 = imeiList.get(0);
        }

        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version = pInfo.versionName;

        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("username", edtUsername.getText().toString());
            jBody.put("password", edtPassword.getText().toString());
            jBody.put("sim1", sim1);
            jBody.put("sim2", sim2);
            jBody.put("imei1", imei1);
            jBody.put("imei2", imei2);
            jBody.put("phone_model", deviceMan +" "+ deviceName);
            jBody.put("fcm_id", refreshToken);
            jBody.put("version", version);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(LoginScreen.this, jBody, "POST", ServerURL.login, "", "", 0, edtUsername.getText().toString(), edtPassword.getText().toString(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "";
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                        String nik = response.getJSONObject("response").getString("nik");
                        String nama = response.getJSONObject("response").getString("nama");
                        String nikGa = response.getJSONObject("response").getString("nik_ga");
                        String area = response.getJSONObject("response").getString("kodearea");
                        String flag = response.getJSONObject("response").getString("flag");
                        PspPrinter.npwpToko = response.getJSONObject("response").getString("npwp");
                        String statusSales = response.getJSONObject("response").getString("status");
                        String jabatan = response.getJSONObject("response").getString("status_bagian");
                        session.createLoginSession(nikGa,nik, nama ,edtUsername.getText().toString(),edtPassword.getText().toString(), (cbRemeber.isChecked())? "1": "0", jabatan,"", statusSales, area, flag);
                        Toast.makeText(LoginScreen.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginScreen.this, MainNavigationActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }else{
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(String result) {
                Snackbar.make(findViewById(android.R.id.content), "Terjadi kesalahan koneksi, harap ulangi kembali nanti", Snackbar.LENGTH_LONG).show();
                if(progressDialog != null && progressDialog.isShowing()) {
                    try {

                        progressDialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(LoginScreen.this, LoginScreen.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState && !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, timerClose);
    }
}
