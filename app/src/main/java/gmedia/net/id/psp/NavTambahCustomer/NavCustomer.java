package gmedia.net.id.psp.NavTambahCustomer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.NavTambahCustomer.Adapter.ListCustomerAdapter;
import gmedia.net.id.psp.OrderPulsa.Adapter.ListResellerAdapter;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.DetailCustomer;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavCustomer extends Fragment {

    private Context context;
    private View layout;
    private ListView lvCustomer;
    private AutoCompleteTextView actvCustomer;
    private ProgressBar pbProses;
    private SessionManager session;
    private List<CustomItem> masterList;
    private ItemValidation iv = new ItemValidation();
    private FloatingActionButton fabAdd;
    private boolean firstLoad = true;

    public NavCustomer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_nav_customer, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        lvCustomer = (ListView) layout.findViewById(R.id.lv_customer);
        actvCustomer = (AutoCompleteTextView) layout.findViewById(R.id.actv_customer);
        pbProses = (ProgressBar) layout.findViewById(R.id.pb_proses);
        fabAdd = (FloatingActionButton) layout.findViewById(R.id.btn_add);

        session = new SessionManager(context);
        getDataCustomer();
    }

    private void getDataCustomer() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"), jo.getString("alamat"), jo.getString("notelp"), jo.getString("nohp"), jo.getString("status")));
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

            actvCustomer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvCustomer.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvCustomer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvCustomer.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword)) items.add(item);
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

        lvCustomer.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListCustomerAdapter adapter = new ListCustomerAdapter(((Activity)context), tableList);
            lvCustomer.setAdapter(adapter);

            lvCustomer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    Intent intent = new Intent(context, DetailCustomer.class);
                    intent.putExtra("koders", selectedItem.getItem1());
                    startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }


}
