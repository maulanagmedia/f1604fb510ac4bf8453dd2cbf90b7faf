package gmedia.net.id.psp.NavPengajuanDeposit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListCCIDDDepositAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListCCIDCBAdapter;
import gmedia.net.id.psp.OrderPerdana.Adapter.ListRentangCCIDAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailCCIDDeposit extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private DialogBox dialogBox;
    private EditText edtOrder, edtTotalCCID, edtTotalHarga;
    private ListView lvPerdana;
    private List<CustomItem> listCCID = new ArrayList<>();
    private List<OptionItem> masterCCID = new ArrayList<>();
    private Button btnScan, btnProses;
    private String idTransaksi = "";
    private ListCCIDDDepositAdapter adapter;
    private SessionManager session;
    private int conter = 0;
    private Button btnAmbilCCIDList;
    private String jumlahBarang = "0";
    private Button btnRentangCCID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cciddeposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;
        dialogBox = new DialogBox(context);
        session = new SessionManager(context);

        setTitle("Scan CCID");

        initUI();
        initEvent();
        getMasterCCID();
    }

    private void initUI() {

        edtOrder = (EditText) findViewById(R.id.edt_order);
        btnAmbilCCIDList = (Button) findViewById(R.id.btn_ambil_ccid_list);
        btnScan = (Button) findViewById(R.id.btn_scan);
        lvPerdana = (ListView) findViewById(R.id.lv_perdana);
        edtTotalCCID = (EditText) findViewById(R.id.edt_total_ccid);
        edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);
        btnRentangCCID = (Button) findViewById(R.id.btn_rentang_ccid);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            idTransaksi = bundle.getString("id", "");
            String order = bundle.getString("order", "");
            jumlahBarang = bundle.getString("jumlah", "0");
            edtOrder.setText(order);
        }

        listCCID = new ArrayList<>();
        adapter = new ListCCIDDDepositAdapter((Activity) context, listCCID);
        lvPerdana.setAdapter(adapter);
    }

    private void initEvent() {

        btnAmbilCCIDList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListCCID();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openScanBarcode();
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(listCCID == null || listCCID.size() == 0){

                    DialogBox.showDialog(context, 3, "Data masih kosong, harap scan barang");
                    return;
                }

                if(listCCID.size() != iv.parseNullInteger(jumlahBarang)){

                    DialogBox.showDialog(context, 3, "Jumlah tidak sesuai dengan pesanan, jumlah pesanan "+ jumlahBarang);
                    return;
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyimpan data ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent data = new Intent();
                                data.putExtra("id", idTransaksi);
                                Gson gson = new Gson();
                                String jsonItems = gson.toJson(listCCID);
                                data.putExtra("data",jsonItems );
                                setResult(RESULT_OK, data);
                                finish();
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

        btnRentangCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadRentangCCID();
            }
        });
    }

    //region ============================================ Ambil dari list =====================================================
    private boolean[] selectedOptionList;
    private void showListCCID() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_ccid, null);
        builder.setView(view);

        final AutoCompleteTextView actvCCIDBA = (AutoCompleteTextView) view.findViewById(R.id.actv_ccid);
        final ListView lvCCIDBA = (ListView) view.findViewById(R.id.lv_ccid);

        selectedOptionList = new boolean[masterCCID.size()];
        List<CustomItem> dataExisting = adapter.getDataList();
        int x = 0;
        for(OptionItem item: masterCCID){

            selectedOptionList[x] = item.isSelected();

            // cek dari data yang terambil, jika sudah pernah ada di list maka di select
            if(item.isSelected()){

                selectedOptionList[x] = false;
                item.setSelected(false);
                for(CustomItem item1 : dataExisting){

                    if(item1.getItem3().equals(item.getText())){

                        selectedOptionList[x] = true;
                        item.setSelected(true);
                        break;
                    }
                }
            }

            x++;
        }

        final List<OptionItem> lastData = new ArrayList<>(masterCCID);

        lastData.add(0,new OptionItem("0","all"));
        ListCCIDCBAdapter adapterCB = new ListCCIDCBAdapter((Activity) context, lastData);
        lvCCIDBA.setAdapter(adapterCB);

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
                    ListCCIDCBAdapter adapterCB = new ListCCIDCBAdapter((Activity) context, lastData);
                    lvCCIDBA.setAdapter(adapterCB);
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

                    ListCCIDCBAdapter adapterCB = new ListCCIDCBAdapter((Activity) context, items);
                    lvCCIDBA.setAdapter(adapterCB);
                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ListCCIDCBAdapter adapterSelected = (ListCCIDCBAdapter) lvCCIDBA.getAdapter();
                List<OptionItem> optionItems = adapterSelected.getItems();

                int x = 0;
                for(OptionItem item: optionItems){

                    if(item.isSelected()){

                        adapter.addData(new CustomItem(
                                item.getValue(),
                                item.getAtt1(),
                                item.getText(),
                                item.getAtt2(),
                                "1",
                                item.getAtt6(),
                                item.getAtt5()));
                    }

                    masterCCID.get(x).setSelected(item.isSelected());
                    selectedOptionList[x] = item.isSelected();
                    x++;
                }
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

    private void getMasterCCID(){

        dialogBox.showDialog(true);
        JSONObject jBody = new JSONObject();
        String nik = session.getNikGA();

        try {
            jBody.put("kodegudang", nik);
            jBody.put("kodebrg", "");
            jBody.put("harga", "");
            jBody.put("id", idTransaksi);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getListCCIDPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                dialogBox.dismissDialog();
                masterCCID.clear();
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterCCID = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterCCID.add(
                                    new OptionItem(
                                            jo.getString("kodebrg")
                                            , jo.getString("ccid")
                                            , jo.getString("namabrg")
                                            , jo.getString("harga")
                                            , jo.getString("hpp")
                                            , jo.getString("tgl_do")
                                            , jo.getString("id")
                                            , jo.getString("nobukti")
                                            , false)
                            );
                        }
                    }

                } catch (JSONException e) {

                    e.printStackTrace();
                    DialogBox.showDialog(context, 2, "Terjadi kesalahan saat memuat data, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                dialogBox.dismissDialog();
                DialogBox.showDialog(context, 2, "Terjadi kesalahan saat memuat data, harap ulangi");
            }
        });
    }

    //endregion

    //region ================================================ range ccid ===========================================

    private boolean[] selectedRentang;
    private void loadRentangCCID(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_rentang_ccid, null);
        builder.setView(view);

        selectedRentang = new boolean[masterCCID.size()];
        final List<OptionItem> rentangCCIDList = new ArrayList<>();
        final EditText edtCCIDAwal = (EditText) view.findViewById(R.id.edt_ccid_awal);
        final EditText edtCCIDAkhir = (EditText) view.findViewById(R.id.edt_ccid_akhir);
        final EditText edtBanyakCCID = (EditText) view.findViewById(R.id.edt_banyak_ccid);
        final Button btnAmbilCCID = (Button) view.findViewById(R.id.btn_ambil_ccid);
        final ListView lvRentangCCID = (ListView) view.findViewById(R.id.lv_rentang_ccid);

        btnAmbilCCID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edtCCIDAwal.getText().length() == 0){
                    edtCCIDAwal.setError("Harap di isi");
                    return;
                }else{
                    edtCCIDAwal.setError(null);
                }

                if(edtCCIDAkhir.getText().length() == 0){
                    edtCCIDAkhir.setError("Harap di isi");
                    return;
                }else{
                    edtCCIDAkhir.setError(null);
                }

                selectedRentang = new boolean[masterCCID.size()];
                for(int i = 0; i < masterCCID.size();i++) {
                    selectedRentang[i] = false;
                }

                long ccidAwal = iv.parseNullLong(edtCCIDAwal.getText().toString());
                long ccidAkhir = iv.parseNullLong(edtCCIDAkhir.getText().toString());
                List<OptionItem> selectedItems = new ArrayList<OptionItem>();

                for(int x = 0; x < masterCCID.size();x++){
                    long selectedCCID = iv.parseNullLong(masterCCID.get(x).getText());
                    if(selectedCCID >= ccidAwal && selectedCCID <= ccidAkhir){
                        selectedRentang[x] = true;
                        selectedItems.add(masterCCID.get(x));
                    }
                }

                edtBanyakCCID.setText(String.valueOf(selectedItems.size()));

                lvRentangCCID.setAdapter(null);
                if(selectedItems != null && selectedItems.size() > 0){
                    ListRentangCCIDAdapter adapter = new ListRentangCCIDAdapter((Activity) context, selectedItems);
                    lvRentangCCID.setAdapter(adapter);
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for(int x = 0; x < selectedRentang.length; x++){

                    if(selectedRentang[x]){
                        OptionItem item = masterCCID.get(x);
                        masterCCID.get(x).setSelected(true);
                        adapter.addData(new CustomItem(
                                item.getValue(),
                                item.getAtt1(),
                                item.getText(),
                                item.getAtt2(),
                                "1",
                                item.getAtt6(),
                                item.getAtt5()));
                    }
                }
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //endregion

    private List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    private void openScanBarcode() {

        Collection<String> ONE_D_CODE_TYPES =
                list("CODE_128B","QR_CODE");

        IntentIntegrator integrator = new IntentIntegrator(DetailCCIDDeposit.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {

                DialogBox.showDialog(context, 3, "Gagal mendapatkan CCID, silahkan ulangi kembali");
            } else {

                getDetailCCID(result.getContents());
                //String[] a = {"xdxxx0350000042506809", "xdxxx0350000042506807", "xdxxx0350000042506808", "xdxxx0350000042506806", "xdxxx0050000371774706"};
                //getDetailCCID(a[conter]);
                conter++;
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getDetailCCID(final String ccid) {

        dialogBox.showDialog(true);
        String nik = session.getNikGA();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("ccid", ccid);
            jBody.put("id", idTransaksi);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCCIDDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                dialogBox.dismissDialog();
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONObject jo = response.getJSONObject("response");
                        adapter.addData(new CustomItem(
                                jo.getString("kodebrg"),
                                jo.getString("namabrg"),
                                jo.getString("ccid"),
                                jo.getString("harga"),
                                jo.getString("jumlah"),
                                jo.getString("nobukti"),
                                jo.getString("id")));
                    }else{
                        DialogBox.showDialog(context,2, "Barang tidak ditemukan di surat jalan hari ini");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    DialogBox.showDialog(context,2, "Terjadi kesalahan saat memuat data, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                dialogBox.dismissDialog();
                DialogBox.showDialog(context, 2, "Terjadi kesalahan saat memuat data, harap ulangi");
            }
        });
    }

    public void updateTotal(){

        if(adapter != null && edtTotalCCID != null && edtTotalHarga != null){

            List<CustomItem> barangSelected = adapter.getDataList();
            edtTotalCCID.setText(String.valueOf(barangSelected.size()));
            double total = 0;
            for(CustomItem item : barangSelected){

                total+= iv.parseNullDouble(item.getItem4());
            }

            edtTotalHarga.setText(iv.ChangeToRupiahFormat(total));
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
        //super.onBackPressed();
        Intent data = new Intent();
        data.putExtra("id", idTransaksi);
        data.putExtra("data","" );
        setResult(RESULT_OK, data);
        finish();

        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
