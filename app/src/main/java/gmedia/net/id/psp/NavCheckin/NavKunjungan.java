package gmedia.net.id.psp.NavCheckin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.NavCheckin.Adapter.ListKunjunganAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class NavKunjungan extends Fragment {

    private Context context;
    private View layout;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private ListView lvKunjungan;
    private ProgressBar pbLoading;
    private boolean firstLoad = true;
    private boolean isLoading = false;
    private int startIndex = 0, count = 0;
    private View footerList;
    private List<CustomItem> masterList;
    private ListKunjunganAdapter adapter;

    public NavKunjungan() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_nav_kunjungan, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        lvKunjungan = (ListView) layout.findViewById(R.id.lv_kunjungan);
        pbLoading = (ProgressBar) layout.findViewById(R.id.pb_proses);
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);
        session = new SessionManager(context);
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);

        lvKunjungan.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvKunjungan.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvKunjungan.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startIndex = 0;
        getDataKunjungan();
    }

    private void getDataKunjungan() {

        masterList = new ArrayList<>();
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCustomerKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            String jarak = "";
                            if(iv.parseNullDouble(jo.getString("jarak")) <= 1){
                                jarak = (iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "4") + " m");
                            }else{
                                jarak = (iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "4") + " km");
                            }
                            masterList.add(new CustomItem(jo.getString("kdcus"), jo.getString("timestamp"), jo.getString("nama"), jarak, jo.getString("alamat")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getTableList(tableList);
                    pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getTableList(null);
                    pbLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getTableList(null);
                pbLoading.setVisibility(View.GONE);
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
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCustomerKunjungan, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);

                            String jarak = "";
                            if(iv.parseNullDouble(jo.getString("jarak")) <= 1){
                                jarak = (iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "4") + " m");
                            }else{
                                jarak = (iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "4") + " km");
                            }
                            moreList.add(new CustomItem(jo.getString("kdcus"), jo.getString("timestamp"), jo.getString("nama"), jarak, jo.getString("alamat")));
                        }
                    }

                    lvKunjungan.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                    isLoading = false;

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvKunjungan.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvKunjungan.removeFooterView(footerList);
            }
        });
    }


    private void getTableList(List<CustomItem> tableList) {

        lvKunjungan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            adapter = new ListKunjunganAdapter(((Activity)context), tableList);
            lvKunjungan.setAdapter(adapter);

            lvKunjungan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    Intent intent = new Intent(context, DetailCheckin.class);
                    intent.putExtra("kdcus", selectedItem.getItem1());
                    ((Activity) context).startActivity(intent);
                }
            });
        }
    }
}
