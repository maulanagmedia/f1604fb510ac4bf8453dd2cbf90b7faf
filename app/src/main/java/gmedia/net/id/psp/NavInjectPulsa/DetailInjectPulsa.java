package gmedia.net.id.psp.NavInjectPulsa;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.NavInjectPulsa.Adapter.ListBalasanInjectAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;

public class DetailInjectPulsa extends AppCompatActivity {

    private static Context context;
    private SessionManager session;
    private static ItemValidation iv = new ItemValidation();
    private LinearLayout llNonota;
    private EditText edtNonota;
    private EditText edtNomor;
    private RadioGroup rgNominal;
    private RadioButton rbS5, rbS10, rbS20, rbS25, rbS50, rbS100;
    private EditText edtHarga;
    private static ListView lvBalasan;
    private static List<CustomItem> listBalasan;
    private static ListBalasanInjectAdapter balasanAdapter;
    private Button btnProses;
    private static final String TAG = "DetailInject";
    public static boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_inject_pulsa);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Inject Pulsa");
        context = this;

        initUI();
    }

    private void initUI() {

        llNonota = (LinearLayout) findViewById(R.id.ll_nonota);
        edtNonota = (EditText) findViewById(R.id.edt_nonota);
        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        rgNominal = (RadioGroup) findViewById(R.id.rg_nominal);
        rbS5 = (RadioButton) findViewById(R.id.rb_5s);
        rbS10 = (RadioButton) findViewById(R.id.rb_10s);
        rbS20 = (RadioButton) findViewById(R.id.rb_20s);
        rbS25 = (RadioButton) findViewById(R.id.rb_25s);
        rbS50 = (RadioButton) findViewById(R.id.rb_50s);
        rbS100 = (RadioButton) findViewById(R.id.rb_100s);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);
        lvBalasan = (ListView) findViewById(R.id.lv_balasan);

        listBalasan = new ArrayList<>();

        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActive = true;

        boolean isAccessGranted =  isAccessibilityEnabled(context.getPackageName() + "/" + context.getPackageName() + ".NavInjectPulsa.Service.USSDService");
        if(isAccessGranted){

            //Log.d(TAG, "granted");
        }else{
            //Log.d(TAG, "not granted");
            Snackbar.make(findViewById(android.R.id.content), "Mohon ijinkan accessibility pada PSP Sales, Cari PSP Sales dan ubah enable",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        }
                    }).show();

        }
    }

    public static void addTambahBalasan(String text){

        if(balasanAdapter != null){

            CustomItem item = new CustomItem(iv.getCurrentDate(FormatItem.formatTime), text);
            balasanAdapter.addData(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    public boolean isAccessibilityEnabled(String id){
        int accessibilityEnabled = 0;
        final String LIGHTFLOW_ACCESSIBILITY_SERVICE = "com.example.test/com.example.text.ccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            //Log.d(TAG, "***ACCESSIBILIY IS ENABLED***: ");

            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            //Log.d(TAG, "Setting: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    //Log.d(TAG, "Setting: " + accessabilityService);
                    if (accessabilityService.toLowerCase().equals(id.toLowerCase())){
                        //Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            //Log.d(TAG, "***END***");
        }
        else{
            //Log.d(TAG, "***ACCESSIBILIY IS DISABLED***");
        }
        return accessibilityFound;
    }

    private void initEvent() {

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = "*" + 123 + Uri.encode("#");
                startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + code)));

                lvBalasan.setAdapter(null);
                listBalasan  = new ArrayList<>();
                balasanAdapter = new ListBalasanInjectAdapter((Activity) context, listBalasan);
                lvBalasan.setAdapter(balasanAdapter);
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
}
