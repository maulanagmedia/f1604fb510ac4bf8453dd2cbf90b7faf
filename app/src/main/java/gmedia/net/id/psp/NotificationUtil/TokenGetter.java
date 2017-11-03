package gmedia.net.id.psp.NotificationUtil;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Shin on 09/08/2017.
 */

public class TokenGetter extends Service {

    private static Timer timer = new Timer();
    private Context ctx;
    private String TAG = "service FCM";

    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        startService();
    }

    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            /*MyFirebaseInstanceIDService serviceId = new MyFirebaseInstanceIDService();
            serviceId.onTokenRefresh();*/
            Log.d("Firebase", "token: "+ FirebaseInstanceId.getInstance().getToken());
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
