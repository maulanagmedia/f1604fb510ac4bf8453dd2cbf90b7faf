package gmedia.net.id.psp.NavKunjungan;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.NavKunjungan.Adapter.ListKunjunganAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class ActKunjungan extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private ListView lvKunjungan;
    private ProgressBar pbLoading;
    private boolean firstLoad = true;
    private boolean isLoading = false;
    private int startIndex = 0, count = 0;
    private View footerList;
    private List<CustomItem> masterList;
    private ListKunjunganAdapter adapter;
    private FloatingActionButton btnAdd;
    private LinearLayout llBottom;
    private String flag = "";
    private String nik;
    private boolean selfCheckin = true;
    private String namaSales = "";
    private LinearLayout llTop;
    private TextView tvDate;
    private String dateString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_kunjungan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Kunjungan Hari Ini");
        context = this;
        initUI();
    }

    private void initUI() {

        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvDate = (TextView) findViewById(R.id.tv_date);
        lvKunjungan = (ListView) findViewById(R.id.lv_kunjungan);
        pbLoading = (ProgressBar) findViewById(R.id.pb_proses);
        btnAdd = (FloatingActionButton) findViewById(R.id.btn_add);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);
        session = new SessionManager(context);
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);
        selfCheckin = true;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            nik = bundle.getString("nik");
            selfCheckin = false;
            if(nik == null || nik.length() == 0){
                nik = session.getUserDetails().get(SessionManager.TAG_UID);
                selfCheckin = true;
            }else{
                namaSales = bundle.getString("nama");
                setTitle("Kunjungan Sales");
                getSupportActionBar().setSubtitle("a/n "+ namaSales);
                llTop.setVisibility(View.VISIBLE);
                dateString = iv.getCurrentDate(FormatItem.formatDateDisplay);
                tvDate.setText(dateString);

                tvDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Calendar customDate;
                        SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);

                        Date dateValue = null;

                        try {
                            dateValue = sdf.parse(dateString);
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
                                dateString = sdFormat.format(customDate.getTime());
                                tvDate.setText(dateString);
                                startIndex = 0;
                                getDataKunjungan();
                            }
                        };

                        SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
                        new DatePickerDialog(ActKunjungan.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
                    }
                });
            }
        }else{
            nik = session.getUserDetails().get(SessionManager.TAG_UID);
        }

        if(selfCheckin){
            checkSupervisor();
        }else{
            btnAdd.setVisibility(View.GONE);
            llBottom.setVisibility(View.GONE);
        }

        initEvent();
    }

    private void initEvent() {

        lvKunjungan.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvKunjungan.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvKunjungan.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();

                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ActKunjunganOutlet.class);
                startActivity(intent);
            }
        });

        llBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ListSalesKunjungan.class);
                intent.putExtra("flag", flag);
                startActivity(intent);
            }
        });
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
                            flag = jo.getString("status_jabatan");
                            llBottom.setVisibility(View.VISIBLE);
                        }else{
                            llBottom.setVisibility(View.GONE);
                        }
                    }else{
                        llBottom.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Log.d("TAG", "onError: " + result);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startIndex = 0;
        getDataKunjungan();
    }

    private void getDataKunjungan() {

        masterList = new ArrayList<>();
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
            jBody.put("date", (dateString.length() > 0) ? iv.ChangeFormatDateString(dateString, FormatItem.formatDateDisplay, FormatItem.formatDate): "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            masterList.add(new CustomItem(
                                    jo.getString("kdcus")
                                    , jo.getString("timestamp")
                                    , jo.getString("nama")
                                    , jo.getString("jarak")
                                    , jo.getString("alamat")
                                    , jo.getString("outlet_location")
                                    , jo.getString("nik")
                                    , jo.getString("is_pjp")
                                    , jo.getString("id")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getTableList(tableList);
                    pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getTableList(null);
                    pbLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getTableList(null);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void getMoreData() {

        isLoading = true;
        final List<CustomItem> moreList = new ArrayList<>();
        //String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
            jBody.put("date", (dateString.length() > 0) ? iv.ChangeFormatDateString(dateString, FormatItem.formatDateDisplay, FormatItem.formatDate): "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("kdcus"), jo.getString("timestamp"), jo.getString("nama"), jo.getString("jarak"), jo.getString("alamat"), jo.getString("outlet_location"), jo.getString("nik"), jo.getString("is_pjp"), jo.getString("id")));
                        }
                    }

                    lvKunjungan.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                    isLoading = false;

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvKunjungan.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvKunjungan.removeFooterView(footerList);
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvKunjungan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapter = new ListKunjunganAdapter(((Activity)context), tableList);
            lvKunjungan.setAdapter(adapter);

            lvKunjungan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    if(!selectedItem.getItem9().equals("0")){
                        Intent intent = new Intent(context, DetailKunjungan.class);
                        intent.putExtra("kdcus", selectedItem.getItem1());
                        intent.putExtra("timestamp", selectedItem.getItem2());
                        intent.putExtra("nik", selectedItem.getItem7());
                        intent.putExtra("flag", selfCheckin);
                        intent.putExtra("nama", namaSales);
                        intent.putExtra("id", selectedItem.getItem9());
                        ((Activity) context).startActivity(intent);
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
        if(selfCheckin){
            Intent intent = new Intent(ActKunjungan.this, MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }else{
            super.onBackPressed();
        }
    }
}
