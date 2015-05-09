package com.aaltomsp.androidlights.app.feature.notification;

import android.content.Context;
import android.content.SharedPreferences;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {

    private Gson gson = new Gson();
    private Set<Notification> notifications = new HashSet<Notification>();
    private SharedPreferences sharedPreferences;
    public static final String NOTIFICATION_PREFERENCES = "NotificationFeature";

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        notifications = new HashSet<Notification>();
        readPreferences();
    }

    public void writePreferences(String applicationType, Light light, int brightnessValue, String frequency, int colorCode, int satValue) {
        Notification notification = new Notification(applicationType, light, brightnessValue, frequency, colorCode, satValue);
        notifications.remove(notification);
        notifications.add(notification);
        savePreferences();
    }

    public void deletePreferences(String applicationType){
        notifications.remove(getNotificationForName(applicationType));
        savePreferences();
    }

    public boolean isStarted() {
        return sharedPreferences.getBoolean(NOTIFICATION_PREFERENCES + ".IsActive", false);
    }

    public void writeStarted(boolean s) {
        sharedPreferences.edit().putBoolean(NOTIFICATION_PREFERENCES + ".IsActive", s).commit();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonString = gson.toJson(notifications).toString();
        editor.putString(NOTIFICATION_PREFERENCES + ".Notifications", jsonString);
        editor.commit();
    }

    public void readPreferences() {
        String jsonString = sharedPreferences.getString(NOTIFICATION_PREFERENCES + ".Notifications", "");
        notifications = new Gson().fromJson(jsonString, new TypeToken<Set<Notification>>() {}.getType());
        if (notifications == null) {
            notifications = new HashSet<Notification>();
        }
    }

    public ArrayList<Notification> getNotificationList() {
        ArrayList<Notification> objectList = new ArrayList<Notification>();
        objectList.addAll(notifications);
        return objectList;
    }

    public Notification getNotificationForName(String name) {
        for (Notification notif : notifications) {
            String appName = notif.getName();
            if (appName.equals(name)) {
                return notif;
            }
        }
        return null;
    }

    public Notification getNotificationForPackagename(String pkgName) {
        for (Notification notification : notifications) {
            String appPkgName = notification.getPkgName();
            if (appPkgName.equals(pkgName)) {
                return notification;
            }
        }
        return null;
    }

    public void turnOn(String name, boolean on) {
        Notification notification;
        notification = getNotificationForName(name);
        notification.setOn(on);
        notifications.remove(notification);
        notifications.add(notification);
        savePreferences();
    }

}
