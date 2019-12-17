package gmedia.net.id.psp.ActKonsinyasi.Rekonsinyasi;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//import gmedia.net.id.psp.ActDirectSelling.Adapter.ListCCIDAllAdapter;
import gmedia.net.id.psp.ActKonsinyasi.Adapter.ListCCIDReKonsinyasiAdapter;
//import gmedia.net.id.psp.ActivityHome;
//import gmedia.net.id.psp.MapsResellerActivity;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.OrderDirectSelling.Adapter.ListCCIDAllAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailRekonsinyasi extends AppCompatActivity implements LocationListener {

	private Context context;
	private SessionManager session;
	private ItemValidation iv = new ItemValidation();
	private DialogBox dialogBox;

	// Location
	private double latitude, longitude;
	private LocationManager locationManager;
	private Criteria criteria;
	private String provider;
	private static Location location;
	private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
	private final int REQUEST_PERMISSION_FINE_LOCATION = 3;
	public boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
	private final long MIN_TIME_BW_UPDATES = 1; // 1 minute
	private static String jarak = "", range = "";
	private Button btnMapsOutlet;

	private boolean isUpdateLocation = false;
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationCallback mLocationCallback;
	private LocationRequest mLocationRequest;
	private final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
	private final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
			UPDATE_INTERVAL_IN_MILLISECONDS / 2;
	private LocationSettingsRequest mLocationSettingsRequest;
	private SettingsClient mSettingsClient;
	private final int REQUEST_CHECK_SETTINGS = 0x1;
	private Boolean mRequestingLocationUpdates;
	private Location mCurrentLocation;
	private boolean isLoading = false;
	private String TAG = "KONSINYASI_RE";
	public static final String flag = "KONSINYASI_RE";

	private Button btnAmbilCCIDList, btnScanCCID;
	private EditText edtCCID, edtNamaBrg, edtHargaBrg, edtTotalCCID, edtTotalHarga;
	private ListView lvPerdana;
	private ProgressBar pbProses;
	private LinearLayout llJarak;
	private ImageView ivRefreshJarak;
	private TextView tvJarak;
	private Button btnProses;
	private String nama = "", kdcus = "";
	private List<CustomItem> listBarang;
	private ListCCIDReKonsinyasiAdapter adapter;
	private String latitudeOutlet = "", longitudeOutlet = "";
	private List<OptionItem> listCCID;
	private String nik = "";
	private EditText edtReseller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_rekonsinyasi);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		);

		setTitle("Detail Rekon Stok");
		context = this;
		dialogBox = new DialogBox(context);
		session = new SessionManager(context);
