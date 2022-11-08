package org.codebase.reminderalarm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

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
    RecyclerView recyclerView;
    public static ArrayList<EventsModel> eventsModelArrayList;
    public static EventsAdapter eventsAdapter;
    public static long longTime;
    public static long currentTime;
    private ArrayList<PendingIntent> intentArrayList = new ArrayList<>();
//    private ArrayList<String> dates = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        dates.add("2022-11-04T15:35:05Z");
//        dates.add("2022-11-04T15:45:05Z");
//        dates.add("2022-11-04T16:5:05Z");
//        dates.add("2022-11-05T18:30:05Z");
//        dates.add("2022-11-05T19:45:05Z");
        eventsModelArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

//        Log.e("check array ", String.valueOf(dates.size()) + dates.toString());
//        for (int i = 0; i < dates.size(); i++) {
//            Log.e("check array ", dates.get(i));

//            setAlarm("Alarm title" + i, "Alarm text is here! ", dates.get(i), i);
//            eventsModel = new EventsModel("Alarm title" + i, "Alarm text is here! ", dates.get(i));
//            eventsModelArrayList.add(eventsModel);
//            eventsAdapter = new EventsAdapter(eventsModelArrayList, this);
//
//            recyclerView.setAdapter(eventsAdapter);
//            LinearLayoutManager layoutManager
//                    = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
//                    false);
//            recyclerView.setLayoutManager(layoutManager);
//        }
        getJSONFile();
    }

    public void getJSONFile() {
        HttpRequest httpRequest = new HttpRequest();

        httpRequest.setOnResponseListener(response -> {
            if (response.code == HttpResponse.HTTP_OK) {
                JSONObject jsonObject = response.toJSONObject();
                try {
//                    String events = jsonObject.getString("events");
                    JSONArray jsonArray = jsonObject.getJSONArray("events");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String title = jsonObject1.getString("title");
                            String text = jsonObject1.getString("text");
                            String date = jsonObject1.getString("date");
                            setAlarm(title, text, date, i);
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

    public void setAlarm(String title, String text, String eventDate, int i) {
        Date alarmDate = new Date();
        SimpleDateFormat formatter5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            alarmDate = formatter5.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert alarmDate != null;
        longTime = Long.parseLong(String.valueOf(alarmDate.getTime()));
        currentTime = System.currentTimeMillis();

        Date currentDate = new Date(currentTime);
        long secs = TimeUnit.MILLISECONDS.toSeconds(longTime - currentTime);
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmDate.after(currentDate)) {
            //Schedule Alarm Receiver in Main Activity
            Intent intent1 = new Intent(this, AlarmReceiver.class);
            intent1.setAction("PLAY_ACTION");
            intent1.putExtra("title", title);
            intent1.putExtra("text", text);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    this, i, intent1, PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager1.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +
                                TimeUnit.SECONDS.toMillis(secs), pendingIntent1);
                intentArrayList.add(pendingIntent1);
            } else {
                alarmManager1.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + TimeUnit.SECONDS.toMillis(20), pendingIntent1);
                intentArrayList.add(pendingIntent1);
            }
        }
        if (alarmDate.before(currentDate)) {
            if (intentArrayList.size() > 0) {
                for (int j = 0; j < intentArrayList.size(); j++) {
                    alarmManager1.cancel(intentArrayList.get(j));
                }
                intentArrayList.clear();
            }
        }
    }
}