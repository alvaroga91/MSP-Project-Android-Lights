package com.aaltomsp.androidlights.app.feature.notification;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aaltomsp.androidlights.app.R;

import java.util.ArrayList;

public class NotificationActivity extends ActionBarActivity {

    private ArrayList<Notification> notifications;
    private ExpandableListView expandableListView;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.notificationFeatureTogglebutton);

        preferenceManager = new PreferenceManager(this);
        preferenceManager.readPreferences();

        notifications = preferenceManager.getNotificationList();

        expandableListView = (ExpandableListView) findViewById(R.id.notificationFeatureExpandableListView);
        expandableListView.setAdapter(new ExpandableNotificationAdapter(this, notifications));

        toggleButton.setChecked(preferenceManager.isStarted());
    }

    public void addApplication(View view) {
        Intent intent = new Intent(this, AddNotificationActivity.class);
        startActivity(intent);
    }

    public void onToggleClicked(View view) {
        preferenceManager.readPreferences();
        if (((ToggleButton) view).isChecked() && !preferenceManager.isStarted()) {
            startService(new Intent(this, NotificationService.class));
            preferenceManager.writeStarted(true);
            if (!Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                Toast.makeText(getApplicationContext(), "Enable Notification access, then come back to the app and create your first notification!", Toast.LENGTH_LONG).show();
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        } else {
            stopService(new Intent(this, NotificationService.class));
            preferenceManager.writeStarted(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        preferenceManager.readPreferences();
        notifications = preferenceManager.getNotificationList();
        expandableListView.setAdapter(new ExpandableNotificationAdapter(getApplicationContext(), notifications));
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferenceManager.readPreferences();
        notifications = preferenceManager.getNotificationList();
        expandableListView.setAdapter(new ExpandableNotificationAdapter(getApplicationContext(), notifications));
    }
}
