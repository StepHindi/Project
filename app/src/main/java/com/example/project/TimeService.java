package com.example.project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TimeService extends Service {
    private static final String TAG = "MyActivity_output";

    public TimeService() {
    }

    private MainActivity mainActivity = new MainActivity();
    long sec, min, hor, loc_time;
    private static final int NOTIFY_ID = 101;
    private static String CHANNEL_ID = "Service channel";
    protected MediaPlayer mediaPlayer;
    AudioManager audioManager;
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

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        loc_time = (long)  intent.getSerializableExtra("time") / 1000;
        do {
            sec = loc_time % 60;
            min = loc_time / 60 % 60;
            hor = loc_time / 3600 % 60;
            Notification notification = new NotificationCompat.Builder(TimeService.this, CHANNEL_ID)
                    .setContentTitle("Таймер запущен")
                    .setContentText("Осталось:" + loc_time)
                    .build();
            notification.flags = Notification.FLAG_INSISTENT;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFY_ID, notification);
                Log.i(TAG, "Notify showed");
            }
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
            Log.i(TAG, "TS: " + "Payer started!");
            audioManager.abandonAudioFocus(audioFocusChangeListener);

        });
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void sendMessageToActivity(long hor, long min, long sec) {
        Intent intent = new Intent("TimeSend");
        intent.putExtra("Hours", hor);
        intent.putExtra("Minutes", min);
        intent.putExtra("Seconds", sec);
        sendBroadcast(intent);
        Log.i(TAG, "Broadcast send");
    }
}