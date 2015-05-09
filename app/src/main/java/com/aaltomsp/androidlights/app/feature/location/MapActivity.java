package com.aaltomsp.androidlights.app.feature.location;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.aaltomsp.androidlights.app.MainActivity;
import com.aaltomsp.androidlights.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapClickListener, OnMapLongClickListener, OnMapReadyCallback {

    private SharedPreferences sharedPreferences;
    private Editor sharedPreferencesEditor;
    private LatLng baseLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sharedPreferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        float baseLatitude = sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLatitude", LocationActivity.DEFAULT_LATITUDE);
        float baseLongitude = sharedPreferences.getFloat(LocationService.LOCATION_FEATURE + ".CoordinateLongitude", LocationActivity.DEFAULT_LONGITUDE);
        baseLatLng = new LatLng(baseLatitude, baseLongitude);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
     public void onMapReady(GoogleMap map) {
        map.setOnMapClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(baseLatLng, 18));
        map.addMarker(new MarkerOptions().position(baseLatLng));
    }

    private void storeSelectedCoordinates(LatLng coordinates) {
        if (coordinates == null) {
            return;
        }
        sharedPreferencesEditor.putFloat(LocationService.LOCATION_FEATURE + ".CoordinateLatitude", (float) coordinates.latitude);
        sharedPreferencesEditor.putFloat(LocationService.LOCATION_FEATURE + ".CoordinateLongitude", (float) coordinates.longitude);
        sharedPreferencesEditor.commit();
    }

    @Override
    public void onMapClick(LatLng coordinates) {
        storeSelectedCoordinates(coordinates);
        finish();
    }

    @Override
    public void onMapLongClick(LatLng coordinates) {
        storeSelectedCoordinates(coordinates);
        finish();
    }
}
