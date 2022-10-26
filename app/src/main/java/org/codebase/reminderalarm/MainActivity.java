package org.codebase.reminderalarm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import org.codebase.reminderalarm.databinding.ActivityMainBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

public class MainActivity extends AppCompatActivity {

    EventsModel eventsModel;
    private ActivityMainBinding mainBinding;
    RecyclerView recyclerView;
    public static ArrayList<EventsModel> eventsModelArrayList;
    public static EventsAdapter eventsAdapter;
    public static long longTime;
    public static long currentTime;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventsModelArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewId);

//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.e("Here is", "Alarm");
//            alarmManager.set(AlarmManager.RTC, longTime, pendingIntent);
//        } else {
//            Log.e("Here is", "Alarm1");
//            alarmManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + (2 * 1000)),
//                    pendingIntent);
//        }
//        if (System.currentTimeMillis() > longTime) {
//        } else {
//            alarmManager.cancel(pendingIntent);
//        }


//        Intent intent = new Intent();
//        String packageName = this.getPackageName();
//        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        if (pm.isIgnoringBatteryOptimizations(packageName))
//            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//        else {
//            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + packageName));
//        }
//        this.startActivity(intent);

        getJSONFile();
    }

    public void getJSONFile() {
        HttpRequest httpRequest = new HttpRequest();

        httpRequest.setOnResponseListener(response -> {
            if (response.code == HttpResponse.HTTP_OK) {
                JSONObject jsonObject = response.toJSONObject();
                try {
                    String events = jsonObject.getString("events");
                    Log.e("events", events);
                    JSONArray jsonArray = jsonObject.getJSONArray("events");
                    Log.e("events", String.valueOf(jsonArray.length()));

                    for (int i= 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Log.e("events array ", jsonObject1.toString());
                            String title = jsonObject1.getString("title");
                            String text = jsonObject1.getString("text");
                            String date = jsonObject1.getString("date");
                            Log.e("events date ", date);
                            setAlarm(date);
                            eventsModel = new EventsModel(title, text, date);
                            eventsModelArrayList.add(eventsModel);
                            eventsAdapter = new EventsAdapter(eventsModelArrayList, this);

                            recyclerView.setAdapter(eventsAdapter);
                            LinearLayoutManager layoutManager
                                    = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                                    false);
                            recyclerView.setLayoutManager(layoutManager);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        httpRequest.setOnErrorListener(error -> {
            Log.e("Error ", error.reason);
        });

        httpRequest.get("https://visihelp.com/feed/?id=re462eea7cfec4e84d4267435");
    }

    public void setAlarm(String eventDate) {
        String times = "2022-10-27 03:31:05";
        Date date = null;
        SimpleDateFormat formatter5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            date = formatter5.parse(eventDate);
            Log.e("check date", String.valueOf(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        longTime = Long.parseLong(String.valueOf(date.getTime()));
        Log.e("check date1", String.valueOf(longTime));
        currentTime = System.currentTimeMillis();
        Log.e("check date2", String.valueOf(currentTime));
        long secs = TimeUnit.MILLISECONDS.toSeconds(longTime - currentTime);
        Log.e("check date3", String.valueOf(secs));

        //Schedule Alarm Receiver in Main Activity
        Intent intent1 = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234, intent1, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("here is? ", "yes");
            alarmManager1.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()+
                            TimeUnit.SECONDS.toMillis(secs), pendingIntent1);
        } else
        {
            Log.e("No! ", "here");
            alarmManager1.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()
                            + TimeUnit.SECONDS.toMillis(20),pendingIntent1);
        }
    }
}