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

public class MainActivity extends AppCompatActivity {

    protected ActivityMainBinding binding;
    private Timer timer;
    private MediaPlayer mediaPlayer;
    private static final String TAG = "MyActivity_output";

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
        setContentView(binding.getRoot());
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.stopper);
        binding.buttonRun.setOnClickListener(v -> {
            Log.i(TAG, "Button clicked");
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            String text = binding.editTextTime.getText().toString();
            if (!text.equals("")) {
                long time = Long.parseLong(text) * 1000 * 60;
                Log.i(TAG, "Time: " + time + " wil be start on schedule");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);
                        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                            return;
                        mediaPlayer.start();
                        Log.i(TAG, "Payer started!");
                        audioManager.abandonAudioFocus(audioFocusChangeListener);
                    }
                }, time);
            }

        });
        binding.buttonCancel.setOnClickListener(v -> {
            Log.i(TAG, "Cancel clicked");
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        });
    }
}
