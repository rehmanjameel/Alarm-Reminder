package org.codebase.reminderalarm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                234324243, intent, PendingIntent.FLAG_IMMUTABLE);

        String times = "2022-10-25 20:25:00";
        Date date = null;
        SimpleDateFormat formatter5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = formatter5.parse(times);
            Log.e("check date", String.valueOf(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        long longTime = Long.parseLong(String.valueOf(date.getTime()));
        Log.e("check date", String.valueOf(longTime));

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("Here is", "Alarm");
            alarmManager.set(AlarmManager.RTC_WAKEUP, longTime, pendingIntent);
        } else {
            Log.e("Here is", "Alarm1");
            alarmManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + (2 * 1000)),
                    pendingIntent);
        }
//        if (System.currentTimeMillis() > longTime) {
//        } else {
//            alarmManager.cancel(pendingIntent);
//        }
    }
}