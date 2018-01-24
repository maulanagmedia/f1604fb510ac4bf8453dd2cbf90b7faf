package gmedia.net.id.psp.NavCheckin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.List;

import gmedia.net.id.psp.NavCheckin.Adapter.ListSalesKunjunganAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class ListSalesKunjungan extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private AutoCompleteTextView actvSales;
    private ListView lvSales;
    private ProgressBar pbLoading;
    private boolean firstLoad = true;
    private boolean isLoading = false;
    private int startIndex = 0, count = 0;
    private String keyword = "";
    private View footerList;
    private List<CustomItem> masterList;
    private ListSalesKunjunganAdapter adapter;
    private String flag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sales_kunjungan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            flag = bundle.getString("flag");
        }

        setTitle("Kunjungan Sales");
        getSupportActionBar().setSubtitle("Pilih Sales");

        context = this;
        initUI();
    }

    private void initUI() {

        actvSales = (AutoCompleteTextView) findViewById(R.id.actv_sales);
        lvSales= (ListView) findViewById(R.id.lv_sales);
        pbLoading = (ProgressBar) findViewById(R.id.pb_proses);
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);
        session = new SessionManager(context);
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);
        keyword = "";

        lvSales.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvSales.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvSales.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyword = actvSales.getText().toString();
        startIndex = 0;
        getSalesKunjungan();
    }

    private void getSalesKunjungan() {

        masterList = new ArrayList<>();
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("flag", flag);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getSalesKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("nik"), jo.getString("nama"), jo.getString("jml")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getAutocompleteEvent(tableList);
                    getTableList(tableList);
                    pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                    getTableList(null);
                    pbLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getAutocompleteEvent(null);
                getTableList(null);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void getMoreData() {

        isLoading = true;
        final List<CustomItem> moreList = new ArrayList<>();
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("flag", flag);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getSalesKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("nik"), jo.getString("nama"), jo.getString("jml")));
                        }
                    }

                    lvSales.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                    isLoading = false;

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvSales.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvSales.removeFooterView(footerList);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvSales.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvSales.getText().length() == 0){
                        keyword = "";
                        startIndex = 0;
                        getSalesKunjungan();
                    }
                }
            });
        }

        actvSales.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvSales.getText().toString();
                    startIndex = 0;
                    getSalesKunjungan();

                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvSales.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapter = new ListSalesKunjunganAdapter(((Activity)context), tableList);
            lvSales.setAdapter(adapter);

            lvSales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    Intent intent = new Intent(ListSalesKunjungan.this, ActKunjungan.class);
                    intent.putExtra("nik", selectedItem.getItem1());
                    intent.putExtra("nama", selectedItem.getItem2());
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
        super.onBackPressed();
    }
}
