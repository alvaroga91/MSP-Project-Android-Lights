package com.aaltomsp.androidlights.app.feature.notification;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.MainActivity;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationService extends NotificationListenerService {

    public enum Alert {
        none,
        select,
        lselect
    }

    private PreferenceManager pManager;
    private LightService lightService;
    private static final String TAG = "NotificationService";

    @Override
    public void onCreate() {
        super.onCreate();
        lightService = LightService.getInstance();
        pManager = new PreferenceManager(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        pManager.readPreferences();

        //TODO: trigger bulbs on with proper configurations
        //Only if the income notification has been set we can process it
        if (pManager.getNotificationForPackagename(sbn.getPackageName()) != null && pManager.getNotificationForPackagename(sbn.getPackageName()).isOn()) {
            Notification not = pManager.getNotificationForPackagename(sbn.getPackageName());

            //Building JSON for turn bulb on
            Bundle extras = new Bundle();

            extras.putBoolean("on", true);
            extras.putInt("brightness", not.getBrightness());
            extras.putInt("saturation", not.getSat());
            extras.putInt("hue", not.getHue());
            extras.putString("alert", not.getFrequency());

            lightService.setLightState(getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS), not.getLight().getId(), extras);

            Log.i(TAG, "**********  onNotificationPosted");
            Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        pManager.readPreferences();

        //TODO: trigger bulbs off
        //Only if the income notification has been set we can process it
        if (pManager.getNotificationForPackagename(sbn.getPackageName()) != null && pManager.getNotificationForPackagename(sbn.getPackageName()).isOn()) {
            Notification not = pManager.getNotificationForPackagename(sbn.getPackageName());
            if (not.isOn()) {

                //Building JSON for turn bulb off
                Bundle extras = new Bundle();
                extras.putBoolean("on", false);

                lightService.setLightState(getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS), not.getLight().getId(), extras);

                Log.i(TAG, "********** onNOtificationRemoved");
                Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
            }
        }
    }
}
