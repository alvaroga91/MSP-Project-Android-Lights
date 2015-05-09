package com.aaltomsp.androidlights.app;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aaltomsp.androidlights.app.feature.location.LocationActivity;
import com.aaltomsp.androidlights.app.feature.location.LocationService;
import com.aaltomsp.androidlights.app.feature.notification.NotificationActivity;
import com.aaltomsp.androidlights.app.feature.notification.PreferenceManager;
import com.aaltomsp.androidlights.app.feature.time.AlarmHelper;
import com.aaltomsp.androidlights.app.feature.time.TimeActivity;

public class MainActivity extends ActionBarActivity implements OnClickListener {

    public static final String PREFERENCES_NAME = "ANDROID_LIGHTS_PREFS";
    private SharedPreferences preferences;
    private Editor sharedPreferencesEditor;
    private LinearLayout locationViewButton;
    private LinearLayout notificationViewButton;
    private LinearLayout alarmViewButton;
    private ExpandableListView expandableListView;
    private LightService lightService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
        lightService = LightService.getInstance();

        ExpandableLightAdapter lightAdapter = new ExpandableLightAdapter(this);
        expandableListView = (ExpandableListView) findViewById(R.id.expandableLightList);
        expandableListView.setAdapter(lightAdapter);
        lightService.addAdapterToBeNotifiedOnNewLights(lightAdapter);

        initializeFeatureButtons();

        Button getLightsButton = (Button) findViewById(R.id.getLightsButton);
        getLightsButton.setOnClickListener(this);

        Button FindNewLightsButton = (Button) findViewById(R.id.findNewLightsButton);
        FindNewLightsButton.setOnClickListener(this);

        AlarmHelper.renewAlarms(this);
        LightAPIResponseEventBroadcaster.getInstance().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lightService.refreshLights(preferences);
        locationViewButton.setBackgroundColor(getFeatureButtonBackgroundColor(preferences.getBoolean(LocationService.LOCATION_FEATURE + ".IsActive", false)));
        alarmViewButton.setBackgroundColor(getFeatureButtonBackgroundColor(preferences.getBoolean(TimeActivity.TIME_FEATURE + "IsActive", false)));
        notificationViewButton.setBackgroundColor(getFeatureButtonBackgroundColor(preferences.getBoolean(PreferenceManager.NOTIFICATION_PREFERENCES + ".IsActive", false)));
    }

    @Override
    protected void onDestroy() {
        LightAPIResponseEventBroadcaster.getInstance().unregister(this);
        super.onDestroy();
    }

    private void initializeFeatureButtons() {
        locationViewButton = (LinearLayout) findViewById(R.id.locationViewButton);
        locationViewButton.setOnClickListener(this);

        notificationViewButton = (LinearLayout) findViewById(R.id.notificationViewButton);
        notificationViewButton.setOnClickListener(this);

        alarmViewButton = (LinearLayout) findViewById(R.id.alarmViewButton);
        alarmViewButton.setOnClickListener(this);

        getResources().getDrawable(R.drawable.ic_action_place).setColorFilter(new LightingColorFilter(getResources().getColor(R.color.extradark), Color.BLACK));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openConfigMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens the settings menu.
     */
    private void openConfigMenu() {
        final View configView = LayoutInflater.from(this).inflate(R.layout.config, null);
        Builder alert = new Builder(this);
        alert.setTitle("Configuration");
        alert.setView(configView);
        final EditText ipField = (EditText) configView.findViewById(R.id.ipField);
        ipField.setText(preferences.getString("ip", "localhost"));
        final EditText portField = (EditText) configView.findViewById(R.id.portField);
        portField.setText(preferences.getInt("port", 80) + "");

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String ip = ipField.getText().toString().trim();
                String port = portField.getText().toString().trim();
                sharedPreferencesEditor = preferences.edit();
                sharedPreferencesEditor.putString("ip", ip);
                try {
                    int portNum = Integer.parseInt(port);
                    sharedPreferencesEditor.putInt("port", portNum);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Port must be a positive integer. Port not saved", Toast.LENGTH_SHORT).show();
                } finally {
                    sharedPreferencesEditor.apply();
                }
            }
        });
        alert.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.findNewLightsButton:
                lightService.findNewLights(preferences);
                Toast.makeText(this, R.string.searchingForNewLights, Toast.LENGTH_SHORT).show();
                break;
            case R.id.getLightsButton:
                lightService.refreshLights(preferences);
                Toast.makeText(this, R.string.refeshingLights, Toast.LENGTH_SHORT).show();
                break;
            case R.id.locationViewButton:
            case R.id.locationViewImage:
                startActivity(new Intent(this, LocationActivity.class));
                break;
            case R.id.alarmViewButton:
            case R.id.alarmViewImage:
                startActivity(new Intent(this, TimeActivity.class));
                break;
            case R.id.notificationViewButton:
            case R.id.notificationViewImage:
                startActivity(new Intent(this, NotificationActivity.class));
                break;
        }
    }

    private int getFeatureButtonBackgroundColor(boolean isActive) {
        return isActive ? getResources().getColor(R.color.accent_medium) : getResources().getColor(R.color.medium);
    }
}
