package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceHelper {
    SharedPreferences sharedPreferences;

    SharedPreferenceHelper(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    long getTime() {
        return sharedPreferences.getLong("Time", 0);
    }

    boolean getHasStopped() {
        return sharedPreferences.getBoolean("hasStopped", false);
    }

    void setTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("Time", time);
        editor.apply();
    }

    void setHasStopped(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("hasStopped", value);
        editor.apply();
    }
}
