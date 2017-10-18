package gmedia.net.id.psp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.Utils.ServerURL;

public class LoginScreen extends AppCompatActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private EditText edtUsername, edtPassword;
    private CheckBox cbRemeber;
    private Button btnLogin;
    private SessionManager session;
    private boolean visibleTapped;
    private ItemValidation iv = new ItemValidation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_login_screen);

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.getBoolean("exit", false)){
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }

        initUI();
    }

    private void initUI() {

        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        cbRemeber = (CheckBox) findViewById(R.id.cb_remeber_me);
        btnLogin = (Button) findViewById(R.id.btn_login);

        visibleTapped = true;
        session = new SessionManager(LoginScreen.this);

        if(session.isSaved()){

            edtUsername.setText(session.getUserDetails().get(SessionManager.TAG_USERNAME));
            edtPassword.setText(session.getUserDetails().get(SessionManager.TAG_PASSWORD));
            cbRemeber.setChecked(true);
            login();
        }

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

                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(event.getX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[position].getBounds().width())) {

                        if(visibleTapped){
                            edtPassword.setTransformationMethod(null);
                            edtPassword.setSelection(edtPassword.getText().length());
                            visibleTapped = false;
                        }else{
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
    }

    private void validasiLogin() {

        if(edtUsername.getText().length() <= 0){

            Snackbar.make(findViewById(android.R.id.content), "Username tidak boleh kosong",
                    Snackbar.LENGTH_LONG).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();

            return;
        }

        if(edtPassword.getText().length() <= 0){

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

    private void login(){

        final ProgressDialog progressDialog = new ProgressDialog(LoginScreen.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("username", edtUsername.getText().toString());
            jBody.put("password", edtPassword.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(LoginScreen.this, jBody, "POST", ServerURL.login, "", "", 0, edtUsername.getText().toString(), edtPassword.getText().toString(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "";

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        String nik = response.getJSONObject("response").getString("nik");
                        String nama = response.getJSONObject("response").getString("nama");
                        String nikGa = response.getJSONObject("response").getString("nik_ga");
                        String area = response.getJSONObject("response").getString("kodearea");
                        String flag = response.getJSONObject("response").getString("flag");
                        session.createLoginSession(nikGa,nik, nama ,edtUsername.getText().toString(),edtPassword.getText().toString(), (cbRemeber.isChecked())? "1": "0","","","",area, flag);
                        Toast.makeText(LoginScreen.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginScreen.this, MainNavigationActivity.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(String result) {
                Snackbar.make(findViewById(android.R.id.content), "Terjadi kesalahan koneksi, harap ulangi kembali nanti", Snackbar.LENGTH_LONG).show();
                progressDialog.dismiss();
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
