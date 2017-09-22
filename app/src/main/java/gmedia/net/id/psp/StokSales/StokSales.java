package gmedia.net.id.psp.StokSales;

import android.support.v7.app.AppCompatActivity;
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

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.StokSales.Adapter.ListStokAdapter;

public class StokSales extends AppCompatActivity {

    private ListView lvStok;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvBarang;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stok_sales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Informasi Stok");

        initUI();
    }

    private void initUI() {

        lvStok = (ListView) findViewById(R.id.lv_stok);
        actvBarang = (AutoCompleteTextView) findViewById(R.id.actv_barang);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        getData();
    }

    private void getData() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);

        for(int i  = 1; i < 10; i++){
            masterList.add(new CustomItem(""+i, "Maul Cell "+i, "1"+i, (i*3)+"0000"));
        }

        final List<CustomItem> tableList = new ArrayList<>(masterList);

        getAutocompleteEvent(tableList);
        getTableList(tableList);
        pbProses.setVisibility(View.GONE);
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvBarang.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvBarang.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvBarang.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvBarang.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword) || item.getItem3().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTableList(items);
                    iv.hideSoftKey(StokSales.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvStok.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListStokAdapter adapter = new ListStokAdapter(StokSales.this, tableList);
            lvStok.setAdapter(adapter);

            lvStok.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
