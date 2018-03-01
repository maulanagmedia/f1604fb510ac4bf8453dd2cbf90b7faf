package gmedia.net.id.psp.PenjualanHariIni;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
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

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.OrderPerdana.DetailOrderPerdana;
import gmedia.net.id.psp.OrderPulsa.DetailOrderPulsa;
import gmedia.net.id.psp.OrderTcash.DetailTcashOrder;
import gmedia.net.id.psp.PenjualanHariIni.Adapter.PenjualanHariIniAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class PenjualanHariIni extends AppCompatActivity {

    private AutoCompleteTextView actvOutlet;
    private ListView lvPenjualan;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private TextView tvTotal;
    private String keyword = "";
    private List<CustomItem> masterList;
    private boolean firstLoad = true;
    private String currentFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_hari_ini);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Penjualan Hari Ini");

        initUI();
    }

    private void initUI() {

        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        lvPenjualan = (ListView) findViewById(R.id.lv_penjualan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        session = new SessionManager(PenjualanHariIni.this);
        keyword = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyword = actvOutlet.getText().toString();
        getPenjualanHariIni();
    }

    private void getPenjualanHariIni() {

        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(PenjualanHariIni.this, jBody, "POST", ServerURL.getPenjualanHariIni, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();

                    long total = 0, totalPerNama = 0;
                    String nama = "";

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);

                            masterList.add(new CustomItem("I", jo.getString("nonota"), jo.getString("nama"), jo.getString("piutang"), jo.getString("flag"), jo.getString("tgl"), jo.getString("app_flag"), jo.getString("jarak"), jo.getString("status")));
                            if(!jo.getString("flag").toUpperCase().equals("MKIOS_TR") && !jo.getString("flag").toUpperCase().equals("TCASH_TR")){
                                total += iv.parseNullLong(jo.getString("piutang"));
                            }

                            if(i < items.length() - 1){

                                JSONObject jo2 = items.getJSONObject(i+1);
                                if(jo2.getString("kdcus").equals(jo.getString("kdcus"))){

                                    if(!jo.getString("flag").toUpperCase().equals("MKIOS_TR") && !jo.getString("flag").toUpperCase().equals("TCASH_TR")){
                                        totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                    }
                                }else{

                                    if(!jo.getString("flag").toUpperCase().equals("MKIOS_TR") && !jo.getString("flag").toUpperCase().equals("TCASH_TR")){
                                        totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                        masterList.add(new CustomItem("F", String.valueOf(totalPerNama), jo.getString("flag")));
                                        totalPerNama = 0;
                                    }
                                }
                            }else{

                                if(!jo.getString("flag").toUpperCase().equals("MKIOS_TR") && !jo.getString("flag").toUpperCase().equals("TCASH_TR")){
                                    totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                    masterList.add(new CustomItem("F", String.valueOf(totalPerNama), jo.getString("flag")));
                                    totalPerNama = 0;
                                }
                            }
                        }
                    }

                    tvTotal.setText(iv.ChangeToRupiahFormat(total));
                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getAutocompleteEvent(tableList);
                    getTableList(tableList);
                    pbProses.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                    getTableList(null);
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getAutocompleteEvent(null);
                getTableList(null);
                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){

            firstLoad = false;
            actvOutlet.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvOutlet.getText().length() == 0){

                        keyword = "";
                        getPenjualanHariIni();
                    }
                }
            });
        }

        actvOutlet.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvOutlet.getText().toString();
                    getPenjualanHariIni();

                    iv.hideSoftKey(PenjualanHariIni.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPenjualan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            List<CustomItem> bufferList = new ArrayList<>();
            List<CustomItem> mkiosTrList = new ArrayList<>();
            List<CustomItem> mkiosList = new ArrayList<>();
            List<CustomItem> gaList = new ArrayList<>();
            List<CustomItem> tcashTrList = new ArrayList<>();
            List<CustomItem> tcashList = new ArrayList<>();

            // Get MKIOS & GA
            for(CustomItem itemApPosition:tableList){

                if(itemApPosition.getItem1().toUpperCase().equals("I")){

                    if(itemApPosition.getItem5().toUpperCase().equals("MKIOS")){
                        mkiosList.add(itemApPosition);
                    }else if(itemApPosition.getItem5().toUpperCase().equals("MKIOS_TR")){
                        mkiosTrList.add(itemApPosition);
                    }else if(itemApPosition.getItem5().toUpperCase().equals("GA")){
                        gaList.add(itemApPosition);
                    }else if(itemApPosition.getItem5().toUpperCase().equals("TCASH")){
                        tcashList.add(itemApPosition);
                    }else if(itemApPosition.getItem5().toUpperCase().equals("TCASH_TR")){
                        tcashTrList.add(itemApPosition);
                    }
                }else if(itemApPosition.getItem1().toUpperCase().equals("F")){

                    if(itemApPosition.getItem3().toUpperCase().equals("MKIOS")){
                        mkiosList.add(itemApPosition);
                    }else if(itemApPosition.getItem3().toUpperCase().equals("MKIOS_TR")){
                        mkiosTrList.add(itemApPosition);
                    }else if(itemApPosition.getItem3().toUpperCase().equals("GA")){
                        gaList.add(itemApPosition);
                    }else if(itemApPosition.getItem3().toUpperCase().equals("TCASH")){
                        tcashList.add(itemApPosition);
                    }else if(itemApPosition.getItem3().toUpperCase().equals("TCASH_TR")){
                        tcashTrList.add(itemApPosition);
                    }
                }

            }

            if(mkiosTrList.size() > 0){
                bufferList.add(new CustomItem("H", "Transaksi MKIOS"));
                bufferList.addAll(mkiosTrList);
            }

            if(tcashTrList.size() > 0){
                bufferList.add(new CustomItem("H", "Transaksi TCash"));
                bufferList.addAll(tcashTrList);
            }

            if(mkiosList.size() > 0){
                bufferList.add(new CustomItem("H", "Penjualan MKIOS"));
                bufferList.addAll(mkiosList);
            }

            if(gaList.size() > 0){
                bufferList.add(new CustomItem("H", "Penjualan Perdana"));
                bufferList.addAll(gaList);
            }

            if(tcashList.size() > 0){
                bufferList.add(new CustomItem("H", "Penjualan TCash"));
                bufferList.addAll(tcashList);
            }

            PenjualanHariIniAdapter adapter = new PenjualanHariIniAdapter(PenjualanHariIni.this, bufferList);
            lvPenjualan.setAdapter(adapter);

            lvPenjualan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    if(selectedItem.getItem1().equals("I")){

                        currentFlag = selectedItem.getItem5().equals("MKIOS_TR") ? "MKIOS" : (selectedItem.getItem5().equals("TCASH_TR") ? "TCASH" : selectedItem.getItem5());

                        getDetailPenjualan(selectedItem.getItem2(), currentFlag, selectedItem.getItem8());
                    }
                }
            });
        }
    }

    private void getDetailPenjualan(String nonota, final String flag, final String jarak) {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nonota", nonota);
            jBody.put("flag", flag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(PenjualanHariIni.this, jBody, "POST", ServerURL.getDetailPenjualanHariIni, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    pbProses.setVisibility(View.GONE);
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    long total = 0, totalPerNama = 0;
                    String nama = "";

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");

                        if(items.length() > 0){
                            for(int i  = 0; i < items.length(); i++){

                                JSONObject jo = items.getJSONObject(i);

                                if(flag.equals("GA")){
                                    Intent intent = new Intent(PenjualanHariIni.this, DetailOrderPerdana.class);
                                    intent.putExtra("nobukti", jo.getString("nobukti"));
                                    intent.putExtra("suratjalan", jo.getString("surat_jalan"));
                                    intent.putExtra("nocus", jo.getString("kdcus"));
                                    intent.putExtra("namacus", jo.getString("nama"));
                                    intent.putExtra("notelpcus", jo.getString("notelp"));
                                    intent.putExtra("kodebrg", jo.getString("kodebrg"));
                                    intent.putExtra("namabrg", jo.getString("namabrg"));
                                    intent.putExtra("crbayar", jo.getString("crbayar"));
                                    intent.putExtra("tempo", jo.getString("tempo_asli"));
                                    intent.putExtra("kdgudang", jo.getString("kodegudang"));
                                    intent.putExtra("harga", jo.getString("harga"));
                                    intent.putExtra("noba", jo.getString("no_ba"));
                                    intent.putExtra("status", jo.getString("status"));
                                    intent.putExtra("jarak", jarak);
                                    startActivity(intent);
                                    break;
                                }else if (flag.equals("MKIOS")){
                                    Intent intent = new Intent(PenjualanHariIni.this, DetailOrderPulsa.class);
                                    intent.putExtra("nonota", jo.getString("nonota"));
                                    intent.putExtra("flag", jo.getString("flag"));
                                    intent.putExtra("koders", jo.getString("kode"));
                                    intent.putExtra("kode_cv", jo.getString("kode_cv"));
                                    intent.putExtra("jarak", jarak);
                                    startActivity(intent);
                                    break;
                                }else if(flag.equals("TCASH")){
                                    Intent intent = new Intent(PenjualanHariIni.this, DetailTcashOrder.class);
                                    intent.putExtra("nonota", jo.getString("nonota"));
                                    intent.putExtra("koders", jo.getString("kode"));
                                    startActivity(intent);
                                }
                            }
                        }else{
                            Toast.makeText(PenjualanHariIni.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(PenjualanHariIni.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PenjualanHariIni.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                Toast.makeText(PenjualanHariIni.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
                pbProses.setVisibility(View.GONE);
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
        Intent intent = new Intent(PenjualanHariIni.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
