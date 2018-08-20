package gmedia.net.id.psp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.RuntimePermissionsActivity;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.DaftarPiutang.PiutangPerOutlet;
import gmedia.net.id.psp.InfoDeposit.ActDeposit;
import gmedia.net.id.psp.LocationService.LocationUpdater;
import gmedia.net.id.psp.NavAccount.NavAccount;
import gmedia.net.id.psp.NavChangePassword.NavChangePassword;
import gmedia.net.id.psp.NavCheckin.ActKunjungan;
import gmedia.net.id.psp.NavCheckin.NavCheckin;
import gmedia.net.id.psp.NavHome.NavHome;
import gmedia.net.id.psp.NavKomplain.ActKomplain;
import gmedia.net.id.psp.NavKomplain.NavKomplain;
import gmedia.net.id.psp.NavMapsKunjungan.MapsKunjunganActivity;
import gmedia.net.id.psp.NavPreorderPerdana.ActPreorderPerdanaActivity;
import gmedia.net.id.psp.NavTambahCustomer.ActTambahOutlet;
import gmedia.net.id.psp.NavTambahCustomer.NavCustomer;
import gmedia.net.id.psp.NavVerifikasiOutlet.ActVerifikasiOutlet;
import gmedia.net.id.psp.OrderPerdana.CustomerPerdana;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.OrderTcash.ActOrderTcash;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.RiwayatPenjualan.RiwayatPenjualan;
import gmedia.net.id.psp.StokSales.StokSales;
import gmedia.net.id.psp.Utils.ServerURL;

