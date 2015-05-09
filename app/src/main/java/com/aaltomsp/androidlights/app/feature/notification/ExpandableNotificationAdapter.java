package com.aaltomsp.androidlights.app.feature.notification;

import com.aaltomsp.androidlights.app.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class ExpandableNotificationAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Notification> notifications;
    private PreferenceManager preferenceManager;

    public ExpandableNotificationAdapter(Context context, ArrayList<Notification> notifications) {
        this.notifications = notifications;
        this.preferenceManager = new PreferenceManager(context);
        this.context = context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (notifications.size() <= groupPosition) {
            return null;
        }
        return notifications.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (notifications.size() <= groupPosition) {
            return null;
        }
        return notifications.get(groupPosition).getName();
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Notification notification = (Notification) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.notification_list_item, null);
        }
        initializeNotificationRuleView(convertView, notification, groupPosition, childPosition);
        return convertView;
    }

    @Override
    public int getGroupCount() {
        return notifications.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listHeaderLabel = (TextView) convertView.findViewById(R.id.listHeaderLabel);
        listHeaderLabel.setTypeface(null, Typeface.BOLD);
        listHeaderLabel.setText(headerTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private int getColor(SeekBar hueSeekBar, SeekBar saturationSeekBar, SeekBar brightnessSeekBar) {
        float hue = (float) hueSeekBar.getProgress() * (float) 360 / (float) 65535;
        float saturation = (float) saturationSeekBar.getProgress() / (float) 255;
        float brightness = (float) brightnessSeekBar.getProgress() / (float) 255;
        float[] hsv = {hue, saturation, brightness};
        return Color.HSVToColor(hsv);
    }

    private void initializeNotificationRuleView(final View view, final Notification notification, final int groupPosition, final int childPosition) {
        final ToggleButton activeToggleButton = (ToggleButton) view.findViewById(R.id.activeToggleButton);
        final SurfaceView colorView = (SurfaceView) view.findViewById(R.id.colorView);
        final SeekBar brightnessSeekBar = (SeekBar) view.findViewById(R.id.brightnessSeekBar);
        final SeekBar saturationSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
        final SeekBar hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);
        final Button deleteButton = (Button) view.findViewById(R.id.deleteRuleButton);

        activeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                notification.setOn(isChecked);
                preferenceManager.turnOn(notification.getName(), isChecked);
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
                notification.setBrightness(seekBar.getProgress());
                preferenceManager.writePreferences(notification.getName(), notification.getLight(), seekBar.getProgress(), notification.getFrequency(), notification.getHue(), notification.getSat());
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
                notification.setSat(seekBar.getProgress());
                preferenceManager.writePreferences(notification.getName(), notification.getLight(), notification.getBrightness(), notification.getFrequency(), notification.getHue(), seekBar.getProgress());
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
                notification.setHue(seekBar.getProgress());
                preferenceManager.writePreferences(notification.getName(), notification.getLight(), notification.getBrightness(), notification.getFrequency(), seekBar.getProgress(), notification.getSat());
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferenceManager.deletePreferences(notifications.get(groupPosition).getName());
                notifications.remove(groupPosition);
                notifyDataSetChanged();
            }
        });

        activeToggleButton.setChecked(notification.isOn());
        brightnessSeekBar.setProgress(notification.getBrightness());
        saturationSeekBar.setProgress(notification.getSat());
        hueSeekBar.setProgress(notification.getHue());
    }
}
