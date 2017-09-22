package gmedia.net.id.psp.PelunasanPenjualan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import gmedia.net.id.psp.R;

public class VerifikasiPelunasan extends AppCompatActivity {

    private EditText edtNoBukti, edtNoNota, edtOutlet, edtTotal, edtKasBank, edtJumlah, edtKeterangan;
    private Button btnProses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_pelunasan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Verifikasi Pelunasan");
        initUI();
    }

    private void initUI() {

        edtNoBukti = (EditText) findViewById(R.id.edt_nobukti);
        edtNoNota = (EditText) findViewById(R.id.edt_nonota);
        edtOutlet = (EditText) findViewById(R.id.edt_outlet);
        edtTotal = (EditText) findViewById(R.id.edt_total);
        edtKasBank =(EditText) findViewById(R.id.edt_kas_bank);
        edtJumlah = (EditText) findViewById(R.id.edt_jumlah);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);

        btnProses = (Button) findViewById(R.id.btn_proses);
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
