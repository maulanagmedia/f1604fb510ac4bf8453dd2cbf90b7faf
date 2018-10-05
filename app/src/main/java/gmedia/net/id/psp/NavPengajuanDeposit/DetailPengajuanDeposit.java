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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListPengajuanDepositAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailPengajuanDeposit extends AppCompatActivity {

    private View footerList;
    private ListView lvDeposit;
    private ProgressBar pbLoading;
    private static ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private Context context;
    private List<CustomItem> listPengajuan, moreData;
    private static ListPengajuanDepositAdapter adapterDeposit;
    private int start = 0, count = 40;
    private String keyword = "";
    private boolean isLoading = false;
    private String TAG = "test";
    private String nik = "";
    private String kdcus = "", nama = "", flag = "";
    private static TextView tvTotal;
    private Button btnTolak, btnTerima;
    private static String total = "0";
    public HashMap<String, List<CustomItem>> listCCID = new HashMap<>();

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

        listCCID = new HashMap<>();
    }

    private void initUI() {

        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        lvDeposit = (ListView) findViewById(R.id.lv_deposit);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        btnTolak = (Button) findViewById(R.id.btn_tolak);
        btnTerima = (Button) findViewById(R.id.btn_terima);
        session = new SessionManager(context);
        nik = session.getUserDetails().get(SessionManager.TAG_UID);
        total = "0";

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            kdcus = bundle.getString("kdcus", "");
            nama = bundle.getString("nama", "");
            flag = bundle.getString("flag", "");

            setTitle("Pengajuan");
            if(flag.equals("2")) setTitle("Pembelian Perdana");
            getSupportActionBar().setSubtitle("a/n " + nama);

            isLoading = false;
            getDataPengajuan();

            initEvent();
        }
    }

    private void initEvent() {

        btnTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapterDeposit == null || adapterDeposit.getItems().size() == 0){

                    Toast.makeText(context, "Data masih kosong, harap diisi", Toast.LENGTH_LONG).show();
                    return;
                }

                showDialogTolak();
            }
        });

        btnTerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapterDeposit == null || adapterDeposit.getItems().size() == 0){

                    Toast.makeText(context, "Data masih kosong, harap diisi", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean isSelected = false;
                for(CustomItem item:adapterDeposit.getItems()){

                    if(item.getItem8().equals("1")){
                        isSelected = true;
                        break;
                    }
                }

                if(!isSelected){

                    Toast.makeText(context, "Harap pilih minimal satu item untuk disetujui", Toast.LENGTH_LONG).show();
                    return;
                }

                final AlertDialog alertTerima = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyetujui deposit ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                saveData("2", "");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getDataPengajuan() {

        isLoading = true;
        start = 0;
        pbLoading.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
            jBody.put("keyword", keyword);
            jBody.put("flag", flag);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDetail, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                            listPengajuan.add(new CustomItem(
                                    jo.getString("id"),
                                    jo.getString("nama"),
                                    jo.getString("debit"),
                                    jo.getString("nilai_status"),
                                    jo.getString("tgl") +" "+ jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan"),
                                    "0"
                            ));

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

            adapterDeposit = new ListPengajuanDepositAdapter((Activity) context, listItem, flag);
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

            /*lvDeposit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    if(item.getItem6().equals("1")) showDialog(item);
                }
            });*/
        }
    }

    private void getMoreData() {

        isLoading = true;
        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
            jBody.put("keyword", keyword);
            jBody.put("flag", flag);
            jBody.put("start", String.valueOf(start));
            jBody.put("end", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley apiVolley = new ApiVolley(context, jBody, "POST", ServerURL.getPengajuanDetail, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
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
                                    jo.getString("tgl") +" "+ jo.getString("jam"),
                                    jo.getString("status"),
                                    jo.getString("keterangan"),
                                    "0"
                            ));

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
        final Button btnBatal = (Button) viewDialog.findViewById(R.id.btn_batal);
        final Button btnTolak = (Button) viewDialog.findViewById(R.id.btn_tolak);
        final Button btnSetujui = (Button) viewDialog.findViewById(R.id.btn_setuju);

        tvText1.setText(item.getItem2());
        tvText2.setText("Mengajukan deposit sebesar "+iv.ChangeToRupiahFormat(item.getItem3()));

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

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

    private void showDialogTolak(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View viewDialog = inflater.inflate(R.layout.dialog_message, null);
        builder.setView(viewDialog);

        final EditText edtMessage = (EditText) viewDialog.findViewById(R.id.edt_message);
        final Button btnBatal = (Button) viewDialog.findViewById(R.id.btn_batal);
        final Button btnOk = (Button) viewDialog.findViewById(R.id.btn_ok);

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog alertTolak = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menolak pengajuan ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(edtMessage.getText().toString().isEmpty()){

                                    edtMessage.setError("Alasan penolakan harap diisi");
                                    edtMessage.requestFocus();
                                    return;
                                }else{
                                    edtMessage.setError(null);
                                }

                                alert.dismiss();
                                saveData( "9", edtMessage.getText().toString());
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

    public static void updateHarga(){

        double totalDouble = 0;
        if(adapterDeposit != null){

            for(CustomItem item:adapterDeposit.getItems()){

                if(item.getItem8().equals("1")){

                    totalDouble += iv.parseNullDouble(item.getItem3());
                }
            }
        }

        total = iv.doubleToString(totalDouble);
        tvTotal.setText(iv.ChangeToRupiahFormat(totalDouble));
    }

    private void saveData(String type, String alasan) {

        isLoading = true;
        final ProgressDialog progressDialog = new ProgressDialog(DetailPengajuanDeposit.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if(adapterDeposit != null && adapterDeposit.getItems().size() > 0){

            JSONArray jArray = new JSONArray();

            for(CustomItem item : adapterDeposit.getItems()){

                if(item.getItem8().equals("1")){

                    if(flag.equals("2")){

                        List<CustomItem> selectedItem = listCCID.get(item.getItem1());
                        for(CustomItem item2: selectedItem){
                            JSONObject jDataDetail = new JSONObject();
                            try {
                                jDataDetail.put("nobukti","");
                                jDataDetail.put("nik", nik);
                                jDataDetail.put("kodebrg", item2.getItem1());
                                jDataDetail.put("ccid", item2.getItem3());
                                jDataDetail.put("harga", item2.getItem4());
                                jDataDetail.put("jumlah", item2.getItem5());
                                jDataDetail.put("total", item2.getItem4());
                                jDataDetail.put("kdcus", kdcus);
                                jDataDetail.put("nama", nama);
                                jDataDetail.put("alamat", "");
                                jDataDetail.put("nomor", "");
                                jDataDetail.put("nomor_event", "");
                                jDataDetail.put("jarak", "");
                                jDataDetail.put("no_surat_jalan", item2.getItem6());
                                jDataDetail.put("id_surat_jalan", item2.getItem7());
                                jDataDetail.put("transaction_id", "0");
                                jDataDetail.put("is_rs", "1");
                                jDataDetail.put("id_transaksi", item.getItem1());
                                jDataDetail.put("keterangan", alasan);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            jArray.put(jDataDetail);
                        }
                    }else{

                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("id", item.getItem1());
                            jo.put("nik", nik);
                            jo.put("apv", type);
                            jo.put("keterangan", alasan);

                            jArray.put(jo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            JSONObject jData = new JSONObject();

            String saveUrl = ServerURL.savePengajuanDeposite;

            if(flag.equals("2")){

                saveUrl = ServerURL.savePengajuanDepositePerdana;

                try {
                    jData.put("data", jArray);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                try {
                    jData.put("approval", jArray);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            ApiVolley request = new ApiVolley(context, jData, "POST", saveUrl, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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
                            getDataPengajuan();

                        }

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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
        }else {
            Toast.makeText(context, "Barang masih kosong", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 9 && data != null && resultCode == RESULT_OK){
            String jsonItems = data.getStringExtra("data");

            Type typeList = new TypeToken<List<CustomItem>>(){}.getType();
            Gson gson = new Gson();
            List<CustomItem> selectedCCID = gson.fromJson(jsonItems, typeList);
            String id = data.getStringExtra("id");
            listCCID.put(id, selectedCCID);

            if(selectedCCID == null || selectedCCID.size() == 0){

                adapterDeposit.updateStatus(id, "0");

            }

            Log.d(TAG, "onActivityResult: ");
        }
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
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
