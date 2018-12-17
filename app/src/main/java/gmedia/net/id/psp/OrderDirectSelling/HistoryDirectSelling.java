package gmedia.net.id.psp.OrderDirectSelling;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import gmedia.net.id.psp.OrderDirectSelling.Adapter.HistoryDSAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class HistoryDirectSelling extends AppCompatActivity {

    private ListView lvHistory;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvKeyword;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private SessionManager session;
    private Context context;
    private String nobukti = "";
    private PspPrinter printer;
    private String namaSales = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_direct_selling);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;
        printer = new PspPrinter(context);
        printer.startService();
        session = new SessionManager(context);
        setTitle("Riwayat Penjualan DS");

        initUI();
    }

    private void initUI() {

        lvHistory = (ListView) findViewById(R.id.lv_history);
        actvKeyword = (AutoCompleteTextView) findViewById(R.id.actv_keyword);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            nobukti = bundle.getString("nobukti", "");
            namaSales = bundle.getString("namasales",session.getUser());
            getData();
        }
    }

    /*@Override
    protected void onDestroy() {
        printer.stopService();
        super.onDestroy();
    }*/

    private void getData() {

        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_NIK);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nobukti", nobukti);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getRiwayatDS, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                                    jo.getString("id"),
                                    jo.getString("tgl"),
                                    jo.getString("namabrg"),
                                    jo.getString("harga"),
                                    jo.getString("total"),
                                    jo.getString("POI"),
                                    jo.getString("nama"),
                                    jo.getString("alamat"),
                                    jo.getString("nomor"),
                                    jo.getString("nomor_event"),
                                    jo.getString("ccid")
                            ));
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

            actvKeyword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvKeyword.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvKeyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvKeyword.getText().toString().trim().toUpperCase();

                    if(tableList != null){

                        for (CustomItem item: tableList){

                            if(item.getItem3().toUpperCase().contains(keyword)
                                    || item.getItem7().toUpperCase().contains(keyword)
                                    || item.getItem9().toUpperCase().contains(keyword)) items.add(item);
                        }
                    }

                    getTableList(items);
                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvHistory.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            HistoryDSAdapter adapter = new HistoryDSAdapter((Activity) context, tableList);
            lvHistory.setAdapter(adapter);

            lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View viewDialog = inflater.inflate(R.layout.dialog_cetak, null);
                    builder.setView(viewDialog);

                    final Button btnTutup = (Button) viewDialog.findViewById(R.id.btn_tutup);
                    final Button btnCetak = (Button) viewDialog.findViewById(R.id.btn_cetak);

                    final AlertDialog alert = builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    List<Item> items = new ArrayList<>();
                    items.add(new Item(item.getItem3(), "-", iv.parseNullDouble(item.getItem5())));

                    Calendar date = Calendar.getInstance();
                    final Transaksi transaksi = new Transaksi(
                            item.getItem7()
                            ,namaSales
                            ,nobukti
                            ,date.getTime()
                            ,items
                            ,iv.ChangeFormatDateString(item.getItem2(), FormatItem.formatDate, FormatItem.formatDateDisplay)
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

                                    printer.print(transaksi, "");

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
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
