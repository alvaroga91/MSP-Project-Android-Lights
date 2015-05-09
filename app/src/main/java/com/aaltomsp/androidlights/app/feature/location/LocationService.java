package com.aaltomsp.androidlights.app.feature.location;

import com.aaltomsp.androidlights.app.feature.location.LocationRule.LocationRuleActivationEvent;
import com.aaltomsp.androidlights.app.feature.notification.NotificationService.Alert;
import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class LocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private LightService lightService;
    private SharedPreferences sharedPreferences;
    private Editor sharedPreferencesEditor;
    public static final String LOCATION_FEATURE = "LocationFeature";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        lightService = LightService.getInstance();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        sharedPreferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        googleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
        Log.d(getClass().getSimpleName(), "Location services disconnected");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "Location services connected");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30000)
                .setFastestInterval(10000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getClass().getSimpleName(), "Location services suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "Location services connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        int range = sharedPreferences.getInt(LOCATION_FEATURE + ".Range", LocationActivity.DEFAULT_RANGE);
        float latitude = sharedPreferences.getFloat(LOCATION_FEATURE + ".CoordinateLatitude", LocationActivity.DEFAULT_LATITUDE);
        float longitude = sharedPreferences.getFloat(LOCATION_FEATURE + ".CoordinateLongitude", LocationActivity.DEFAULT_LONGITUDE);
        boolean wasInRange = sharedPreferences.getBoolean(LOCATION_FEATURE + ".InRange", false);
        boolean isInRange = isInRange(location, range, latitude, longitude);
        if (wasInRange == isInRange) {
            return;
        }
        sharedPreferencesEditor.putBoolean(LOCATION_FEATURE + ".InRange", isInRange);
        sharedPreferencesEditor.commit();
        Collection<LocationRule> rules = getRules(isInRange ? LocationRuleActivationEvent.Approaching : LocationRuleActivationEvent.Leaving);
        for(LocationRule rule : rules) {
            lightService.setLightState(sharedPreferences,
                    rule.getLightId(),
                    lightService.createExtras(rule.isOn(), rule.getHue(), rule.getBrightness(), rule.getSaturation(), Alert.none.name()));
        }
    }

    private Collection<LocationRule> getRules(LocationRuleActivationEvent activationEvent) {
        Collection<LocationRule> rules = getRules(sharedPreferences);
        for (Iterator<LocationRule> iterator = rules.iterator(); iterator.hasNext();) {
            LocationRule rule = iterator.next();
            if (rule.getActivationEvent() != activationEvent) {
                iterator.remove();
            }
        }
        return rules;
    }

    public static Collection<LocationRule> getRules(SharedPreferences sharedPreferences) {
        Map<Pair<Integer, LocationRuleActivationEvent>, LocationRule> rulesByIdAndActivationEvent = new HashMap<Pair<Integer, LocationRuleActivationEvent>, LocationRule>();
        Map<String, ?> preferences = sharedPreferences.getAll();
        for (Entry<String, ?> preference : preferences.entrySet()) {
            if (!preference.getKey().startsWith(LOCATION_FEATURE)) {
                continue;
            }
            String[] parts = preference.getKey().split("\\.");
            if (parts.length != 4) {
                continue;
            }
            int lightId = Integer.parseInt(parts[1]);
            LocationRuleActivationEvent activationEvent = LocationRuleActivationEvent.valueOf(parts[2]);
            LocationRule rule = rulesByIdAndActivationEvent.get(new Pair<Integer, LocationRuleActivationEvent>(lightId, activationEvent));
            if (rule == null) {
                rule = new LocationRule(lightId, activationEvent);
                rulesByIdAndActivationEvent.put(new Pair<Integer, LocationRuleActivationEvent>(lightId, activationEvent), rule);
            }
            switch (parts[3]) {
                case "on":
                    rule.setOn((Boolean) preference.getValue());
                    break;
                case "hue":
                    rule.setHue((Integer) preference.getValue());
                    break;
                case "brightness":
                    rule.setBrightness((Integer) preference.getValue());
                    break;
                case "saturation":
                    rule.setSaturation((Integer) preference.getValue());
                    break;
            }
        }
        return rulesByIdAndActivationEvent.values();
    }

    private boolean isInRange(Location location, int range, float latitude, float longitude) {
        return calculateDistance(location.getLatitude(), location.getLongitude(), latitude, longitude) <= range;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // in meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }

    public static void storeRule(LocationRule rule, Editor editor) {
        editor.putBoolean(getLocationRuleBaseString(rule) + ".on", rule.isOn());
        editor.putInt(getLocationRuleBaseString(rule) + ".hue", rule.getHue());
        editor.putInt(getLocationRuleBaseString(rule) + ".brightness", rule.getBrightness());
        editor.putInt(getLocationRuleBaseString(rule) + ".saturation", rule.getSaturation());
        editor.commit();
    }

    public static void removeRule(LocationRule rule, Editor editor) {
        editor.remove(getLocationRuleBaseString(rule) +".on");
        editor.remove(getLocationRuleBaseString(rule) +".hue");
        editor.remove(getLocationRuleBaseString(rule) +".brightness");
        editor.remove(getLocationRuleBaseString(rule) +".saturation");
        editor.commit();
    }

    private static String getLocationRuleBaseString(LocationRule rule) {
        return LOCATION_FEATURE + "." + rule.getLightId() + "." + rule.getActivationEvent().name();
    }
}
