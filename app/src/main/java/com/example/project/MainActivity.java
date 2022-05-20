package com.example.project;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.project.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    protected SharedPreferences sharedPreferences;
    private static final String TAG = "MyActivity_output";
    private long time;
    private boolean isRunning;
    private Disposable disposable;
    protected MediaPlayer mediaPlayer;
    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.pause();
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        isRunning = false;
        setContentView(binding.getRoot());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);
        binding.buttonRunStop.setOnClickListener(v -> {
            if (!isRunning) {
                Log.i(TAG, "Run clicked");
                isRunning = true;
                binding.buttonRunStop.setText(R.string.cancel);
                // optimise in future
                long timeHours;
                long timeSec;
                long timeMin;
                String textMin = binding.editTextTimeMin.getText().toString();
                String textHours = binding.editTextTimeHours.getText().toString();
                String textSec = binding.editTextTimeSeconds.getText().toString();
                if (!textMin.equals("")) {
                    timeMin = Long.parseLong(textMin) * 60;
                } else {
                    timeMin = 0;
                }
                if (!textSec.equals("")) {
                    timeSec = Long.parseLong(textSec);
                } else {
                    timeSec = 0;
                }
                if (!textHours.equals("")) {
                    timeHours = Long.parseLong(textHours) * 3600;
                } else {
                    timeHours = 0;
                }
                time = timeHours + timeSec + timeMin;
                if (time == 0) {
                    isRunning = false;
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

            } else {
                time = 0;
                Log.i(TAG, "Cancel clicked");
                zeroTimeActions();

            }
        });
    }


    private long updateTimer() {
        time--;
        return time;
    }

    private void timeFieldsActivate(boolean needToActivate) {
        binding.editTextTimeSeconds.setEnabled(needToActivate);
        binding.editTextTimeMin.setEnabled(needToActivate);
        binding.editTextTimeHours.setEnabled(needToActivate);
    }

    @SuppressLint("SetTextI18n")
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
                disposable.dispose();
            }
            startMusicSequence();
            return;
        }
        long sec = time % 60;
        long min = time / 60 % 60;
        long hor = time / 3600 % 60;
        setRemainTime(hor, min, sec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        Log.i(TAG, "Service start conditions:\n SH time: " + sharedPreferences.getLong("Time", 0) + "\n locTime: " + time);
        if (sharedPreferences.getLong("Time", 0) > 0 || time > 0) {
            TimeService.start(MainActivity.this, time);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(MainActivity.this, TimeService.class));
        if (sharedPreferences.getBoolean("hasStopped", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("Time", 0);
            editor.apply();
            time = 0;
        } else {
            time = sharedPreferences.getLong("Time", 0);
        }
        Log.i(TAG, "Received time: " + time);
        if (time == 0) {
            zeroTimeActions();
        } else {
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
        Log.i(TAG, "Payer started in activity");
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        binding.buttonRunStop.setText(R.string.run);
        isRunning = false;
    }

    private void zeroTimeActions() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        Log.i("SIM_output", "disposed!");
        isRunning = false;
        binding.buttonRunStop.setText(R.string.run);
        setRemainTime();
        timeFieldsActivate(true);
    }
}
