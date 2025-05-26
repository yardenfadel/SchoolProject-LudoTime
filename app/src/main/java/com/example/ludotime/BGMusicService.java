/**
 * BGMusicService.java
 *
 * Background Music Service for the LudoTime application.
 * Handles playing, pausing, and volume control for background music.
 */
package com.example.ludotime;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BGMusicService extends Service {
    // ===== Media Player =====
    private MediaPlayer mediaPlayer;
    private float currentVolume = 1.0f;

    // ===== Constants =====
    public static final String PREFS_NAME = "LudoTimePrefs";
    public static final String MUSIC_VOLUME_KEY = "musicVolume";
    public static final String ACTION_UPDATE_VOLUME = "com.example.ludotime.ACTION_UPDATE_VOLUME";
    public static final String ACTION_STOP_MUSIC = "com.example.ludotime.ACTION_STOP_MUSIC";

    /**
     * Initialize the service and set up the media player
     */
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

    /**
     * Handle service start commands
     *
     * @param intent The Intent that was used to bind to this service
     * @param flags Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return The return value indicates what semantics the system should use for the service's current started state
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            // Handle volume update action
            if (ACTION_UPDATE_VOLUME.equals(action)) {
                if (intent.hasExtra(MUSIC_VOLUME_KEY)) {
                    currentVolume = intent.getFloatExtra(MUSIC_VOLUME_KEY, currentVolume);
                    updateVolume(currentVolume);
                }
            }
            // Handle stop music action
            else if (ACTION_STOP_MUSIC.equals(action)) {
                stopSelf(); // Stop the service when this action is received
            }
            // Default action: play music if not at minimum volume
            else {
                if (currentVolume > 0 && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        }

        // Use START_NOT_STICKY instead of START_STICKY to prevent automatic restart
        return START_NOT_STICKY;
    }

    /**
     * Update the volume level and save to preferences
     *
     * @param volume Volume level between 0.0 and 1.0
     */
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

    /**
     * Clean up resources when service is destroyed
     */
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

    /**
     * Return null as this is not a bound service
     *
     * @param intent The Intent that was used to bind to this service
     * @return Always returns null since this service is not designed to be bound
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}