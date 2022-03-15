package com.example.project;


import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.util.Log;

import com.example.project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements Runnable{

    protected ActivityMainBinding binding;

    private static final String TAG = "MyActivity_output";
    protected long time, sec, min, hor, loc_time;
    private boolean is_running;
    private TimeService timeService;
    Thread timeThread;
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


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long hor = intent.getLongExtra("Hours", 0);
            long min = intent.getLongExtra("Minutes", 0);
            long sec = intent.getLongExtra("Seconds", 0);
            setRemainTime(hor, min, sec);
            Log.i(TAG, "Broadcast recived");

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        is_running = false;
        setContentView(binding.getRoot());

        registerReceiver(
                mMessageReceiver, new IntentFilter("TimeSend"));

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);

        binding.buttonRunStop.setOnClickListener(v -> {
            if (!is_running) {
                Log.i(TAG, "Run clicked");
                is_running = true;
                binding.buttonRunStop.setText(R.string.cancel);
                long time_hours;
                long time_sec;
                long time_min;
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
                    is_running = false;
                    return;
                }
                timeThread = new Thread(MainActivity.this);
                timeThread.start();
                timeFieldsActivate(false);
                Log.i(TAG, "Time: " + time + " wil be start on schedule");

            }
            else {
                Log.i(TAG, "Cancel clicked");
                    time = -1;
                    timeThread.interrupt();
                    Log.i(TAG, "interruped!");
                    is_running = false;
                    binding.buttonRunStop.setText(R.string.run);
                    setRemainTime();
                try {

                    timeThread.join();
                    Log.i(TAG, "joined!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            runOnUiThread(() -> setRemainTime(hor, min, sec));
            time -= 1000;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (time >= 0);
        runOnUiThread(() -> {
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
        });
        is_running = false;

    }

    protected void timeFieldsActivate(boolean needToActivate) {
        binding.editTextTimeSeconds.setEnabled(needToActivate);
        binding.editTextTimeMin.setEnabled(needToActivate);
        binding.editTextTimeHours.setEnabled(needToActivate);
    }
    protected void setRemainTime(long hor, long min, long sec) {
        binding.editTextTimeHours.setText("" + hor);
        binding.editTextTimeMin.setText("" + min);
        binding.editTextTimeSeconds.setText("" + sec);
    }
    protected void setRemainTime() {
        binding.editTextTimeHours.setText("");
        binding.editTextTimeMin.setText("");
        binding.editTextTimeSeconds.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause");
        /*Intent intent = new Intent(MainActivity.this, TimeService.class);
        intent.putExtra("time", time);
        startService(intent);*/
        TimeService.start(MainActivity.this, time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(MainActivity.this, TimeService.class));
    }
}
