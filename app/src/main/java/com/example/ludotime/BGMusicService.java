package com.example.ludotime;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BGMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private float currentVolume = 1.0f;
    public static final String PREFS_NAME = "LudoTimePrefs";
    public static final String MUSIC_VOLUME_KEY = "musicVolume";
    public static final String ACTION_UPDATE_VOLUME = "com.example.ludotime.ACTION_UPDATE_VOLUME";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music_track_jazz); // Replace with your actual MP3 filename
        mediaPlayer.setLooping(true);

        // Get saved volume from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentVolume = prefs.getFloat(MUSIC_VOLUME_KEY, 1.0f);
        updateVolume(currentVolume);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_UPDATE_VOLUME.equals(intent.getAction())) {
            if (intent.hasExtra(MUSIC_VOLUME_KEY)) {
                currentVolume = intent.getFloatExtra(MUSIC_VOLUME_KEY, currentVolume);
                updateVolume(currentVolume);
            }
        } else {
            // Start playing if not at minimum volume
            if (currentVolume > 0 && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

        return START_STICKY;
    }

    private void updateVolume(float volume) {
        if (mediaPlayer != null) {
            // Set the volume for both left and right channels
            mediaPlayer.setVolume(volume, volume);

            // Pause if volume is 0, play otherwise
            if (volume <= 0) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } else {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }

            // Save the current volume to preferences
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putFloat(MUSIC_VOLUME_KEY, volume);
            editor.apply();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}