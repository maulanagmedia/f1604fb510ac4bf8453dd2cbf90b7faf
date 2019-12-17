package gmedia.net.id.psp;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leonardus.irfan.bluetoothprinter.PspPrinter;
import com.maulana.custommodul.SessionManager;

import gmedia.net.id.psp.DaftarPiutang.DaftarPiutang;
import gmedia.net.id.psp.OrderPerdana.CustomerPerdana;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.PelunasanPenjualan.PelunasanPenjualan;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.SemuaPenjualan.SemuaPenjualan;
import gmedia.net.id.psp.StokSales.StokSales;

public class MainActivity extends AppCompatActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private LinearLayout llOrderPulsa, llOrderPerdana, llPenjualanPulsa, llPenjualanPerdana, llSemuaPenjualan, llpelunasanPenjualan, llDaftarPiutang;
    SessionManager session;
    private LinearLayout llStokSales;
    private PspPrinter printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printer = new PspPrinter(this);
        printer.startService();

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.getBoolean("exit", false)){

                printer.stopService();

                exitState = true;
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }

        initUI();
    }

    private void initUI() {

        llOrderPulsa = (LinearLayout) findViewById(R.id.ll_order_pulsa);
        llOrderPerdana = (LinearLayout) findViewById(R.id.ll_order_perdana);
        llPenjualanPulsa = (LinearLayout) findViewById(R.id.ll_penjualan_pulsa);
        llPenjualanPerdana = (LinearLayout) findViewById(R.id.ll_penjualan_pendana);
        llSemuaPenjualan = (LinearLayout) findViewById(R.id.ll_semua_penjualan);
        llpelunasanPenjualan = (LinearLayout) findViewById(R.id.ll_pelunasan_penjualan);
        llDaftarPiutang = (LinearLayout) findViewById(R.id.ll_daftar_piutang);
        llStokSales = (LinearLayout) findViewById(R.id.ll_stok_sales);

        session = new SessionManager(MainActivity.this);

        initEvent();
    }

    private void initEvent(){


    }

    public void selectMenu(View v){

        Intent intent = null;

        if(v.getId() == llOrderPulsa.getId()){

            intent = new Intent(MainActivity.this, ListReseller.class);
        }else if(v.getId() == llOrderPerdana.getId()){

            intent = new Intent(MainActivity.this, CustomerPerdana.class);
        }else if(v.getId() == llPenjualanPulsa.getId()){

            intent = new Intent(MainActivity.this, PenjualanMKIOS.class);
        }else if(v.getId() == llPenjualanPerdana.getId()){

            intent = new Intent(MainActivity.this, PenjualanPerdana.class);
        }else if(v.getId() == llSemuaPenjualan.getId()){

            intent = new Intent(MainActivity.this, SemuaPenjualan.class);
        }else if(v.getId() == llpelunasanPenjualan.getId()){

            intent = new Intent(MainActivity.this, PelunasanPenjualan.class);
        }else if(v.getId() == llDaftarPiutang.getId()){

            intent = new Intent(MainActivity.this, DaftarPiutang.class);
        }else if(v.getId() == llStokSales.getId()){

            intent = new Intent(MainActivity.this, StokSales.class);
        }

        if(intent != null){
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logout:

                loguoutUser();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loguoutUser(){

        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        session.logoutUser(intent);
    }

    @Override
    public void onBackPressed() {

        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState && !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, timerClose);
    }
}
