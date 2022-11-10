package org.codebase.reminderalarm;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class Helper {

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        Log.e("isService? ", String.valueOf(App.getContext().getSystemService(Context.ACTIVITY_SERVICE) != null));
        ActivityManager manager = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
