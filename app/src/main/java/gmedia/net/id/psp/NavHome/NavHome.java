package gmedia.net.id.psp.NavHome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
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

import gmedia.net.id.psp.ActKonsinyasi.ActKonsinyasi;
import gmedia.net.id.psp.CustomView.WrapContentViewPager;
import gmedia.net.id.psp.DaftarPiutang.PiutangPerOutlet;
import gmedia.net.id.psp.InfoDeposit.ActDeposit;
import gmedia.net.id.psp.NavEvent.DetailEventManual;
import gmedia.net.id.psp.NavKunjungan.ActKunjungan;
import gmedia.net.id.psp.NavEvent.ActEvent;
import gmedia.net.id.psp.NavEvent.DetailEvent;
import gmedia.net.id.psp.NavHome.Adapter.HeaderSliderAdapter;
import gmedia.net.id.psp.NavKomplain.ActKomplain;
import gmedia.net.id.psp.NavMarketSurvey.ActMarketSurvey;
import gmedia.net.id.psp.NavMarketSurveyAOC.ActMarketSurveyAOC;
import gmedia.net.id.psp.NavMarketSurveyMD.ActMarketSurveyMD;
import gmedia.net.id.psp.NavPOL.ListOutletLocation;
import gmedia.net.id.psp.NavPengajuanDeposit.MainPengajuanDeposit;
import gmedia.net.id.psp.NavPreorderPerdana.ActPreorderPerdanaActivity;
import gmedia.net.id.psp.NavTambahCustomer.ActTambahOutlet;
import gmedia.net.id.psp.NavVerifikasiOutlet.ActVerifikasiOutlet;
import gmedia.net.id.psp.OrderNgrs.ActOrderNGRS;
import gmedia.net.id.psp.OrderPerdana.CustomerPerdana;
import gmedia.net.id.psp.OrderPulsa.ListReseller;
import gmedia.net.id.psp.OrderTcash.ActOrderTcash;
import gmedia.net.id.psp.PenjualanHariIni.PenjualanHariIni;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.RiwayatPenjualan.RiwayatPenjualan;
import gmedia.net.id.psp.StokSales.StokSales;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavHome extends Fragment implements ViewPager.OnPageChangeListener {

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
	private TextView tvNamaSales, tvTotalOmset, tvOmsetMkios, tvOmsetPerdana, tvTargetMkios, tvGapMkios, tvTargetPerdana, tvGapPerdana, tvTargetPJP, tvPencapaianPJP, tvGapPJP;
	private LinearLayout llAddCustomer, llCheckIn, llKomplain;
	private LinearLayout ll1, ll1a, ll2, ll3, ll3a, ll4, ll5, ll6, ll7, ll8, ll9, ll10;
	private LinearLayout llOrderTcash;
	private LinearLayout llPenjualan;
	private LinearLayout llRiwayatPenjualan;
	private LinearLayout llVerifikasiOutlet;
	private LinearLayout llInfoDeposit;
	private TextView tvJabatan;
	private TextView tvOutletBaru;
	private LinearLayout llMapsKunjungan;
	private LinearLayout llPreorderPerdana;
	private LinearLayout llEvent, llMarketSurvey, llMarketSurveyAoc;
	private LinearLayout llDirectSelling;
	private LinearLayout llOrderNgrs;
	private LinearLayout llMenuPenjualanReseller, llMenuDirectSelling, llMenuDirectSellingManual, llMenuDDS, llMenuSPV, llMenuMD, llMenuOperasional, llPengajuanDeposit, llMarketSurveyMD, llKonsinyasi;
	private LinearLayout llIDS, llDS;
	private TextView tvDSTargetMkios, tvDSOmsetMkios, tvDSGapMkios, tvDSTargetPerdana, tvDSOmsetPerdana, tvDSGapPerdana;
	private TextView tvDSTargetDataMkios, tvDSOmsetDataMkios, tvDSGAPDataMkios, tvDSTargetRevenueDigital, tvDSOmsetRevenueDigital, tvDSGAPRevenueDigital, tvDSDailyPJP, tvDSEffectiveCall, tvDSGAPPJP, tvDSTargetAkuisisi, tvDSAkuisisi, tvDSGAPAkuisisi;

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
				new int[]{android.R.attr.actionBarSize});
		int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();
		mBottomSheetBehavior2.setPeekHeight((display[1] - mActionBarSize) / 10);
		mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);

		ivExpand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
					mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
				} else if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
					mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
				} else if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
					mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
				}
			}
		});

		mBottomSheetBehavior2.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

				if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_down));
				} else if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
				} else if (mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
					ivExpand.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_arrow_up));
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});

		//button
		ll1 = (LinearLayout) layout.findViewById(R.id.ll_1);
		ll1a = (LinearLayout) layout.findViewById(R.id.ll_1a);
		ll2 = (LinearLayout) layout.findViewById(R.id.ll_2);
		ll3 = (LinearLayout) layout.findViewById(R.id.ll_3);
		ll3a = (LinearLayout) layout.findViewById(R.id.ll_3a);
		ll4 = (LinearLayout) layout.findViewById(R.id.ll_4);
		ll5 = (LinearLayout) layout.findViewById(R.id.ll_5);
		ll6 = (LinearLayout) layout.findViewById(R.id.ll_6);
		ll7 = (LinearLayout) layout.findViewById(R.id.ll_7);
		ll8 = (LinearLayout) layout.findViewById(R.id.ll_8);
		ll9 = (LinearLayout) layout.findViewById(R.id.ll_9);
		ll10 = (LinearLayout) layout.findViewById(R.id.ll_10);

		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll1.getLayoutParams();

		lp.width = display[0];
		lp.height = (display[1] - mActionBarSize) * 15 / 100;

		ll1.setLayoutParams(lp);
		ll1a.setLayoutParams(lp);
		ll2.setLayoutParams(lp);
		ll3.setLayoutParams(lp);
		ll3a.setLayoutParams(lp);
		ll4.setLayoutParams(lp);
		ll5.setLayoutParams(lp);
		ll6.setLayoutParams(lp);
		ll7.setLayoutParams(lp);
		ll8.setLayoutParams(lp);
		ll9.setLayoutParams(lp);
		ll10.setLayoutParams(lp);


		llMenuPenjualanReseller = (LinearLayout) layout.findViewById(R.id.ll_penjualan_reseller);
		llMenuDirectSelling = (LinearLayout) layout.findViewById(R.id.ll_menu_direct_selling);
		llMenuDirectSellingManual = (LinearLayout) layout.findViewById(R.id.ll_direct_selling_manual);
		llMenuDDS = (LinearLayout) layout.findViewById(R.id.ll_menu_dds);
		llMenuSPV = (LinearLayout) layout.findViewById(R.id.ll_menu_spv);
		llMenuOperasional = (LinearLayout) layout.findViewById(R.id.ll_operasional);
		llMenuMD = (LinearLayout) layout.findViewById(R.id.ll_menu_md);

		llAddCustomer = (LinearLayout) layout.findViewById(R.id.ll_add_customer);           // 1
		llOrderMkios = (LinearLayout) layout.findViewById(R.id.ll_order_mkios);             // 2
		llOrderPerdana = (LinearLayout) layout.findViewById(R.id.ll_order_perdana);         // 3
		llOrderTcash = (LinearLayout) layout.findViewById(R.id.ll_order_tcash);             // 4
		llPenjualan = (LinearLayout) layout.findViewById(R.id.ll_penjualan);                // 5

		llRiwayatPenjualan = (LinearLayout) layout.findViewById(R.id.ll_riwayat_penjualan); // 6
		llDaftarPiutang = (LinearLayout) layout.findViewById(R.id.ll_daftar_piutang);       // 7
		llStokSales = (LinearLayout) layout.findViewById(R.id.ll_stok_sales);               // 8
		llKomplain = (LinearLayout) layout.findViewById(R.id.ll_complaint);                 // 9
		llVerifikasiOutlet = (LinearLayout) layout.findViewById(R.id.ll_verifikasi_outlet); // 10

		llCheckIn = (LinearLayout) layout.findViewById(R.id.ll_checkin);                    // 11
		llInfoDeposit = (LinearLayout) layout.findViewById(R.id.ll_info_deposit);           // 12
		llEvent = (LinearLayout) layout.findViewById(R.id.ll_event);                        // 13
		llMarketSurvey = (LinearLayout) layout.findViewById(R.id.ll_market_survey);         // 14
		llMarketSurveyAoc = (LinearLayout) layout.findViewById(R.id.ll_market_survey_aoc);  // 15

		llPreorderPerdana = (LinearLayout) layout.findViewById(R.id.ll_preorder_perdana);   // 16
		llDirectSelling = (LinearLayout) layout.findViewById(R.id.ll_direct_selling);       // 17
		llPengajuanDeposit = (LinearLayout) layout.findViewById(R.id.ll_pengajuan_deposit); // 18
		llKonsinyasi = (LinearLayout) layout.findViewById(R.id.ll_konsinyasi);              // 19
		llMarketSurveyMD = (LinearLayout) layout.findViewById(R.id.ll_market_survey_md);    // 20

		llPenjualanMkios = (LinearLayout) layout.findViewById(R.id.ll_penjualan_mkios);     // 21
		llPenjualanPerdana = (LinearLayout) layout.findViewById(R.id.ll_penjualan_perdana); // 22
		llMapsKunjungan = (LinearLayout) layout.findViewById(R.id.ll_maps_kunjungan);       // 23
		llOrderNgrs = (LinearLayout) layout.findViewById(R.id.ll_order_ngrs);               // 24

		tvNamaSales = (TextView) layout.findViewById(R.id.tv_nama_sales);
		tvTotalOmset = (TextView) layout.findViewById(R.id.tv_total_omset);

		tvTargetMkios = (TextView) layout.findViewById(R.id.tv_target_mkios);
		tvOmsetMkios = (TextView) layout.findViewById(R.id.tv_omset_mkios);
		tvGapMkios = (TextView) layout.findViewById(R.id.tv_gap_mkios);

		tvTargetPerdana = (TextView) layout.findViewById(R.id.tv_target_perdana);
		tvOmsetPerdana = (TextView) layout.findViewById(R.id.tv_omset_perdana);
		tvGapPerdana = (TextView) layout.findViewById(R.id.tv_gap_perdana);

		tvTargetPJP = (TextView) layout.findViewById(R.id.tv_target_pjp);
		tvPencapaianPJP = (TextView) layout.findViewById(R.id.tv_pencapaian_pjp);
		tvGapPJP = (TextView) layout.findViewById(R.id.tv_gap_pjp);

		tvOutletBaru = (TextView) layout.findViewById(R.id.tv_outlet_baru);
		tvJabatan = (TextView) layout.findViewById(R.id.tv_jabatan);

		llIDS = (LinearLayout) layout.findViewById(R.id.ll_ids);
		llDS = (LinearLayout) layout.findViewById(R.id.ll_ds);

		if (session.getLevel().equals("SF")) {

			llIDS.setVisibility(View.VISIBLE);
			llDS.setVisibility(View.GONE);
		} else {

			llIDS.setVisibility(View.GONE);
			llDS.setVisibility(View.VISIBLE);
			llMenuDDS.setVisibility(View.VISIBLE);
		}

		tvDSTargetMkios = (TextView) layout.findViewById(R.id.tv_ds_target_mkios);
		tvDSOmsetMkios = (TextView) layout.findViewById(R.id.tv_ds_omset_mkios);
		tvDSGapMkios = (TextView) layout.findViewById(R.id.tv_ds_gap_mkios);

		tvDSTargetDataMkios = (TextView) layout.findViewById(R.id.tv_ds_target_data_mkios);
		tvDSOmsetDataMkios = (TextView) layout.findViewById(R.id.tv_ds_omset_data_mkios);
		tvDSGAPDataMkios = (TextView) layout.findViewById(R.id.tv_ds_gap_data_mkios);

		tvDSTargetRevenueDigital = (TextView) layout.findViewById(R.id.tv_ds_target_revenue_digital);
		tvDSOmsetRevenueDigital = (TextView) layout.findViewById(R.id.tv_ds_omset_revenue_digital);
		tvDSGAPRevenueDigital = (TextView) layout.findViewById(R.id.tv_ds_gap_revenue_digital);

		tvDSDailyPJP = (TextView) layout.findViewById(R.id.tv_ds_daily_pjp);
		tvDSEffectiveCall = (TextView) layout.findViewById(R.id.tv_ds_effective_call);
		tvDSGAPPJP = (TextView) layout.findViewById(R.id.tv_ds_gap_pjp);

		tvDSTargetPerdana = (TextView) layout.findViewById(R.id.tv_ds_target_perdana);
		tvDSOmsetPerdana = (TextView) layout.findViewById(R.id.tv_ds_omset_perdana);
		tvDSGapPerdana = (TextView) layout.findViewById(R.id.tv_ds_gap_perdana);

		tvDSTargetAkuisisi = (TextView) layout.findViewById(R.id.tv_ds_target_akuisisi_kantin);
		tvDSAkuisisi = (TextView) layout.findViewById(R.id.tv_ds_akuisisi_kantin);
		tvDSGAPAkuisisi = (TextView) layout.findViewById(R.id.tv_ds_gap_akuisisi_kantin);

		initEvent();

		tvNamaSales.setText(session.getUser());

		getListHeaderSlider();
		//getUserOmset();
		getDataAkun();

        /*if(session.getUserInfo(SessionManager.TAG_LEVEL).equals("DS")){

            llEvent.setVisibility(View.VISIBLE);
            llMarketSurvey.setVisibility(View.VISIBLE);
            ll5.setVisibility(View.VISIBLE);
        }else{
            llEvent.setVisibility(View.GONE);
            llMarketSurvey.setVisibility(View.GONE);
            ll5.setVisibility(View.GONE);
        }*/

		// Pembagian layout
		if (!session.getJabatan().equals("TSA")) { // AOC, SF, SPV

			llMenuPenjualanReseller.setVisibility(View.VISIBLE);
		} else {
			llMenuPenjualanReseller.setVisibility(View.GONE);
		}

		if (session.getJabatan().equals("TSA") || session.getJabatan().equals("AOC") || session.getJabatan().equals("SPVDS") || session.getJabatan().equals("BM")) { // DS

			llMenuDirectSelling.setVisibility(View.VISIBLE);
			llMenuDDS.setVisibility(View.VISIBLE);
		} else {
			llMenuDirectSelling.setVisibility(View.GONE);
		}

		if (session.getJabatan().equals("SPVDS") || session.getJabatan().equals("SPVSF") || session.getJabatan().equals("BM")) {

			llMenuSPV.setVisibility(View.VISIBLE);
			llMenuDDS.setVisibility(View.VISIBLE);
		} else {
			llMenuSPV.setVisibility(View.GONE);
		}

		if (true) {

			llMenuMD.setVisibility(View.VISIBLE);
		} else {
			llMenuMD.setVisibility(View.GONE);
		}
	}

	private void initEvent() {

		llAddCustomer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, ActTambahOutlet.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llOrderMkios.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ListReseller.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llPenjualanMkios.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, PenjualanMKIOS.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llOrderPerdana.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, CustomerPerdana.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llPenjualanPerdana.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, PenjualanPerdana.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llPenjualan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, PenjualanHariIni.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llOrderTcash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActOrderTcash.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llOrderNgrs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActOrderNGRS.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llRiwayatPenjualan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, RiwayatPenjualan.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llDaftarPiutang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, PiutangPerOutlet.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llStokSales.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, StokSales.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llCheckIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, ActKunjungan.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llKomplain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, ActKomplain.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llVerifikasiOutlet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, ActVerifikasiOutlet.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llInfoDeposit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, ActDeposit.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llMapsKunjungan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ListOutletLocation.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llPreorderPerdana.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActPreorderPerdanaActivity.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llEvent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActEvent.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llMarketSurvey.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActMarketSurvey.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llMarketSurveyAoc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActMarketSurveyAOC.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llDirectSelling.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, DetailEvent.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llMenuDirectSellingManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, DetailEventManual.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        });

		llPengajuanDeposit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, MainPengajuanDeposit.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});
		llKonsinyasi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(context, ActKonsinyasi.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});

		llMarketSurveyMD.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, ActMarketSurveyMD.class);
				context.startActivity(intent);
				((Activity) context).finish();
			}
		});
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

					if (iv.parseNullInteger(status) == 200) {

						JSONArray jsonArray = responseAPI.getJSONArray("response");

						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject item = jsonArray.getJSONObject(i);

							sliderList.add(new CustomItem(item.getString("id"), item.getString("image"), item.getString("keterangan"), item.getString("link")));
						}
					}

					if (firstLoad) {
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

	private void getDataAkun() {

		JSONObject jBody = new JSONObject();
		try {
			jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getUserInfo, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
			@Override
			public void onSuccess(String result) {

				try {

					JSONObject response = new JSONObject(result);
					String status = response.getJSONObject("metadata").getString("status");
					if (iv.parseNullInteger(status) == 200) {

						JSONObject jo = response.getJSONObject("response");
						tvNamaSales.setText(jo.getString("nama"));
						tvJabatan.setText(jo.getString("jabatan"));

						getUserOmset();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					getUserOmset();
				}
			}

			@Override
			public void onError(String result) {

				getUserOmset();
			}
		});
	}

	private void getUserOmset() {

		JSONObject jBody = new JSONObject();
		try {
			jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
			jBody.put("flag", session.getUserInfo(SessionManager.TAG_LEVEL));
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
					if (iv.parseNullInteger(status) == 200) {

						JSONObject item = responseAPI.getJSONObject("response");

						if (session.getLevel().equals("SF") || session.getLevel().equals("MD")) {

							tvTargetMkios.setText(iv.ChangeToCurrencyFormat(item.getString("target_mkios")));
							tvOmsetMkios.setText(iv.ChangeToCurrencyFormat(item.getString("omset_mkios")));
							tvGapMkios.setText(iv.ChangeToCurrencyFormat(item.getString("gap_mkios")));

							tvTargetPerdana.setText(item.getString("target_perdana"));
							tvOmsetPerdana.setText(item.getString("omset_perdana"));
							tvGapPerdana.setText(item.getString("gap_perdana"));

							tvTargetPJP.setText(item.getString("target_pjp"));
							tvPencapaianPJP.setText(item.getString("pencapaian_pjp"));
							tvGapPJP.setText(item.getString("gap_pjp"));

							tvOutletBaru.setText(item.getString("outlet_baru"));

						} else {

							tvDSTargetMkios.setText(iv.ChangeToCurrencyFormat(item.getString("target_mkios")));
							tvDSOmsetMkios.setText(iv.ChangeToCurrencyFormat(item.getString("omset_mkios")));
							tvDSGapMkios.setText(iv.ChangeToCurrencyFormat(item.getString("gap_mkios")));

							tvDSTargetDataMkios.setText(iv.ChangeToCurrencyFormat(item.getString("target_data_bulk")));
							tvDSOmsetDataMkios.setText(iv.ChangeToCurrencyFormat(item.getString("omset_data_bulk")));
							tvDSGAPDataMkios.setText(iv.ChangeToCurrencyFormat(item.getString("gap_data_bulk")));

							tvDSTargetRevenueDigital.setText(iv.ChangeToCurrencyFormat(item.getString("target_revenue")));
							tvDSOmsetRevenueDigital.setText(iv.ChangeToCurrencyFormat(item.getString("omset_revenue")));
							tvDSGAPRevenueDigital.setText(iv.ChangeToCurrencyFormat(item.getString("gap_revenue")));

							tvDSDailyPJP.setText(iv.ChangeToCurrencyFormat(item.getString("daily_pjp")));
							tvDSEffectiveCall.setText(iv.ChangeToCurrencyFormat(item.getString("effective_call")));
							tvDSGAPPJP.setText(iv.ChangeToCurrencyFormat(item.getString("gap_pjp")));

							tvDSTargetPerdana.setText(iv.ChangeToCurrencyFormat(item.getString("target_sellout")));
							tvDSOmsetPerdana.setText(iv.ChangeToCurrencyFormat(item.getString("omset_sellout")));
							tvDSGapPerdana.setText(iv.ChangeToCurrencyFormat(item.getString("gap_sellout")));

							tvDSTargetAkuisisi.setText(iv.ChangeToCurrencyFormat(item.getString("omset_akuisisi")));
							tvDSAkuisisi.setText(iv.ChangeToCurrencyFormat(item.getString("target_akuisisi")));
							tvDSGAPAkuisisi.setText(iv.ChangeToCurrencyFormat(item.getString("gap_akuisisi")));

						}

						tvTotalOmset.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getString("total_omset"))));
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

	private void setHeaderSlider() {

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

	private void setViewPagerTimer(int seconds) {
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

					if (vpHeaderSlider.getCurrentItem() == mAdapter.getCount() - 1) {
						vpHeaderSlider.setCurrentItem(0);

					} else {
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
