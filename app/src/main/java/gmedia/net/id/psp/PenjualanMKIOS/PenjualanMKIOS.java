package gmedia.net.id.psp.PenjualanMKIOS;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
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

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.MainActivity;
import gmedia.net.id.psp.OrderPulsa.DetailOrderPulsa;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.PenjualanMKIOS.Adapter.ListPenjualanMKIOSAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class PenjualanMKIOS extends AppCompatActivity {

    private ListView lvPenjualan;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvPelanggan;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private FloatingActionButton btnAdd;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_mkios);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Penjualan MKIOS");

        initUI();
    }

    private void initUI() {

        lvPenjualan = (ListView) findViewById(R.id.lv_penjualan);
        actvPelanggan = (AutoCompleteTextView) findViewById(R.id.actv_pelanggan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        btnAdd = (FloatingActionButton) findViewById(R.id.btn_add);

        session = new SessionManager(PenjualanMKIOS.this);
        getData();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PenjualanMKIOS.this, ListReseller.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void getData() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        ApiVolley request = new ApiVolley(PenjualanMKIOS.this, new JSONObject(), "GET", ServerURL.getMkios+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("nonota"), jo.getString("nama"), jo.getString("nomor"), jo.getString("voucher"), jo.getString("total"), jo.getString("status"), jo.getString("flag"), jo.getString("kode")));
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

            actvPelanggan.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvPelanggan.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvPelanggan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvPelanggan.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTableList(items);
                    iv.hideSoftKey(PenjualanMKIOS.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPenjualan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListPenjualanMKIOSAdapter adapter = new ListPenjualanMKIOSAdapter(PenjualanMKIOS.this, tableList);
            lvPenjualan.setAdapter(adapter);

            lvPenjualan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    Intent intent = new Intent(PenjualanMKIOS.this, DetailOrderPulsa.class);
                    intent.putExtra("nonota", selectedItem.getItem1());
                    intent.putExtra("flag", selectedItem.getItem7());
                    intent.putExtra("koders", selectedItem.getItem8());
                    startActivity(intent);
                }
            });
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

        Intent intent = new Intent(PenjualanMKIOS.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
