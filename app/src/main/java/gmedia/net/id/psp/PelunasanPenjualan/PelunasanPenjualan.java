package gmedia.net.id.psp.PelunasanPenjualan;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.PelunasanPenjualan.Adapter.ListPelunasanPenjualanAdapter;
import gmedia.net.id.psp.R;

public class PelunasanPenjualan extends AppCompatActivity {

    private ListView lvPenjualan;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvPelanggan;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pelunasan_penjualan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Pelunasan Penjualan");

        initUI();
    }

    private void initUI() {

        lvPenjualan = (ListView) findViewById(R.id.lv_penjualan);
        actvPelanggan = (AutoCompleteTextView) findViewById(R.id.actv_pelanggan);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        btnAdd = (FloatingActionButton) findViewById(R.id.btn_add);

        getData();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PelunasanPenjualan.this, VerifikasiPelunasan.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void getData() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);

        for(int i  = 1; i < 100; i++){
            masterList.add(new CustomItem(""+i, "Maul Cell "+i, "Nonota"+i, "300000"));
        }

        final List<CustomItem> tableList = new ArrayList<>(masterList);

        getAutocompleteEvent(tableList);
        getTableList(tableList);
        pbProses.setVisibility(View.GONE);
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvPelanggan.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvPelanggan.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvPelanggan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvPelanggan.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTableList(items);
                    iv.hideSoftKey(PelunasanPenjualan.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPenjualan.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListPelunasanPenjualanAdapter adapter = new ListPelunasanPenjualanAdapter(PelunasanPenjualan.this, tableList);
            lvPenjualan.setAdapter(adapter);

            lvPenjualan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);

                }
            });
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
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
