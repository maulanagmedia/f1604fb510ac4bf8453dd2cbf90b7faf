package gmedia.net.id.psp.NavPOL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
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
import gmedia.net.id.psp.NavPOL.Adapter.ListPelangganAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class ListOutletLocation extends AppCompatActivity {

    private EditText edtSearch;
    private ImageButton btnSearch;
    private ListView lvPelanggan;
    private ProgressBar pbLoading;
    private View footerList;
    private int startIndex = 0, count = 10;
    private String keyword = "";
    private List<CustomItem> listPelanggan, moreList;
    private boolean isLoading = false;
    private final String TAG = "MainAc";
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private ListPelangganAdapter adapter;
    private SessionManager session;
    private String kodeArea;
    private Context context;
    private String jenis = "", pengaju = "";
    private TabLayout tlJenis;
    private TabItem tiTerverifikasi, tiButuhVerifikasi;
    private String flagSPV = "";
    private boolean isTerverifikasi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_outlet_location);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Outlet Location");
        context = this;
        initUI();
    }

    private void initUI() {

        tlJenis = (TabLayout) findViewById(R.id.tl_jenis);
        tiTerverifikasi = (TabItem) findViewById(R.id.ti_terverifikasi);
        tiButuhVerifikasi = (TabItem) findViewById(R.id.ti_butuh_verifikasi);
        edtSearch = (EditText) findViewById(R.id.edt_search);
        btnSearch = (ImageButton) findViewById(R.id.btn_serach);
        lvPelanggan = (ListView) findViewById(R.id.lv_pelanggan);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);

        session = new SessionManager(context);
        kodeArea = session.getUserInfo(SessionManager.TAG_AREA);
        isLoading = false;
        //getDataPelanggan();

        initEvent();

        checkSupervisor();
    }

    private void initEvent() {

        tlJenis.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayout.Tab tab = tlJenis.getTabAt(0);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            String flag = bundle.getString("flag", "0");

            if(flag.equals("1")){

                tab = tlJenis.getTabAt(1);
                tab.select();
                setTabPosition(1);

            }else{
                tab.select();
                setTabPosition(0);
            }
        }else{
            tab.select();
            setTabPosition(0);
        }

        lvPelanggan.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvPelanggan.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvPelanggan.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();
                    Log.i(TAG, "onScroll: last");
                }
            }
        });
    }

    private void setTabPosition(int position) {

        if(position == 0){

            isTerverifikasi = true;
        }else{
            isTerverifikasi = false;
        }

        edtSearch.setText("");
        keyword = edtSearch.getText().toString();
        startIndex = 0;
        getDataPelanggan();
    }

    private void checkSupervisor(){

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.checkSupervisor, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        if(items.length() > 0){
                            JSONObject jo = items.getJSONObject(0);
                            flagSPV = jo.getString("status_jabatan");
                            tlJenis.setVisibility(View.VISIBLE);
                        }else{
                            tlJenis.setVisibility(View.GONE);
                            Toast.makeText(context, "Anda tidak memiliki akses untuk halaman ini", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    }else{
                        tlJenis.setVisibility(View.GONE);
                        Toast.makeText(context, "Anda tidak memiliki akses untuk halaman ini", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Anda tidak memiliki akses untuk halaman ini", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }

            @Override
            public void onError(String result) {
                Log.d("TAG", "onError: " + result);
                Toast.makeText(context, "Anda tidak memiliki akses untuk halaman ini", Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //edtSearch.setText("");
        //getDataPelanggan();
    }

    private void getDataPelanggan() {

        startIndex = 0;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("kodearea", kodeArea);
            jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jBody.put("keyword", keyword);
            jBody.put("terverifikasi", isTerverifikasi ? "1": "0");
            jBody.put("flag", session.getLevel());
            jBody.put("startindex", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getLocationDS, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listPelanggan = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listPelanggan.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"), jo.getString("alamat"), jo.getString("notelp"), jo.getString("nohp"), jo.getString("latitude"), jo.getString("longitude"), jo.getString("pengaju")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(listPelanggan);
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

        moreList = new ArrayList<>();
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("kodeArea", kodeArea);
            jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            jBody.put("keyword", keyword);
            jBody.put("terverifikasi", isTerverifikasi ? "1": "0");
            jBody.put("flag", session.getLevel());
            jBody.put("startindex", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getLocationDS, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    moreList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"), jo.getString("alamat"), jo.getString("notelp"), jo.getString("nohp"), jo.getString("latitude"), jo.getString("longitude"), jo.getString("pengaju")));
                        }
                    }
                    isLoading = false;
                    lvPelanggan.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvPelanggan.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvPelanggan.removeFooterView(footerList);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(edtSearch.getText().length() == 0){

                        keyword = "";
                        startIndex = 0;
                        getDataPelanggan();
                    }
                }
            });
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                keyword = edtSearch.getText().toString();
                startIndex = 0;
                getDataPelanggan();

                iv.hideSoftKey(context);
            }
        });

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = edtSearch.getText().toString();
                    startIndex = 0;
                    getDataPelanggan();

                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPelanggan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapter = new ListPelangganAdapter((Activity) context, tableList);
            lvPelanggan.setAdapter(adapter);

            lvPelanggan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    if(isTerverifikasi){
                        Intent intent = new Intent(context, FormMapsActivity.class);
                        intent.putExtra("kdcus", selectedItem.getItem1());
                        intent.putExtra("nama", selectedItem.getItem2());
                        intent.putExtra("pengaju", selectedItem.getItem8());
                        intent.putExtra("terverifikasi", true);
                        intent.putExtra("flag", flagSPV);
                        intent.putExtra("latitude", selectedItem.getItem6());
                        intent.putExtra("longitude", selectedItem.getItem7());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else{

                        Intent intent = new Intent(context, FormMapsActivity.class);
                        intent.putExtra("kdcus", selectedItem.getItem1());
                        intent.putExtra("nama", selectedItem.getItem2());
                        intent.putExtra("pengaju", selectedItem.getItem8());
                        intent.putExtra("terverifikasi", false);
                        intent.putExtra("flag", flagSPV);
                        intent.putExtra("latitude", selectedItem.getItem6());
                        intent.putExtra("longitude", selectedItem.getItem7());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
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
        Intent intent = new Intent(context, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
