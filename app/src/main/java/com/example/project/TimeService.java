package com.example.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TimeService extends Service {
    private static final String TAG = "MyActivity_output";

    public TimeService() {
    }

    private MainActivity mainActivity = new MainActivity();
    long sec, min, hor, loc_time;
    private static final int NOTIFY_ID = 101;
    private static String CHANNEL_ID = "Service channel";
    protected MediaPlayer mediaPlayer;
    private Disposable disposable;

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
        super.onCreate();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Старт")
                .build();

        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service started");
        loc_time = (long)  intent.getSerializableExtra("time") / 1000;

        disposable =
                Observable.fromCallable(this::updateTimer).subscribeOn(Schedulers.io())
                        .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::showResultNotification, throwable -> Log.e(TAG, throwable.toString()));

        /*do {
            sec = loc_time % 60;
            min = loc_time / 60 % 60;
            hor = loc_time / 3600 % 60;
            Notification notification = new NotificationCompat.Builder(TimeService.this, CHANNEL_ID)
                    .setContentTitle("Таймер запущен")
                    .setContentText("Осталось:" + loc_time)
                    .build();
            //notification.flags = Notification.FLAG_INSISTENT;
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
        while (loc_time >= 0);*/
        /*mainActivity.runOnUiThread(() -> {
            int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return;
            mediaPlayer.start();
            Log.i(TAG, "TS: " + "Payer started!");
            audioManager.abandonAudioFocus(audioFocusChangeListener);

        });*/
        return Service.START_STICKY;
    }

    private long updateTimer() {
        loc_time -= 1;
        return loc_time;
    }

    private void showResultNotification(long time) {
        sec = time % 60;
        min = time / 60 % 60;
        hor = time / 3600 % 60;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Таймер запущен")
                .setContentText("Осталось:" + time)
                .build();

        //notification.flags = Notification.FLAG_INSISTENT;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (time <= 0) {
                stopSelf();
                notificationManager.cancel(NOTIFY_ID);
            } else {
                notificationManager.notify(NOTIFY_ID, notification);
                Log.i(TAG, "Notify showed");
            }
        }


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

    }

    private void sendMessageToActivity(long hor, long min, long sec) {
        Intent intent = new Intent("TimeSend");
        intent.putExtra("Hours", hor);
        intent.putExtra("Minutes", min);
        intent.putExtra("Seconds", sec);
        sendBroadcast(intent);
        Log.i(TAG, "Broadcast send");
    }

    public static void start(Context context, long time) {
        Intent serviceIntent = new Intent(context, TimeService.class);
        serviceIntent.putExtra("time", time);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}