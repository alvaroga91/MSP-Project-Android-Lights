package com.aaltomsp.androidlights.app.feature.location;

import com.aaltomsp.androidlights.app.feature.location.LocationRule.LocationRuleActivationEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LocationActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnClickListener {

    private SharedPreferences sharedPreferences;
    private Editor sharedPreferencesEditor;
    private ToggleButton featureToggleButton;
    private ExpandableListView expandableListView;
    private ImageButton mapCoordinatesButton;
    private ImageButton currentLocationButton;
    private TextView coordinateLatitude;
    private TextView coordinateLongitude;
    private TextView range;
    private Button addRuleButton;
    private List<String> listDataHeaders;
    private HashMap<String, LocationRule> listDataContent;
    private LightService lightService;
    private GoogleApiClient googleApiClient;

    public static final float DEFAULT_LATITUDE = (float) 60.1833;
    public static final float DEFAULT_LONGITUDE = (float) 24.8333;
    public static final int DEFAULT_RANGE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        listDataHeaders = new ArrayList<String>();
        listDataContent = new HashMap<String, LocationRule>();
        sharedPreferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        lightService = LightService.getInstance();
        loadRules();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        expandableListView = (ExpandableListView) findViewById(R.id.locationFeatureExpandableListView);
        expandableListView.setAdapter(new ExpandableLocationRuleAdapter(this, listDataHeaders, listDataContent));
    }

    private void initializeButtons() {
        featureToggleButton = (ToggleButton) findViewById(R.id.locationFeatureTogglebutton);
        featureToggleButton.setChecked(sharedPreferences.getBoolean(LocationService.LOCATION_FEATURE + ".IsActive", false));

        mapCoordinatesButton = (ImageButton) findViewById(R.id.mapCoordinatesButton);
        currentLocationButton = (ImageButton) findViewById(R.id.currentLocationButton);

        mapCoordinatesButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_map, getResources().getColor(R.color.extradark)));
        currentLocationButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_locate, getResources().getColor(R.color.extradark)));

        coordinateLatitude = (TextView) findViewById(R.id.coordinateLatitude);
        coordinateLongitude = (TextView) findViewById(R.id.coordinateLongitude);
        range = (TextView) findViewById(R.id.locationRange);

        addRuleButton = (Button) findViewById(R.id.addLocationRuleButton);

        coordinateLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                float latitude;
                try {
                    latitude = Float.valueOf(editable.toString());
                } catch (NumberFormatException nfe) {
                    showCenterAlignedLongDurationToast(R.string.invalid_coordinate);
                    latitude = sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLatitude", DEFAULT_LATITUDE);
                }
                sharedPreferencesEditor.putFloat(LocationService.LOCATION_FEATURE + ".CoordinateLatitude", latitude);
                sharedPreferencesEditor.commit();
            }
        });

        coordinateLongitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                float longitude;
                try {
                    longitude = Float.valueOf(editable.toString());
                } catch (NumberFormatException e) {
                    showCenterAlignedLongDurationToast(R.string.invalid_coordinate);
                    longitude = sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLongitude", DEFAULT_LONGITUDE);
                }
                sharedPreferencesEditor.putFloat(LocationService.LOCATION_FEATURE + ".CoordinateLongitude", longitude);
                sharedPreferencesEditor.commit();
            }
        });

        range.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int range;
                try {
                    range = Integer.valueOf(editable.toString());
                } catch (NumberFormatException e) {
                    showCenterAlignedLongDurationToast(R.string.invalid_range);
                    range = sharedPreferences.getInt(LocationService.LOCATION_FEATURE + ".Range", DEFAULT_RANGE);
                }
                sharedPreferencesEditor.putInt(LocationService.LOCATION_FEATURE + ".Range", range);
                sharedPreferencesEditor.commit();
            }
        });

        currentLocationButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        currentLocationButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_locate, getResources().getColor(R.color.accent_light)));
                        break;
                    case MotionEvent.ACTION_UP:
                        currentLocationButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_locate, getResources().getColor(R.color.extradark)));
                        setCurrentLocation();
                        break;
                }
                return true;
            }
        });

        mapCoordinatesButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mapCoordinatesButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_map, getResources().getColor(R.color.accent_light)));
                        break;
                    case MotionEvent.ACTION_UP:
                        mapCoordinatesButton.setImageDrawable(setDrawableColor(R.drawable.ic_action_map, getResources().getColor(R.color.extradark)));
                        startActivity(new Intent(LocationActivity.this, MapActivity.class));
                        break;
                }
                return true;
            }
        });

        addRuleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addLocationRuleButton:
                displayAddRuleDialog();
                break;
            default:
                break;
        }
    }

    private void displayAddRuleDialog() {
        final List<Light> lights = getAllowedLights();
        if (lights == null || lights.isEmpty()) {
            Toast.makeText(this, R.string.no_lights_to_add_rules_to, Toast.LENGTH_SHORT).show();
            return;
        }

        final View lightsListLayout = LayoutInflater.from(this).inflate(R.layout.lights_list, null);
        final ListView lightsListView = (ListView) lightsListLayout.findViewById(R.id.lightsListView);

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lightService.getLightNames(lights));
        lightsListView.setAdapter(modeAdapter);
        modeAdapter.notifyDataSetChanged();

        Builder alert = new Builder(this);
        alert.setTitle("Select light to add rule for");
        alert.setView(lightsListLayout);
        final AlertDialog dialog = alert.show();

        lightsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Light light = lights.get(position);
                LocationRule newRule = new LocationRule(light.getId());
                newRule.setOn(true);
                newRule.setBrightness(0);
                newRule.setSaturation(0);
                newRule.setHue(0);
                if (approachingRuleAlreadyExists(light.getId())) {
                    newRule.setActivationEvent(LocationRuleActivationEvent.Leaving);
                } else if (leavingRuleAlreadyExists(light.getId())) {
                    newRule.setActivationEvent(LocationRuleActivationEvent.Approaching);
                }
                if (newRule.getActivationEvent() != null) {
                    addAndShowNewRule(newRule);
                    dialog.dismiss();
                } else {
                    displayNewRuleActivationTypeDialog(newRule);
                    dialog.dismiss();
                }
            }
        });
    }
    private void displayNewRuleActivationTypeDialog(final LocationRule newRule) {
        Builder alert = new Builder(this)
                .setTitle("Select rule activation type")
                .setCancelable(false)
                .setNeutralButton("Approaching", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newRule.setActivationEvent(LocationRuleActivationEvent.Approaching);
                        addAndShowNewRule(newRule);
                        dialog.dismiss();
                    }
                }).setPositiveButton("Leaving", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newRule.setActivationEvent(LocationRuleActivationEvent.Leaving);
                        addAndShowNewRule(newRule);
                        dialog.dismiss();
                    }
                });
        alert.show();
    }

    private boolean approachingRuleAlreadyExists(int lightId) {
        for (LocationRule rule : listDataContent.values()) {
            if (rule.getLightId() == lightId && rule.getActivationEvent() == LocationRuleActivationEvent.Approaching) {
                return true;
            }
        }
        return false;
    }
    private boolean leavingRuleAlreadyExists(int lightId) {
        for (LocationRule rule : listDataContent.values()) {
            if (rule.getLightId() == lightId && rule.getActivationEvent() == LocationRuleActivationEvent.Leaving) {
                return true;
            }
        }
        return false;
    }

    private List<Light> getAllowedLights() {
        List<Light> allowedLights = new ArrayList<Light>();
        allowedLights.addAll(lightService.getLights());
        Map<Integer, Integer> lightRuleCounts = new HashMap<Integer, Integer>();
        for (LocationRule rule : listDataContent.values()) {
            int lightId = rule.getLightId();
            if(!lightRuleCounts.containsKey(lightId)) {
                lightRuleCounts.put(lightId, 0);
            }
            lightRuleCounts.put(lightId, lightRuleCounts.get(lightId) + 1);
        }
        for (Iterator<Light> iterator = allowedLights.iterator(); iterator.hasNext();) {
            Light light = iterator.next();
            Integer lightRuleCount = lightRuleCounts.get(light.getId());
            if (lightRuleCount != null && lightRuleCount >= 2) {
                iterator.remove();
            }
        }
        return allowedLights;
    }

    public void onToggleClicked(View view) {
        if (((ToggleButton) view).isChecked()) {
            startService(new Intent(this, LocationService.class));
        } else {
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeButtons();
        loadState();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void persistStateAndFinish() {
        sharedPreferencesEditor.putBoolean(LocationService.LOCATION_FEATURE + ".IsActive", featureToggleButton.isChecked());
        sharedPreferencesEditor.commit();
        finish();
    }

    private void loadState() {
        featureToggleButton.setChecked(sharedPreferences.getBoolean(LocationService.LOCATION_FEATURE + ".IsActive", false));
        coordinateLatitude.setText(String.valueOf(sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLatitude", DEFAULT_LATITUDE)));
        coordinateLongitude.setText(String.valueOf(sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLongitude", DEFAULT_LONGITUDE)));
        range.setText(String.valueOf(sharedPreferences.getInt(LocationService.LOCATION_FEATURE + ".Range", DEFAULT_RANGE)));
    }

    @Override
    public void onBackPressed() {
        persistStateAndFinish();
    }

    private void loadRules() {
        Collection<LocationRule> rules = LocationService.getRules(sharedPreferences);
        for (LocationRule rule : rules) {
            addRule(rule);
        }
    }

    private void addRule(LocationRule rule) {
        listDataHeaders.add(rule.getActivationEvent().name() + " " + lightService.getLightName(rule.getLightId()));
        listDataContent.put(listDataHeaders.get(listDataHeaders.size() - 1), rule);
    }

    private void addAndShowNewRule(LocationRule rule) {
        LocationService.storeRule(rule, sharedPreferencesEditor);
        addRule(rule);
        expandableListView.expandGroup(listDataContent.size() - 1);
    }

    private Drawable setDrawableColor(int drawableId, int color) {
        LightingColorFilter lightingColorFilter = new LightingColorFilter(color, Color.BLACK);
        Drawable drawable = getResources().getDrawable(drawableId);
        drawable.setColorFilter(lightingColorFilter);
        return drawable;
    }

    private void showCenterAlignedLongDurationToast(int messageId) {
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(messageId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "Location services connected");
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
        Log.d(getClass().getSimpleName(), "Location changed to: " + location.getLatitude() + " - " + location.getLongitude());
        coordinateLatitude.setText(String.valueOf(location.getLatitude()));
        coordinateLongitude.setText(String.valueOf(location.getLongitude()));
    }

    private void setCurrentLocation() {
        if (!googleApiClient.isConnected()) {
            Log.w(getClass().getSimpleName(), "Tried to set current location while googleApiClient is disconnected");
            return;
        }
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(1000)
                .setFastestInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
}
