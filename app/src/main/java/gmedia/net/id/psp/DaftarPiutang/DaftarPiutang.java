package gmedia.net.id.psp.DaftarPiutang;

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

import gmedia.net.id.psp.DaftarPiutang.Adapter.ListPiutangAdapter;
import gmedia.net.id.psp.R;

public class DaftarPiutang extends AppCompatActivity {

    private ListView lvPiutang;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvOutlet;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_piutang);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Daftar Piutang");

        initUI();
    }

    private void initUI() {

        lvPiutang = (ListView) findViewById(R.id.lv_piutang);
        actvOutlet = (AutoCompleteTextView) findViewById(R.id.actv_outlet);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);

        getData();
    }

    private void getData() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);

        for(int i  = 1; i < 10; i++){
            masterList.add(new CustomItem(""+i, "Maul Cell "+i, "2017-09-0"+i, (i*3)+"0000", "Tunai / 34"));
        }

        final List<CustomItem> tableList = new ArrayList<>(masterList);

        getAutocompleteEvent(tableList);
        getTableList(tableList);
        pbProses.setVisibility(View.GONE);
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvOutlet.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvOutlet.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvOutlet.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvOutlet.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword) || item.getItem3().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTableList(items);
                    iv.hideSoftKey(DaftarPiutang.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvPiutang.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListPiutangAdapter adapter = new ListPiutangAdapter(DaftarPiutang.this, tableList);
            lvPiutang.setAdapter(adapter);

            lvPiutang.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
