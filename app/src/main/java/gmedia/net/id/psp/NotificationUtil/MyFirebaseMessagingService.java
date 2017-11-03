package gmedia.net.id.psp.NotificationUtil;

/**
 * Created by Shin on 2/17/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import gmedia.net.id.psp.MainNavigationActivity;
import gmedia.net.id.psp.PelunasanPenjualan.PelunasanPenjualan;
import gmedia.net.id.psp.PenjualanMKIOS.PenjualanMKIOS;
import gmedia.net.id.psp.PenjualanPerdana.PenjualanPerdana;
import gmedia.net.id.psp.R;


/**
 * Created by Shin on 2/13/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = "MyFirebaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getFrom());

        Map<String, String> extra = new HashMap<>();
        if(remoteMessage.getData().size() > 0){
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getData());
            extra = remoteMessage.getData();
        }

        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getNotification());
            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(), new HashMap<String, String>(extra));
        }

    }

    private void sendNotification(String title, String body , HashMap<String, String> extra) {

        // need no change
        Intent intent = null;
        int typeContent = 0;
        for(String key: extra.keySet()){
            if(key.trim().toUpperCase().equals("JENIS")){
                if(extra.get(key).trim().toUpperCase().equals("MKIOS")){
                    typeContent = 1;
                }else if(extra.get(key).trim().toUpperCase().equals("GA")){
                    typeContent = 2;
                }
            }
        }

        switch (typeContent){
            case 1:
                intent = new Intent(this, PenjualanMKIOS.class);
                break;
            case 2:
                intent = new Intent(this, PenjualanPerdana.class);
                break;
            default:
                intent = new Intent(this, MainNavigationActivity.class);
                break;
        }

        intent.putExtra("backto", true);
        for(String key: extra.keySet()){
            intent.putExtra(key, extra.get(key));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0 /*request code*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int IconColor = getResources().getColor(R.color.color_notif);

        // Set Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(IconColor)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /*Id of Notification*/, notificationBuilder.build());
    }
}
