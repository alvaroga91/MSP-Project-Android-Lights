package com.aaltomsp.androidlights.app.feature.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.aaltomsp.androidlights.app.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmHelper {
    public static final String EXTRAS_ID = "EXTRAS_ID";

    private static Map<Long, PendingIntent> pendingIntents = new HashMap<Long, PendingIntent>();

    public static void addAlarm(Context context, long triggerTime, long id) {
        if (pendingIntents.containsKey(id)) {
            cancelAlarm(context, id);
        }
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(EXTRAS_ID, id);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntents.put(id, pendingIntent);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public static void cancelAlarm(Context context, long id) {
        if (pendingIntents.containsKey(id)) {
            PendingIntent pendingIntent = pendingIntents.get(id);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void renewAlarms(Context context) {
        removeExpired(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);

        String savedIds = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time", "");
        String[] split = savedIds.split("" + TimeActivity.LIGHT_ITEM_SEPARATOR);
        for (String idString : split) {
            if (!"".equals(idString)) {
                long id = Long.valueOf(idString);
                String lightsString = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time_" + id, null);
                if (lightsString != null) {
                    int index = lightsString.indexOf(TimeActivity.LIGHT_ITEM_SEPARATOR);
                    long time = Long.valueOf(lightsString.substring(0, index));
                    AlarmHelper.addAlarm(context, time, id);
                }
            }
        }
    }

    public static void removeExpired(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        String savedIds = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time", "");
        String[] split = savedIds.split("" + TimeActivity.LIGHT_ITEM_SEPARATOR);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        List<Long> nonExpiredIds = new ArrayList<Long>();
        for (String idString : split) {
            if (!"".equals(idString)) {
                long id = Long.valueOf(idString);
                String lightsString = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time_" + id, null);
                if (lightsString != null) {
                    int index = lightsString.indexOf(TimeActivity.LIGHT_ITEM_SEPARATOR);
                    long time = Long.valueOf(lightsString.substring(0, index));
                    if (time >= System.currentTimeMillis()) {
                        nonExpiredIds.add(id);
                    } else {
                        sharedPreferencesEditor.remove(TimeActivity.TIME_FEATURE + "lights_time_" + id);
                    }
                }
            }
        }

        String lightsString = "";
        for (int i = 0; i < nonExpiredIds.size(); i++) {
            if (i > 0) {
                lightsString += TimeActivity.LIGHT_ITEM_SEPARATOR;
            }
            Long id = nonExpiredIds.get(i);
            lightsString += id;
        }
        sharedPreferencesEditor.putString(TimeActivity.TIME_FEATURE + "lights_time", lightsString);
        sharedPreferencesEditor.commit();
    }
}
