package au.com.splinter.mymedia;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.IOException;

public class MyHttpService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wifiLock;

    private MyHTTPD httpd;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    @Override
//    public void onCreate() {
//        Toast.makeText(this, "Service created!", Toast.LENGTH_SHORT).show();
//
//        handler = new Handler();
//        runnable = new Runnable() {
//            public void run() {
//                Toast.makeText(context, "Service is still running", Toast.LENGTH_SHORT).show();
//                handler.postDelayed(runnable, 10000);
//            }
//        };
//
//        handler.postDelayed(runnable, 15000);
//    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();

        stopForeground(true);
        wakeLock.release();
        httpd.stop();
        wifiLock.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the httpd.
        try {
            httpd = new MyHTTPD();
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Service failed to start.", Toast.LENGTH_LONG).show();
        }

        // Keep the CPU awake.
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Httpd");
        wakeLock.acquire();

        // Keep the wifi awake.
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Httpd");
        wifiLock.acquire();


        // Request foreground running:
        // http://developer.android.com/guide/components/services.html#Foreground

        // https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
        // In this sample, we'll use the same text for the ticker and the expanded notification
        String text = "My http service";
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DashboardActivity.class), 0);
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_sync_black_24dp)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Http")  // the label
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when clicked
                .build();
        startForeground(1, notification);

        Toast.makeText(this, "Service started by user.", Toast.LENGTH_SHORT).show();

        return Service.START_STICKY;
    }
}
