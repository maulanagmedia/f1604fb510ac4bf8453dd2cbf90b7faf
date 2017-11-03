package gmedia.net.id.psp.NavCheckin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.NavCheckin.Adapter.ListCheckinAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class NavCheckin extends Fragment {

    private Context context;
    private View layout;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private AutoCompleteTextView actvCustomer;
    private ListView lvCustomer;
    private ProgressBar pbLoading;
    private TabLayout tabTop;
    private boolean firstLoad = true;
    private boolean isLoading = false;
    private int startIndex = 0, count = 0;
    private String keyword = "";
    private View footerList;
    private List<CustomItem> masterList;
    private ListCheckinAdapter adapter;
    private boolean verifikasiMode = true;

    public NavCheckin() {
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
        layout = inflater.inflate(R.layout.fragment_nav_checkin, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        actvCustomer = (AutoCompleteTextView) layout.findViewById(R.id.actv_customer);
        lvCustomer = (ListView) layout.findViewById(R.id.lv_customer);
        pbLoading = (ProgressBar) layout.findViewById(R.id.pb_proses);
        tabTop = (TabLayout) layout.findViewById(R.id.tab_top);
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.layout_footer_listview, null);
        session = new SessionManager(context);
        startIndex = 0;
        count = getResources().getInteger(R.integer.count_table);
        keyword = "";

        tabTop.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                setTabPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayout.Tab tab = tabTop.getTabAt(0);
        tab.select();
        setTabPosition(0);

        lvCustomer.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                if(absListView.getLastVisiblePosition() == i2-1 && lvCustomer.getCount() > (count-1) && !isLoading ){
                    isLoading = true;
                    lvCustomer.addFooterView(footerList);
                    startIndex += count;
                    getMoreData();

                }
            }
        });
    }

    private void setTabPosition(int i) {

        if(i == 0){
            verifikasiMode = true;
        }else{
            verifikasiMode = false;
        }

        actvCustomer.setText("");
        keyword = "";
        startIndex = 0;
        getDataCustomer();
    }

    private void getDataCustomer() {

        masterList = new ArrayList<>();
        pbLoading.setVisibility(View.VISIBLE);
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", "");
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = ServerURL.getCustomerKunjungan;
        if(verifikasiMode)  url = ServerURL.getCustomerVerifikasi;
        ApiVolley request = new ApiVolley(context, jBody, "POST", url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            masterList.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"), jo.getString("alamat"), jo.getString("notelp"), jo.getString("nohp"), jo.getString("status")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getAutocompleteEvent(tableList);
                    getTableList(tableList);
                    pbLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                    getTableList(null);
                    pbLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getAutocompleteEvent(null);
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
            jBody.put("kdcus", "");
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ServerURL.getCustomerKunjungan;
        if(verifikasiMode)  url = ServerURL.getCustomerVerifikasi;
        ApiVolley request = new ApiVolley(context, jBody, "POST", url, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("kdcus"), jo.getString("nama"), jo.getString("alamat"), jo.getString("notelp"), jo.getString("nohp"), jo.getString("status")));
                        }
                    }

                    lvCustomer.removeFooterView(footerList);
                    if(adapter != null) adapter.addMoreData(moreList);
                    isLoading = false;

                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvCustomer.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {

                isLoading = false;
                lvCustomer.removeFooterView(footerList);
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

                    if(actvCustomer.getText().length() == 0){
                        keyword = "";
                        startIndex = 0;
                        getDataCustomer();
                    }
                }
            });
        }

        actvCustomer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    keyword = actvCustomer.getText().toString();
                    startIndex = 0;
                    getDataCustomer();

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

            adapter = new ListCheckinAdapter(((Activity)context), tableList);
            lvCustomer.setAdapter(adapter);

            lvCustomer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                    if(verifikasiMode){

                        showUpdateStatus(selectedItem.getItem1(), selectedItem.getItem2());
                    }else{


                    }
                }
            });
        }
    }

    private void showUpdateStatus(final String id, final String nama) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Login_Dialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_verification, null);
        builder.setView(view);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Verifikasi");
        builder.setMessage("Setujui outlet "+nama+" ?");
        builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final LinearLayout llTolak = (LinearLayout) view.findViewById(R.id.ll_tolak);
        final LinearLayout llSetujui = (LinearLayout) view.findViewById(R.id.ll_setujui);

        final AlertDialog alert = builder.create();

        llSetujui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.support.v7.app.AlertDialog konfirmasi = new android.support.v7.app.AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin menyetujui " + nama+" ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                updateCustomer(id,"3");
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        llTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.support.v7.app.AlertDialog konfirmasi = new android.support.v7.app.AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin menolak " + nama+" ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                updateCustomer(id,"0");
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        alert.show();
    }

    private void updateCustomer(String id, String status) {

        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Memproses...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jData = new JSONObject();
        try {
            jData.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdcus", id);
            jBody.put("data", jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.updateCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        setTabPosition(0);
                    }else{
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat menyimpan data", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan saat menyimpan data", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }
}
