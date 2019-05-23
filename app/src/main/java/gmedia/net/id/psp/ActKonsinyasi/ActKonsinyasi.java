package gmedia.net.id.psp.ActKonsinyasi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import gmedia.net.id.psp.ActKonsinyasi.InformasiBarang.OutletInfoBarang;
import gmedia.net.id.psp.ActKonsinyasi.MutasiKonsinyasi.DetailMutasiKonsinyasi;
import gmedia.net.id.psp.ActKonsinyasi.MutasiKonsinyasi.MutasiKonsinyasi;
import gmedia.net.id.psp.ActKonsinyasi.Rekonsinyasi.Rekonsinyasi;
import gmedia.net.id.psp.ActKonsinyasi.Retur.ActReturKonsinyasi;
import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.R;

public class ActKonsinyasi extends AppCompatActivity {

	private Context context;
	private DialogBox dialogBox;
	private ItemValidation iv = new ItemValidation();
	private SessionManager session;
	private RelativeLayout rlMutasi, rlRekonsinyasi, rlInfoBarang, rlRetur;
	private String flag = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_act_konsinyasi);

		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("Konsinyasi");
		}

		context = this;
		dialogBox = new DialogBox(context);
		session = new SessionManager(context);

		initUI();
		initAction();
	}

	private void initUI() {
		rlMutasi = (RelativeLayout) findViewById(R.id.rl_mutasi);
		rlRekonsinyasi = (RelativeLayout) findViewById(R.id.rl_rekonsinyasi);
		rlInfoBarang = (RelativeLayout) findViewById(R.id.rl_info_barang);
		rlRetur = (RelativeLayout) findViewById(R.id.rl_retur);

		/*Bundle bundle = getIntent().getExtras();
		if(bundle != null){

			flag = bundle.getString("flag", "");

			if(!flag.isEmpty()){

				if(flag.equals(DetailMutasiKonsinyasi.flag)){
					startActivity(new Intent(context, MutasiKonsinyasi.class));
				}else if(flag.equals(DetailRekonsinyasi.flag)){
					startActivity(new Intent(context, Rekonsinyasi.class));
				}else if(flag.equals(DetailReturKonsinyasi.flag)){
					startActivity(new Intent(context, ActReturKonsinyasi.class));
				}
			}
		}*/
	}

	private void initAction() {
		rlMutasi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				startActivity(new Intent(context, MutasiKonsinyasi.class));
			}
		});

		rlRekonsinyasi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				startActivity(new Intent(context, Rekonsinyasi.class));
			}
		});

		rlInfoBarang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				startActivity(new Intent(context, OutletInfoBarang.class));
			}
		});

		rlRetur.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				startActivity(new Intent(context, ActReturKonsinyasi.class));
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
		Intent intent = new Intent(ActKonsinyasi.this, MainNavigationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
	}
}
