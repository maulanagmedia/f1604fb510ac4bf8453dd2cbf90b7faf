package gmedia.net.id.psp.RiwayatPenjualan;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
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

import gmedia.net.id.psp.LoginScreen;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.OrderDirectSelling.HistoryDirectSelling;
import gmedia.net.id.psp.OrderPerdana.DetailOrderPerdana;
import gmedia.net.id.psp.OrderPulsa.DetailOrderPulsa;
import gmedia.net.id.psp.OrderTcash.DetailTcashOrder;
import gmedia.net.id.psp.PenjualanHariIni.Adapter.PenjualanHariIniAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class RiwayatPenjualan extends AppCompatActivity {

    private AutoCompleteTextView actvOutlet;
    private ListView lvPenjualan;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private TextView tvTotal, tvTotalDeposit, tvTotalSales, tvTotalTcash;
    private String keyword = "";
    private List<CustomItem> masterList, salesList;
    private boolean firstLoad = true;
    private String currentFlag = "";
    private TextView tvFrom, tvTo;
    private String dateFrom, dateTo;
    private ImageButton ibShow;
    private LinearLayout llTop;
    private Spinner spSales;
    private String nik = "";
    private String flagJabatan = "", namaSales = "";
    private int selectedSalesPosition = 0;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_penjualan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Riwayat Penjualan");
        session = new SessionManager(RiwayatPenjualan.this);

        if(!session.isLoggedIn()){
            Intent intent = new Intent(this, LoginScreen.class);
            session.logoutUser(intent);
        }

        context = this;

        initUI();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void initUI() {

        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        lvPenjualan = (ListView) findViewById(R.id.lv_penjualan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvTo = (TextView) findViewById(R.id.tv_to);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        tvTotalDeposit = (TextView) findViewById(R.id.tv_total_deposit);
        tvTotalSales = (TextView) findViewById(R.id.tv_total_sales);
        tvTotalTcash = (TextView) findViewById(R.id.tv_total_tcash);
        ibShow = (ImageButton) findViewById(R.id.ib_show);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        spSales = (Spinner) findViewById(R.id.sp_sales);
        keyword = "";
        dateFrom = iv.sumDate(iv.getCurrentDate(FormatItem.formatDateDisplay), -7, FormatItem.formatDateDisplay) ;
        dateTo = iv.getCurrentDate(FormatItem.formatDateDisplay);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);

        tvFrom.setText(dateFrom);
        tvTo.setText(dateTo);

        namaSales = session.getUser();

        checkSupervisor();

        initEvent();
    }

    private void checkSupervisor(){

        String nikCurrent = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nikCurrent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(RiwayatPenjualan.this, jBody, "POST", ServerURL.checkSupervisor, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        if(items.length() > 0){
                            JSONObject jo = items.getJSONObject(0);
                            flagJabatan = jo.getString("status_jabatan"); // spv atau bm
                            llTop.setVisibility(View.VISIBLE);

                            getSalesList();
                        }else{
                            llTop.setVisibility(View.GONE);
                        }
                    }else{
                        llTop.setVisibility(View.GONE);
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

    private void getSalesList() {

        salesList = new ArrayList<>();
        String nikCurrent = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nikCurrent);
            jBody.put("flag", flagJabatan);
            jBody.put("keyword", "");
            jBody.put("start", "0");
            jBody.put("count", "0");
            jBody.put("all", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(RiwayatPenjualan.this, jBody, "POST", ServerURL.getSalesKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    salesList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            salesList.add(new CustomItem(jo.getString("nik"), jo.getString("nama")));
                        }
                    }

                    setSalesAdapter(salesList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    setSalesAdapter(null);
                }
            }

            @Override
            public void onError(String result) {

                setSalesAdapter(null);
            }
        });
    }

    private void setSalesAdapter(List<CustomItem> listItem) {

        if(listItem != null && listItem.size() > 0){

            String nikCurrent = session.getUserDetails().get(SessionManager.TAG_UID);

            int x = 0;
            int selected = 0;
            for(CustomItem item: listItem){

                if(item.getItem1().equals(nikCurrent)){
                    selected = x;
                }
                x++;
            }

            final CustomItem itemCurrentNik = listItem.get(selected);
            listItem.remove(selected);
            listItem.add(0,itemCurrentNik);

            List<String> listNamaSales = new ArrayList<>();

            for(CustomItem item: listItem){

                listNamaSales.add(item.getItem2());
            }

            salesList = new ArrayList<>(listItem);

            ArrayAdapter adapter = new ArrayAdapter(RiwayatPenjualan.this, R.layout.layout_simple_list, listNamaSales);
            spSales.setAdapter(adapter);
            selectedSalesPosition = 0;
            spSales.setSelection(selectedSalesPosition);

            spSales.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(spSales.getAdapter() != null){

                        ((TextView) spSales.getSelectedView()).setTextColor(getResources().getColor(R.color.color_white));
                        selectedSalesPosition = position;
                        CustomItem salesAtPosition = salesList.get(selectedSalesPosition);
                        namaSales = salesAtPosition.getItem2();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
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
                new DatePickerDialog(RiwayatPenjualan.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
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
                new DatePickerDialog(RiwayatPenjualan.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        ibShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = actvOutlet.getText().toString();
                dateFrom = tvFrom.getText().toString();
                dateTo = tvTo.getText().toString();
                if(salesList!= null && salesList.size() >0){

                    CustomItem salesAtPosition = salesList.get(selectedSalesPosition);
                    nik = salesAtPosition.getItem1();
                }
                getPenjualan();
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

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", iv.ChangeFormatDateString(dateFrom, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("end", iv.ChangeFormatDateString(dateTo, FormatItem.formatDateDisplay, FormatItem.formatDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(RiwayatPenjualan.this, jBody, "POST", ServerURL.getRiwayatPenjualan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    long total = 0, totalPerNama = 0, totalDeposit = 0, totalSales = 0, totalTcash = 0;
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

                            masterList.add(new CustomItem(
                                    "I"
                                    , jo.getString("nonota")
                                    , jo.getString("nama")
                                    , jo.getString("piutang")
                                    , jo.getString("flag")
                                    , jo.getString("tgl")
                                    , jo.getString("app_flag")
                                    , jo.getString("jarak")
                                    , jo.getString("flag_nama")));

                            total += iv.parseNullLong(jo.getString("piutang"));

                            if(jo.getString("app_flag").equals("0")){

                                totalDeposit += iv.parseNullLong(jo.getString("piutang"));
                            }else{

                                if(jo.getString("flag").trim().toUpperCase().equals("TCASH")){

                                    totalTcash += iv.parseNullLong(jo.getString("piutang"));
                                }else{

                                    totalSales += iv.parseNullLong(jo.getString("piutang"));
                                }
                            }

                            if(i < items.length() - 1){

                                JSONObject jo2 = items.getJSONObject(i+1);
                                if(jo2.getString("kdcus").equals(jo.getString("kdcus"))){

                                    totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                }else{

                                    totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                    masterList.add(new CustomItem("F", String.valueOf(totalPerNama)));
                                    totalPerNama = 0;
                                }
                            }else{

                                totalPerNama += iv.parseNullLong(jo.getString("piutang"));
                                masterList.add(new CustomItem("F", String.valueOf(totalPerNama)));
                                totalPerNama = 0;
                            }
                        }
                    }

                    tvTotal.setText(iv.ChangeToRupiahFormat(total));
                    tvTotalDeposit.setText(iv.ChangeToRupiahFormat(totalDeposit));
                    tvTotalSales.setText(iv.ChangeToRupiahFormat(totalSales));
                    tvTotalTcash.setText(iv.ChangeToRupiahFormat(totalTcash));
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

                    iv.hideSoftKey(RiwayatPenjualan.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPenjualan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            PenjualanHariIniAdapter adapter = new PenjualanHariIniAdapter(RiwayatPenjualan.this, tableList);
            lvPenjualan.setAdapter(adapter);

            lvPenjualan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    if(selectedItem.getItem1().equals("I")){

                        currentFlag = selectedItem.getItem5();

                        if(currentFlag.equals("DS")){

                            Intent intent = new Intent(RiwayatPenjualan.this, HistoryDirectSelling.class);
                            intent.putExtra("nobukti", selectedItem.getItem2());
                            intent.putExtra("namasales", namaSales);
                            startActivity(intent);

                        }else{
                            getDetailPenjualan(selectedItem.getItem2(), currentFlag, selectedItem.getItem8(), selectedItem.getItem3());
                        }
                    }
                }
            });
        }
    }

    //TODO: get detail order Mkios / GA
    private void getDetailPenjualan(String nonota, final String flag, final String jarak, final String namaOutlet) {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nonota", nonota);
            jBody.put("flag", flag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(RiwayatPenjualan.this, jBody, "POST", ServerURL.getDetailPenjualanHariIni, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                                    Intent intent = new Intent(RiwayatPenjualan.this, DetailOrderPerdana.class);
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
                                    intent.putExtra("tglnota", jo.getString("tgl"));
                                    intent.putExtra("jarak", jarak);
                                    intent.putExtra("namasales", namaSales);
                                    startActivity(intent);
                                    break;
                                }else if (flag.equals("MKIOS")){
                                    Intent intent = new Intent(RiwayatPenjualan.this, DetailOrderPulsa.class);
                                    intent.putExtra("nonota", jo.getString("nonota"));
                                    intent.putExtra("flag", jo.getString("flag"));
                                    intent.putExtra("koders", jo.getString("kode"));
                                    intent.putExtra("kode_cv", jo.getString("kode_cv"));
                                    intent.putExtra("tgl", jo.getString("tgl"));
                                    intent.putExtra("jarak", jarak);
                                    intent.putExtra("namasales", namaSales);
                                    intent.putExtra("namaoutlet", namaOutlet);
                                    startActivity(intent);
                                    break;
                                }else if(flag.equals("TCASH")){
                                    Intent intent = new Intent(RiwayatPenjualan.this, DetailTcashOrder.class);
                                    intent.putExtra("nonota", jo.getString("nonota"));
                                    intent.putExtra("koders", jo.getString("kode"));
                                    intent.putExtra("tgl", jo.getString("tgl"));
                                    intent.putExtra("namasales", namaSales);
                                    startActivity(intent);
                                }
                            }
                        }else{
                            Toast.makeText(RiwayatPenjualan.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(RiwayatPenjualan.this, "Data sudah tidak tersedia", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RiwayatPenjualan.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                Toast.makeText(RiwayatPenjualan.this, "Data tidak termuat, mohon coba kembali", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(RiwayatPenjualan.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
