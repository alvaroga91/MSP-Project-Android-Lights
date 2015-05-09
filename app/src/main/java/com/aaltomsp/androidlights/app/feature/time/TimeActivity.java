package com.aaltomsp.androidlights.app.feature.time;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aaltomsp.androidlights.app.Light;
import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aaltomsp.androidlights.app.R.id.add_button;

public class TimeActivity extends ActionBarActivity {
    public static final String TIME_FEATURE = "TimeFeature";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private ToggleButton featureToggleButton;
    private ListView listView;

    private Map<Long, Pair<Long, Light[]>> timeLights;
    private List<Long> sortedIds;

    public final static char LIGHT_ITEM_SEPARATOR = '_';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        sharedPreferences = this.getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        timeLights = new HashMap<Long, Pair<Long, Light[]>>();
        sortedIds = new ArrayList<Long>();

        initializeButtons();

        listView = (ListView) findViewById(R.id.timeFeatureListView);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TimeActivity.this, TimeDetailActivity.class);
                intent.putExtra(TimeDetailActivity.EXTRA_ID, sortedIds.get(position));
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);

                builder.setTitle("Delete");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Long id = sortedIds.remove(position);
                        timeLights.remove(id);
                        listAdapter.notifyDataSetChanged();
                        AlarmHelper.renewAlarms(TimeActivity.this);
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

        Button addButton = (Button) findViewById(add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimeActivity.this, TimeDetailActivity.class);
                intent.putExtra(TimeDetailActivity.EXTRA_NEW, true);

                long newId = System.currentTimeMillis();
                sortedIds.add(newId);
                intent.putExtra(TimeDetailActivity.EXTRA_ID, newId);
                startActivity(intent);
            }
        });
    }

    private void initializeButtons() {
        featureToggleButton = (ToggleButton) findViewById(R.id.timeFeatureToggleButton);
        featureToggleButton.setChecked(sharedPreferences.getBoolean(TIME_FEATURE + "IsActive", false));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initializeButtons();
    }

    public void onToggleClicked(View view) {
        persistState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmHelper.renewAlarms(this);
        loadState();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        persistState();
    }

    private void persistState() {
        boolean isActive = featureToggleButton.isChecked();

        sharedPreferencesEditor.putBoolean(TIME_FEATURE + "IsActive", isActive);

        String idString = "";
        for (int i = 0; i < sortedIds.size(); i++) {
            if (i > 0) {
                idString += LIGHT_ITEM_SEPARATOR;
            }
            idString += sortedIds.get(i);
        }
        sharedPreferencesEditor.putString(TIME_FEATURE + "lights_time", idString);

        for (Long id : timeLights.keySet()) {
            Pair<Long, Light[]> lightPair = timeLights.get(id);
            String lightsString = "" + lightPair.first + LIGHT_ITEM_SEPARATOR;
            lightsString += Light.serialize(lightPair.second);
            sharedPreferencesEditor.putString(TIME_FEATURE + "lights_time_" + id, lightsString);
        }

        sharedPreferencesEditor.commit();
    }

    private void persistStateAndFinish() {
        persistState();

        if (featureToggleButton.isChecked()) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void loadState() {
        featureToggleButton.setChecked(sharedPreferences.getBoolean(TIME_FEATURE + "IsActive", false));

        sortedIds.clear();
        String savedIds = sharedPreferences.getString(TIME_FEATURE + "lights_time", "");
        String[] split = savedIds.split("" + LIGHT_ITEM_SEPARATOR);
        for (String id : split) {
            if (!"".equals(id)) {
                sortedIds.add(Long.valueOf(id));
            }
        }

        timeLights.clear();
        for (Long id : sortedIds) {
            String lightsString = sharedPreferences.getString(TIME_FEATURE + "lights_time_" + id, null);
            if (lightsString != null) {
                int index = lightsString.indexOf(LIGHT_ITEM_SEPARATOR);
                long time = Long.valueOf(lightsString.substring(0, index));

                Light[] lights = Light.deserialize(lightsString.substring(index+1));

                Pair<Long, Light[]> lightPair = new Pair<Long, Light[]>(time, lights);
                timeLights.put(id, lightPair);
            }
        }

        Collections.sort(sortedIds, new Comparator<Long>() {
            @Override
            public int compare(Long lhs, Long rhs) {
                Pair<Long, Light[]> leftPair = timeLights.get(lhs);
                Pair<Long, Light[]> rightPair = timeLights.get(rhs);
                return leftPair.first < rightPair.first ? -1 : 1;
            }
        });
    }

    @Override
    public void onBackPressed() {
        persistStateAndFinish();
    }

    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return sortedIds.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) {
                LayoutInflater inflater = (LayoutInflater) TimeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Long id = sortedIds.get(position);
            Pair<Long, Light[]> lightPair = timeLights.get(id);
            TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeDetailActivity.DATE_STYLE);
            String format = simpleDateFormat.format(lightPair.first);
            text1.setText(format);

            return v;
        }
    };
}
