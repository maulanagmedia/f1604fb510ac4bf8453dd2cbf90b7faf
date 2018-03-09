package gmedia.net.id.psp.NavEvent;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailEvent extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private EditText edtNomor;
    private EditText edtLokasi;
    private AutoCompleteTextView actvNama;
    private EditText actvAlamat;
    private LinearLayout llBeliPulsa, llBeliPerdana;
    private ProgressBar pbLoading;
    private List<CustomItem> masterList;
    public SimpleAdapter adapter;

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

        edtNomor = (EditText) findViewById(R.id.edt_nomor);
        edtLokasi = (EditText) findViewById(R.id.edt_lokasi);
        actvNama = (AutoCompleteTextView) findViewById(R.id.actv_nama);
        actvAlamat = (EditText) findViewById(R.id.edt_alamat);
        llBeliPulsa = (LinearLayout) findViewById(R.id.ll_beli_pulsa);
        llBeliPerdana = (LinearLayout) findViewById(R.id.ll_beli_perdana);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

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


                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getAutocompleteEvent(tableList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                }
            }

            @Override
            public void onError(String result) {

                getAutocompleteEvent(null);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void getAutocompleteEvent(List<CustomItem> listItem) {

        String[] from = {"name"};
        int[] to = new int[] { android.R.id.text1 };

        /*adapter = new SimpleAdapter(context, result, android.R.layout.simple_list_item_1,
                from, to);
        actvNama.setAdapter(adapter);
        adapter.notifyDataSetChanged();*/
    }

    private void initEvent() {


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
