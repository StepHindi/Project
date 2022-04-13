package com.example.project;


import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import com.example.project.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    protected SharedPreferences sharedPreferences;
    private static final String TAG = "MyActivity_output";
    private long time, sec, min, hor;
    private boolean is_running;
    private Disposable disposable;
    private static final String APP_PREFERENCE = "TimePreference";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        is_running = false;
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);

        binding.buttonRunStop.setOnClickListener(v -> {
            if (!is_running) {
                Log.i(TAG, "Run clicked");
                is_running = true;
                binding.buttonRunStop.setText(R.string.cancel);
                // optimise in future
                long time_hours;
                long time_sec;
                long time_min;
                String text_min = binding.editTextTimeMin.getText().toString();
                String text_hours = binding.editTextTimeHours.getText().toString();
                String text_sec = binding.editTextTimeSeconds.getText().toString();
                if (!text_min.equals("")) {
                    time_min = Long.parseLong(text_min) * 60;
                } else {
                    time_min = 0;
                }
                if (!text_sec.equals("")) {
                    time_sec = Long.parseLong(text_sec) ;
                } else {
                    time_sec = 0;
                }
                if (!text_hours.equals("")) {
                    time_hours = Long.parseLong(text_hours) * 3600;
                } else {
                    time_hours = 0;
                }
                time = time_hours + time_sec + time_min + 1;
                if (time == 0) {
                    is_running = false;
                    return;
                }
                disposable =
                        Observable.fromCallable(this::updateTimer)
                                .subscribeOn(Schedulers.io())
                                .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::timeHandler, throwable -> Log.e(TAG, throwable.toString()));
                timeFieldsActivate(false);
                Log.i(TAG, "Time: " + time + " wil be start on schedule");

            }
            else {
                Log.i(TAG, "Cancel clicked");
                zeroTimeActions();

            }
        });
    }


    private long updateTimer() {
        time --;
        return time;
    }

    private void timeFieldsActivate(boolean needToActivate) {
        binding.editTextTimeSeconds.setEnabled(needToActivate);
        binding.editTextTimeMin.setEnabled(needToActivate);
        binding.editTextTimeHours.setEnabled(needToActivate);
    }
    private void setRemainTime(long hor, long min, long sec) {
        binding.editTextTimeHours.setText("" + hor);
        binding.editTextTimeMin.setText("" + min);
        binding.editTextTimeSeconds.setText("" + sec);
    }
    private void setRemainTime() {
        binding.editTextTimeHours.setText("");
        binding.editTextTimeMin.setText("");
        binding.editTextTimeSeconds.setText("");
    }

    private void timeHandler(long time) {
        if (time <= 0) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();}
            startMusicSequence();
            return;
        }
        sec = time % 60;
        min = time / 60 % 60;
        hor = time / 3600 % 60;
        setRemainTime(hor, min, sec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();}
        TimeService.start(MainActivity.this, time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(MainActivity.this, TimeService.class));
        if (sharedPreferences.contains("Time")){
        time = sharedPreferences.getLong("Time", 0);}
        Log.i(TAG, "Getted time: " + time);
        if (time == 0) {
            zeroTimeActions();
        }
        else {
            disposable =
                    Observable.fromCallable(this::updateTimer)
                            .subscribeOn(Schedulers.io())
                            .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::timeHandler, throwable -> Log.e(TAG, throwable.toString()));
        }
    }
    private void startMusicSequence() {
        setRemainTime();
        timeFieldsActivate(true);
        int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;
        mediaPlayer.start();
        Log.i(TAG, "Payer started!");
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        binding.buttonRunStop.setText(R.string.run);
        is_running = false;
    }

    private void zeroTimeActions() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();}
        Log.i(TAG, "disposed!");
        is_running = false;
        binding.buttonRunStop.setText(R.string.run);
        setRemainTime();
        timeFieldsActivate(true);
    }
}
