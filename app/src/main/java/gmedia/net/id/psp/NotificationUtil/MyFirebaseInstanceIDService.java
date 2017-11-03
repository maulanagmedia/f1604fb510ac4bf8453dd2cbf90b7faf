package gmedia.net.id.psp.NotificationUtil;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Shin on 2/17/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebase";

    @Override
    public void onTokenRefresh() {
//        super.onTokenRefresh();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: " + refreshToken);
//        FirebaseMessaging.getInstance().subscribeToTopic("group");
        //sendRegistrationToServer(refreshToken);
    }
}
