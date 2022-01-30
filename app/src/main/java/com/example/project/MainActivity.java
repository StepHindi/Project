package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.project.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Runnable{

    protected ActivityMainBinding binding;
    private Timer timer;
    private MediaPlayer mediaPlayer;
    private static final String TAG = "MyActivity_output";
    long time;
    long sec, min, hor, loc_time;
    boolean is_running;
    Thread timerThread;

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        is_running = false;
        setContentView(binding.getRoot());
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);
        binding.buttonRunStop.setOnClickListener(v -> {
            if (!is_running) {
                Log.i(TAG, "Run clicked");
                is_running = true;
                if (timer != null) {
                    timer.cancel();
                }
                binding.buttonRunStop.setText(R.string.cancel);
                timer = new Timer();
                long time_hours;
                long time_sec;
                long time_min;
                binding.editTextTimeSeconds.setEnabled(false);
                binding.editTextTimeMin.setEnabled(false);
                binding.editTextTimeHours.setEnabled(false);
                String text_min = binding.editTextTimeMin.getText().toString();
                String text_hours = binding.editTextTimeHours.getText().toString();
                String text_sec = binding.editTextTimeSeconds.getText().toString();
                if (!text_min.equals("")) {
                    time_min = Long.parseLong(text_min) * 1000 * 60;
                } else {
                    time_min = 0;
                }
                if (!text_sec.equals("")) {
                    time_sec = Long.parseLong(text_sec) * 1000;
                } else {
                    time_sec = 0;
                }
                if (!text_hours.equals("")) {
                    time_hours = Long.parseLong(text_hours) * 1000 * 3600;
                } else {
                    time_hours = 0;
                }
                time = time_hours + time_sec + time_min;
                if (time == 0) {
                    return;
                }
                Log.i(TAG, "Time: " + time + " wil be start on schedule");
                timerThread = new Thread(MainActivity.this);
                timerThread.start();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);
                            if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                                return;
                            mediaPlayer.start();
                        });

                        Log.i(TAG, "Payer started!");
                        audioManager.abandonAudioFocus(audioFocusChangeListener);
                        binding.buttonRunStop.setText(R.string.run);
                    }
                }, time);
            }
            else {
                Log.i(TAG, "Cancel clicked");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                    time = -1;
                    timerThread.interrupt();
                    Log.i(TAG, "interruped!");
                    is_running = false;
                    binding.buttonRunStop.setText(R.string.run);
                    binding.editTextTimeSeconds.setText("");
                    binding.editTextTimeHours.setText("");
                    binding.editTextTimeMin.setText("");
                    try {

                        timerThread.join();
                        Log.i(TAG, "joined!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void run() {

        do {
            loc_time = time / 1000;
            sec = loc_time % 60;
            min = loc_time / 60 % 60;
            hor = loc_time / 3600 % 60;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.editTextTimeHours.setText("" + hor);
                    binding.editTextTimeMin.setText("" + min);
                    binding.editTextTimeSeconds.setText("" + sec);
                }
            });
            time -= 1000;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (time >= 0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.editTextTimeSeconds.setEnabled(true);
                binding.editTextTimeMin.setEnabled(true);
                binding.editTextTimeHours.setEnabled(true);
            }
        });

    }
}
