package gmedia.net.id.psp.NavMarketSurvey;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
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

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.NavMarketSurvey.Adapter.ListServiceMarketAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class ActMarketSurvey extends AppCompatActivity {

    private Context context;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private ProgressBar pbProses;
    private FloatingActionButton fabAdd;
    private AutoCompleteTextView actvNama;
    private TextView tvFrom;
    private TextView tvTo;
    private ImageButton ibShow;
    private ListView lvMarket;
    private List<CustomItem> listService;
    private String keyword = "", dateFrom = "", dateTo = "", nik = "";
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_market_survey);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;

        setTitle("Market Survey TSA");
        session = new SessionManager(context);

        initUI();
    }

    private void initUI() {

        actvNama = (AutoCompleteTextView) findViewById(R.id.actv_nama);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        tvTo = (TextView) findViewById(R.id.tv_to);
        ibShow = (ImageButton) findViewById(R.id.ib_show);
        lvMarket = (ListView) findViewById(R.id.lv_market);
        fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);

        initEvent();

        keyword = "";
        dateFrom = iv.getCurrentDate(FormatItem.formatDateDisplay) ;
        dateTo = iv.getCurrentDate(FormatItem.formatDateDisplay);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);

        tvFrom.setText(dateFrom);
        tvTo.setText(dateTo);
    }

    private void initEvent() {

        fabAdd.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               /*if(!session.getJabatan().equals("TSA")){ // AOC, SF, SPV


               }else if(!session.getJabatan().equals("AOC")){
                   Intent intent = new Intent(context, MarketSurveyAOC.class);
                   startActivity(intent);
               }*/

               Intent intent = new Intent(context, MarketSurveyTSA.class);
               startActivity(intent);
            }
       });

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
                new DatePickerDialog(ActMarketSurvey.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
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
                new DatePickerDialog(ActMarketSurvey.this ,date , iv.parseNullInteger(yearOnly.format(dateValue)),dateValue.getMonth(),dateValue.getDate()).show();
            }
        });

        ibShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = actvNama.getText().toString();
                dateFrom = tvFrom.getText().toString();
                dateTo = tvTo.getText().toString();

                getSurveyMarket();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyword = actvNama.getText().toString();
        dateFrom = tvFrom.getText().toString();
        dateTo = tvTo.getText().toString();
        getSurveyMarket();
    }

    private void getSurveyMarket() {

        pbProses.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("flag", "TSA");
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("date1", iv.ChangeFormatDateString(dateFrom, FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("date2", iv.ChangeFormatDateString(dateTo, FormatItem.formatDateDisplay, FormatItem.formatDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getMarketSurvey, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbProses.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listService = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listService.add(new CustomItem(jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("alamat"),
                                    jo.getString("state"),
                                    jo.getString("timestamp"),
                                    jo.getString("latitude"),
                                    jo.getString("longitude")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(listService);
                    getAutocompleteEvent(tableList);
                    getTableList(tableList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                    getTableList(null);
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
            actvNama.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvNama.getText().length() == 0){

                        keyword = "";
                        getSurveyMarket();
                    }
                }
            });
        }

        actvNama.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvNama.getText().toString();
                    getSurveyMarket();

                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvMarket.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListServiceMarketAdapter adapter = new ListServiceMarketAdapter((Activity) context, tableList);
            lvMarket.setAdapter(adapter);

            lvMarket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    Intent intent = new Intent(context, MarketSurveyTSA.class);
                    intent.putExtra("id", selectedItem.getItem1());
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
        Intent intent = new Intent(context, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
