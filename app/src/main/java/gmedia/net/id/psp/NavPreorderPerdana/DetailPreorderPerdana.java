package gmedia.net.id.psp.NavPreorderPerdana;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailPreorderPerdana extends AppCompatActivity {

    private Context context;
    private EditText edtNamaBarang, edtHarga, edtJumlah, edtTanggal;
    private LinearLayout llTanggal;
    private Button btnHapus, btnSimpan;
    private String tanggal = "";
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private boolean editMode = false;
    private String idDetail = "", kodebrg = "", namaBrg = "", harga = "";
    private ProgressBar pbProcess;
    private String TAG = "DetailPreorder";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_preorder_perdana);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Preorder Perdana");

        context = this;
        initUI();
    }

    private void initUI() {

        edtNamaBarang = (EditText) findViewById(R.id.edt_nama_barang);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        edtJumlah = (EditText) findViewById(R.id.edt_jumlah);
        edtTanggal = (EditText) findViewById(R.id.edt_tanggal);
        llTanggal = (LinearLayout) findViewById(R.id.ll_tanggal);
        pbProcess = (ProgressBar) findViewById(R.id.pb_proses);

        btnHapus = (Button) findViewById(R.id.btn_hapus);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);

        session = new SessionManager(DetailPreorderPerdana.this);
        tanggal = iv.sumDate(iv.getCurrentDate(FormatItem.formatDateDisplay), 1, FormatItem.formatDateDisplay) ;
        edtTanggal.setText(tanggal);
        editMode = false;

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            idDetail = bundle.getString("id","");
            if(idDetail.length() > 0){

                editMode = true;
                btnHapus.setVisibility(View.VISIBLE);
                btnSimpan.setEnabled(false);

                getDetailPreOrder();
            }

            kodebrg = bundle.getString("kodebrg","");
            namaBrg = bundle.getString("namabrg","");
            harga = bundle.getString("harga","");

            edtNamaBarang.setText(namaBrg);
            edtHarga.setText(iv.ChangeToRupiahFormat(harga));
        }

        initEvent();
    }

    private void initEvent() {

        llTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar customDate;
                SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);

                Date dateValue = null;

                try {
                    dateValue = sdf.parse(tanggal);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                customDate = Calendar.getInstance();
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        customDate.set(Calendar.YEAR,year);
                        customDate.set(Calendar.MONTH,month);
                        customDate.set(Calendar.DATE,date);

                        SimpleDateFormat sdFormat = new SimpleDateFormat(FormatItem.formatDateDisplay, Locale.US);
                        tanggal = sdFormat.format(customDate.getTime());
                        edtTanggal.setText(tanggal);
                    }
                };

                SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
                new DatePickerDialog(DetailPreorderPerdana.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // validasi
                if(kodebrg.length() == 0){

                    Toast.makeText(context, "Data tidak termuat, harap ulangi kembali", Toast.LENGTH_LONG).show();
                    return;
                }

                if(edtJumlah.getText().toString().length() == 0 || iv.parseNullLong(edtJumlah.getText().toString()) == 0){

                    edtJumlah.setError("Jumlah harap diisi");
                    edtJumlah.requestFocus();
                    return;
                }else{

                    edtJumlah.setError(null);
                }

                if(edtTanggal.getText().toString().length() == 0 || !iv.isMoreThanCurrentDate(edtTanggal.getText().toString(), iv.getCurrentDate(FormatItem.formatDateDisplay), FormatItem.formatDateDisplay)){

                    edtTanggal.setError("Tanggal minimal besok");
                    edtTanggal.requestFocus();
                    return;
                }else{

                    edtTanggal.setError(null);
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyimpan data preorder?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveData(true);
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

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menghapus data preorder?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveData(false);
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

    private void saveData(boolean isInsert) {

        progressDialog = new ProgressDialog(DetailPreorderPerdana.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(isInsert? "Menyimpan..." : "Menghapus...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        JSONObject jData = new JSONObject();

        if(isInsert){

            try {
                jData.put("nik", session.getUserInfo(SessionManager.TAG_UID));
                jData.put("kodebrg", kodebrg);
                jData.put("harga", harga);
                jData.put("banyak", edtJumlah.getText().toString());
                jData.put("order_date", iv.ChangeFormatDateString(tanggal, FormatItem.formatDateDisplay, FormatItem.formatDate));
                jData.put("user_id", session.getUserInfo(SessionManager.TAG_UID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{

            try {
                jData.put("status", "0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ServerURL.savePreorder;
        String method = "POST";

        if(!isInsert){

            url = url + idDetail;
            method = "PUT";
        }

        ApiVolley request = new ApiVolley(DetailPreorderPerdana.this, jBody, method, url, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(context, ActPreorderPerdanaActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    }else{

                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {

                    if(progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                    btnSimpan.setEnabled(true);
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                if(progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    private void getDetailPreOrder() {

        pbProcess.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getPreorder+idDetail, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbProcess.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtJumlah.setText(jo.getString("banyak"));
                            tanggal = iv.ChangeFormatDateString(jo.getString("order_date"), FormatItem.formatDate, FormatItem.formatDateDisplay);
                            edtTanggal.setText(tanggal);
                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                Log.d(TAG, "onError: "+ result);
                pbProcess.setVisibility(View.GONE);
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

        if(editMode){
            super.onBackPressed();
        }else{

            Intent intent = new Intent(context, ListBarangPreorderPerdana.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
