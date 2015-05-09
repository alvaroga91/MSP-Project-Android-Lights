package com.aaltomsp.androidlights.app;

import com.aaltomsp.androidlights.app.feature.notification.NotificationService.Alert;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ExpandableLightAdapter extends BaseExpandableListAdapter {

    private LightService lightService;
    private Activity baseActivity;

    public ExpandableLightAdapter(Activity baseActivity) {
        lightService = LightService.getInstance();
        this.baseActivity = baseActivity;
    }

    @Override
    public int getGroupCount() {
        return lightService.getLights().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Object group = getGroup(groupPosition);
        if (group == null) {
            Log.w(getClass().getSimpleName(), "Attempting to calculate children count of not-existent group");
            return 0;
        }
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (lightService.getLights().size() <= groupPosition) {
            Log.w(getClass().getSimpleName(), "Attempting to get not-existent group");
            return null;
        }
        return lightService.getLights().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listHeaderLabel = (TextView) convertView.findViewById(R.id.listHeaderLabel);
        listHeaderLabel.setTypeface(null, Typeface.BOLD);
        Light light = (Light) getGroup(groupPosition);
        String lightName = light.getName();
        listHeaderLabel.setText(lightName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Light light = (Light) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_without_delete, null);
        }
        initializeLightView(convertView, light);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void initializeLightView(final View view, final Light light) {
        final ToggleButton activeToggleButton = (ToggleButton) view.findViewById(R.id.activeToggleButton);
        final SurfaceView colorView = (SurfaceView) view.findViewById(R.id.colorView);
        final SeekBar brightnessSeekBar = (SeekBar) view.findViewById(R.id.brightnessSeekBar);
        final SeekBar saturationSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
        final SeekBar hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);

        activeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                light.setOn(isChecked);
                sendLightStateUpdate(light);
            }
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorView.setBackgroundColor(getColor(hueSeekBar, saturationSeekBar, brightnessSeekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                light.setBrightness(seekBar.getProgress());
                sendLightStateUpdate(light);
            }
        });

        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorView.setBackgroundColor(getColor(hueSeekBar, saturationSeekBar, brightnessSeekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                light.setSaturation(seekBar.getProgress());
                sendLightStateUpdate(light);
            }
        });

        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorView.setBackgroundColor(getColor(hueSeekBar, saturationSeekBar, brightnessSeekBar));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                light.setHue(seekBar.getProgress());
                sendLightStateUpdate(light);
            }
        });

        activeToggleButton.setChecked(light.isOn());
        brightnessSeekBar.setProgress(light.getBrightness());
        saturationSeekBar.setProgress(light.getSaturation());
        hueSeekBar.setProgress(light.getHue());
    }

    private int getColor(SeekBar hueSeekBar, SeekBar saturationSeekBar, SeekBar brightnessSeekBar) {
        float hue = (float) hueSeekBar.getProgress() * (float) 360 / (float) 65535;
        float saturation = (float) saturationSeekBar.getProgress() / (float) 255;
        float brightness = (float) brightnessSeekBar.getProgress() / (float) 255;
        float[] hsv = {hue, saturation, brightness};
        return Color.HSVToColor(hsv);
    }

    private void sendLightStateUpdate(Light light) {
        lightService.setLightState(baseActivity.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE),
                light.getId(),
                lightService.createExtras(light.isOn(), light.getHue(), light.getBrightness(), light.getSaturation(), Alert.none.name()));
    }
}
