package gmedia.net.id.psp.DaftarPiutang;

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

import gmedia.net.id.psp.DaftarPiutang.Adapter.PiutangPerOutletAdapter;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.OrderPerdana.DetailOrderPerdana;
import gmedia.net.id.psp.OrderPulsa.DetailOrderPulsa;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class PiutangPerOutlet extends AppCompatActivity {

    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private ListView lvPiutang;
    private AutoCompleteTextView actvOutlet;
    private ProgressBar pbProses;
    private List<CustomItem> masterList;
    private String keyword = "";
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piutang_per_outlet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Daftar Piutang");

        initUI();
    }

    private void initUI() {

        lvPiutang = (ListView) findViewById(R.id.lv_piutang);
        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        session = new SessionManager(PiutangPerOutlet.this);
        firstLoad = true;

        getData();
    }

    private void getData() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(PiutangPerOutlet.this, jBody, "POST", ServerURL.getPiutangOutlet, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();
                    String lastNama = "";
                    long total = 0, totalPerNama = 0;

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);


                            if(!lastNama.equals(jo.getString("nama"))){

                                masterList.add(new CustomItem("H", jo.getString("nama")));
                                lastNama = jo.getString("nama");
                            }

                            masterList.add(new CustomItem("I", jo.getString("nama"), jo.getString("nonota"), jo.getString("total"),iv.ChangeFormatDateString(jo.getString("tgl"), FormatItem.formatDate, FormatItem.formatDateDisplay)));
                            total += iv.parseNullLong(jo.getString("total"));

                            if(i < items.length() - 1){

                                JSONObject jo2 = items.getJSONObject(i+1);
                                if(jo2.getString("kdcus").equals(jo.getString("kdcus"))){

                                    totalPerNama += iv.parseNullLong(jo.getString("total"));
                                }else{

                                    totalPerNama += iv.parseNullLong(jo.getString("total"));
                                    masterList.add(new CustomItem("F", String.valueOf(totalPerNama)));
                                    totalPerNama = 0;
                                }
                            }else{

                                totalPerNama += iv.parseNullLong(jo.getString("total"));
                                masterList.add(new CustomItem("F", String.valueOf(totalPerNama)));
                                totalPerNama = 0;
                            }
                        }
                    }

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
                        getData();
                    }
                }
            });
        }

        actvOutlet.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvOutlet.getText().toString();
                    getData();

                    iv.hideSoftKey(PiutangPerOutlet.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPiutang.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            PiutangPerOutletAdapter adapterPiutang = new PiutangPerOutletAdapter(PiutangPerOutlet.this, tableList);
            lvPiutang.setAdapter(adapterPiutang);

            lvPiutang.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    if(selectedItem.getItem1().equals("I")){

                        String flag = "MKIOS";
                        if(selectedItem.getItem3().toUpperCase().contains("PB")) flag = "GA";
                        getDetailPenjualan(selectedItem.getItem3(), flag);
                    }
                }
            });
        }
    }

    //TODO: get detail order Mkios / GA
    private void getDetailPenjualan(String nonota, final String flag) {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nonota", nonota);
            jBody.put("flag", flag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(PiutangPerOutlet.this, jBody, "POST", ServerURL.getDetailPenjualanHariIni, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                                    Intent intent = new Intent(PiutangPerOutlet.this, DetailOrderPerdana.class);
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
                                    startActivity(intent);
                                    break;
                                }else if (flag.equals("MKIOS")){
                                    Intent intent = new Intent(PiutangPerOutlet.this, DetailOrderPulsa.class);
                                    intent.putExtra("nonota", jo.getString("nonota"));
                                    intent.putExtra("flag", jo.getString("flag"));
                                    intent.putExtra("koders", jo.getString("kode"));
                                    startActivity(intent);
                                    break;
                                }
                            }
                        }else{
                            Toast.makeText(PiutangPerOutlet.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(PiutangPerOutlet.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PiutangPerOutlet.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                Toast.makeText(PiutangPerOutlet.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(PiutangPerOutlet.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
