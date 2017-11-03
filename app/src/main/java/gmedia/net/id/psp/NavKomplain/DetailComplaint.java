package gmedia.net.id.psp.NavKomplain;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailComplaint extends AppCompatActivity {

    private String idComplaint = "";
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private EditText edtNama, edtKomplain, edtBalasan;
    private Button btnSimpan;
    private LinearLayout llBalasan;
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_complaint);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Komplain");

        initUI();
    }

    private void initUI() {

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtKomplain = (EditText) findViewById(R.id.edt_komplain);
        edtBalasan = (EditText) findViewById(R.id.edt_balasan);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        llBalasan = (LinearLayout) findViewById(R.id.ll_balasan);

        session = new SessionManager(DetailComplaint.this);
        edtNama.setText(session.getUserInfo(SessionManager.TAG_NAMA));
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            idComplaint = bundle.getString("id");

            if(idComplaint != null && idComplaint.length() > 0){

                btnSimpan.setEnabled(false);
                editMode = true;
                llBalasan.setVisibility(View.VISIBLE);
                getDataComplaint();
            }else{
                setButtonAction();
            }
        }else{
            setButtonAction();
        }
    }

    private void setButtonAction(){

        btnSimpan.setEnabled(true);
        editMode = false;

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtKomplain.getText().toString().length() == 0){
                    edtKomplain.setError("Komplain harap diisi");
                    edtKomplain.requestFocus();
                    return;
                }else{
                    edtKomplain.setError(null);
                }


                AlertDialog builder = new AlertDialog.Builder(DetailComplaint.this)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage("Anda yakin ingin menyimpan komplain?")
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
    }

    private void saveData() {

        final ProgressDialog progressDialog = new ProgressDialog(DetailComplaint.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);

        JSONObject jData = new JSONObject();
        try {
            jData.put("nik", nik);
            jData.put("komplain", edtKomplain.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("data", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailComplaint.this, jBody, "POST", ServerURL.saveComplaint, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailComplaint.this, message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        onBackPressed();
                    }else{
                        Toast.makeText(DetailComplaint.this, message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailComplaint.this, "Terjadi kesalahan saat menyimpan data", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(DetailComplaint.this, "Terjadi kesalahan saat menyimpan data", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void getDataComplaint() {

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("id", idComplaint);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailComplaint.this, jBody, "POST", ServerURL.getComplaint, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtKomplain.setText(jo.getString("komplain"));
                            edtBalasan.setText(jo.getString("balasan"));
                        }
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
