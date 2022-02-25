package com.example.project;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class TimeService extends Service {
    private static final String TAG = "TimeService_output";

    public TimeService() {
    }
    private MainActivity mainActivity = new MainActivity();
    protected MediaPlayer mediaPlayer;
    private TimeThread timeThread;
    AudioManager audioManager;
    long sec, min, hor, loc_time;

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            mediaPlayer.start();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mediaPlayer.pause();
                            break;
                        default:
                            mediaPlayer.pause();
                            break;

                    }
                }
            };

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // timeThread = new TimeThread((long) intent.getSerializableExtra("time"));
       // timeThread.start();
        loc_time = (long)  intent.getSerializableExtra("time") / 1000;
        do {
            sec = loc_time % 60;
            min = loc_time / 60 % 60;
            hor = loc_time / 3600 % 60;
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.setRemainigTime(hor, min, sec);
                    });
            loc_time -= 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (loc_time >= 0);
        mainActivity.runOnUiThread(() -> {
            int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return;
            mediaPlayer.start();
            Log.i(TAG, "Payer started!");
            audioManager.abandonAudioFocus(audioFocusChangeListener);

        });
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivity.time = -1;
        timeThread.interrupt();
        try {
            timeThread.join();
            Log.i(TAG, "joined!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected boolean isExecuting() {
        return timeThread.isAlive();
    }

    protected void startPlayer() {


    }
}