package gmedia.net.id.psp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.NavMapsKunjungan.MapsKunjunganActivity;

public class MapsOutletActivity extends AppCompatActivity {

    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private CustomMapView mvMap;
    private GoogleMap googleMap;
    private ProgressBar pbLoading;
    private String latitude = "", longitude = "", latitudeOutlet = "", longitudeOutlet = "";
    private String nama = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_outlet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Peta Outlet / Event");

        initUI();
    }

    private void  initUI(){

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        session = new SessionManager(MapsOutletActivity.this);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(MapsOutletActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            latitude = bundle.getString("lat");
            longitude = bundle.getString("long");
            latitudeOutlet = bundle.getString("lat_outlet");
            longitudeOutlet = bundle.getString("long_outlet");
            nama = bundle.getString("nama");
            getSupportActionBar().setSubtitle(nama);

            setPointMap();
        }
    }

    private void setPointMap(){

        mvMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                googleMap.clear();

                Marker markerS = googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(iv.getBitmapFromVectorDrawable(MapsOutletActivity.this, R.drawable.ic_sales_map)))
                        .title("Anda")
                        .position(new LatLng(iv.parseNullDouble(latitude), iv.parseNullDouble(longitude))));

                Marker markerO = googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(iv.getBitmapFromVectorDrawable(MapsOutletActivity.this, R.drawable.ic_outlet_map)))
                        .title(nama)
                        .position(new LatLng(iv.parseNullDouble(latitudeOutlet), iv.parseNullDouble(longitudeOutlet))));

                markerS.showInfoWindow();
                markerO.showInfoWindow();
                if (ActivityCompat.checkSelfPermission(MapsOutletActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsOutletActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MapsOutletActivity.this, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(MapsOutletActivity.this);
                LatLng position = new LatLng(iv.parseNullDouble(latitudeOutlet), iv.parseNullDouble(longitudeOutlet));
                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(13).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

    }
}
