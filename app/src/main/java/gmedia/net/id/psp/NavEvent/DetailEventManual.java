package gmedia.net.id.psp.NavEvent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.Adapter.AutocompleteAdapter;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.NavHome.NavHome;
import gmedia.net.id.psp.OrderDirectSelling.DetailDSPerdana;
import gmedia.net.id.psp.OrderDirectSelling.DetailInjectPulsa;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailEventManual extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private EditText edtNomor;
    private EditText edtLokasi;
    private AutoCompleteTextView actvNama;
    private EditText edtAlamat;
    private LinearLayout btnSimpan, llBeliPulsa, llBeliPerdana;
    private ProgressBar pbLoading;
    private List<HashMap<String,String >> masterList;
    private SimpleAdapter adapter;
    private String nomor = "", lokasi = "", lastKdcus = "", lastCus = "", selectedKdcus = "", latitudePOI = "", longitudePOI = "", lastRadius = "";
    private boolean isEvent = false;
    private LinearLayout llNomor, llLokasi;
    private String latitude = "", longitude = "", radius = "", flagRadius = "";
    private AutoCompleteTextView actvPoi;
    private LinearLayout llPOI;
    private List<CustomItem> listPOI;
    private DialogBox dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event_manual);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Detail Pelanggan Event");
        context = this;

        initUI();
    }

    private void initUI() {

        llNomor = (LinearLayout) findViewById(R.id.ll_nomor);
        llLokasi = (LinearLayout) findViewById(R.id.ll_lokasi);
        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        edtLokasi = (EditText) findViewById(R.id.edt_lokasi);
        actvPoi = (AutoCompleteTextView) findViewById(R.id.actv_poi);
        actvNama = (AutoCompleteTextView) findViewById(R.id.actv_nama);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat);
        btnSimpan = (LinearLayout) findViewById(R.id.simpan);
        llPOI = (LinearLayout) findViewById(R.id.ll_poi);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        dialog = new DialogBox(context);


        session = new SessionManager(context);
        isEvent = false;
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            nomor = bundle.getString("nomor","");
            lokasi = bundle.getString("lokasi","");

            if(nomor.length() > 0){

                llNomor.setVisibility(View.VISIBLE);
                llLokasi.setVisibility(View.VISIBLE);

                edtNomor.setText(nomor);
                edtLokasi.setText(lokasi);
                isEvent = true;
                latitude = bundle.getString("lat", "");
                longitude = bundle.getString("long", "");
                radius = bundle.getString("radius", "");
                flagRadius = bundle.getString("flag_radius", "");
            }

        }

        if(!isEvent){
            setTitle("Detail Direct Selling");
            llPOI.setVisibility(View.VISIBLE);
        }

        initEvent();

        getDataOutlet();
    }

    private void getDataOutlet() {

        pbLoading.setVisibility(View.VISIBLE);
        String area = session.getUserInfo(SessionManager.TAG_AREA);
        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("area", area);
            jBody.put("flag", "DS");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCustomerPerdana, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                pbLoading.setVisibility(View.GONE);

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    masterList = new ArrayList<>();
                    listPOI = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            /*HashMap<String,String> data = new HashMap<>();
                            data.put("kdcus", jo.getString("kdcus"));
                            data.put("nama", jo.getString("nama"));
                            data.put("alamat", jo.getString("alamat"));
                            data.put("latitude", jo.getString("latitude"));
                            data.put("longitude", jo.getString("longitude"));
                            data.put("radius", jo.getString("toleransi_jarak"));
                            masterList.add(data);*/

                            listPOI.add(new CustomItem(jo.getString("kdcus"),
                                    jo.getString("nama"),
                                    jo.getString("alamat"),
                                    jo.getString("latitude"),
                                    jo.getString("longitude"),
                                    jo.getString("toleransi_jarak")));

                        }
                    }

                    //getAutocompleteEvent(masterList);
                    setAutocomplete(listPOI);

                } catch (JSONException e) {
                    e.printStackTrace();
                    //getAutocompleteEvent(null);
                    setAutocomplete(null);
                }
            }

            @Override
            public void onError(String result) {

                //getAutocompleteEvent(null);
                setAutocomplete(null);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void getAutocompleteEvent(List<HashMap<String, String>> listItem) {

        String[] from = {"nama"};
        int[] to = new int[] { android.R.id.text1 };

        actvPoi.setAdapter(null);
        if(listItem != null){

            adapter = new SimpleAdapter(context, listItem, android.R.layout.simple_list_item_1,
                    from, to);
            actvPoi.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actvPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    HashMap<String, String> customer = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                    lastKdcus = customer.get("kdcus");
                    lastCus = customer.get("nama");
                    String alamat = customer.get("alamat");

                    actvPoi.setText(lastCus);
                    if(actvPoi.getText().length() > 0) actvPoi.setSelection(actvPoi.getText().length());
                    latitudePOI = customer.get("latitude");
                    longitudePOI = customer.get("longitude");
                    lastRadius = customer.get("radius");

                    //edtAlamat.setText(alamat);
                }
            });
        }
    }

    private void setAutocomplete(List<CustomItem> listItem) {

        actvPoi.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            AutocompleteAdapter adapterACTV = new AutocompleteAdapter(context, listItem, "");
            actvPoi.setAdapter(adapterACTV);
            actvPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    lastKdcus = item.getItem1();
                    lastCus = item.getItem2();
                    String alamat =item.getItem3();

                    //actvPoi.setText(lastCus);
                    //if(actvPoi.getText().length() > 0) actvPoi.setSelection(actvPoi.getText().length());
                    latitudePOI = item.getItem4();
                    longitudePOI = item.getItem5();
                    lastRadius = item.getItem6();
                }
            });
        }
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });

        /*llBeliPulsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                redirectToDetail(true);
            }
        });

        llBeliPerdana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                redirectToDetail(false);
            }
        });*/
    }

    /*"kdcus" : "GM0001",
            "nama" : "Test",
            "alamat" : "Jangli dalam",
            "nik" : "2035"*/

    private void simpanData() {

        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject body = new JSONObject();
        try {
            body.put("kdcus", lastKdcus);
            body.put("nama", actvNama.getText().toString());
            body.put("alamat", edtAlamat.getText().toString());
            body.put("nik", nik);

        }catch (JSONException e){
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, body, "POST", ServerURL.getCustomerPerdanaManual, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                pbLoading.setVisibility(View.GONE);

                try {
                    Log.d("Response", result);
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message;
                    if(iv.parseNullInteger(status) == 200){
                        message = response.getJSONObject("metadata").getString("message");
                    } else {
                        message = response.getJSONObject("metadata").getString("message");
                    }
                    Log.d("Sukses", message);
                    Toast.makeText(DetailEventManual.this, message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DetailEventManual.this, DetailEventManual.class));
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("respon", e.getMessage());
                    finish();
                }
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
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

        if(isEvent){
            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }else{
            Intent intent = new Intent(context, MainNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
    }
}
