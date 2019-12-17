package gmedia.net.id.psp.NavAccount;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class NavAccount extends Fragment {

    private Context context;
    private View layout;
    private EditText edtNama, edtNikGa, edtNikMkios, edtAlamat, edtNoTelp, edtJabatan;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;

    public NavAccount() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_nav_account, container, false);
        context = getContext();
        session = new SessionManager(context);
        initUI();
        return layout;
    }

    private void initUI() {

        edtNama = (EditText) layout.findViewById(R.id.edt_nama);
        edtNikGa = (EditText) layout.findViewById(R.id.edt_nik_ga);
        edtNikMkios = (EditText) layout.findViewById(R.id.edt_nik_mkios);
        edtAlamat = (EditText) layout.findViewById(R.id.edt_alamat);
        edtNoTelp = (EditText) layout.findViewById(R.id.edt_no_telepon);
        edtJabatan = (EditText) layout.findViewById(R.id.edt_jabatan);

        getDataAkun();
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
                    if(iv.parseNullInteger(status) == 200){

                        JSONObject jo = response.getJSONObject("response");
                        edtNama.setText(jo.getString("nama"));
                        edtAlamat.setText(jo.getString("alamat"));
                        edtNikGa.setText(jo.getString("nik_ga"));
                        edtNikMkios.setText(jo.getString("nik_mkios"));
                        edtNoTelp.setText(jo.getString("notelp"));
                        edtJabatan.setText(jo.getString("jabatan"));
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
}
