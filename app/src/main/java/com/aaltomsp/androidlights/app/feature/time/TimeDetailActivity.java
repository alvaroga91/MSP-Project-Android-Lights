package com.aaltomsp.androidlights.app.feature.time;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.LightService;
import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TimeDetailActivity extends ActionBarActivity {
    public static final String EXTRA_NEW = "new";
    public static final String EXTRA_ID = "id";

    public static final String DATE_STYLE = "dd.MM.yyyy HH:mm:ss";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private ExpandableListView lightListView;
    private TextView dateTextView;

    private LightService lightService;

    private long time = -1;
    private List<Light> lights = new ArrayList<Light>();
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timedetail);
        Intent intent = getIntent();
        sharedPreferences = this.getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        lightService = LightService.getInstance();

        lightListView = (ExpandableListView) findViewById(R.id.timeFeatureExpandableListView);
        lightListView.setAdapter(listAdapter);

        dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        dateTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TimeDetailActivity.this);

                builder.setTitle("Delete");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        time = -1;
                        persistStateAndFinish();
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });

        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Light> availableLights = new ArrayList<Light>();

                List<Light> allLights = lightService.getLights();
                for (Light light : allLights) {
                    boolean isAvailable = true;
                    for (Light lightTmp : lights) {
                        if (light.getId() == lightTmp.getId()) {
                            isAvailable = false;
                            break;
                        }
                    }
                    if (isAvailable) {
                        availableLights.add(light.clone());
                    }
                }

                if (!availableLights.isEmpty()) {
                    openLightsDialog(availableLights);
                }
            }
        });

        id = intent.getLongExtra(EXTRA_ID, -1);
        if (!intent.getBooleanExtra(EXTRA_NEW, false)) {
            String lightsString = sharedPreferences.getString(TimeActivity.TIME_FEATURE + "lights_time_" + id, null);
            if (lightsString != null) {
                int index = lightsString.indexOf(TimeActivity.LIGHT_ITEM_SEPARATOR);
                time = Long.valueOf(lightsString.substring(0, index));
                lights.clear();
                Collections.addAll(lights, Light.deserialize(lightsString.substring(index+1)));
                updateTime();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadState();
        listAdapter.notifyDataSetChanged();
        if (time == -1) {
            showDatePicker();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        persistState();
    }

    private void persistState() {
        if (time == -1) {
            sharedPreferencesEditor.remove(TimeActivity.TIME_FEATURE + "lights_time_" + id);
        } else {
            String lightsString = "" + time + TimeActivity.LIGHT_ITEM_SEPARATOR;
            lightsString += Light.serialize(lights.toArray(new Light[0]));
            sharedPreferencesEditor.putString(TimeActivity.TIME_FEATURE + "lights_time_" + id, lightsString);
        }

        sharedPreferencesEditor.commit();
    }

    private void persistStateAndFinish() {
        persistState();
        finish();
    }

    private void loadState() {

    }

    @Override
    public void onBackPressed() {
        persistStateAndFinish();
    }

    private BaseExpandableListAdapter listAdapter = new BaseExpandableListAdapter() {
        @Override
        public int getGroupCount() {
            return lights.size();
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
            if (lights.size() <= groupPosition) {
                Log.w(getClass().getSimpleName(), "Attempting to get not-existent group");
                return null;
            }
            return lights.get(groupPosition);
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
                LayoutInflater layoutInflater = (LayoutInflater) TimeDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                LayoutInflater layoutInflater = (LayoutInflater) TimeDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item, null);
            }
            initializeLightView(convertView, light, groupPosition, childPosition);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private void initializeLightView(final View view, final Light light, final int groupPosition, final int childPosition) {
            final ToggleButton activeToggleButton = (ToggleButton) view.findViewById(R.id.activeToggleButton);
            final SurfaceView colorView = (SurfaceView) view.findViewById(R.id.colorView);
            final SeekBar brightnessSeekBar = (SeekBar) view.findViewById(R.id.brightnessSeekBar);
            final SeekBar saturationSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
            final SeekBar hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);
            final Button deleteButton = (Button) view.findViewById(R.id.deleteRuleButton);

            activeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    light.setOn(isChecked);
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
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lights.remove(light);
                    notifyDataSetChanged();
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
    };

    private void updateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeDetailActivity.DATE_STYLE);
        String format = simpleDateFormat.format(time);
        dateTextView.setText(format);

    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        if (time != -1) {
            c.setTimeInMillis(time);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and show it
        DatePickerDialog dialog =  new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    showTimePicker(year, monthOfYear, dayOfMonth);
                }
            }
        }, year, month, day);
        dialog.setOnCancelListener(cancelListener);

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.show();
    }

    private void showTimePicker(int year, int monthOfYear, int dayOfMonth) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        if (time != -1) {
            c.setTimeInMillis(time);
        }
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and show it
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);
                    if (c.before(Calendar.getInstance())) {
                        Toast.makeText(TimeDetailActivity.this, R.string.time_error_date_in_past, Toast.LENGTH_SHORT).show();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        showTimePicker(year, month, day);
                    } else {
                        time = c.getTimeInMillis();
                        updateTime();
                    }
                }
            }
        }, hour, minute, DateFormat.is24HourFormat(this));
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
    }

    private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            if (time == -1) {
                persistStateAndFinish();
            }
        }
    };

    /**
     * Opens a dialog with a list with all the lights retrieved from the response.
     */
    private void openLightsDialog(final List<Light> availableLights) {
        final View view = LayoutInflater.from(this).inflate(R.layout.lights_list, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Lights");

        alert.setView(view);

        final ListView lv = (ListView) view.findViewById(R.id.lightsListView);
        String[] lightNames = new String[availableLights.size()];
        for (int i = 0; i < availableLights.size(); i++) {
            lightNames[i] = availableLights.get(i).getName();
        }

        final ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lightNames);
        lv.setAdapter(modeAdapter);
        modeAdapter.notifyDataSetChanged();
        final AlertDialog dialog = alert.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Light light = availableLights.get(pos);
                if (!lights.contains(light)) {
                    lights.add(light);
                    Collections.sort(lights, new Comparator<Light>() {
                        @Override
                        public int compare(Light lhs, Light rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });
                }
                listAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
}
