package com.aaltomsp.androidlights.app.feature.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlarmService.class);
        service.putExtras(intent.getExtras());
        context.startService(service);
    }
}
