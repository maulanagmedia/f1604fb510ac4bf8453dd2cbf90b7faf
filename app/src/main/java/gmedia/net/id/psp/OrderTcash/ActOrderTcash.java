package gmedia.net.id.psp.OrderTcash;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.ImageButton;
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

import gmedia.net.id.psp.InfoDeposit.ActDeposit;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.PenjualanHariIni.Adapter.PenjualanHariIniAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class ActOrderTcash extends AppCompatActivity {

    private AutoCompleteTextView actvOutlet;
    private ListView lvTcash;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private TextView tvTotal;
    private String keyword = "";
    private List<CustomItem> masterList;
    private boolean firstLoad = true;
    private String currentFlag = "";
    private TextView tvFrom, tvTo;
    private String dateFrom, dateTo;
    private ImageButton ibShow;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_order_tcash);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Penjualan Tcash");
        session = new SessionManager(ActOrderTcash.this);

        initUI();
    }

    private void initUI() {

        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        lvTcash = (ListView) findViewById(R.id.lv_tcash);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvTo = (TextView) findViewById(R.id.tv_to);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        ibShow = (ImageButton) findViewById(R.id.ib_show);
        fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        keyword = "";
        dateFrom = iv.getCurrentDate(FormatItem.formatDateDisplay); //iv.sumDate(iv.getCurrentDate(FormatItem.formatDateDisplay), -7, FormatItem.formatDateDisplay) ;
        dateTo = iv.getCurrentDate(FormatItem.formatDateDisplay);

        tvFrom.setText(dateFrom);
        tvTo.setText(dateTo);

        initEvent();
    }

    private void initEvent() {

        tvFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar customDate;
                SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);

                Date dateValue = null;

                try {
                    dateValue = sdf.parse(dateFrom);
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
                        dateFrom = sdFormat.format(customDate.getTime());
                        tvFrom.setText(dateFrom);
                    }
                };

                SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
                new DatePickerDialog(ActOrderTcash.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        tvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar customDate;
                SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);

                Date dateValue = null;

                try {
                    dateValue = sdf.parse(dateTo);
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
                        dateTo = sdFormat.format(customDate.getTime());
                        tvTo.setText(dateTo);
                    }
                };

                SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
                new DatePickerDialog(ActOrderTcash.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        ibShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = actvOutlet.getText().toString();
                dateFrom = tvFrom.getText().toString();
                dateTo = tvTo.getText().toString();
                getPenjualan();
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ActOrderTcash.this, ResellerTcash.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyword = actvOutlet.getText().toString();
        dateFrom = tvFrom.getText().toString();
        dateTo = tvTo.getText().toString();
        getPenjualan();
    }

    private void getPenjualan() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", iv.ChangeFormatDateString(dateFrom, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("end", iv.ChangeFormatDateString(dateTo, FormatItem.formatDateDisplay, FormatItem.formatDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(ActOrderTcash.this, jBody, "POST", ServerURL.getPenjualanTcash, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();

                    long total = 0, totalPerNama = 0;
                    String nama = "";
                    String lastTaggal = "";

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);

                            if(!lastTaggal.equals(jo.getString("tgl"))){

                                masterList.add(new CustomItem("H", iv.ChangeFormatDateString(jo.getString("tgl"), FormatItem.formatDate, FormatItem.formatDateDisplay)));
                                lastTaggal = jo.getString("tgl");
                            }

                            masterList.add(new CustomItem("I", jo.getString("nonota"), jo.getString("nama"), jo.getString("total"), jo.getString("flag"), jo.getString("tgl"), jo.getString("kode")));
                            total += iv.parseNullLong(jo.getString("total"));

                            if(i < items.length() - 1){

                                JSONObject jo2 = items.getJSONObject(i+1);
                                if(jo2.getString("nomor").equals(jo.getString("nomor"))){

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
                        getPenjualan();
                    }
                }
            });
        }

        actvOutlet.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvOutlet.getText().toString();
                    getPenjualan();

                    iv.hideSoftKey(ActOrderTcash.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvTcash.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            PenjualanHariIniAdapter adapter = new PenjualanHariIniAdapter(ActOrderTcash.this, tableList);
            lvTcash.setAdapter(adapter);

            lvTcash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    if(selectedItem.getItem1().equals("I")){
                        Intent intent = new Intent(ActOrderTcash.this, DetailTcashOrder.class);
                        intent.putExtra("nonota", selectedItem.getItem2());
                        intent.putExtra("kode", selectedItem.getItem7());
                        startActivity(intent);
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
        Intent intent = new Intent(ActOrderTcash.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
