package com.aaltomsp.androidlights.app.feature.notification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.R;

import java.util.ArrayList;

public class AddNotificationActivity extends ActionBarActivity implements View.OnClickListener {

    private SeekBar brightness;
    private SeekBar sat;
    private SeekBar hue;
    private String selectedApp, selectedFrequency = "none";
    private int brightnessValue;
    private int satValue;
    private int hueValue;
    public ArrayList<Light> lights = new ArrayList<Light>();
    private Light light;
    private PreferenceManager preferenceManager;
    private LightService lightService;
    private Button mSelectButton;
    private Button mSelectLight;
    private Button mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_select);
        lightService = LightService.getInstance();
        brightnessValue = 0;
        satValue = 0;
        hueValue = 0;
        preferenceManager = new PreferenceManager(this);
        final SurfaceView colorView = (SurfaceView)findViewById(R.id.colorView);
        brightness = (SeekBar) findViewById(R.id.brightnessSeekBar);
        sat = (SeekBar) findViewById(R.id.saturationSeekBar);
        hue = (SeekBar) findViewById(R.id.hueSeekBar);

        mDeleteButton = (Button) findViewById(R.id.delete_activity);
        mDeleteButton.setOnClickListener(this);

        mSelectButton = (Button) findViewById(R.id.select_button);
        mSelectButton.setOnClickListener(this);

        mSelectLight = (Button) findViewById(R.id.select_light);
        mSelectLight.setOnClickListener(this);

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                brightnessValue = progressValue;
                colorView.setBackgroundColor(getColor(hue, sat, brightness));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                satValue = progressValue;
                colorView.setBackgroundColor(getColor(hue, sat, brightness));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                hueValue = progressValue;
                colorView.setBackgroundColor(getColor(hue, sat, brightness));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Intent myIntent = getIntent(); // gets the previously created intent
        String applicationName = myIntent.getStringExtra("applicationName"); // will return "FirstKeyValue"

        if (applicationName != null && !applicationName.isEmpty()) {
            mDeleteButton.setText("Delete");
            setProperties(applicationName);
        }
    }

    private int getColor(SeekBar hueSeekBar, SeekBar saturationSeekBar, SeekBar brightnessSeekBar) {
        float hue = (float) hueSeekBar.getProgress() * (float) 360 / (float) 65535;
        float saturation = (float) saturationSeekBar.getProgress() / (float) 255;
        float brightness = (float) brightnessSeekBar.getProgress() / (float) 255;
        float[] hsv = {hue, saturation, brightness};
        return Color.HSVToColor(hsv);
    }

    private void setProperties(String applicationName) {
        Notification notification = preferenceManager.getNotificationForName(applicationName);
        mSelectButton.setText(notification.getName());
        selectedApp = notification.getName();

        light = notification.getLight();
        mSelectLight.setText(light.getName()+"");

        brightness = (SeekBar) findViewById(R.id.brightnessSeekBar);
        brightness.setProgress(notification.getBrightness());
        brightnessValue = notification.getBrightness();

        sat = (SeekBar) findViewById(R.id.saturationSeekBar);
        satValue = notification.getSat();
        sat.setProgress(notification.getSat());

        hue = (SeekBar) findViewById(R.id.hueSeekBar);
        hue.setProgress(notification.getHue());
        hueValue = notification.getHue();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.select_button) {

            final CharSequence[] items = {
                    Notification.FACEBOOK, Notification.GMAIL, Notification.INSTAGRAM, Notification.TWITTER, Notification.WHATSAPP
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make your selection");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    selectedApp = (String) items[item];
                    mSelectButton.setText(selectedApp);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        else if (v.getId() == R.id.delete_activity){
            cancelActivity();
        }

        else if (v.getId() == R.id.select_light) {
            openLightsDialog();
        }
    }

    public void closeActivity(View view) {
        int flag = 0;

        if (selectedApp == null) {
            Toast.makeText(getApplicationContext(), "select the application", Toast.LENGTH_SHORT).show();
            flag = 1;
        } else if (light == null) {
            Toast.makeText(getApplicationContext(), "select the light", Toast.LENGTH_SHORT).show();
            flag = 1;
        } else if (selectedFrequency == null) {
            Toast.makeText(getApplicationContext(), "select the frequency", Toast.LENGTH_SHORT).show();
            flag = 1;
        }

        if (flag == 0) {
            preferenceManager.writePreferences(selectedApp, light, brightnessValue, selectedFrequency, hueValue, satValue);
            finish();
        }
    }

    public void cancelActivity() {
        if (selectedApp != null ) {
            preferenceManager.deletePreferences(selectedApp);
        }
        finish();
    }


    private void openLightsDialog() {
        final View lightsListLayout = LayoutInflater.from(this).inflate(R.layout.lights_list, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Lights");
        alert.setView(lightsListLayout);
        final ListView lightsListView = (ListView) lightsListLayout.findViewById(R.id.lightsListView);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lightService.getLightNames());
        lightsListView.setAdapter(modeAdapter);
        modeAdapter.notifyDataSetChanged();
        final AlertDialog dialog = alert.show();
        lightsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                light = lightService.getLights().get(position);
                mSelectLight.setText(light.getName() + "");
                dialog.dismiss();
            }
        });
    }
}
