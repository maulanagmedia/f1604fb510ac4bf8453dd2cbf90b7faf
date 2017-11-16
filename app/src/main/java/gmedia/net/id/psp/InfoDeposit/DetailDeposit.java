package gmedia.net.id.psp.InfoDeposit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailDeposit extends AppCompatActivity {

    private EditText edtNama, edtJumlah, edtKeterangan;
    private Button btnSimpan;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private String kdcus = "";
    private String newString = "", totalFinal = "";
    private EditText edtNomor;
    private ProgressBar pbProses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_deposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Deposit");

        initUI();
    }

    private void initUI() {

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        edtJumlah = (EditText) findViewById(R.id.edt_jumlah);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        session = new SessionManager(DetailDeposit.this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            String nomor = bundle.getString("nomor");
            edtNomor.setText(nomor);

            kdcus = bundle.getString("kdcus");
            if(kdcus !=  null && kdcus.length() >0){

                initEvent();
                getDataCustomer();
            }else{

                String nama = bundle.getString("nama");
                String total = iv.ChangeToCurrencyFormat(bundle.getString("total"));
                String keterangan = bundle.getString("keterangan");
                edtNama.setText(nama);
                edtJumlah.setText(total);
                edtJumlah.setFocusable(false);
                edtJumlah.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtKeterangan.setText(total);
                edtKeterangan.setFocusable(false);
                edtKeterangan.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                edtKeterangan.setText(keterangan);
                btnSimpan.setEnabled(false);
            }
        }
    }

    private void getDataCustomer() {

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailDeposit.this, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pbProses.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void initEvent() {

        newString = "";
        if(firstLoad){
            firstLoad = false;

            edtJumlah.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(!editable.toString().equals(newString)){

                        String newJml = editable.toString().replace(".", "").replace(",","");
                        newString = iv.ChangeToCurrencyFormat(newJml);
                        edtJumlah.setText(newString);
                        edtJumlah.setSelection(edtJumlah.length());
                        totalFinal = newString.toString().replace(".", "").replace(",","");
                    }
                }
            });
        }

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Validasi
                if(kdcus.equals("")){
                    Toast.makeText(DetailDeposit.this, "Data outlet belum termuat, harap ulangi", Toast.LENGTH_LONG).show();
                    return;
                }

                if(iv.parseNullLong(totalFinal) <= 0){

                    edtJumlah.setError("Jumlah tidak boleh kosong");
                    edtJumlah.requestFocus();
                    return;
                }else{
                    edtJumlah.setError(null);
                }

                AlertDialog confirm = new AlertDialog.Builder(DetailDeposit.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan deposit a/n " + edtNama.getText().toString() + " sebesar Rp " + edtJumlah.getText().toString() + " ?")
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
    }

    private void saveData() {

        final ProgressDialog progressDialog = new ProgressDialog(DetailDeposit.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        JSONObject jData = new JSONObject();
        try {
            jData.put("tgl", iv.getCurrentDate(FormatItem.formatDate));
            jData.put("kdcus", kdcus);
            jData.put("masuk", totalFinal);
            jData.put("keterangan", edtKeterangan.getText().toString());
            jData.put("kode_lokasi", session.getUserInfo(SessionManager.TAG_AREA));
            jData.put("userid", session.getUserInfo(SessionManager.TAG_UID));
            jData.put("useru", session.getUserInfo(SessionManager.TAG_UID));
            jData.put("useru_update", iv.getCurrentDate(FormatItem.formatTimestamp));
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("data_deposit", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailDeposit.this, jBody, "POST", ServerURL.saveDeposite, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan saat memproses data, silahkan ulangi kembali";

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailDeposit.this, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(DetailDeposit.this, ActDeposit.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                    btnSimpan.setEnabled(true);
                }
            }

            @Override
            public void onError(String result) {

                btnSimpan.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Terjadi kesalahan saat memproses data, harap ulangi kembali", Snackbar.LENGTH_LONG).show();
                progressDialog.dismiss();
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