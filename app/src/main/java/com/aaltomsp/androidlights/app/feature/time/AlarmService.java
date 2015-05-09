package com.aaltomsp.androidlights.app.feature.time;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.feature.notification.NotificationService.Alert;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

public class AlarmService extends Service {

    private SharedPreferences sharedPreferences;
    private LightService lightService;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = this.getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        lightService = LightService.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sharedPreferences.getBoolean(TimeActivity.TIME_FEATURE + "IsActive", false)) {
            Bundle extras = intent.getExtras();
            long id = extras.getLong(AlarmHelper.EXTRAS_ID);
            String lightsString = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time_" + id, null);
            if (lightsString != null) {
                int index = lightsString.indexOf(TimeActivity.LIGHT_ITEM_SEPARATOR);
                Light[] arrayLights = Light.deserialize(lightsString.substring(index + 1));
                for (Light light : arrayLights) {
                    lightService.setLightState(sharedPreferences,
                            light.getId(),
                            lightService.createExtras(light.isOn(), light.getHue(), light.getBrightness(), light.getSaturation(), Alert.none.name()));
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}