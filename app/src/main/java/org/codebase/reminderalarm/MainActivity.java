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
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

public class MainActivity extends AppCompatActivity {

    EventsModel eventsModel;
    public static RecyclerView recyclerView;
    public static ArrayList<EventsModel> eventsModelArrayList;
    public static EventsAdapter eventsAdapter;
    public static long longTime;
    public static long currentTime;
    public static AlarmManager alarmManager1;
    public static Intent intent1;
    public static PendingIntent pendingIntent1;
    private ArrayList<PendingIntent> intentArrayList = new ArrayList<>();
    private ArrayList<String> dates = new ArrayList<>();

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
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

        //cancel all tasks
        cancelPreviousAlarms();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {

            // Assign new tasks
            handler.post(this::getJSONFile);
        });
    }

    public void sampleAlarmTime() {

        dates.clear();
        dates.add("2022-11-12T16:20:05Z");
        dates.add("2022-11-12T16:25:05Z");
        dates.add("2022-11-12T16:30:05Z");
        dates.add("2022-11-12T16:35:05Z");
        dates.add("2022-11-12T16:40:05Z");

//        Log.e("check array ", String.valueOf(dates.size()) + dates.toString());
        eventsModelArrayList.clear();
        for (int i = 0; i < dates.size(); i++) {

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
//            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date1 = null;
            try {
                date1 = df.parse(dates.get(i));
                assert date1 != null;
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            df.setTimeZone(TimeZone.getDefault());
            assert date1 != null;
            String formattedDate = df.format(date1);
            Log.e("TAG date", date1.toString());
            setAlarm("Alarm title" + i, "Alarm text is here! ", date1, i);
            eventsModel = new EventsModel("Alarm title" + i, "Alarm text is here! ", dates.get(i));
            eventsModelArrayList.add(eventsModel);
            eventsAdapter = new EventsAdapter(eventsModelArrayList, this);

            recyclerView.setAdapter(eventsAdapter);
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                    false);
            recyclerView.setLayoutManager(layoutManager);
        }

    }
    public Object getJSONFile() {
        cancelPreviousAlarms();
        HttpRequest httpRequest = new HttpRequest();

        httpRequest.setOnResponseListener(response -> {
            if (response.code == HttpResponse.HTTP_OK) {
                JSONObject jsonObject = response.toJSONObject();
                try {
//                    String events = jsonObject.getString("events");
                    JSONArray jsonArray = jsonObject.getJSONArray("events");

                    eventsModelArrayList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String title = jsonObject1.getString("title");
                            String text = jsonObject1.getString("text");
                            String date = jsonObject1.getString("date");

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                            df.setTimeZone(TimeZone.getTimeZone("UTC"));
                            Date date1 = null;
                            try {
                                date1 = df.parse(date);
                                assert date1 != null;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            df.setTimeZone(TimeZone.getDefault());
                            assert date1 != null;
                            String formattedDate = df.format(date1);

                            setAlarm(title, text, date1, i);
                            eventsModel = new EventsModel(title, text, formattedDate);
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
        return null;
    }

    public void setAlarm(String title, String text, Date eventDate, int i) {
        longTime = Long.parseLong(String.valueOf(eventDate.getTime()));
        currentTime = System.currentTimeMillis();

        Date currentDate = new Date(currentTime);
        long secs = TimeUnit.MILLISECONDS.toSeconds(longTime - currentTime);
        if (eventDate.after(currentDate)) {
            //Schedule Alarm Receiver in Main Activity
            intent1 = new Intent(App.getContext(), AlarmReceiver.class);
            intent1.setAction("PLAY_ACTION");
            intent1.putExtra("title", title);
            intent1.putExtra("text", text);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pendingIntent1 = PendingIntent.getBroadcast(
                    App.getContext(), i, intent1, PendingIntent.FLAG_MUTABLE);

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
        if (eventDate.before(currentDate)) {
            cancelPreviousAlarms();
        }
    }

    public void cancelPreviousAlarms() {
        if (intentArrayList.size() > 0) {
            for (int j = 0; j < intentArrayList.size(); j++) {
                alarmManager1.cancel(intentArrayList.get(j));
            }
            intentArrayList.clear();
        }
    }

    @Override
    protected void onResume() {
        if (Helper.isMyServiceRunning(AlarmService.class)){
            Toast.makeText(App.getContext(), "Alarm service running1", Toast.LENGTH_LONG).show();
            startTimer();
        }
        super.onResume();
    }

    private void startTimer() {
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run() {
                        getJSONFile();
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 1, 300000);
    }

    @Override
    protected void onPause() {
        stopTimer();
        super.onPause();
    }

    private void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    @Override
    protected void onDestroy() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("PLAY_ACTION");
        broadcastIntent.setClass(this, AlarmReceiver.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }
}