//		nik = session.getNikGa();

		initLocationUtils();
		initUI();
		initEvent();
	}

	private void initUI() {

		btnAmbilCCIDList = (Button) findViewById(R.id.btn_ambil_ccid_list);
		btnScanCCID = (Button) findViewById(R.id.btn_scan_ccid);
		edtReseller = (EditText) findViewById(R.id.edt_reseller);
		edtCCID = (EditText) findViewById(R.id.edt_ccid);
		edtNamaBrg = (EditText) findViewById(R.id.edt_nama_barang_scan);
		edtHargaBrg = (EditText) findViewById(R.id.edt_harga_scan);
		lvPerdana = (ListView) findViewById(R.id.lv_perdana);
		edtTotalCCID = (EditText) findViewById(R.id.edt_total_ccid);
		edtTotalHarga = (EditText) findViewById(R.id.edt_total_harga);
		pbProses = (ProgressBar) findViewById(R.id.pb_loading);
		llJarak = (LinearLayout) findViewById(R.id.ll_jarak);
		btnMapsOutlet = (Button) findViewById(R.id.btn_peta);
		ivRefreshJarak = (ImageView) findViewById(R.id.iv_refresh_jarak);
		tvJarak = (TextView) findViewById(R.id.tv_jarak);
		btnProses = (Button) findViewById(R.id.btn_proses);

		session = new SessionManager(context);

		nama = "";
		kdcus = "";
		jarak = "";

		listBarang = new ArrayList<>();
		adapter = new ListCCIDReKonsinyasiAdapter((Activity) context, listBarang);
		lvPerdana.setAdapter(adapter);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			kdcus = bundle.getString("kdcus", "");
			nama = bundle.getString("nama", "");

			edtReseller.setText(nama);
		}
	}

	private void initLocationUtils() {

		// getLocation update by google
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mSettingsClient = LocationServices.getSettingsClient(this);
		mRequestingLocationUpdates = false;

		createLocationCallback();
		createLocationRequest();
		buildLocationSettingsRequest();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		setCriteria();
		latitude = 0;
		longitude = 0;
		location = new Location("set");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		//location = getLocation();
		updateAllLocation();
	}

	private void initEvent() {

		btnAmbilCCIDList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				getBarang("");
			}
		});

		btnScanCCID.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				edtCCID.setText("");
				edtNamaBrg.setText("");
				edtHargaBrg.setText("");
				openScanBarcode();
			}
		});

		lvPerdana.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
				//1. kdbrg
				//2. namabrg
				//3. ccid
				//4. harga
				//5. jumlah
				//6. nomor surat jalan
				//7. id surat jalam

				edtCCID.setText(item.getItem3());
				edtNamaBrg.setText(item.getItem2());
				edtHargaBrg.setText(iv.ChangeToRupiahFormat(item.getItem4()));
			}
		});

		btnProses.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				//validasi
				if (adapter == null) {

					Toast.makeText(context, "Silahkan masukkan minimal satu barang", Toast.LENGTH_LONG).show();
					return;
				}

				if (adapter.getDataList().size() == 0) {

					Toast.makeText(context, "Silahkan masukkan minimal satu barang", Toast.LENGTH_LONG).show();
					return;
				}

				String message = "Apakah anda yakin ingin memproses data?\n\n";
				// if(editMode) message = "Apakah anda yakin ingin mengubah "+noBukti+" ?\n\n";
				message += ("Total " + String.valueOf(adapter.getDataList().size()) + " CCID");
				AlertDialog builder = new AlertDialog.Builder(context)
						.setTitle("Konfirmasi")
						.setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
						.setMessage(message)
						.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								saveData();
							}
						})
						.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						}).show();
			}
		});

		ivRefreshJarak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (!isUpdateLocation) {
					updateAllLocation();
				}
			}
		});

		btnMapsOutlet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

