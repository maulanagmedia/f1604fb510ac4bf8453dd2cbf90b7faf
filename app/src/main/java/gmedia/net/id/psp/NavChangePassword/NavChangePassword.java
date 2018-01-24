package gmedia.net.id.psp.NavChangePassword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class NavChangePassword extends Fragment {

    private Context context;
    private View layout;
    private EditText edtPasswordLama, edtPasswordBaru, edtUlangiPasswordBaru;
    private ItemValidation  iv = new ItemValidation();
    private Button btnChangePassword;
    private SessionManager session;

    public NavChangePassword() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_nav_change_password, container, false);
        context = getContext();
        session = new SessionManager(context);
        initUI();
        return layout;
    }

    private void initUI() {

        edtPasswordLama = (EditText) layout.findViewById(R.id.edt_password_lama);
        edtPasswordBaru= (EditText) layout.findViewById(R.id.edt_password_baru);
        edtUlangiPasswordBaru = (EditText) layout.findViewById(R.id.edt_ulangi_password_baru);
        btnChangePassword = (Button) layout.findViewById(R.id.btn_change_password);

        initEvent();
    }

    private void initEvent() {

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // validate before save

                if(edtPasswordLama.getText().length() == 0){

                    edtPasswordLama.setError("Password lama harap diisi");
                    edtPasswordLama.requestFocus();
                    return;
                }else{
                    edtPasswordLama.setError(null);
                }

                if(edtPasswordBaru.getText().length() == 0){

                    edtPasswordBaru.setError("Password baru harap diisi");
                    edtPasswordBaru.requestFocus();
                    return;
                }else{
                    edtPasswordBaru.setError(null);
                }

                if(edtUlangiPasswordBaru.getText().length() == 0){

                    edtUlangiPasswordBaru.setError("Ulangi Password Baru harap diisi");
                    edtUlangiPasswordBaru.requestFocus();
                    return;
                }else{
                    edtUlangiPasswordBaru.setError(null);
                }

                if(!edtPasswordLama.getText().toString().equals(session.getPassword())){
                    edtPasswordLama.setError("Password lama tidak benar");
                    edtPasswordLama.requestFocus();
                    return;
                }else{
                    edtPasswordLama.setError(null);
                }

                if(!edtPasswordBaru.getText().toString().equals(edtUlangiPasswordBaru.getText().toString())){

                    edtUlangiPasswordBaru.setError("Ulangi Password Baru tidak sama dengan password baru");
                    edtUlangiPasswordBaru.requestFocus();
                    return;
                }else{
                    edtUlangiPasswordBaru.setError(null);
                }

                AlertDialog alert = new AlertDialog.Builder(context)
                        .setIcon(context.getResources().getDrawable(R.mipmap.ic_launcher))
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin mengubah password")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                changePassword();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    private void changePassword() {

        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("passwordlama", edtPasswordLama.getText().toString());
            jBody.put("passwordbaru", edtPasswordBaru.getText().toString());
            jBody.put("username", session.getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.changePassword, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                String message = "Terjadi kesalahan saat memproses, harap ulangi";

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        session.savePassword(edtPasswordBaru.getText().toString());
                        progressDialog.dismiss();
                        edtPasswordLama.setText("");
                        edtPasswordBaru.setText("");
                        edtUlangiPasswordBaru.setText("");
                        loadSuccessDialog();
                    }else{
                        progressDialog.dismiss();
                        Snackbar.make(((Activity)context).findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE).setAction("Ok",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                    }
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Snackbar.make(((Activity)context).findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Snackbar.make(((Activity)context).findViewById(android.R.id.content), "Terjadi kesalahan koneksi, harap ulangi kembali nanti", Snackbar.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void loadSuccessDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) ((Activity)context).getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_success_dialog, null);
        builder.setView(view);

        final TextView tvText1= (TextView) view.findViewById(R.id.tv_text1);
        final Button btnOk = (Button) view.findViewById(R.id.btn_ok);

        tvText1.setText("Password telah berhasil diubah");

        final AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        alert.show();
    }

}
