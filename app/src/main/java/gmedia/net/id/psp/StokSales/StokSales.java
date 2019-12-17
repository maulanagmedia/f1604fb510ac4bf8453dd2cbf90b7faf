package gmedia.net.id.psp.StokSales;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.AbsListView;
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
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.StokSales.Adapter.ListStokDetailAdapter;
import gmedia.net.id.psp.Utils.ServerURL;

public class StokSales extends AppCompatActivity {

    private ListView lvStok;
    private List<CustomItem> masterList;
    private HashMap<String, List<CustomItem>> detailList;
    private AutoCompleteTextView actvBarang;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private SessionManager session;
    private int startIndex = 0, count = 0;
    private String keyword = "";
    private boolean isLoading = false;
    private final String TAG = "TAG";
    private View footerList;
    private ListStokDetailAdapter adapterStok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stok_sales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Informasi Stok");

        initUI();
    }

    private void initUI() {

        lvStok = (ListView) findViewById(R.id.lv_stok);
        actvBarang = (AutoCompleteTextView) findViewById(R.id.actv_barang);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);
        keyword = "";
        session = new SessionManager(StokSales.this);
        getData();

        lvStok.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvStok.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvStok.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();
                    Log.i(TAG, "onScroll: last");
                }
            }
        });
    }

    private void getData() {

        masterList = new ArrayList<>();
        detailList = new HashMap<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("keyword", keyword);
            jBody.put("startindex", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(StokSales.this, jBody, "POST", ServerURL.getStokDetail+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();
                    detailList = new HashMap<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("nobukti"), jo.getString("namabrg"), jo.getString("jumlah"), jo.getString("harga"), jo.getString("jumlah_lama"), jo.getString("id")));

                            JSONArray details = jo.getJSONArray("detail");
                            List<CustomItem> detailTerjual = new ArrayList<>();
                            for(int j = 0; j < details.length(); j++){
                                JSONObject jo2 = details.getJSONObject(j);
                                detailTerjual.add(new CustomItem(jo2.getString("nama"), jo2.getString("jumlah")));
                            }
                            detailList.put(jo.getString("id"),detailTerjual);
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



    private void getMoreData() {

        final List<CustomItem> moreList = new ArrayList<>();
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("keyword", keyword);
            jBody.put("startindex", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(StokSales.this, jBody, "POST", ServerURL.getStokDetail+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("nobukti"), jo.getString("namabrg"), jo.getString("jumlah"), jo.getString("harga"), jo.getString("jumlah_lama"), jo.getString("id")));

                            JSONArray details = jo.getJSONArray("detail");
                            List<CustomItem> detailTerjual = new ArrayList<>();
                            for(int j = 0; j < details.length(); j++){
                                JSONObject jo2 = details.getJSONObject(j);
                                detailTerjual.add(new CustomItem(jo2.getString("nama"), jo2.getString("jumlah")));
                            }
                            detailList.put(jo.getString("id"),detailTerjual);
                        }
                    }

                    isLoading = false;
                    lvStok.removeFooterView(footerList);
                    if(adapterStok != null) adapterStok.addMoreData(moreList, detailList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvStok.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvStok.removeFooterView(footerList);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvBarang.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvBarang.getText().length() == 0){

                        keyword = "";
                        startIndex = 0;
                        getData();
                    }
                }
            });
        }

        actvBarang.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvBarang.getText().toString();
                    startIndex = 0;
                    getData();

                    iv.hideSoftKey(StokSales.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvStok.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapterStok = new ListStokDetailAdapter(StokSales.this, tableList, detailList);
            lvStok.setAdapter(adapterStok);

            lvStok.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

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
        Intent intent = new Intent(StokSales.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