//				getLokasiReseller();
			}
		});
	}

	public void updateTotal() {

		if (adapter != null && edtTotalCCID != null && edtTotalHarga != null) {

			List<CustomItem> barangSelected = adapter.getDataList();
			edtTotalCCID.setText(String.valueOf(barangSelected.size()));
			double total = 0;
			for (CustomItem item : barangSelected) {

				total += iv.parseNullDouble(item.getItem4());
			}

			edtTotalHarga.setText(iv.ChangeToRupiahFormat(total));
		}
	}

	private void getBarang(final String ccid) {

		isLoading = true;
		dialogBox.showDialog(true);

		JSONObject jBody = new JSONObject();
		try {
			jBody.put("ccid", ccid);
			jBody.put("kdcus", kdcus);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.viewBarangRekonsinyasi, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
			@Override
			public void onSuccess(String result) {

				isLoading = false;
				dialogBox.dismissDialog();
				String message = "Terjadi kesalahan saat memuat data, harap ulangi";
				try {

					JSONObject response = new JSONObject(result);
					String status = response.getJSONObject("metadata").getString("status");
					message = response.getJSONObject("metadata").getString("message");
					if (ccid.isEmpty()) listCCID = new ArrayList<>();

					if (iv.parseNullInteger(status) == 200) {

						JSONArray items = response.getJSONArray("response");
						for (int i = 0; i < items.length(); i++) {

							JSONObject jo = items.getJSONObject(i);

							if (ccid.isEmpty()) {
								boolean flag = false;

								if (adapter != null && adapter.getDataList().size() > 0) {
									for (CustomItem item : adapter.getDataList()) {
										if (item.getItem3().equals(jo.getString("ccid"))) {
											flag = true;
										}
									}
								}

								//1. kdbrg
								//2. namabrg
								//3. ccid
								//4. harga
								//5. jumlah
								//6. nomor surat jalan
								//7. id surat jalan

								listCCID.add(new OptionItem(
										jo.getString("kodebrg"),
										jo.getString("namabrg"), // namabrg
										jo.getString("ccid"), // ccid
										jo.getString("harga"), // harga
										jo.getString("jumlah"), // jumlah
										jo.getString("nomor_surat_jalan"), // nosuratjalan
										jo.getString("id_surat_jalan"), // idsuratjalan
										flag
								));
							} else {

								adapter.addData(new CustomItem(
										jo.getString("kodebrg"),
										jo.getString("namabrg"),
										jo.getString("ccid"),
										jo.getString("harga"),
										jo.getString("jumlah"),
										jo.getString("nobukti"),
										jo.getString("id")
								));

								edtCCID.setText(jo.getString("ccid"));
								edtNamaBrg.setText(jo.getString("namabrg"));
								edtHargaBrg.setText(jo.getString("harga"));
								//1. kdbrg
								//2. namabrg
								//3. ccid
								//4. harga
								//5. jumlah
								//6. nomor surat jalan
								//7. id surat jalan
								break;
							}

						}

						if (ccid.isEmpty()) showListCCID();
					} else {

						DialogBox.showDialog(context, 3, message);
					}

				} catch (JSONException e) {
					e.printStackTrace();

					View.OnClickListener clickListener = new View.OnClickListener() {
						@Override
						public void onClick(View view) {

							dialogBox.dismissDialog();
							getBarang(ccid);
						}
					};

					dialogBox.showDialog(clickListener, "Ulangi Proses", message);
				}
			}

			@Override
			public void onError(String result) {

				isLoading = false;
				dialogBox.dismissDialog();

				View.OnClickListener clickListener = new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						dialogBox.dismissDialog();
						getBarang(ccid);
					}
				};

				dialogBox.showDialog(clickListener, "Ulangi Proses", result);
			}
		});
	}

	private void openScanBarcode() {

		IntentIntegrator integrator = new IntentIntegrator(DetailRekonsinyasi.this);
		integrator.setOrientationLocked(false);
		integrator.initiateScan();
	}

	/*private void getLokasiReseller() {

		isLoading = true;
		dialogBox.showDialog(true);
		JSONObject jBody = new JSONObject();

		try {
			jBody.put("nomor", "");
			jBody.put("keyword", "");
			jBody.put("start", "");
			jBody.put("count", "");
			jBody.put("kdcus", kdcus);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.getLokasiReseller, new ApiVolley.VolleyCallback() {
			@Override
			public void onSuccess(String result) {

				isLoading = false;
				dialogBox.dismissDialog();
				String message = "Terjadi kesalahan saat memuat data, harap ulangi";

				try {

					JSONObject response = new JSONObject(result);
					String status = response.getJSONObject("metadata").getString("status");
					message = response.getJSONObject("metadata").getString("message");

					if (iv.parseNullInteger(status) == 200) {

						JSONArray ja = response.getJSONArray("response");
						JSONObject jo = ja.getJSONObject(0);
						String namaRS = jo.getString("nama");
						latitudeOutlet = jo.getString("latitude");
						longitudeOutlet = jo.getString("longitude");
						String photo = jo.getString("image");

						if (latitude != 0 || longitude != 0) {

							Intent intent = new Intent(context, MapsResellerActivity.class);
							intent.putExtra("lat", iv.doubleToStringFull(latitude));
							intent.putExtra("long", iv.doubleToStringFull(longitude));
							intent.putExtra("lat_outlet", latitudeOutlet);
							intent.putExtra("long_outlet", longitudeOutlet);
							intent.putExtra("nama", namaRS);
							intent.putExtra("photo", photo);

							startActivity(intent);
						}
					} else {

						DialogBox.showDialog(context, 3, message);
					}

				} catch (JSONException e) {

					e.printStackTrace();
					View.OnClickListener clickListener = new View.OnClickListener() {
						@Override
						public void onClick(View view) {

							dialogBox.dismissDialog();
							getLokasiReseller();
						}
					};

					dialogBox.showDialog(clickListener, "Ulangi Proses", "Terjadi kesalahan, harap ulangi proses");
				}
			}

			@Override
			public void onError(String result) {

				isLoading = false;
				dialogBox.dismissDialog();
				View.OnClickListener clickListener = new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						dialogBox.dismissDialog();
						getLokasiReseller();
					}
				};

				dialogBox.showDialog(clickListener, "Ulangi Proses", result);
			}
		});
	}*/

	private void showListCCID() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Login_Default_Dialog);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_list_ccid, null);
		builder.setView(view);

		final AutoCompleteTextView actvCCIDBA = (AutoCompleteTextView) view.findViewById(R.id.actv_ccid);
		final ListView lvCCIDBA = (ListView) view.findViewById(R.id.lv_ccid);

		final List<OptionItem> lastData = new ArrayList<>(listCCID);

		lastData.add(0, new OptionItem("0", "", "all", "", "", "", "", false));
		final ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, lastData);
		lvCCIDBA.setAdapter(adapterB);

		actvCCIDBA.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {

				if (actvCCIDBA.getText().length() <= 0) {

					lvCCIDBA.setAdapter(null);
					ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, lastData);
					lvCCIDBA.setAdapter(adapterB);
				}
			}
		});

		//1. kdbrg
		//2. namabrg
		//3. ccid
		//4. harga
		//5. jumlah
		//6. nomor surat jalan
		//7. id surat jalan
		actvCCIDBA.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

				if (i == EditorInfo.IME_ACTION_SEARCH) {

					List<OptionItem> items = new ArrayList<OptionItem>();
					String keyword = actvCCIDBA.getText().toString().trim().toUpperCase();

					for (OptionItem item : lastData) {

						if (item.getAtt3().toUpperCase().contains(keyword)) items.add(item);
					}

					ListCCIDAllAdapter adapterB = new ListCCIDAllAdapter((Activity) context, items);
					lvCCIDBA.setAdapter(adapterB);
					iv.hideSoftKey(context);
					return true;
				}

				return false;
			}
		});

		builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				ListCCIDAllAdapter adapterSelected = (ListCCIDAllAdapter) lvCCIDBA.getAdapter();
				List<OptionItem> optionItems = adapterSelected.getItems();

				adapter.clearData();
				int x = 0;
				for (OptionItem item : optionItems) {

					if (item.isSelected()) {

						adapter.addData(
								new CustomItem(

										item.getAtt1(),
										item.getAtt2(),
										item.getAtt3(),
										item.getAtt4(),
										item.getAtt5(),
										item.getAtt6(),
										item.getAtt7()
								)
						);
					}
					x++;
				}

			}
		});

		builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void saveData() {

		isLoading = true;
		final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Login_Default_Dialog);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Menyimpan...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		JSONObject jBody = new JSONObject();

		PackageInfo pInfo = null;
		String version = "";

		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		version = pInfo.versionName;

		//1. kdbrg
		//2. namabrg
		//3. ccid
		//4. harga
		//5. jumlah
		//6. nomor surat jalan
		//7. id surat jalam

		JSONArray jData = new JSONArray();
		for (CustomItem item : adapter.getDataList()) {

			JSONObject jDataDetail = new JSONObject();
			try {

				jDataDetail.put("ccid", item.getItem3());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			jData.put(jDataDetail);
		}

		try {
			jBody.put("version", version);
			jBody.put("data", jData);
			String imei = "";
			ArrayList<String> imeis = iv.getIMEI(context);
			if (imeis != null) if (imeis.size() > 0) imei = imeis.get(0);
			jBody.put("imei", imei);
			jBody.put("kdcus", kdcus);
			jBody.put("latitude", iv.doubleToStringFull(location.getLatitude()));
			jBody.put("longitude", iv.doubleToStringFull(location.getLongitude()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.simpanRekonsinyasi, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
			@Override
			public void onSuccess(String result) {

				isLoading = false;
				progressDialog.dismiss();

				String superMessage = "Terjadi kesalahan saat menyimpan data, harap ulangi";
				try {

					JSONObject response = new JSONObject(result);
					String status = response.getJSONObject("metadata").getString("status");
					superMessage = response.getJSONObject("metadata").getString("message");

					if (iv.parseNullInteger(status) == 200) {

						Toast.makeText(context, superMessage, Toast.LENGTH_LONG).show();
						Intent intent = new Intent(context, MainNavigationActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("flag", flag);
						context.startActivity(intent);
						((Activity) context).finish();
					} else {

						DialogBox.showDialog(context, 3, superMessage);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(context, superMessage, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onError(String result) {
				isLoading = false;
				progressDialog.dismiss();
				Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			}
		});
	}

	//region location

	private void createLocationRequest() {

		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void createLocationCallback() {
		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				super.onLocationResult(locationResult);

				mCurrentLocation = locationResult.getLastLocation();
				//mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
				onLocationChanged(mCurrentLocation);
			}
		};
	}

	private void buildLocationSettingsRequest() {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
		builder.addLocationRequest(mLocationRequest);
		mLocationSettingsRequest = builder.build();
	}

	private void stopLocationUpdates() {
		if (!mRequestingLocationUpdates) {
			Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
			return;
		}

		// It is a good practice to remove location requests when the activity is in a paused or
		// stopped state. Doing so helps battery performance and is especially
		// recommended in applications that request frequent location updates.
		mFusedLocationClient.removeLocationUpdates(mLocationCallback)
				.addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						mRequestingLocationUpdates = false;
					}
				});
	}

	private boolean checkPermissions() {
		int permissionState = ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}

	private void startLocationUpdates() {
		// Begin by checking if the device has the necessary location settings.

		isUpdateLocation = true;
		mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
				.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
					@Override
					public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
						Log.i(TAG, "All location settings are satisfied.");

						isUpdateLocation = false;
						//noinspection MissingPermission
						if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

							return;
						}
						mFusedLocationClient.requestLocationUpdates(mLocationRequest,
								mLocationCallback, Looper.myLooper());

						mFusedLocationClient.getLastLocation()
								.addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
									@Override
									public void onSuccess(Location clocation) {

										mRequestingLocationUpdates = true;
										if (clocation != null) {

											onLocationChanged(clocation);
										} else {
											location = getLocation();
										}
									}
								});
					}
				})
				.addOnFailureListener(this, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						int statusCode = ((ApiException) e).getStatusCode();
						switch (statusCode) {
							case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
								Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
										"location settings ");
								try {
									// Show the dialog by calling startResolutionForResult(), and check the
									// result in onActivityResult().
									ResolvableApiException rae = (ResolvableApiException) e;
									rae.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
								} catch (IntentSender.SendIntentException sie) {
									Log.i(TAG, "PendingIntent unable to execute request.");
								}
								break;
							case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
								String errorMessage = "Location settings are inadequate, and cannot be " +
										"fixed here. Fix in Settings.";
								Log.e(TAG, errorMessage);
								Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
								mRequestingLocationUpdates = false;
								//refreshMode = false;
						}

						//get Location
						isUpdateLocation = false;
						location = getLocation();
					}
				});
	}

	private void updateAllLocation() {
		mRequestingLocationUpdates = true;
		startLocationUpdates();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CHECK_SETTINGS) {

			if (resultCode == Activity.RESULT_CANCELED) {

				mRequestingLocationUpdates = false;
			} else if (resultCode == Activity.RESULT_OK) {

				startLocationUpdates();
			}
		} else if (result != null) {

			if (result.getContents() != null) {
				//System.out.println(result.getContents());

				//Menambahkan data CCID ke list
				if (result.getContents().length() >= 21) {

					getBarang(result.getContents().substring(5, 21));
				} else {
					DialogBox.showDialog(context, 3, "Format CCID tidak benar");
				}
			}
		}
	}

	public Location getLocation() {

		isUpdateLocation = true;
		try {

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			Log.v("isGPSEnabled", "=" + isGPSEnabled);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

			if (isGPSEnabled == false && isNetworkEnabled == false) {
				// no network provider is enabled
				Toast.makeText(context, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
						Toast.LENGTH_LONG).show();

			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					//location = null;

					// Granted the permission first
					if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
								Manifest.permission.ACCESS_COARSE_LOCATION)) {
							showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
						} else {
							requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
						}

						if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
								Manifest.permission.ACCESS_FINE_LOCATION)) {
							showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
						} else {
							requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
						}
						isUpdateLocation = false;
						return null;
					}

					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");

					if (locationManager != null) {

						if (jarak != "") { // Jarak sudah terdeteksi

							Location locationBuffer = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

							location = locationBuffer;

						} else {
							location = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						}

						if (location != null) {
							//Changed(location);
						}
					}
				}

				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {

					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("GPS Enabled", "GPS Enabled");

					if (locationManager != null) {

						if (jarak != "") { // Jarak sudah terdeteksi

							Location locationBuffer = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);

							location = locationBuffer;

						} else {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						}

						if (location != null) {
							//onLocationChanged(location);
						}
					}
				} else {
					//Toast.makeText(context, "Turn on your GPS for better accuracy", Toast.LENGTH_SHORT).show();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		isUpdateLocation = false;
		if (location != null) {
			onLocationChanged(location);
		}

		return location;
	}

	public void setCriteria() {
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		provider = locationManager.getBestProvider(criteria, true);
	}

	private void showExplanation(String title,
								 String message,
								 final String permission,
								 final int permissionRequestCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						requestPermission(permission, permissionRequestCode);
					}
				});
		builder.create().show();
	}

	private void requestPermission(String permissionName, int permissionRequestCode) {
		ActivityCompat.requestPermissions(DetailRekonsinyasi.this,
				new String[]{permissionName}, permissionRequestCode);
	}

	@Override
	public void onLocationChanged(Location clocation) {

		this.location = clocation;
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();

		if (!isUpdateLocation && !kdcus.isEmpty()) {
			if (!isLoading) getJarak();
		}
	}

	private void getJarak() {

		isUpdateLocation = true;
		pbProses.setVisibility(View.VISIBLE);
		String nik = session.getUserDetails().get(SessionManager.TAG_UID);
		JSONObject jBody = new JSONObject();
		try {
			jBody.put("kode", "");
			jBody.put("kdcus", kdcus);
			jBody.put("lat", iv.doubleToStringFull(latitude));
			jBody.put("long", iv.doubleToStringFull(longitude));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ApiVolley request = new ApiVolley(DetailRekonsinyasi.this, jBody, "POST", ServerURL.getJarak, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
			@Override
			public void onSuccess(String result) {

				isUpdateLocation = false;
				pbProses.setVisibility(View.GONE);

				try {

					JSONObject response = new JSONObject(result);
					String status = response.getJSONObject("metadata").getString("status");
					String message = response.getJSONObject("metadata").getString("message");

					if (iv.parseNullInteger(status) == 200) {

						JSONArray items = response.getJSONArray("response");
						for (int i = 0; i < items.length(); i++) {

							JSONObject jo = items.getJSONObject(i);
							range = jo.getString("range");
							jarak = jo.getString("jarak");
							latitudeOutlet = jo.getString("latitude");
							longitudeOutlet = jo.getString("longitude");
							String pesan = jo.getString("pesan");
							String keteranganJarak = "";
							if (iv.parseNullDouble(jo.getString("jarak")) <= 6371) {
								if (iv.parseNullDouble(jo.getString("jarak")) <= 1) {
									keteranganJarak = iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")) * 1000, "2") + " m";
								} else {
									keteranganJarak = iv.doubleToString(iv.parseNullDouble(jo.getString("jarak")), "2") + " km";
								}

								if (iv.parseNullDouble(jarak) > iv.parseNullDouble(range)) {

									keteranganJarak = "<font color='#ec1c25'>" + keteranganJarak + "</font>";
								}

							} else {


								keteranganJarak = "<font color='#ec1c25'>Lokasi outlet tidak diketahui</font>";
							}

							tvJarak.setText(Html.fromHtml(pesan + keteranganJarak));
							break;
						}
					}else{

						tvJarak.setText(message);
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String result) {
				isUpdateLocation = false;
				pbProses.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

		//location = getLocation();
	}

	@Override
	public void onProviderEnabled(String s) {

		//location = getLocation();
	}

	@Override
	public void onProviderDisabled(String s) {

	}

	//endregion
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