public class MainNavigationActivity extends RuntimePermissionsActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;
    private SessionManager session;
    private static final int REQUEST_PERMISSIONS = 20;
    private static NavigationView navigationView;
    private Context context;
    private boolean dialogActive = false;
    private ItemValidation iv = new ItemValidation();

    private String version = "";
    private String latestVersion = "";
    private String link = "";
    private boolean updateRequired = false;
    private AlertDialog dialogVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        dialogActive = false;

        if (ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.WRITE_APN_SETTINGS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainNavigationActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            MainNavigationActivity.super.requestAppPermissions(new
                            String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_SETTINGS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WAKE_LOCK,
                            Manifest.permission.WRITE_APN_SETTINGS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.getBoolean("exit", false)){
                exitState = true;
                //finish();
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
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

        if(!session.isLoggedIn()){
            stopCurrentService();
            Intent intent = new Intent(MainNavigationActivity.this, LoginScreen.class);
            session.logoutUser(intent);
        }

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

        //requestSmsPermission();
    }

    private void requestSmsPermission() {
        String permission1 = Manifest.permission.RECEIVE_SMS;
        String permission2 = Manifest.permission.READ_SMS;
        //String permission3 = Manifest.permission.SMS;
        int grant1 = ContextCompat.checkSelfPermission(this, permission1);
        int grant2 = ContextCompat.checkSelfPermission(this, permission2);
        if ( grant1 != PackageManager.PERMISSION_GRANTED || grant2 != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission1;
            permission_list[0] = permission2;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    private void checkVersion(){

        PackageInfo pInfo = null;
        version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version = pInfo.versionName;
        getSupportActionBar().setSubtitle(getResources().getString(R.string.app_name) + " v "+ version);
        latestVersion = "";
        link = "";

        ApiVolley request = new ApiVolley(MainNavigationActivity.this, new JSONObject(), "GET", ServerURL.getLatestVersion, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {

            @Override
            public void onSuccess(String result) {

                JSONObject responseAPI;
                if(dialogVersion != null){
                    if(dialogVersion.isShowing()) dialogVersion.dismiss();
                }

                try {
                    responseAPI = new JSONObject(result);
                    String status = responseAPI.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){
                        latestVersion = responseAPI.getJSONObject("response").getString("versi");
                        link = responseAPI.getJSONObject("response").getString("link");
                        updateRequired = (iv.parseNullInteger(responseAPI.getJSONObject("response").getString("wajib")) == 1) ? true : false;

                        if(!version.trim().equals(latestVersion.trim()) && link.length() > 0){

                            if(updateRequired){

                                dialogVersion = new AlertDialog.Builder(MainNavigationActivity.this)
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle("Update")
                                        .setMessage("Versi terbaru "+latestVersion+" telah tersedia, mohon download versi terbaru.")
                                        .setPositiveButton("Update Sekarang", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                                startActivity(browserIntent);
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }else{

                                dialogVersion = new AlertDialog.Builder(MainNavigationActivity.this)
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setTitle("Update")
                                        .setMessage("Versi terbaru "+latestVersion+" telah tersedia, mohon download versi terbaru.")
                                        .setPositiveButton("Update Sekarang", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                                startActivity(browserIntent);
                                            }
                                        })
                                        .setNegativeButton("Update Nanti", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

                if(dialogVersion != null){
                    if(dialogVersion.isShowing()) dialogVersion.dismiss();
                }
            }
        });
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessageNoGps();
        }else{

            try {
                new CountDownTimer(4000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
                        if(!iv.isServiceRunning(MainNavigationActivity.this, LocationUpdater.class)){
                            startService(new Intent(getApplicationContext(), LocationUpdater.class));
                        }
                    }

                }.start();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void buildAlertMessageNoGps() {
        if(!dialogActive){
            dialogActive = true;
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Mohon Hidupkan Akses Lokasi (GPS) Anda.")
                    .setCancelable(false)
                    .setPositiveButton("Hidupkan", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    dialogActive = false;
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        doubleBackToExitPressedOnce = false;
        exitState = false;

        session = new SessionManager(MainNavigationActivity.this);
        if(!session.isLoggedIn()){

            if(iv.isServiceRunning(MainNavigationActivity.this, LocationUpdater.class)){
                stopCurrentService();
            }
            Intent intent = new Intent(MainNavigationActivity.this, LoginScreen.class);
            session.logoutUser(intent);
        }

        statusCheck();
        checkVersion();
    }

    public static void changeNavigationState(Context context, int position){
        switch (position){
            case 2:
                navigationView.setCheckedItem(R.id.nav_add_customer);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavCustomer();
                callFragment(context, fragment);
                break;
            case 14:
                navigationView.setCheckedItem(R.id.nav_checkin);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavCheckin();
                callFragment(context, fragment);
                break;
            case 12:
                navigationView.setCheckedItem(R.id.nav_complain);
                ((Activity) context).setTitle(navigationView.getMenu().getItem(position).getTitle().toString());
                fragment = new NavKomplain();
                callFragment(context, fragment);
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

        statusCheck();

        if (requestCode == 1) {

        }
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

        if (id == R.id.nav_home) {

            setTitle(item.getTitle());
            fragment = new NavHome();
            callFragment(context, fragment);
        } else if (id == R.id.nav_add_customer) {

            Intent intent = new Intent(MainNavigationActivity.this, ActTambahOutlet.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_order_mkios) {

            Intent intent = new Intent(MainNavigationActivity.this, ListReseller.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_penjualan_mkios) {

            Intent intent = new Intent(MainNavigationActivity.this, PenjualanMKIOS.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_order_perdana) {

            Intent intent = new Intent(MainNavigationActivity.this, CustomerPerdana.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_penjualan_perdana) {

            Intent intent = new Intent(MainNavigationActivity.this, PenjualanPerdana.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_order_tcash) {

            Intent intent = new Intent(MainNavigationActivity.this, ActOrderTcash.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_penjualan) {

            Intent intent = new Intent(MainNavigationActivity.this, PenjualanHariIni.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_riwayat_penjualan) {

            Intent intent = new Intent(MainNavigationActivity.this, RiwayatPenjualan.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_data_piutang) {

            Intent intent = new Intent(MainNavigationActivity.this, PiutangPerOutlet.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_stok_sales) {

            Intent intent = new Intent(MainNavigationActivity.this, StokSales.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_complain) {

            Intent intent = new Intent(MainNavigationActivity.this, ActKomplain.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_verifikasi_outlet) {

            Intent intent = new Intent(MainNavigationActivity.this, ActVerifikasiOutlet.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_checkin) {

            Intent intent = new Intent(MainNavigationActivity.this, ActKunjungan.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_info_deposit) {

            Intent intent = new Intent(MainNavigationActivity.this, ActDeposit.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_map_kunjungan) {

            Intent intent = new Intent(MainNavigationActivity.this, MapsKunjunganActivity.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.nav_preorder_perdana) {

            Intent intent = new Intent(MainNavigationActivity.this, ActPreorderPerdanaActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_akun) {

            setTitle(item.getTitle());
            fragment = new NavAccount();
            callFragment(context, fragment);
        } else if (id == R.id.nav_change_password) {

            setTitle(item.getTitle());
            fragment = new NavChangePassword();
            callFragment(context, fragment);
        } else if (id == R.id.nav_keluar) {

            if(iv.isServiceRunning(MainNavigationActivity.this, LocationUpdater.class)){
                stopCurrentService();
            }
            Intent intent = new Intent(MainNavigationActivity.this, LoginScreen.class);
            session.logoutUser(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void stopCurrentService(){
        try {
            //stopService(new Intent(getApplicationContext(), LocationUpdater.class));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Fragment fragment;
    private static void callFragment(Context context, Fragment fragment) {
        ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_main_container, fragment, fragment.getClass().getSimpleName()).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).addToBackStack(null).commit();
    }
}
