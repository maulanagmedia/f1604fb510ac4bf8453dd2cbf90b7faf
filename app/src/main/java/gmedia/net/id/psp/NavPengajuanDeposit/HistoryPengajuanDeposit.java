package gmedia.net.id.psp.NavPengajuanDeposit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
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

import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListHistoryDepositAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class HistoryPengajuanDeposit extends AppCompatActivity {

    private View footerList;
    private ListView lvDeposit;
    private ProgressBar pbLoading;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private Context context;
    private List<CustomItem> listPengajuan, moreData;
    private ListHistoryDepositAdapter adapterDeposit;
    private int start = 0, count = 10;
    private String keyword = "";
    private boolean isLoading = false;
    private String TAG = "test";
    private String nik = "";
    private PspPrinter printer;
    private TextView tvFrom, tvTo;
    private String dateFrom, dateTo;
    private AutoCompleteTextView actvOutlet;
    private ImageButton ibShow;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_pengajuan_deposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;
        printer = new PspPrinter(context);
        printer.startService();

        setTitle("History Deposit");

        initUI();
        initEvent();
    }

    private void initUI() {

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        lvDeposit = (ListView) findViewById(R.id.lv_deposit);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvTo = (TextView) findViewById(R.id.tv_to);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        ibShow = (ImageButton) findViewById(R.id.ib_show);
        session = new SessionManager(context);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);
        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        dateFrom = iv.sumDate(iv.getCurrentDate(FormatItem.formatDateDisplay), -7, FormatItem.formatDateDisplay) ;
        dateTo = iv.getCurrentDate(FormatItem.formatDateDisplay);
        keyword = "";
        tvFrom.setText(dateFrom);
        tvTo.setText(dateTo);

    }

    private void initEvent(){

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
                new DatePickerDialog(HistoryPengajuanDeposit.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
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
                new DatePickerDialog(context,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        ibShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = actvOutlet.getText().toString();
                dateFrom = tvFrom.getText().toString();
                dateTo = tvTo.getText().toString();
                start = 0;
                iv.hideSoftKey(context);
                getDataPengajuan();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        isLoading = false;
        getDataPengajuan();
    }

    private void getDataPengajuan() {

        isLoading = true;
        start = 0;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("datestart", iv.ChangeFormatDateString(dateFrom, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("dateend", iv.ChangeFormatDateString(dateTo, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getHistoryDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    listPengajuan = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listPengajuan.add(new CustomItem(
                                    jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl")+ " "+jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan")));

                        }
                    }

                    setAdapter(listPengajuan);
                } catch (JSONException e) {
                    setAdapter(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                setAdapter(null);
            }
        });
    }

    private void setAdapter(List<CustomItem> listItem) {

        lvDeposit.setAdapter(null);
        if(listItem != null){

            adapterDeposit = new ListHistoryDepositAdapter((Activity) context, listItem);
            lvDeposit.addFooterView(footerList);
            lvDeposit.setAdapter(adapterDeposit);
            lvDeposit.removeFooterView(footerList);

            lvDeposit.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int countMerchant = lvDeposit.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvDeposit.getLastVisiblePosition() >= countMerchant - threshold && !isLoading) {

                            isLoading = true;
                            lvDeposit.addFooterView(footerList);
                            start += count;
                            getMoreData();
                            Log.i(TAG, "onScroll: last ");
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });

            lvDeposit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View viewDialog = inflater.inflate(R.layout.dialog_cetak, null);
                    builder.setView(viewDialog);
                    builder.setCancelable(false);

                    final Button btnTutup = (Button) viewDialog.findViewById(R.id.btn_tutup);
                    final Button btnCetak = (Button) viewDialog.findViewById(R.id.btn_cetak);

                    final AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    List<Item> items = new ArrayList<>();
                    items.add(new Item(item.getItem7(), "-", iv.parseNullDouble(item.getItem3())));

                    Calendar date = Calendar.getInstance();
                    final Transaksi transaksi = new Transaksi(item.getItem2(), session.getUser(), "", date.getTime(), items, iv.ChangeFormatDateString(item.getItem5(), FormatItem.formatDate2, FormatItem.formatDateDisplay2));

                    btnTutup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view2) {

                            if(alert != null){

                                try {

                                    alert.dismiss();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            getDataPengajuan();
                        }
                    });

                    btnCetak.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(!printer.bluetoothAdapter.isEnabled()) {

                                printer.dialogBluetooth.show();
                                Toast.makeText(context, "Hidupkan bluetooth anda kemudian klik cetak kembali", Toast.LENGTH_LONG).show();
                            }else{

                                if(printer.isPrinterReady()){

                                    printer.print(transaksi, true);

                                }else{

                                    Toast.makeText(context, "Harap pilih device printer telebih dahulu", Toast.LENGTH_LONG).show();
                                    printer.showDevices();
                                }
                            }
                        }
                    });

                    try {
                        alert.show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    //if(item.getItem6().equals("1")) showDialog(item);
                }
            });
        }
    }

    private void getMoreData() {

        isLoading = true;
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("datestart", iv.ChangeFormatDateString(dateFrom, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("dateend", iv.ChangeFormatDateString(dateTo, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getHistoryDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                lvDeposit.removeFooterView(footerList);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    moreData = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreData.add(new CustomItem(jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl")+ " "+jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan")));

                        }

                        if(adapterDeposit != null) adapterDeposit.addMoreData(moreData);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvDeposit.removeFooterView(footerList);
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                start = 0;
                keyword = queryText;
                iv.hideSoftKey(context);
                getDataPengajuan();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : "";
                if(newText.length() == 0){

                    start = 0;
                    keyword = "";
                    getDataPengajuan();
                }

                return true;
            }
        });

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        return super.onCreateOptionsMenu(menu);
    }*/

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
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
