package gmedia.net.id.psp.ActKonsinyasi.Rekonsinyasi;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.FormatItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.ActKonsinyasi.Adapter.AdapterRekonsinyasi;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class Rekonsinyasi extends AppCompatActivity {

	private Context context;
	private DialogBox dialogBox;
	private SessionManager session;
	private ItemValidation iv = new ItemValidation();
	private FloatingActionButton fabAdd;
	private TextView tvStart, tvEnd;
	private ImageView ivShow;
	private LinearLayout btnKalenderStart, btnKalenderEnd;
	private String keyword = "", dateStart = "", dateEnd = "";
	private EditText edtKeyword;
	private List<CustomItem> listKonsinyasi = new ArrayList<>();
	private RecyclerView rvKonsinyasi;
	private AdapterRekonsinyasi adapter;
	private String name[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};
	private String alamat[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};
	private String name1[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};
	private String alamat1[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rekonsinyasi);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("Rekon Stok Konsinyasi");
		}

		context = this;
		session = new SessionManager(context);
		dialogBox = new DialogBox(context);
		initUI();
		initEvent();
		initData();
	}

	private void initUI() {

		fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
		tvStart = (TextView) findViewById(R.id.tv_start);
		tvEnd = (TextView) findViewById(R.id.tv_end);
		ivShow = (ImageView) findViewById(R.id.iv_show);
		btnKalenderStart = findViewById(R.id.btn_kalender_start);
		btnKalenderEnd = findViewById(R.id.btn_kalender_end);
		edtKeyword = (EditText) findViewById(R.id.edt_keyword);
		rvKonsinyasi = (RecyclerView) findViewById(R.id.rcy_konsinyasi);
		keyword = "";

		dateStart = iv.getCurrentDate(FormatItem.formatDate);
		dateEnd = iv.getCurrentDate(FormatItem.formatDate);
		tvStart.setText(iv.ChangeFormatDateString(dateStart, FormatItem.formatDate, FormatItem.formatDateDisplay));
		tvEnd.setText(iv.ChangeFormatDateString(dateEnd, FormatItem.formatDate, FormatItem.formatDateDisplay));

		adapter = new AdapterRekonsinyasi(listKonsinyasi, context);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		rvKonsinyasi.setLayoutManager(layoutManager);
		rvKonsinyasi.setItemAnimator(new DefaultItemAnimator());
		rvKonsinyasi.setAdapter(adapter);
	}

	private void initEvent() {

		btnKalenderStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);
				Date dateValue = null;
				final Calendar customDate;

				try {
					dateValue = sdf.parse(iv.ChangeFormatDateString(dateStart, FormatItem.formatDate, FormatItem.formatDateDisplay));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				customDate = Calendar.getInstance();
				final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker datePicker, int year, int month, int date) {
						customDate.set(Calendar.YEAR, year);
						customDate.set(Calendar.MONTH, month);
						customDate.set(Calendar.DATE, date);

						SimpleDateFormat sdFormat = new SimpleDateFormat(FormatItem.formatDateDisplay, Locale.US);
						dateStart = iv.ChangeFormatDateString(sdFormat.format(customDate.getTime()), FormatItem.formatDateDisplay, FormatItem.formatDate);
						tvStart.setText(sdFormat.format(customDate.getTime()));
					}
				};

				SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
				new DatePickerDialog(context, date, iv.parseNullInteger(yearOnly.format(dateValue)), dateValue.getMonth(), dateValue.getDate()).show();
			}
		});

		btnKalenderEnd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				SimpleDateFormat sdf = new SimpleDateFormat(FormatItem.formatDateDisplay);
				Date dateValue = null;
				final Calendar customDate;

				try {
					dateValue = sdf.parse(iv.ChangeFormatDateString(dateEnd, FormatItem.formatDate, FormatItem.formatDateDisplay));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				customDate = Calendar.getInstance();
				final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker datePicker, int year, int month, int date) {
						customDate.set(Calendar.YEAR, year);
						customDate.set(Calendar.MONTH, month);
						customDate.set(Calendar.DATE, date);

						SimpleDateFormat sdFormat = new SimpleDateFormat(FormatItem.formatDateDisplay, Locale.US);
						dateEnd = iv.ChangeFormatDateString(sdFormat.format(customDate.getTime()), FormatItem.formatDateDisplay, FormatItem.formatDate);
						tvEnd.setText(sdFormat.format(customDate.getTime()));
					}
				};

				SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy");
				new DatePickerDialog(context, date, iv.parseNullInteger(yearOnly.format(dateValue)), dateValue.getMonth(), dateValue.getDate()).show();
			}
		});

		ivShow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				initData();
			}
		});

		edtKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {

				keyword = editable.toString();
			}
		});

		fabAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(context, OutletRekonsinyasi.class);
				startActivity(intent);
			}
		});
	}

	private void initData() {
		/*listKonsinyasi = prepareDataEKupon();
		adapter = new AdapterRekonsinyasi(listKonsinyasi, context);
		rvKonsinyasi.setAdapter(adapter);*/
        dialogBox.showDialog(true);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("datestart", dateStart);
            jBody.put("dateend", dateEnd);
            jBody.put("keyword", keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.viewBarangRekonsinyasi,"","",0,session.getUsername(),session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                dialogBox.dismissDialog();
                String message = "";

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    listKonsinyasi.clear();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray ja = response.getJSONArray("response");

                        for(int i = 0; i < ja.length();i++){

                            JSONObject jo = ja.getJSONObject(i);
                            listKonsinyasi.add(new CustomItem(
                                    jo.getString("id")
                                    ,jo.getString("customer")
                                    ,iv.ChangeFormatDateString(jo.getString("usertgl"), FormatItem.formatTimestamp, FormatItem.formatDateTimeDisplay)
                                    ,iv.ChangeToCurrencyFormat(jo.getString("total"))
                                    ,jo.getString("jumlah_ccid")
                            ));

                        }
                    }else{

                        DialogBox.showDialog(context, 3, message);
                    }

                } catch (JSONException e) {

                    e.printStackTrace();
                    View.OnClickListener clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dialogBox.dismissDialog();
                            initData();
                        }
                    };

                    dialogBox.showDialog(clickListener, "Ulangi Proses", "Terjadi kesalahan, harap ulangi proses");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String result) {

                dialogBox.dismissDialog();
                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialogBox.dismissDialog();
                        initData();
                    }
                };

                dialogBox.showDialog(clickListener, "Ulangi Proses", "Terjadi kesalahan, harap ulangi proses");
            }
        });
	}

	private ArrayList<CustomItem> prepareDataEKupon() {
		ArrayList<CustomItem> rvData = new ArrayList<>();
		for (int i = 0; i < name.length; i++) {
			CustomItem custom = new CustomItem(String.valueOf(listKonsinyasi.size()), name1[i], alamat1[i], name[i], alamat[i]);
			rvData.add(custom);
		}
		return rvData;
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
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}
