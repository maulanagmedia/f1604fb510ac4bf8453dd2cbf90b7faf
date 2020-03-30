package gmedia.net.id.psp.OrderNgrs;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;
import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.FormatItem;
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
import gmedia.net.id.psp.PenjualanHariIni.Adapter.PenjualanHariIniAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class ActOrderNGRS extends AppCompatActivity {

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
    private PspPrinter printer;
    private Context context;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_order_ngrs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Penjualan NGRS");
        session = new SessionManager(ActOrderNGRS.this);
        context = this;
        activity = this;
        printer = new PspPrinter(context);
        printer.startService();

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
                new DatePickerDialog(activity ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
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
                new DatePickerDialog(activity ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
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

                Intent intent = new Intent(activity, ResellerNGRS.class);
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

        ApiVolley request = new ApiVolley(activity, jBody, "POST", ServerURL.getPenjualanNgrs, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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

                            masterList.add(new CustomItem(
                                    "I"
                                    , jo.getString("nonota")
                                    , jo.getString("nama")
                                    , jo.getString("total")
                                    , jo.getString("flag")
                                    , jo.getString("tgl")
                                    , jo.getString("kode")));

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

                    iv.hideSoftKey(activity);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvTcash.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            PenjualanHariIniAdapter adapter = new PenjualanHariIniAdapter(activity, tableList);
            lvTcash.setAdapter(adapter);

            lvTcash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    if(selectedItem.getItem1().equals("I")){

                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View viewDialog = inflater.inflate(R.layout.dialog_cetak, null);
                        builder.setView(viewDialog);

                        final Button btnTutup = (Button) viewDialog.findViewById(R.id.btn_tutup);
                        final Button btnCetak = (Button) viewDialog.findViewById(R.id.btn_cetak);

                        btnTutup.setText("Detail");

                        final AlertDialog alert = builder.create();
                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                        List<Item> items = new ArrayList<>();
                        items.add(new Item("NGRS", "-", iv.parseNullDouble(selectedItem.getItem4())));


                        Calendar date = Calendar.getInstance();
                        final Transaksi transaksi = new Transaksi(
                                selectedItem.getItem3()
                                ,session.getUser()
                                ,selectedItem.getItem2()
                                ,date.getTime()
                                ,items
                                ,iv.ChangeFormatDateString(selectedItem.getItem6(), FormatItem.formatDate, FormatItem.formatDateDisplay)
                        );

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

                                Intent intent = new Intent(activity, DetailOrderNGRS.class);
                                intent.putExtra("nonota", selectedItem.getItem2());
                                intent.putExtra("kode", selectedItem.getItem7());
                                startActivity(intent);
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

                                        printer.print(transaksi);

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
        Intent intent = new Intent(activity, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
