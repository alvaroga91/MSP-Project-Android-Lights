package com.aaltomsp.androidlights.app;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LightAPIResponseEventBroadcaster {

    private static LightAPIResponseEventBroadcaster lightAPIResponseEventBroadcaster;
    private List<Context> listeners;

    private LightAPIResponseEventBroadcaster() {
        listeners = new ArrayList<Context>();
    }

    public static LightAPIResponseEventBroadcaster getInstance() {
        if (lightAPIResponseEventBroadcaster == null) {
            lightAPIResponseEventBroadcaster = new LightAPIResponseEventBroadcaster();
        }
        return lightAPIResponseEventBroadcaster;
    }

    public void register(Context listener) {
        listeners.add(listener);
    }

    public void unregister(Context listener) {
        listeners.remove(listener);
    }

    public void broadcast(String message) {
        for (Context listener : listeners) {
            Toast.makeText(listener, message, Toast.LENGTH_LONG).show();
        }
    }
}
