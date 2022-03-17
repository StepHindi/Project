package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopUpdateServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, TimeService.class);
        context.stopService(service);
    }
}