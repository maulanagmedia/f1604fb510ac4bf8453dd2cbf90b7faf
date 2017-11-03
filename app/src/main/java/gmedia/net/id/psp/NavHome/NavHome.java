package gmedia.net.id.psp.NavHome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gmedia.net.id.psp.CustomView.WrapContentViewPager;
import gmedia.net.id.psp.DaftarPiutang.DaftarPiutang;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.NavHome.Adapter.HeaderSliderAdapter;
import gmedia.net.id.psp.OrderPerdana.CustomerPerdana;
import gmedia.net.id.psp.OrderPerdana.DetailOrderPerdana;
import gmedia.net.id.psp.OrderPerdana.ListBarang;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.StokSales.StokSales;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavHome extends Fragment implements ViewPager.OnPageChangeListener{

    private View layout;
    private Context context;

    private List<CustomItem> sliderList;
    private int dotsCount;
    private ImageView[] dots;
    private WrapContentViewPager vpHeaderSlider;
    private LinearLayout llPagerIndicator;
    private HeaderSliderAdapter mAdapter;
    private Timer timer;
    private int changeHeaderTimes = 5;
    private Boolean firstLoad = true;
    private ItemValidation iv = new ItemValidation();
    private ImageView ivAdv;
    private String offerImage = "";
    private String TAG = "Tes";
    private int startIndex, count;
    private SessionManager session;
    private View bottomView;
    private BottomSheetBehavior mBottomSheetBehavior2;
    private ImageView ivExpand;
    private LinearLayout llOrderMkios, llPenjualanMkios, llOrderPerdana, llPenjualanPerdana, llDaftarPiutang, llStokSales;
    private TextView tvNamaSales, tvTotalOmset, tvOmsetMkios, tvOmsetPerdana;
    private LinearLayout llAddCustomer, llCheckIn, llKomplain;

    public NavHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_nav_home, container, false);
        context = getContext();
        session = new SessionManager(context);

        initUI();
        return layout;
    }

    private void initUI() {

        // Header
        vpHeaderSlider = (WrapContentViewPager) layout.findViewById(R.id.pager_introduction);
        vpHeaderSlider.setScrollDurationFactor(4);
        llPagerIndicator = (LinearLayout) layout.findViewById(R.id.ll_view_pager_dot_count);

        //Bottom view
        ivExpand = (ImageView) layout.findViewById(R.id.iv_expand);
        bottomView = layout.findViewById(R.id.ns_profile);
        mBottomSheetBehavior2 = BottomSheetBehavior.from(bottomView);
        mBottomSheetBehavior2.setHideable(false);
        int[] display = iv.getScreenResolution(context);
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        mBottomSheetBehavior2.setPeekHeight((display[1] - mActionBarSize)/10);
        mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);

        ivExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
            }
        });

        mBottomSheetBehavior2.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //button
        llOrderMkios = (LinearLayout) layout.findViewById(R.id.ll_order_mkios);
        llPenjualanMkios = (LinearLayout) layout.findViewById(R.id.ll_penjualan_mkios);
        llOrderPerdana = (LinearLayout) layout.findViewById(R.id.ll_order_perdana);
        llPenjualanPerdana = (LinearLayout) layout.findViewById(R.id.ll_penjualan_perdana);
        llDaftarPiutang = (LinearLayout) layout.findViewById(R.id.ll_daftar_piutang);
        llStokSales = (LinearLayout) layout.findViewById(R.id.ll_stok_sales);
        llAddCustomer = (LinearLayout) layout.findViewById(R.id.ll_add_customer);
        llCheckIn = (LinearLayout) layout.findViewById(R.id.ll_checkin);
        llKomplain = (LinearLayout) layout.findViewById(R.id.ll_complaint);

        tvNamaSales = (TextView) layout.findViewById(R.id.tv_nama_sales);
        tvTotalOmset = (TextView) layout.findViewById(R.id.tv_total_omset);
        tvOmsetMkios = (TextView) layout.findViewById(R.id.tv_omset_mkios);
        tvOmsetPerdana = (TextView) layout.findViewById(R.id.tv_omset_perdana);

        llOrderMkios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListReseller.class);
                context.startActivity(intent);
            }
        });

        llPenjualanMkios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PenjualanMKIOS.class);
                context.startActivity(intent);
            }
        });

        llOrderPerdana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CustomerPerdana.class);
                context.startActivity(intent);
            }
        });

        llPenjualanPerdana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PenjualanPerdana.class);
                context.startActivity(intent);
            }
        });

        llDaftarPiutang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DaftarPiutang.class);
                context.startActivity(intent);
            }
        });

        llStokSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StokSales.class);
                context.startActivity(intent);
            }
        });

        llAddCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainNavigationActivity.changeNavigationState(context, 2);
            }
        });

        llCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainNavigationActivity.changeNavigationState(context, 9);
            }
        });

        llKomplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainNavigationActivity.changeNavigationState(context, 10);
            }
        });

        tvNamaSales.setText(session.getUser());

        getListHeaderSlider();
        getUserOmset();
    }

    //region Slider Header
    private void getListHeaderSlider() {

        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", ServerURL.getPromosi, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                JSONObject responseAPI;
                try {
                    responseAPI = new JSONObject(result);
                    String status = responseAPI.getJSONObject("metadata").getString("status");
                    sliderList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = responseAPI.getJSONArray("response");

                        for(int i = 0; i < jsonArray.length();i++){

                            JSONObject item = jsonArray.getJSONObject(i);

                            sliderList.add(new CustomItem(item.getString("id"), item.getString("image"), item.getString("keterangan"), item.getString("link")));
                        }
                    }

                    if(firstLoad){
                        setViewPagerTimer(changeHeaderTimes);
                        firstLoad = false;
                    }

                    setHeaderSlider();
                    setUiPageViewController();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void getUserOmset() {

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getUserOmset, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                JSONObject responseAPI;
                try {

                    responseAPI = new JSONObject(result);
                    String status = responseAPI.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONObject item = responseAPI.getJSONObject("response");
                        tvOmsetMkios.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getString("omset_mkios"))));
                        tvOmsetPerdana.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getString("omset_perdana"))));
                        tvTotalOmset.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getString("omset_mkios")) + iv.parseNullDouble(item.getString("omset_perdana"))));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void setHeaderSlider(){

        vpHeaderSlider.setAdapter(null);
        mAdapter = null;
        mAdapter = new HeaderSliderAdapter(context, sliderList);
        vpHeaderSlider.setAdapter(mAdapter);
        vpHeaderSlider.setCurrentItem(0);
        vpHeaderSlider.setOnPageChangeListener(this);
    }

    private void setUiPageViewController() {

        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];
        llPagerIndicator.removeAllViews();

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(context.getResources().getDrawable(R.drawable.dot_unselected_item));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            llPagerIndicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(context.getResources().getDrawable(R.drawable.dot_selected_item));
    }

    private void setViewPagerTimer(int seconds){
        timer = new Timer(); // At this line a new Thread will be created
        timer.scheduleAtFixedRate(new RemindTask(), 0, seconds * 1000);
    }

    class RemindTask extends TimerTask {

        @Override
        public void run() {

            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {

                    if(vpHeaderSlider.getCurrentItem() == mAdapter.getCount() - 1){
                        vpHeaderSlider.setCurrentItem(0);

                    }else{
                        vpHeaderSlider.setCurrentItem(vpHeaderSlider.getCurrentItem() + 1);
                    }
                }
            });

        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(context.getResources().getDrawable(R.drawable.dot_unselected_item));
        }

        dots[position].setImageDrawable(context.getResources().getDrawable(R.drawable.dot_selected_item));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
