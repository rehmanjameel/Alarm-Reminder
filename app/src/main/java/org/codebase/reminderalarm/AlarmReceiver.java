package org.codebase.reminderalarm;

import static android.content.Context.ALARM_SERVICE;
import static android.provider.MediaStore.MediaColumns.TITLE;

import static org.codebase.reminderalarm.MainActivity.currentTime;
import static org.codebase.reminderalarm.MainActivity.longTime;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String RECURRING = "RECURRING";

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            String toastText = String.format("Alarm Reboot");
//            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
//
//        }
//        else {
//            String toastText = String.format("Alarm Received");
//            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
//            startAlarmService(context, intent);
//
//            if (!intent.getBooleanExtra(RECURRING, false)) {
//                startAlarmService(context, intent);
//            }
//        }

//        startAlarmReceiver(context);
        vibrator(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            startAlarmService(context, intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            }
            showNotification(context, "Alarm", "It's Alarm time!");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void showNotification(Context ctx, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "secure";
            CharSequence name = "secure";
            String Description = "The channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "secure")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);
        Intent resultIntent = new Intent(ctx, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);

        notificationManager.notify(12, builder.build());
    }

//    public void startAlarmReceiver(Context context) {
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context.getApplicationContext(), 234, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime()+
//                            TimeUnit.SECONDS.toMillis(20),
//                    pendingIntent);
//        } else {
//            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime() +
//                            TimeUnit.SECONDS.toMillis(20), pendingIntent);
//        }
//    }

    public void vibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(4000);

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show();
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // setting default ringtone
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);

        // play ringtone
        ringtone.play();
    }

//    private void startAlarmService(Context context, Intent intent) {
//        Intent intentService = new Intent(context, AlarmService.class);
//        intentService.putExtra(TITLE, intent.getStringExtra(TITLE));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intentService);
//        } else {
//            context.startService(intentService);
//        }
//    }

}
