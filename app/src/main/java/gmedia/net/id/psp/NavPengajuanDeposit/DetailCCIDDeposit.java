package gmedia.net.id.psp.NavPengajuanDeposit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gmedia.net.id.psp.NavPengajuanDeposit.Adapter.ListCCIDDDepositAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailCCIDDeposit extends AppCompatActivity {

    private Context context;
    private ItemValidation iv = new ItemValidation();
    private DialogBox dialogBox;
    private EditText edtOrder, edtTotalCCID, edtTotalHarga;
    private ListView lvPerdana;
    private List<CustomItem> listCCID = new ArrayList<>();
    private Button btnScan, btnProses;
    private String idTransaksi = "";
    private ListCCIDDDepositAdapter adapter;
    private SessionManager session;
    private int conter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cciddeposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        context = this;
        dialogBox = new DialogBox(context);
        session = new SessionManager(context);

        setTitle("Scan CCID");

        initUI();
        initEvent();
    }

    private void initUI() {

        edtOrder = (EditText) findViewById(R.id.edt_order);
        btnScan = (Button) findViewById(R.id.btn_scan);
        lvPerdana = (ListView) findViewById(R.id.lv_perdana);
        edtTotalCCID = (EditText) findViewById(R.id.edt_total_ccid);
        edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
        btnProses = (Button) findViewById(R.id.btn_proses);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            idTransaksi = bundle.getString("id", "");
            String order = bundle.getString("order", "");
            edtOrder.setText(order);
        }

        listCCID = new ArrayList<>();
        adapter = new ListCCIDDDepositAdapter((Activity) context, listCCID);
        lvPerdana.setAdapter(adapter);
    }

    private void initEvent() {

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openScanBarcode();
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(listCCID == null || listCCID.size() == 0){

                    DialogBox.showDialog(context, 3, "Data masih kosong, harap scan barang");
                    return;
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menyimpan data ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent data = new Intent();
                                data.putExtra("id", idTransaksi);
                                Gson gson = new Gson();
                                String jsonItems = gson.toJson(listCCID);
                                data.putExtra("data",jsonItems );
                                setResult(RESULT_OK, data);
                                finish();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    private List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    private void openScanBarcode() {

        Collection<String> ONE_D_CODE_TYPES =
                list("CODE_128B","QR_CODE");

        IntentIntegrator integrator = new IntentIntegrator(DetailCCIDDeposit.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {

                DialogBox.showDialog(context, 3, "Gagal mendapatkan CCID, silahkan ulangi kembali");
            } else {

                getDetailCCID(result.getContents());
                //String[] a = {"xdxxx0350000042506809", "xdxxx0350000042506807", "xdxxx0350000042506808", "xdxxx0350000042506806", "xdxxx0050000371774706"};
                //getDetailCCID(a[conter]);
                conter++;
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getDetailCCID(final String ccid) {

        dialogBox.showDialog(true);
        String nik = session.getNikGA();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("ccid", ccid);
            jBody.put("id", idTransaksi);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getCCIDDeposit, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                dialogBox.dismissDialog();
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONObject jo = response.getJSONObject("response");
                        adapter.addData(new CustomItem(
                                jo.getString("kodebrg"),
                                jo.getString("namabrg"),
                                jo.getString("ccid"),
                                jo.getString("harga"),
                                jo.getString("jumlah"),
                                jo.getString("nobukti"),
                                jo.getString("id")));
                    }else{
                        DialogBox.showDialog(context,2, "Barang tidak ditemukan di surat jalan hari ini");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    DialogBox.showDialog(context,2, "Terjadi kesalahan saat memuat data, harap ulangi");
                }
            }

            @Override
            public void onError(String result) {

                dialogBox.dismissDialog();
                DialogBox.showDialog(context, 2, "Terjadi kesalahan saat memuat data, harap ulangi");
            }
        });
    }

    public void updateTotal(){

        if(adapter != null && edtTotalCCID != null && edtTotalHarga != null){

            List<CustomItem> barangSelected = adapter.getDataList();
            edtTotalCCID.setText(String.valueOf(barangSelected.size()));
            double total = 0;
            for(CustomItem item : barangSelected){

                total+= iv.parseNullDouble(item.getItem4());
            }

            edtTotalHarga.setText(iv.ChangeToRupiahFormat(total));
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
        //super.onBackPressed();
        Intent data = new Intent();
        data.putExtra("id", idTransaksi);
        data.putExtra("data","" );
        setResult(RESULT_OK, data);
        finish();

        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
