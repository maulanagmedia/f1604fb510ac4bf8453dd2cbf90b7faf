package gmedia.net.id.psp.OrderPulsa;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailOrderPulsa extends AppCompatActivity {

    private EditText edtNonota, edtNamaRS, edtS5, edtS10, edtS20, edtS25, edtS50, edtS100, edtBulk, edtTotal, edtKeterangan;
    private TextView tvS5, tvS10, tvS20, tvS25, tvS50, tvS100, tvBulk;
    private Button btnProses;
    private String kodeRS = "", nomorRS = "", namaRS = "", levelRS = "", noUpline = "", pinRS = "", pinUpline = "";
    private String hargaS5 = "", hargaS10 = "", hargaS20 = "", hargaS25 = "", hargaS50 = "", hargaS100 = "", hargaDiskonBulk = "";
    private String nonota = "", flag = "";
    private ProgressBar pbProses;
    private boolean isProses = false;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private TextView tvHargaS5, tvHargaS10, tvHargaS20, tvHargaS25, tvHargaS50, tvHargaS100;
    private final String TAG = "DetailOrderPulsa";
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_pulsa);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Order Pulsa");

        initUI();
    }

    private void initUI() {

        edtNonota = (EditText) findViewById(R.id.edt_nonota);
        edtNamaRS = (EditText) findViewById(R.id.edt_nama_reseller);
        edtS5 = (EditText) findViewById(R.id.edt_s5);
        edtS10 = (EditText) findViewById(R.id.edt_s10);
        edtS20 = (EditText) findViewById(R.id.edt_s20);
        edtS25 = (EditText) findViewById(R.id.edt_s25);
        edtS50 = (EditText) findViewById(R.id.edt_s50);
        edtS100 = (EditText) findViewById(R.id.edt_s100);
        edtBulk = (EditText) findViewById(R.id.edt_bulk);
        tvS5 = (TextView) findViewById(R.id.tv_s5);
        tvS10 = (TextView) findViewById(R.id.tv_s10);
        tvS20 = (TextView) findViewById(R.id.tv_s20);
        tvS25 = (TextView) findViewById(R.id.tv_s25);
        tvS50 = (TextView) findViewById(R.id.tv_s50);
        tvS100 = (TextView) findViewById(R.id.tv_s100);
        tvBulk = (TextView) findViewById(R.id.tv_bulk);
        tvHargaS5 = (TextView) findViewById(R.id.tv_hargaS5);
        tvHargaS10 = (TextView) findViewById(R.id.tv_hargaS10);
        tvHargaS20 = (TextView) findViewById(R.id.tv_hargaS20);
        tvHargaS25 = (TextView) findViewById(R.id.tv_hargaS25);
        tvHargaS50 = (TextView) findViewById(R.id.tv_hargaS50);
        tvHargaS100 = (TextView) findViewById(R.id.tv_hargaS100);
        edtTotal = (EditText) findViewById(R.id.edt_total);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        btnProses = (Button) findViewById(R.id.btn_proses);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        isProses = false;
        session = new SessionManager(DetailOrderPulsa.this);
        editMode = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kodeRS = bundle.getString("koders");
            nonota = bundle.getString("nonota");

            if(nonota != null && nonota.length()>0){
                editMode = true;
                flag = bundle.getString("flag");
                edtNonota.setText(nonota);
            }

            getRSDetail();
            initEvent();
        }
    }

    private void getRSDetail() {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, new JSONObject(), "GET", ServerURL.getReseller+nik+"/"+kodeRS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){
                            JSONObject jo = items.getJSONObject(i);

                            nomorRS = jo.getString("nomor");
                            namaRS = jo.getString("nama");
                            levelRS = jo.getString("level");
                            noUpline = jo.getString("nomor_upline");
                            pinRS = jo.getString("pin");
                            pinUpline = jo.getString("pin_upline");
                            hargaS5 = jo.getString("harga5");
                            hargaS10 = jo.getString("harga10");
                            hargaS20 = jo.getString("harga20");
                            hargaS25 = jo.getString("harga25");
                            hargaS50 = jo.getString("harga50");
                            hargaS100 = jo.getString("harga100");
                            hargaDiskonBulk = jo.getString("hbulk");

                            tvHargaS5.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS5)));
                            tvHargaS10.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS10)));
                            tvHargaS20.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS20)));
                            tvHargaS25.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS25)));
                            tvHargaS50.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS50)));
                            tvHargaS100.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaS100)));

                            edtNamaRS.setText(namaRS);

                            if(editMode){
                                getDetailMkios();
                            }else{
                                getNonota();
                            }
                            break;
                        }
                    }

                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
            }
        });
    }

    private void getDetailMkios() throws JSONException {

        isLoading(true);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jBody = new JSONObject();
        jBody.put("nonota", nonota);
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, jBody, "POST", ServerURL.getMkios+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String pesan = "", S5 = "0", S10 = "0", S20 = "0", S25 = "0", S50 = "0", S100 = "0", valueBulk = "0";
                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){
                            JSONObject jo = items.getJSONObject(i);

                            kodeRS = jo.getString("kode");
                            nomorRS = jo.getString("nomor");
                            namaRS = jo.getString("nama");
                            levelRS = jo.getString("level");
                            noUpline = jo.getString("nomor_upline");
                            pinRS = jo.getString("pin");
                            pinUpline = jo.getString("pin_upline");
                            pesan = jo.getString("pesan");
                            if(!jo.getString("S5").equals("0") && !jo.getString("S5").equals("")) S5 = jo.getString("S5");
                            if(!jo.getString("S10").equals("0") && !jo.getString("S10").equals("")) S10 = jo.getString("S10");
                            if(!jo.getString("S20").equals("0") && !jo.getString("S20").equals("")) S20 = jo.getString("S20");
                            if(!jo.getString("S25").equals("0") && !jo.getString("S25").equals("")) S25 = jo.getString("S25");
                            if(!jo.getString("S50").equals("0") && !jo.getString("S50").equals("")) S50 = jo.getString("S50");
                            if(!jo.getString("S100").equals("0") && !jo.getString("S100").equals("")) S100 = jo.getString("S100");
                            if(!jo.getString("value_bulk").equals("0") && !jo.getString("value_bulk").equals("")) valueBulk = jo.getString("value_bulk");
                            totalHarga += iv.parseNullDouble(jo.getString("total"));
                        }

                        edtS5.setText(S5);
                        edtS10.setText(S10);
                        edtS20.setText(S20);
                        edtS25.setText(S25);
                        edtS50.setText(S50);
                        edtS100.setText(S100);
                        edtBulk.setText(valueBulk);
                        edtKeterangan.setText(pesan);
                        hitungTotal();
                    }

                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
            }
        });
    }

    private void getNonota() {

        isLoading(true);
        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, new JSONObject(), "GET", ServerURL.getMKIOSNonota +nomorRS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        nonota = response.getJSONObject("response").getString("nonota");
                        edtNonota.setText(nonota);
                    }

                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);
            }
        });
    }

    private void initEvent() {

        setTextWatcherHarga(edtS5);
        setTextWatcherHarga(edtS10);
        setTextWatcherHarga(edtS20);
        setTextWatcherHarga(edtS25);
        setTextWatcherHarga(edtS50);
        setTextWatcherHarga(edtS100);
        setTextWatcherHarga(edtBulk);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = "Apakah anda yakin ingin memproses data?";
                if(editMode) message = "Apakah anda yakin ingin mengubah "+nonota+" ?";
                AlertDialog builder = new AlertDialog.Builder(DetailOrderPulsa.this)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage(message)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                validasiBeforeSave();
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

    private void validasiBeforeSave() {

        if(isProses){
            Snackbar.make(findViewById(android.R.id.content), "Tunggu hingga proses selesai", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(editMode && !flag.equals("1")){
            Snackbar.make(findViewById(android.R.id.content), "Order sudah di proses, tidak dapat diubah", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(nonota == null || nonota.equals("")){
            Snackbar.make(findViewById(android.R.id.content), "Tidak dapat membuat no nota", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(kodeRS.equals("")){
            Snackbar.make(findViewById(android.R.id.content), "Reseller tidak ditemukan", Snackbar.LENGTH_LONG).show();
            return;
        }

        if(edtS5.getText().toString().equals("") && edtS10.getText().toString().equals("") && edtS20.getText().toString().equals("") && edtS25.getText().toString().equals("") && edtS50.getText().toString().equals("") && edtS100.getText().toString().equals("") && edtBulk.getText().toString().equals("")){

            Snackbar.make(findViewById(android.R.id.content), "Harap isi minimal salah satu order", Snackbar.LENGTH_LONG).show();
            edtS5.requestFocus();
            return;
        }

        bagiOrder();
    }

    private JSONArray jsonArray = new JSONArray();

    private void bagiOrder() {

        List<CustomItem> orderList = new ArrayList<>();

        if(iv.parseNullDouble(edtS5.getText().toString()) > 0){
            orderList.add(new CustomItem("V5","5", edtS5.getText().toString(), iv.doubleToStringRound(total5)));
        }

        if(iv.parseNullDouble(edtS10.getText().toString()) > 0){
            orderList.add(new CustomItem("V10","10", edtS10.getText().toString(), iv.doubleToStringRound(total10)));
        }

        if(iv.parseNullDouble(edtS20.getText().toString()) > 0){
            orderList.add(new CustomItem("V20","20", edtS20.getText().toString(), iv.doubleToStringRound(total20)));
        }

        if(iv.parseNullDouble(edtS25.getText().toString()) > 0){
            orderList.add(new CustomItem("V25","25", edtS25.getText().toString(), iv.doubleToStringRound(total25)));
        }

        if(iv.parseNullDouble(edtS50.getText().toString()) > 0){
            orderList.add(new CustomItem("V50","50", edtS50.getText().toString(), iv.doubleToStringRound(total50)));
        }

        if(iv.parseNullDouble(edtS100.getText().toString()) > 0){
            orderList.add(new CustomItem("V100","100", edtS100.getText().toString(), iv.doubleToStringRound(total100)));
        }

        jsonArray = new JSONArray();
        int x = 1, i = 0;
        int clusterX = 1;
        String keteranganOrder = "", orderFormat = "";
        int banyakCluster = orderList.size() / 3;
        int sisaList = orderList.size() % 3;
        double total = 0;
        boolean akhir = false;
        SortedMap<String,String> dataInsert = new TreeMap<>();

        for(CustomItem item :orderList){

            //keteranganOrder = keteranganOrder + ((x == 1) ? item.getItem1(): ", " + item.getItem1()) + "=" + item.getItem3();
            keteranganOrder = keteranganOrder + item.getItem1() + "=" + item.getItem3() + ((i == (orderList.size() - 1) && iv.parseNullDouble(edtBulk.getText().toString()) == 0) ? "" : ",");
            orderFormat = orderFormat + item.getItem3()+ "*" + item.getItem2()+"*";
            total += iv.parseNullDouble(item.getItem4());
            dataInsert.put(item.getItem2(), item.getItem3());

            if(x == 3 && clusterX <= banyakCluster){

                if(sisaList == 0 && iv.parseNullDouble(edtBulk.getText().toString()) > 0) akhir = true;

                pushJsonData(dataInsert, keteranganOrder, orderFormat, iv.doubleToStringRound(total));
                x = 0;
                clusterX +=1 ;
                total = 0;
                dataInsert = new TreeMap<>();
                keteranganOrder = "";
                orderFormat = "";
            }else if(x == sisaList && clusterX > banyakCluster){

                if(iv.parseNullDouble(edtBulk.getText().toString()) > 0) akhir = true;
                pushJsonData(dataInsert, keteranganOrder, orderFormat, iv.doubleToStringRound(total));
            }

            x++;
            i++;
        }

        if(iv.parseNullDouble(edtBulk.getText().toString()) > 0){

            double hasilBulk = iv.parseNullDouble(edtBulk.getText().toString()) / 1000;
            dataInsert = new TreeMap<>();
            dataInsert.put("bulk", edtBulk.getText().toString());
            pushJsonData(dataInsert, "Bulk "+ edtBulk.getText().toString(), "0" + iv.doubleToStringRound(hasilBulk)+"*", iv.doubleToStringRound(totalBulk));
        }

        saveData();
    }

    private void pushJsonData(SortedMap<String, String> data, final String keteranganOrder, String orderFormat, String total) {

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("status", "PENDING");
            jBody.put("tgl", iv.getCurrentDate(FormatItem.formatTimestamp));
            jBody.put("kode_cv", session.getUserDetails().get(SessionManager.TAG_NIK));
            jBody.put("kode", kodeRS);
            jBody.put("nomor", nomorRS);
            jBody.put("nama", namaRS);
            jBody.put("level", levelRS);
            jBody.put("nomor_upline", noUpline);
            jBody.put("pin", pinRS);
            jBody.put("pin_upline", pinUpline);
            jBody.put("pesan", edtKeterangan.getText().toString());
            jBody.put("crbayar", "ISI");
            jBody.put("S5", (data.get("5") == null) ? "": data.get("5"));
            jBody.put("S10", (data.get("10") == null) ? "": data.get("10"));
            jBody.put("S20", (data.get("20") == null) ? "": data.get("20"));
            jBody.put("S25", (data.get("25") == null) ? "": data.get("25"));
            jBody.put("S50", (data.get("50") == null) ? "": data.get("50"));
            jBody.put("S100", (data.get("100") == null) ? "": data.get("100"));
            jBody.put("value_bulk", (data.get("bulk") == null) ? "": data.get("bulk"));
            jBody.put("hargaS5", hargaS5);
            jBody.put("hargaS10", hargaS10);
            jBody.put("hargaS20", hargaS20);
            jBody.put("hargaS25", hargaS25);
            jBody.put("hargaS50", hargaS50);
            jBody.put("hargaS100", hargaS100);
            jBody.put("hargabulk", (data.get("bulk") == null) ? "": iv.doubleToStringRound(totalBulk));
            jBody.put("total", total);
            jBody.put("keterangan", "-");
            jBody.put("keterangan_order", keteranganOrder);
            jBody.put("flag", "1");
            jBody.put("order_format", orderFormat);
            jBody.put("status_transaksi", "INJECK");
            jBody.put("flag_injek", "SMS");
            jBody.put("proses", "1");
            jBody.put("nonota", nonota);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jBody);
    }

    private void saveData() {

        isLoading(true);

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String url = ServerURL.saveMKIOS, method = "POST";
        if(editMode){
            try {
                jBody.put("nonota", nonota);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            method = "PUT";
        }

        ApiVolley request = new ApiVolley(DetailOrderPulsa.this, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = "Order Pulsa berhasil ditambahkan";
                        if(editMode) message = "Order "+ nonota+ " berhasil diubah";
                        Toast.makeText(DetailOrderPulsa.this, message, Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(DetailOrderPulsa.this, PenjualanMKIOS.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
            }

            @Override
            public void onError(String result) {
                isLoading(false);
            }
        });
    }

    private void setTextWatcherHarga(EditText edt){

        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                hitungTotal();
            }
        });
    }

    private  double total5, total10, total20, total25, total50, total100, totalBulk, totalHarga;

    private void hitungTotal(){

        double jumlah5 = iv.parseNullDouble(edtS5.getText().toString());
        double jumlah10 = iv.parseNullDouble(edtS10.getText().toString());
        double jumlah20 = iv.parseNullDouble(edtS20.getText().toString());
        double jumlah25 = iv.parseNullDouble(edtS25.getText().toString());
        double jumlah50 = iv.parseNullDouble(edtS50.getText().toString());
        double jumlah100 = iv.parseNullDouble(edtS100.getText().toString());
        double jumlahBulk = iv.parseNullDouble(edtBulk.getText().toString());

        double harga5 = iv.parseNullDouble(hargaS5);
        double harga10 = iv.parseNullDouble(hargaS10);
        double harga20 = iv.parseNullDouble(hargaS20);
        double harga25 = iv.parseNullDouble(hargaS25);
        double harga50 = iv.parseNullDouble(hargaS50);
        double harga100 = iv.parseNullDouble(hargaS100);
        double diskonBulk = iv.parseNullDouble(hargaDiskonBulk);

        total5 = jumlah5 * harga5;
        total10 = jumlah10 * harga10;
        total20 = jumlah20 * harga20;
        total25 = jumlah25 * harga25;
        total50 = jumlah50 * harga50;
        total100 = jumlah100 * harga100;
        totalBulk = jumlahBulk - (jumlahBulk * diskonBulk / 100);

        tvS5.setText(iv.ChangeToRupiahFormat(total5));
        tvS10.setText(iv.ChangeToRupiahFormat(total10));
        tvS20.setText(iv.ChangeToRupiahFormat(total20));
        tvS25.setText(iv.ChangeToRupiahFormat(total25));
        tvS50.setText(iv.ChangeToRupiahFormat(total50));
        tvS100.setText(iv.ChangeToRupiahFormat(total100));
        tvBulk.setText(iv.ChangeToRupiahFormat(totalBulk));

        totalHarga = total5 + total10 + total20 + total25 + total50 + total100 + totalBulk;

        edtTotal.setText(iv.ChangeToRupiahFormat(totalHarga));
    }

    private void isLoading(boolean status){

        isProses = status;
        if(status){
            pbProses.setVisibility(View.VISIBLE);
        }else{
            pbProses.setVisibility(View.GONE);
        }
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
