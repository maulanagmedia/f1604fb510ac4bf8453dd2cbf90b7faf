package gmedia.net.id.psp.NavPengajuanDeposit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.R;

public class MainPengajuanDeposit extends AppCompatActivity {

    private ItemValidation iv = new ItemValidation();
    private RelativeLayout llPengajuan, llHistory, llPembelianPerdana;
    private SessionManager session;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pengajuan_deposit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Pengajuan Deposit");

        context = this;


        initUI();

    }

    private void initUI() {

        llPengajuan = (RelativeLayout) findViewById(R.id.ll_pengajuan);
        llPembelianPerdana = (RelativeLayout) findViewById(R.id.ll_pembelian_perdana);
        llHistory = (RelativeLayout) findViewById(R.id.ll_history);

        initEvent();
    }

    private void initEvent() {

        llPembelianPerdana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, HeaderPengajuanDeposit.class);
                intent.putExtra("perdana", "1");
                startActivity(intent);
            }
        });

        llPengajuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, HeaderPengajuanDeposit.class);
                startActivity(intent);
            }
        });

        llHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, HistoryPengajuanDeposit.class);
                startActivity(intent);
            }
        });
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
        Intent intent = new Intent(MainPengajuanDeposit.this, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
