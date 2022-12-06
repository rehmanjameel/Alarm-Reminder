package org.codebase.reminderalarm;


import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.net.IDN;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private static final int ID_SERVICE = 101;

    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    MainActivity mainActivity = new MainActivity();
    @Override
    public void onCreate() {
        super.onCreate();

//        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
//        mediaPlayer.setLooping(true);

//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
//        Notification notification = notificationBuilder.setOngoing(true)
////                .setSmallIcon(R.mipmap.ic_launcher)
//                .setPriority(PRIORITY_MIN)
//                .setCategory(NotificationCompat.CATEGORY_SERVICE)
//                .build();
//        startForeground(ID_SERVICE, notification);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(ID_SERVICE, new Notification());

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock");
        wifiLock.acquire();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Handler handler = new Handler();
        final int delay = 300000;

        handler.postDelayed(new Runnable() {
            public void run() {
                mainActivity.getJSONFile();
                Toast.makeText(App.getContext(), "Alarm Service Running", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, delay);
            }
        }, delay);

        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(ID_SERVICE, notification);
    }

    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
        return channelId;
    }

//    public static void showNotification(Context ctx, String message, String title) {
//        NotificationManager notificationManager =
//                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            String CHANNEL_ID = "secure";
//            CharSequence name = "secure";
//            String Description = getContext().getResources().getString(R.string.notification_channel);
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            mChannel.setDescription(Description);
//            mChannel.enableLights(true);
//            mChannel.setLightColor(Color.RED);
//            mChannel.enableVibration(true);
//            mChannel.setShowBadge(false);
//            notificationManager.createNotificationChannel(mChannel);
//        }
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "secure")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(title)
//                .setContentText(message);
//        Intent resultIntent = new Intent(ctx, MainActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//
//        builder.setContentIntent(resultPendingIntent);
//        builder.setAutoCancel(true);
//
//        notificationManager.notify(12, builder.build());
//    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("PLAY_ACTION");
        broadcastIntent.setClass(this, AlarmReceiver.class);
        this.sendBroadcast(broadcastIntent);

//        mediaPlayer.stop();
//        vibrator.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService(new Intent(this, AlarmService.class));
    }
}
