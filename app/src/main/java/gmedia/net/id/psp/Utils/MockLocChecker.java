package gmedia.net.id.psp.Utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.psp.BuildConfig;
import gmedia.net.id.psp.MainActivity;

/**
 * Created by Shinmaul on 4/19/2018.
 */

public class MockLocChecker {

    private Context context;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();

    public MockLocChecker(Context context){

        this.context = context;
        session = new SessionManager(context);
        getMockLocation();
    }

    private void getMockLocation() {

        boolean isMockLocation = false;
        try {
            //if marshmallow
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID)== AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(context.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(isMockLocation){


            JSONObject jBody = new JSONObject();
            try {
                jBody.put("nik", session.getUserInfo(SessionManager.TAG_UID));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.saveLogMock, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
                @Override
                public void onSuccess(String result) {

                    try {

                        JSONObject response = new JSONObject(result);
                        String status = response.getJSONObject("metadata").getString("status");

                        if(iv.parseNullInteger(status) == 200){

                            String flag = response.getJSONObject("response").getString("flag");
                            String message = response.getJSONObject("response").getString("message");

                            if(flag.equals("1")){

                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("exit", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                ((Activity)context).startActivity(intent);
                                ((Activity)context).finish();
                            }else{
                                Toast.makeText(context, "Anda terdeteksi menggunakan aplikasi yang mengganggu proses app PSP", Toast.LENGTH_LONG).show();
                            }
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
}
