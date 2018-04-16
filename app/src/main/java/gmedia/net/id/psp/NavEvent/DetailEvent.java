package gmedia.net.id.psp.NavEvent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
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
import gmedia.net.id.psp.OrderDirectSelling.DetailDSPerdana;
import gmedia.net.id.psp.OrderDirectSelling.DetailInjectPulsa;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailEvent extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private EditText edtNomor;
    private EditText edtLokasi;
    private AutoCompleteTextView actvNama;
    private EditText edtAlamat;
    private LinearLayout llBeliPulsa, llBeliPerdana;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

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
        llBeliPulsa = (LinearLayout) findViewById(R.id.ll_beli_pulsa);
        llBeliPerdana = (LinearLayout) findViewById(R.id.ll_beli_perdana);
        llPOI = (LinearLayout) findViewById(R.id.ll_poi);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

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

        llBeliPulsa.setOnClickListener(new View.OnClickListener() {
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
        });
    }

    private void redirectToDetail(boolean isPulsa){

        //Validasi

        if(actvPoi.getText().length() > 0 && !lastCus.equals(actvPoi.getText().toString())){
            actvPoi.setError("POI tidak ditemukan");
            actvPoi.requestFocus();
            return;

        }

        if(isEvent && edtNomor.getText().toString().length() == 0){

            Toast.makeText(context, "Data event tidak termuat, silahkan ulangi proses", Toast.LENGTH_LONG).show();
            return;
        }

        if(actvPoi.getText().toString().length() > 0) {
            selectedKdcus = actvPoi.getText().toString().equals(lastCus) ? lastKdcus: "";
        }else{
            selectedKdcus = "";
        }

        if(actvNama.getText().toString().length() == 0){

            actvNama.setError("Nama harap diisi");
            actvNama.requestFocus();
            return;
        }else{
            actvNama.setError(null);
            //selectedKdcus = actvNama.getText().toString().equals(lastCus) ? lastKdcus: "";
        }

        if(edtAlamat.getText().toString().length() == 0){

            edtAlamat.setError("Alamat harap diisi");
            edtAlamat.requestFocus();
            return;
        }else{

            edtAlamat.setError(null);
        }

        Intent intent = new Intent(context, DetailInjectPulsa.class);
        if(!isPulsa) intent = new Intent(context, DetailDSPerdana.class);

        intent.putExtra("nama", actvNama.getText().toString());
        intent.putExtra("alamat", edtAlamat.getText().toString());
        intent.putExtra("nomor", nomor);
        intent.putExtra("lat", latitude);
        intent.putExtra("long", longitude);
        intent.putExtra("radius", radius);
        intent.putExtra("flag_radius", flagRadius);
        if(lastCus.equals(actvPoi.getText().toString())){
            intent.putExtra("kdcus", lastKdcus);
            intent.putExtra("lat_poi", latitudePOI);
            intent.putExtra("long_poi", longitudePOI);
            intent.putExtra("radius", lastRadius);
            intent.putExtra("poi", lastCus);

        }
        startActivity(intent);
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
