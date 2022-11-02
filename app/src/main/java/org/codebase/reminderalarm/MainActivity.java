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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            Log.e("events array ", jsonObject1.toString());
                            String title = jsonObject1.getString("title");
                            String text = jsonObject1.getString("text");
                            String date = jsonObject1.getString("date");
                            Log.e("events date ", date);
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
//        String times = "2022-10-27 03:31:05";
        Date alarmDate = new Date();
        SimpleDateFormat formatter5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            alarmDate = formatter5.parse(eventDate);
            Log.e("check date", String.valueOf(alarmDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert alarmDate != null;
        longTime = Long.parseLong(String.valueOf(alarmDate.getTime()));
        Log.e("check date1", String.valueOf(longTime));
        currentTime = System.currentTimeMillis();

        Date currentDate = new Date(currentTime);
        Log.e("check date2", String.valueOf(currentTime));
        long secs = TimeUnit.MILLISECONDS.toSeconds(longTime - currentTime);
        Log.e("check date3", String.valueOf(secs));
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmDate.after(currentDate)) {
            Log.e("check datess", i + "  --- " + String.valueOf(alarmDate));

            //Schedule Alarm Receiver in Main Activity
            Intent intent1 = new Intent(this, AlarmReceiver.class);
            intent1.setAction("PLAY_ACTION");
            intent1.putExtra("title", title);
            intent1.putExtra("text", text);
            intent1.putExtra("date", eventDate);
//        sendBroadcast(intent1);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    this, i, intent1, PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("here is? ", "yes");
                alarmManager1.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +
                                TimeUnit.SECONDS.toMillis(secs), pendingIntent1);
                intentArrayList.add(pendingIntent1);
            } else {
                Log.e("No! ", "here");
                alarmManager1.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()
                                + TimeUnit.SECONDS.toMillis(20), pendingIntent1);
                intentArrayList.add(pendingIntent1);
            }
        }
        if (alarmDate.before(currentDate)) {
            if (intentArrayList.size() > 0) {
                for (int j = 0; j < intentArrayList.size(); j++) {
                    Log.e("is here in if lese ", alarmDate.toString());
                    alarmManager1.cancel(intentArrayList.get(j));
                }
                intentArrayList.clear();
            }
        }
    }
}