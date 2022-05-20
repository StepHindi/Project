package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StopUpdateServiceReceiver extends BroadcastReceiver {
    SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, TimeService.class);
        sharedPreferenceHelper = new SharedPreferenceHelper(context);
        sharedPreferenceHelper.setHasStopped(true);
        context.stopService(service);
    }
}