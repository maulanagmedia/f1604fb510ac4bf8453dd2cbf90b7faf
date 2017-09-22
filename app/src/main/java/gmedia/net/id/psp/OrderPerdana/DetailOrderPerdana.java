package gmedia.net.id.psp.OrderPerdana;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDCBAdapter;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailOrderPerdana extends AppCompatActivity {

    private EditText edtSuratJalan, edtCCID, edtNamaBarangScan, edtHargaScan;
    private ImageView ivSuratJalan, ivAddCCID;
    private static ItemValidation iv = new ItemValidation();
    private List<OptionItem> suratJalanList;
    private int SuratjalanSelected = 0;
    private final String TAG = "DetailOrderPerdana";
    private String suratJalan = "";
    private EditText edtNobukti;
    private static List<CustomItem> ccidList = new ArrayList<>();
    private static List<OptionItem> masterCCID = new ArrayList<>();
    private static ListView lvCCID;
    private EditText edtNamaBarang, edtHarga;
    private String noCus = "", namaCus = "", kdBrg = "", namaBrg = "", crBayar = "", tempo = "", kdGudang = "", hargaBrg = "";
    private SessionManager session;
    private LinearLayout llScanCCID;
    private ProgressBar pbLoading;
    private boolean isProses = false;
    private Button btnUbahHarga;
    private static EditText edtTotalHarga;
    private static double totalHarga = 0;
    private Button btnAmbilCCIDList, btnScanCCID;
    private boolean firstLoad = true;
    private String noBukti = "";
    private Button btnProses;
    private String notelpCus = "";
    private String noBa = "", status = "";
    private boolean editMode = false;
    private List<CustomItem> listCCIDLama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order_perdana);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Order Perdana");

        initUI();
    }

    private void initUI() {

        edtNamaBarang = (EditText) findViewById(R.id.edt_nama_barang);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        edtSuratJalan = (EditText) findViewById(R.id.edt_surat_jalan);
        ivSuratJalan = (ImageView) findViewById(R.id.iv_suran_jalan);
        edtNobukti = (EditText) findViewById(R.id.edt_nobukti);
        edtCCID = (EditText) findViewById(R.id.edt_ccid);
        ivAddCCID = (ImageView) findViewById(R.id.iv_add_ccid);
        edtNamaBarangScan = (EditText) findViewById(R.id.edt_nama_barang_scan);
        edtHargaScan = (EditText) findViewById(R.id.edt_harga_scan);
        btnUbahHarga = (Button) findViewById(R.id.btn_ubah_harga);

        lvCCID = (ListView) findViewById(R.id.lv_perdana);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        btnAmbilCCIDList = (Button) findViewById(R.id.btn_ambil_ccid_list);
        btnScanCCID =(Button) findViewById(R.id.btn_scan_ccid);

        llScanCCID = (LinearLayout) findViewById(R.id.ll_scan_ccid);

        edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);

        isProses = false;
        totalHarga = 0;
        session = new SessionManager(DetailOrderPerdana.this);
        Bundle bundle = getIntent().getExtras();
        ccidList = new ArrayList<>();
        if(bundle != null){

            suratJalan = bundle.getString("suratjalan");
            noCus = bundle.getString("nocus");
            namaCus = bundle.getString("namacus");
            notelpCus = bundle.getString("notelpcus");
            kdBrg = bundle.getString("kodebrg");
            namaBrg = bundle.getString("namabrg");
            crBayar = bundle.getString("crbayar");
            tempo = bundle.getString("tempo");
            kdGudang = bundle.getString("kdgudang");
            hargaBrg = bundle.getString("harga");
            noBa = bundle.getString("noba");

            edtNamaBarang.setText(namaBrg);
            edtHarga.setText(hargaBrg);
            edtSuratJalan.setText(suratJalan);

            ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this,ccidList);
            updateHargaTotal();
            lvCCID.setAdapter(adapter);

            noBukti = bundle.getString("nobukti");
            if(noBukti != null && noBukti.length() > 0 ){

                editMode = true;
                edtNobukti.setText(noBukti);
                status = bundle.getString("status");
            }else{

                editMode = false;
                noBukti = "";
                getNobukti();
            }

            getMasterCCID();
            initEvent();
        }
    }

    private void getNobukti() {

        isLoading(true);
        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, new JSONObject(), "GET", ServerURL.getPerdanaNonota +noCus, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        noBukti = response.getJSONObject("response").getString("nonota");
                        edtNobukti.setText(noBukti);
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

        edtHarga.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                double hargaBaru = iv.parseNullDouble(edtHarga.getText().toString());
                if(hargaBaru < iv.parseNullDouble(hargaBrg)){
                   edtHarga.setError("Harga minimal "+iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)));
                }else{
                    edtHarga.setError(null);
                }
            }
        });

        edtHargaScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                double harga = iv.parseNullDouble(edtHargaScan.getText().toString());

                if(hargaBrg == null){
                    edtHargaScan.setError("Data harga belum termuat");
                }else{
                    if( harga < iv.parseNullDouble(hargaBrg)){
                        edtHargaScan.setError("Harga minimal "+ iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)));
                    }else{
                        edtHargaScan.setError(null);
                    }
                }
            }
        });

        btnUbahHarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtCCID.getText().length() == 0){

                    Snackbar.make(findViewById(android.R.id.content), "CCID masih kosong", Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                if(hargaBrg == null || masterCCID == null){
                    Snackbar.make(findViewById(android.R.id.content), "Harap tunggu semua data termuat", Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                double hargaDouble = iv.parseNullDouble(edtHargaScan.getText().toString());
                if(hargaDouble < iv.parseNullDouble(hargaBrg)){
                    Snackbar.make(findViewById(android.R.id.content), "Harga minimal " + iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)), Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                    return;
                }

                String ccid = edtCCID.getText().toString();
                String harga = edtHargaScan.getText().toString();

                int x = 0;
                for (OptionItem item: masterCCID){

                    if(item.getText().equals(ccid)){

                        masterCCID.get(x).setAtt2(harga);
                        break;
                    }
                    x++;
                }

                ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
                adapter.changeData(ccid, harga);
                updateHargaTotal();
                iv.hideSoftKey(DetailOrderPerdana.this);

                Toast.makeText(DetailOrderPerdana.this, "Data berhasil diubah", Toast.LENGTH_LONG).show();
            }
        });

        lvCCID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                edtCCID.setText(item.getItem2());
                edtNamaBarangScan.setText(item.getItem3());
                edtHargaScan.setText(item.getItem4());
                edtCCID.requestFocus();
                edtHargaScan.requestFocus();
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = "Apakah anda yakin ingin memproses data?";
                if(editMode) message = "Apakah anda yakin ingin mengubah "+noBukti+" ?";
                AlertDialog builder = new AlertDialog.Builder(DetailOrderPerdana.this)
                        .setTitle("Konfirmasi")
                        .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                        .setMessage(message)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                validateSaveData();
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

    private void validateSaveData() {

        if(isProses){

            Snackbar.make(findViewById(android.R.id.content), "Tunggu Hingga Proses Selesai", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(editMode && status.equals("3")){

            Snackbar.make(findViewById(android.R.id.content), "Tidak dapat diubah karena penjualan sudah diproses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(iv.parseNullDouble(edtHarga.getText().toString()) < iv.parseNullDouble(hargaBrg)){

            Snackbar.make(findViewById(android.R.id.content), "Harga minimal "+ iv.ChangeToRupiahFormat(iv.parseNullDouble(hargaBrg)), Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            edtHarga.requestFocus();
            return;
        }

        if(edtSuratJalan.getText().length() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Surat Jalan tidak termuat, harap ulangi proses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        if(edtNobukti.getText().length() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Nomor nota tidak termuat, harap ulangi proses", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
        List<CustomItem> listItem = adapter.getDataList();

        if(listItem == null || listItem.size() == 0){

            Snackbar.make(findViewById(android.R.id.content), "Tidak ada CCID yang terpilih", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
            return;
        }

        saveData(listItem);
    }

    private void saveData(List<CustomItem> listItem) {

        // Jual D lama
        JSONArray jualDLama = new JSONArray();

        if(editMode){

            for(CustomItem item: listCCIDLama){
                JSONObject joLama = new JSONObject();

                try {
                    joLama.put("nobukti", noBukti);
                    joLama.put("kodebrg", item.getItem1());
                    joLama.put("ccid", item.getItem2());
                    joLama.put("harga", item.getItem4());
                    joLama.put("jumlah", "1");
                    joLama.put("hpp", item.getItem5());
                    joLama.put("nopengeluaran", suratJalan);
                    joLama.put("tgl_do", item.getItem6());
                    joLama.put("nodo", item.getItem7());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jualDLama.put(joLama);
            }
        }

        // JUAL D
        JSONArray jualD = new JSONArray();

        double totalHPP = 0;
        for(CustomItem item: listItem){

            JSONObject jo = new JSONObject();

            try {
                jo.put("nobukti", noBukti);
                jo.put("kodebrg", item.getItem1());
                jo.put("ccid", item.getItem2());
                jo.put("harga", item.getItem4());
                jo.put("jumlah", "1");
                jo.put("hpp", item.getItem5());
                jo.put("nopengeluaran", suratJalan);
                jo.put("tgl_do", item.getItem6());
                jo.put("nodo", item.getItem7());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jualD.put(jo);
            totalHPP += iv.parseNullDouble(item.getItem5());
        }

        // JUAL H
        JSONObject jualH = new JSONObject();
        try {
            jualH.put("nobukti", noBukti);
            jualH.put("tgl", iv.getCurrentDate(FormatItem.formatDate));
            jualH.put("kdcus", noCus);
            jualH.put("tgltempo", (crBayar.equals("T")) ? iv.getCurrentDate(FormatItem.formatDate) : iv.sumDate(iv.getCurrentDate(FormatItem.formatDate), iv.parseNullInteger(tempo), FormatItem.formatDate));
            jualH.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jualH.put("total", iv.doubleToStringRound(totalHarga));
            jualH.put("totalhpp", iv.doubleToStringRound(totalHPP));
            jualH.put("keterangan", namaCus + " | " + notelpCus + " | " + session.getUserInfo(SessionManager.TAG_FLAG) + " " + session.getUserInfo(SessionManager.TAG_NAMA));
            jualH.put("userid", session.getUserInfo(SessionManager.TAG_USERNAME));
            jualH.put("status", "1");
            jualH.put("nomutasi", suratJalan);
            jualH.put("crbayar", crBayar);
            jualH.put("kode_lokasi", session.getUserInfo(SessionManager.TAG_AREA));
            jualH.put("no_ba", noBa);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Post Piurang
        JSONObject postPiutang = new JSONObject();
        try {
            postPiutang.put("nonota", noBukti);
            postPiutang.put("tgl", iv.getCurrentDate(FormatItem.formatDate));
            postPiutang.put("tgltempo", (crBayar.equals("T")) ? iv.getCurrentDate(FormatItem.formatDate) : iv.sumDate(iv.getCurrentDate(FormatItem.formatDate), iv.parseNullInteger(tempo), FormatItem.formatDate));
            postPiutang.put("kdcus", noCus);
            postPiutang.put("kdsales", session.getUserInfo(SessionManager.TAG_UID));
            postPiutang.put("piutang", iv.doubleToStringRound(totalHarga));
            postPiutang.put("keterangan", namaBrg);
            postPiutang.put("crbayar", crBayar);
            postPiutang.put("kode_lokasi", session.getUserInfo(SessionManager.TAG_AREA));
            postPiutang.put("jml", String.valueOf(listItem.size()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String method = (editMode) ? "PUT" : "POST";
        String url = ServerURL.savePerdana;
        JSONObject jBody = new JSONObject();
        try {

            if(editMode){
                jBody.put("nobukti", noBukti);
                jBody.put("jual_d_lama", jualDLama);
            }
            jBody.put("jual_d", jualD);
            jBody.put("jual_h", jualH);
            jBody.put("post_piutang", postPiutang);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        isLoading(true);
        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, method, url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = "Order Pulsa berhasil ditambahkan";
                        if(editMode) message = "Order "+ noBukti + " berhasil diubah";
                        isLoading(false);
                        Toast.makeText(DetailOrderPerdana.this, message, Toast.LENGTH_LONG).show();
                        //Snackbar.make(findViewById(android.R.id.content), "Order Pulsa berhasil ditambahkan", Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(DetailOrderPerdana.this, PenjualanPerdana.class);
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

    private void getMasterCCID(){

        masterCCID = new ArrayList<>();
        isLoading(true);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kodegudang", kdGudang);
            jBody.put("kodebrg", kdBrg);
            jBody.put("harga", hargaBrg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, "POST", ServerURL.getListCCIDPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterCCID.add(new OptionItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo") , false));
                        }
                    }
                    isLoading(false);
                    if(editMode){
                        getSelectedCCID();
                    }else{
                        setRadioButtonEvent();
                    }

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

    private void getSelectedCCID(){

        ccidList = new ArrayList<>();
        listCCIDLama = new ArrayList<>();
        isLoading(true);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nonota", noBukti);
            jBody.put("kodegudang", kdGudang);
            jBody.put("kodebrg", kdBrg);
            jBody.put("harga", hargaBrg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrderPerdana.this, jBody, "POST", ServerURL.getSelectedCCIDPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            ccidList.add(new CustomItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo")));
                            listCCIDLama.add(new CustomItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo")));
                            masterCCID.add(i, new OptionItem(jo.getString("kodebrg"), jo.getString("ccid"), jo.getString("namabrg"), jo.getString("harga"), jo.getString("hpp"), jo.getString("tgl_do"), jo.getString("nodo") , true));
                        }
                    }

                    ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                    lvCCID.setAdapter(adapter);
                    updateHargaTotal();
                    setRadioButtonEvent();
                    isLoading(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading(false);
                    setRadioButtonEvent();
                }
            }

            @Override
            public void onError(String result) {

                isLoading(false);
                setRadioButtonEvent();
            }
        });
    }

    private void setRadioButtonEvent(){

        btnAmbilCCIDList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListCCID();
            }
        });

        btnScanCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edtCCID.setText("");
                edtNamaBarangScan.setText("");
                edtHargaScan.setText("");
                openScanBarcode();
            }
        });
    }

    private void isLoading(boolean status){
        isProses = status;
        if(isProses){
            pbLoading.setVisibility(View.VISIBLE);
        }else{
            pbLoading.setVisibility(View.GONE);
        }
    }

    private void openScanBarcode() {

        IntentIntegrator integrator = new IntentIntegrator(DetailOrderPerdana.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {

                Log.d(TAG, "onActivityResult: Scan failed ");
            } else {

                updateCCID(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateCCID(final String ccid){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String finalCCID = "";
                CustomItem barang = null;
                boolean found = false;
                try {

                    finalCCID = ccid.substring(5,21);
                    //finalCCID = "0640000028317945";

                    int x = 0;
                    for(OptionItem item: masterCCID){

                        if(item.getText().equals(finalCCID)){
                            barang = new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2(), item.getAtt3(), item.getAtt4(), item.getAtt5());
                            found = true;
                            break;
                        }
                        x++;
                    }

                    //finalCCID = ccid;
                }catch (Exception e){
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), "CCID tidak terdeteksi, harap scan ulang",
                            Snackbar.LENGTH_LONG).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }

                if(found){

                    edtCCID.setText(finalCCID);
                    edtNamaBarangScan.setText(barang.getItem3());
                    edtHargaScan.setText(barang.getItem4());
                    if(finalCCID.length() > 0){
                        updateList(barang);
                    }
                }else{

                    Snackbar.make(findViewById(android.R.id.content), "CCID tidak terdeteksi, harap scan ulang",
                            Snackbar.LENGTH_LONG).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }
            }
        });
    }

    private void updateList(CustomItem itemToAdd) {

        boolean isExist = false;
        for(CustomItem item : ccidList){

            if(item.getItem2().equals(itemToAdd.getItem2())) isExist = true;
        }

        if(!isExist) {

            ccidList.add(0,itemToAdd);
            ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();
            adapter.notifyDataSetChanged();
            updateHargaTotal();

            int x = 0;
            for(OptionItem item:masterCCID){

                if(item.getText().equals(itemToAdd.getItem2())){
                    masterCCID.get(x).setSelected(true);
                    //selectedOptionList[x+1] = true;
                }
                x++;
            }
        }else{

            Snackbar.make(findViewById(android.R.id.content), "CCID sudah ada", Snackbar.LENGTH_LONG)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
        }
    }

    public static void deleteSelectedCCID(String ccid){

        int x = 0;
        for(OptionItem item: masterCCID){

            if(ccid.equals(item.getText())){

                masterCCID.get(x).setSelected(false);
                break;
            }
            x++;
        }
        updateHargaTotal();
    }

    private boolean[] selectedOptionList;
    private void showListCCID() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderPerdana.this, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_ccid, null);
        builder.setView(view);

        final AutoCompleteTextView actvCCIDBA = (AutoCompleteTextView) view.findViewById(R.id.actv_ccid);
        final ListView lvCCIDBA = (ListView) view.findViewById(R.id.lv_ccid);

        selectedOptionList = new boolean[masterCCID.size()];
        int x = 0;
        for(OptionItem item: masterCCID){
            selectedOptionList[x] = item.isSelected();
            x++;
        }

        final List<OptionItem> lastData = new ArrayList<>(masterCCID);

        lastData.add(0,new OptionItem("0","all"));
        ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, lastData);
        lvCCIDBA.setAdapter(adapter);

        actvCCIDBA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(actvCCIDBA.getText().length() <= 0){

                    lvCCIDBA.setAdapter(null);
                    ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, lastData);
                    lvCCIDBA.setAdapter(adapter);
                }
            }
        });

        actvCCIDBA.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<OptionItem> items = new ArrayList<OptionItem>();
                    String keyword = actvCCIDBA.getText().toString().trim().toUpperCase();

                    for (OptionItem item: lastData){

                        if(item.getText().toUpperCase().contains(keyword)) items.add(item);
                    }

                    ListCCIDCBAdapter adapter = new ListCCIDCBAdapter(DetailOrderPerdana.this, items);
                    lvCCIDBA.setAdapter(adapter);
                    iv.hideSoftKey(DetailOrderPerdana.this);
                    return true;
                }

                return false;
            }
        });

        /*final CharSequence[] choiceList = new CharSequence[masterCCID.size()+1];
        selectedOptionList = new boolean[masterCCID.size()+1];
        choiceList[0] = "All";
        boolean checkAll = true;
        for(int x = 1; x <= masterCCID.size();x++){
            choiceList[x] = x +". "+ masterCCID.get(x-1).toString();
            selectedOptionList[x] = masterCCID.get(x-1).isSelected();
            if(!masterCCID.get(x-1).isSelected()) checkAll = masterCCID.get(x-1).isSelected();
        }
        selectedOptionList[0] = checkAll;

        builder.setMultiChoiceItems(choiceList, selectedOptionList, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                if(i == 0){

                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    ListView v = dialog.getListView();
                    int x = 1;
                    selectedOptionList[i] = b;
                    while(x < choiceList.length) {
                        v.setItemChecked(x, b);
                        //masterCCID.get(x-1).setSelected(b);
                        selectedOptionList[x] = b;
                        x++;
                    }
                }else{

                    //masterCCID.get(i-1).setSelected(b);
                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    ListView v = dialog.getListView();
                    int x = 1;
                    selectedOptionList[i] = b;
                    boolean checkALL = true;

                    while(x < choiceList.length) {
                        if(!selectedOptionList[x]) checkALL = false;
                        x++;
                    }
                    selectedOptionList[0] = checkALL;
                    v.setItemChecked(0, checkALL);
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int x = 1; x <= masterCCID.size();x++){
                    masterCCID.get(x-1).setSelected(selectedOptionList[x]);
                }

                ccidList = new ArrayList<CustomItem>();
                for(OptionItem item: masterCCID){

                    if(item.isSelected()){

                        ccidList.add(new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2()));
                    }
                }

                ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                lvCCID.setAdapter(null);
                lvCCID.setAdapter(adapter);
                updateHargaTotal();
            }
        });*/

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ListCCIDCBAdapter adapterSelected = (ListCCIDCBAdapter) lvCCIDBA.getAdapter();
                List<OptionItem> optionItems = adapterSelected.getItems();

                ccidList = new ArrayList<CustomItem>();

                int x = 0;
                for(OptionItem item: optionItems){

                    if(item.isSelected()){

                        ccidList.add(new CustomItem(item.getValue(), item.getText(), item.getAtt1(), item.getAtt2(), item.getAtt3(), item.getAtt4(), item.getAtt5()));
                    }

                    masterCCID.get(x).setSelected(item.isSelected());
                    selectedOptionList[x] = item.isSelected();
                    x++;
                }

                ListCCIDAdapter adapter = new ListCCIDAdapter(DetailOrderPerdana.this, ccidList);
                lvCCID.setAdapter(null);
                lvCCID.setAdapter(adapter);
                updateHargaTotal();
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int x = 0;
                for(OptionItem item: masterCCID){

                    masterCCID.get(x).setSelected(selectedOptionList[x]);
                    x++;
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private static void updateHargaTotal(){

        ListCCIDAdapter adapter = (ListCCIDAdapter) lvCCID.getAdapter();

        totalHarga = 0;

        if(adapter != null && adapter.getDataList() != null){
            final List<CustomItem> items = new ArrayList<>(adapter.getDataList());

            for(CustomItem item: items){

                totalHarga += iv.parseNullDouble(item.getItem4());
            }
        }

        edtTotalHarga.setText(iv.ChangeToRupiahFormat(totalHarga));
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
