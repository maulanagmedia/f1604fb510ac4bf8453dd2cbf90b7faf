package gmedia.net.id.psp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.maulana.custommodul.RuntimePermissionsActivity;
import com.maulana.custommodul.SessionManager;

import gmedia.net.id.psp.DaftarPiutang.DaftarPiutang;
import gmedia.net.id.psp.NavAccount.NavAccount;
import gmedia.net.id.psp.NavCheckin.NavCheckin;
import gmedia.net.id.psp.NavHome.NavHome;
import gmedia.net.id.psp.NavKomplain.NavKomplain;
import gmedia.net.id.psp.NavTambahCustomer.NavCustomer;
import gmedia.net.id.psp.OrderPerdana.CustomerPerdana;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.StokSales.StokSales;
import gmedia.net.id.psp.TambahCustomer.DetailCustomer;

public class MainNavigationActivity extends RuntimePermissionsActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;
    private SessionManager session;
    private static final int REQUEST_PERMISSIONS = 20;
    private static NavigationView navigationView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        if (ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {

            MainNavigationActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WAKE_LOCK}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.getBoolean("exit", false)){
                exitState = true;
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        session = new SessionManager(MainNavigationActivity.this);

        if(savedInstanceState == null){

            if(bundle != null && bundle.getBoolean("tambahCustomer", false)){
                navigationView.setCheckedItem(R.id.nav_add_customer);
                setTitle(navigationView.getMenu().getItem(3).getTitle().toString());
                fragment = new NavCustomer();
                callFragment(context, fragment);
            }else{
                navigationView.setCheckedItem(R.id.nav_home);
                setTitle(navigationView.getMenu().getItem(0).getTitle().toString());
                /*FrameLayout flContainer = (FrameLayout) findViewById(R.id.fl_main_container);
                flContainer.removeAllViews();*/
                fragment = new NavHome();
                callFragment(context, fragment);
            }
        }
    }

    public static void changeNavigationState(Context context, int position){
        switch (position){
            case 2:
                navigationView.setCheckedItem(R.id.nav_add_customer);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavCustomer();
                callFragment(context, fragment);
                break;
            case 9:
                navigationView.setCheckedItem(R.id.nav_checkin);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavCheckin();
                callFragment(context, fragment);
                break;
            case 10:
                navigationView.setCheckedItem(R.id.nav_complain);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavKomplain();
                callFragment(context, fragment);
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Origin backstage
            if (doubleBackToExitPressedOnce) {
                Intent intent = new Intent(MainNavigationActivity.this, MainNavigationActivity.class);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        setTitle(item.getTitle());

        if (id == R.id.nav_home) {
            fragment = new NavHome();
            callFragment(context, fragment);
        } else if (id == R.id.nav_akun) {
            fragment = new NavAccount();
            callFragment(context, fragment);
        } else if (id == R.id.nav_add_customer) {
            fragment = new NavCustomer();
            callFragment(context, fragment);
        }else if (id == R.id.nav_order_mkios) {

            Intent intent = new Intent(MainNavigationActivity.this, ListReseller.class);
            startActivity(intent);
        } else if (id == R.id.nav_penjualan_mkios) {

            Intent intent = new Intent(MainNavigationActivity.this, PenjualanMKIOS.class);
            startActivity(intent);
        } else if (id == R.id.nav_order_perdana) {

            Intent intent = new Intent(MainNavigationActivity.this, CustomerPerdana.class);
            startActivity(intent);
        } else if (id == R.id.nav_penjualan_perdana) {

            Intent intent = new Intent(MainNavigationActivity.this, PenjualanPerdana.class);
            startActivity(intent);
        } else if (id == R.id.nav_data_piutang) {

            Intent intent = new Intent(MainNavigationActivity.this, DaftarPiutang.class);
            startActivity(intent);
        } else if (id == R.id.nav_stok_sales) {

            Intent intent = new Intent(MainNavigationActivity.this, StokSales.class);
            startActivity(intent);
        } else if (id == R.id.nav_checkin) {

            fragment = new NavCheckin();
            callFragment(context, fragment);
        } else if (id == R.id.nav_complain) {

            fragment = new NavKomplain();
            callFragment(context, fragment);
        } else if (id == R.id.nav_keluar) {

            Intent intent = new Intent(MainNavigationActivity.this, LoginScreen.class);
            session.logoutUser(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static Fragment fragment;
    private static void callFragment(Context context, Fragment fragment) {
        ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_main_container, fragment, fragment.getClass().getSimpleName()).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).addToBackStack(null).commit();
    }
}
