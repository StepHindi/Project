package com.example.project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity{

    protected ActivityMainBinding binding;

    private static final String TAG = "MyActivity_output";
    protected long time;
    private boolean is_running;
    private TimeService timeService;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            long hor = intent.getLongExtra("Hours", 0);
            long min = intent.getLongExtra("Minutes", 0);
            long sec = intent.getLongExtra("Seconds", 0);
            setRemainigTime(hor, min, sec);
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
                timeFieldsActivate(false);
                Log.i(TAG, "Time: " + time + " wil be start on schedule");
                Intent intent = new Intent(MainActivity.this, TimeService.class);
                intent.putExtra("time", time);
                startService(intent);
                timeFieldsActivate(true);
                binding.buttonRunStop.setText(R.string.run);
            }
            else {
                Log.i(TAG, "Cancel clicked");

                    stopService(new Intent(MainActivity.this, TimeService.class));
                    Log.i(TAG, "interruped!");
                    is_running = false;
                    binding.buttonRunStop.setText(R.string.run);
                    binding.editTextTimeSeconds.setText("");
                    binding.editTextTimeHours.setText("");
                    binding.editTextTimeMin.setText("");

            }
        });
    }

    protected void timeFieldsActivate(boolean needToActivate) {
        binding.editTextTimeSeconds.setEnabled(needToActivate);
        binding.editTextTimeMin.setEnabled(needToActivate);
        binding.editTextTimeHours.setEnabled(needToActivate);
    }
    protected void setRemainigTime(long hor, long min, long sec) {
        binding.editTextTimeHours.setText("" + hor);
        binding.editTextTimeMin.setText("" + min);
        binding.editTextTimeSeconds.setText("" + sec);
    }

}
