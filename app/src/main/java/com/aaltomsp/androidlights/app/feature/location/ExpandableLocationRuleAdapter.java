package com.aaltomsp.androidlights.app.feature.location;

import java.util.HashMap;
import java.util.List;

import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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

public class ExpandableLocationRuleAdapter extends BaseExpandableListAdapter {

    private final SharedPreferences sharedPreferences;
    private final Editor sharedPreferencesEditor;
    private Context context;
    private List<String> listDataHeaders;
    private HashMap<String, LocationRule> listDataContent;

    public ExpandableLocationRuleAdapter(Context context, List<String> listDataHeaders, HashMap<String, LocationRule> listDataContent) {
        this.context = context;
        this.listDataHeaders = listDataHeaders;
        this.listDataContent = listDataContent;
        this.sharedPreferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        Object group = getGroup(groupPosition);
        if (group == null) {
            return null;
        }
        return listDataContent.get(group);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final LocationRule rule = (LocationRule) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        initializeLocationRuleView(convertView, rule, groupPosition, childPosition);
        return convertView;
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
        if (listDataHeaders.size() <= groupPosition) {
            Log.w(getClass().getSimpleName(), "Attempting to get not-existent group");
            return null;
        }
        return listDataHeaders.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listDataHeaders.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        return true;
    }

    private void initializeLocationRuleView(final View view, final LocationRule rule, final int groupPosition, final int childPosition) {
        final ToggleButton activeToggleButton = (ToggleButton) view.findViewById(R.id.activeToggleButton);
        final SurfaceView colorView = (SurfaceView) view.findViewById(R.id.colorView);
        final SeekBar brightnessSeekBar = (SeekBar) view.findViewById(R.id.brightnessSeekBar);
        final SeekBar saturationSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
        final SeekBar hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);
        final Button deleteButton = (Button) view.findViewById(R.id.deleteRuleButton);

        activeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rule.setOn(isChecked);
                LocationService.storeRule(rule, sharedPreferencesEditor);
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
                rule.setBrightness(seekBar.getProgress());
                LocationService.storeRule(rule, sharedPreferencesEditor);
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
                rule.setSaturation(seekBar.getProgress());
                LocationService.storeRule(rule, sharedPreferencesEditor);
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
                rule.setHue(seekBar.getProgress());
                LocationService.storeRule(rule, sharedPreferencesEditor);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.removeRule(rule, sharedPreferencesEditor);
                listDataContent.remove(getGroup(groupPosition));
                listDataHeaders.remove(getGroup(groupPosition));
                notifyDataSetChanged();
            }
        });

        activeToggleButton.setChecked(rule.isOn());
        brightnessSeekBar.setProgress(rule.getBrightness());
        saturationSeekBar.setProgress(rule.getSaturation());
        hueSeekBar.setProgress(rule.getHue());
    }

    private int getColor(SeekBar hueSeekBar, SeekBar saturationSeekBar, SeekBar brightnessSeekBar) {
        float hue = (float) hueSeekBar.getProgress() * (float) 360 / (float) 65535;
        float saturation = (float) saturationSeekBar.getProgress() / (float) 255;
        float brightness = (float) brightnessSeekBar.getProgress() / (float) 255;
        float[] hsv = {hue, saturation, brightness};
        return Color.HSVToColor(hsv);
    }
}