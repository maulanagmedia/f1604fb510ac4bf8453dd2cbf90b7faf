package gmedia.net.id.psp.OrderDirectSelling.Service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import gmedia.net.id.psp.OrderDirectSelling.DetailInjectPulsa;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NOTIF";
    Context context;

    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        try {

            String pack = sbn.getPackageName();
            String ticker = "";
            if(sbn.getNotification().tickerText !=null) {
                ticker = sbn.getNotification().tickerText.toString();
            }
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");
            String text = extras.getCharSequence("android.text").toString();
            //int id1 = extras.getInt(Notification.EXTRA_SMALL_ICON);

            Log.i("Package",pack);
            Log.i("Ticker",ticker);
            Log.i("Title",title);
            Log.i("Text",text);

            Intent msgrcv = new Intent("Msg");
            msgrcv.putExtra("package", pack);
            msgrcv.putExtra("ticker", ticker);
            msgrcv.putExtra("title", title);
            msgrcv.putExtra("text", text);

            saveNotif(title, ticker);
            LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void saveNotif(String title, String text) {

        /*Log.d(TAG, "title: " + title);
        Log.d(TAG, "text: " + text);*/

        String[] separated = text.split(": ");

        if(separated.length > 0){

            text = text.replace(separated[0]+": ", "");

            if(!text.isEmpty()) DetailInjectPulsa.addTambahBalasan(separated[0], text);

            Log.d(TAG, "title: " + separated[0]);
            Log.d(TAG, "text: " + text);
        }
    }

    @Override

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification Removed");

    }
}