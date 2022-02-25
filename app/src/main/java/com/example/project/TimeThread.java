package com.example.project;

import android.media.AudioManager;
import android.util.Log;

// не используется
public class TimeThread extends Thread{

    long sec, min, hor, loc_time;

    MainActivity mainActivity = new MainActivity();

    public TimeThread(long loc_time) {
        this.loc_time = loc_time;
    }

    @Override
    public void run() {
        loc_time /= 1000;
        do {
            sec = loc_time % 60;
            min = loc_time / 60 % 60;
            hor = loc_time / 3600 % 60;
    //        mainActivity.runOnUiThread(() -> {
    //            mainActivity.setRemainigTime(hor, min, sec);
    //        });
            loc_time -= 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (loc_time >= 0);
        mainActivity.runOnUiThread(() -> {
            // startPlayer();
        });
    }
}
