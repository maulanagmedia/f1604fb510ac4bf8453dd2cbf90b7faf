package gmedia.net.id.psp.NavKomplain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.AbsListView;
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

import gmedia.net.id.psp.NavKomplain.Adapter.ListComplaintAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.DetailCustomer;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavKomplain extends Fragment {

    private Context context;
    private View layout;
    private ItemValidation iv = new ItemValidation();
    private ListView lvComplaint;
    private AutoCompleteTextView actvComplaint;
    private ProgressBar pbProses;
    private FloatingActionButton fabAdd;
    private SessionManager session;
    private List<CustomItem> masterList;
    private boolean firstLoad = true;
    private boolean isLoading = false;
    private int startIndex = 0, count = 0;
    private String keyword = "";
    private View footerList;
    private ListComplaintAdapter adapter;

    public NavKomplain() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_nav_komplain, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        lvComplaint = (ListView) layout.findViewById(R.id.lv_complaint);
        actvComplaint= (AutoCompleteTextView) layout.findViewById(R.id.actv_complaint);
        pbProses = (ProgressBar) layout.findViewById(R.id.pb_proses);
        fabAdd = (FloatingActionButton) layout.findViewById(R.id.btn_add);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);

        session = new SessionManager(context);
        //getDataCustomer();
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);
        keyword = "";
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();

        keyword = actvComplaint.getText().toString();
        startIndex = 0;
        getDataComplaint();
    }

    private void initEvent() {

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, DetailComplaint.class);
                startActivity(intent);
                ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        lvComplaint.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvComplaint.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvComplaint.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();

                }
            }
        });
    }

    private void getDataComplaint() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getComplaint, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("id"), jo.getString("komplain"), jo.getString("balasan"), jo.getString("timestamp")));
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

    private void getMoreData() {

        isLoading = true;
        final List<CustomItem> moreList = new ArrayList<>();
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getComplaint, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("id"), jo.getString("komplain"), jo.getString("balasan"), jo.getString("timestamp")));
                        }
                    }

                    lvComplaint.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                    isLoading = false;

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvComplaint.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvComplaint.removeFooterView(footerList);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvComplaint.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvComplaint.getText().length() == 0){

                        keyword = "";
                        startIndex = 0;
                        getDataComplaint();
                    }
                }
            });
        }

        actvComplaint.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvComplaint.getText().toString();
                    startIndex = 0;
                    getDataComplaint();

                    iv.hideSoftKey(context);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvComplaint.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapter = new ListComplaintAdapter(((Activity)context), tableList);
            lvComplaint.setAdapter(adapter);

            lvComplaint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    Intent intent = new Intent(context, DetailComplaint.class);
                    intent.putExtra("id", selectedItem.getItem1());
                    startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }

}
