package gmedia.net.id.psp.NavPengajuanDeposit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
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

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListPengajuanDepositAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavPengajuanDeposit extends AppCompatActivity {

    private View footerList;
    private ListView lvDeposit;
    private ProgressBar pbLoading;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private Context context;
    private List<CustomItem> listPengajuan, moreData;
    private ListPengajuanDepositAdapter adapterDeposit;
    private int start = 0, count = 10;
    private String keyword = "";
    private boolean isLoading = false;
    private String TAG = "test";
    private String nik = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_pengajuan_deposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;

        setTitle("Pengajuan Deposit");

        initUI();
    }

    private void initUI() {

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        lvDeposit = (ListView) findViewById(R.id.lv_deposit);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        session = new SessionManager(context);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);

    }

    @Override
    protected void onResume() {
        super.onResume();

        isLoading = false;
        getDataPengajuan();
    }

    private void getDataPengajuan() {

        isLoading = true;
        start = 0;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    listPengajuan = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listPengajuan.add(new CustomItem(jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl"),
                                    jo.getString("status")));

                        }
                    }

                    setAdapter(listPengajuan);
                } catch (JSONException e) {
                    setAdapter(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                setAdapter(null);
            }
        });
    }

    private void setAdapter(List<CustomItem> listItem) {

        lvDeposit.setAdapter(null);
        if(listItem != null){

            adapterDeposit = new ListPengajuanDepositAdapter((Activity) context, listItem);
            lvDeposit.addFooterView(footerList);
            lvDeposit.setAdapter(adapterDeposit);
            lvDeposit.removeFooterView(footerList);

            lvDeposit.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                    int threshold = 1;
                    int countMerchant = lvDeposit.getCount();

                    if (i == SCROLL_STATE_IDLE) {
                        if (lvDeposit.getLastVisiblePosition() >= countMerchant - threshold && !isLoading) {

                            isLoading = true;
                            lvDeposit.addFooterView(footerList);
                            start += count;
                            getMoreData();
                            Log.i(TAG, "onScroll: last ");
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });

            lvDeposit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    if(item.getItem6().equals("1")) showDialog(item);
                }
            });
        }
    }

    private void getMoreData() {

        isLoading = true;
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("keyword", keyword);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                lvDeposit.removeFooterView(footerList);
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    moreData = new ArrayList<>();
                    if(status.equals("200")){

                        JSONArray items = response.getJSONArray("response");

                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreData.add(new CustomItem(jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl"),
                                    jo.getString("status")));

                        }

                        if(adapterDeposit != null) adapterDeposit.addMoreData(moreData);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvDeposit.removeFooterView(footerList);
            }
        });
    }

    private void showDialog(final CustomItem item){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View viewDialog = inflater.inflate(R.layout.dialog_pengajuan_deposit, null);
        builder.setView(viewDialog);

        final TextView tvText1 = (TextView) viewDialog.findViewById(R.id.tv_text1);
        final TextView tvText2 = (TextView) viewDialog.findViewById(R.id.tv_text2);
        final Button btnTolak = (Button) viewDialog.findViewById(R.id.btn_tolak);
        final Button btnSetujui = (Button) viewDialog.findViewById(R.id.btn_setuju);

        tvText1.setText(item.getItem2());
        tvText2.setText("Mengajukan deposit sebesar "+iv.ChangeToRupiahFormat(item.getItem3()));

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog alertTolak = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menolak pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                saveData(item.getItem1(), "9");
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
                            }
                        })
                        .show();
            }
        });

        btnSetujui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {

                final AlertDialog alertSetujui = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyutujui pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alert.dismiss();
                                saveData(item.getItem1(), "2");
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //alert.dismiss();
                            }
                        })
                        .show();
            }
        });

        alert.show();
    }

    private void saveData(String id, String flag) {

        isLoading = true;
        final ProgressDialog progressDialog = new ProgressDialog(NavPengajuanDeposit.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject jData = new JSONObject();
        try {
            jData.put("id", id);
            jData.put("nik", nik);
            jData.put("apv", flag);
        }catch (JSONException e){
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jData, "POST", ServerURL.savePengajuanDeposite, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan saat memproses data, silahkan ulangi kembali";
                isLoading = false;
                progressDialog.dismiss();

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        getDataPengajuan();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {

                Snackbar.make(findViewById(android.R.id.content), "Terjadi kesalahan saat memproses data, harap ulangi kembali", Snackbar.LENGTH_LONG).show();
                isLoading = false;
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                start = 0;
                keyword = queryText;
                iv.hideSoftKey(context);
                getDataPengajuan();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : "";
                if(newText.length() == 0){

                    start = 0;
                    keyword = "";
                    getDataPengajuan();
                }

                return true;
            }
        });

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        return super.onCreateOptionsMenu(menu);
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
        Intent intent = new Intent(NavPengajuanDeposit.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
