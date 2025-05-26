package com.example.ludotime;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * ActivitySettings.java
 *
 * Settings activity for the LudoTime application.
 * Handles user preferences including music volume control,
 * player name changes, and avatar selection.
 */
public class ActivitySettings extends AppCompatActivity {
    private SeekBar musicVolumeSeekBar;
    private static final String PREFS_NAME = "LudoTimePrefs";
    private static final String MUSIC_VOLUME_KEY = "musicVolume";

    /**
     * Initializes the activity and sets up UI components and event listeners
     *
     * @param savedInstanceState Contains data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button bRename = findViewById(R.id.bSettingsRename);
        bRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivitySettings.this, "rename", Toast.LENGTH_SHORT).show();
            }
        });

        Button bChangeAvatar = findViewById(R.id.bSettingsAvatar);
        bChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivitySettings.this, "change avatar", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up music volume control
        setupMusicVolumeControl();
    }

    /**
     * Sets up the music volume control seekbar with saved preferences
     * and configures the volume change listener
     */
    private void setupMusicVolumeControl() {
        musicVolumeSeekBar = findViewById(R.id.volumeMusic);

        // Get the saved volume value
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float savedVolume = prefs.getFloat(MUSIC_VOLUME_KEY, 1.0f);

        // SeekBar works with integers from 0-100, so convert the float value
        int seekBarValue = (int)(savedVolume * 100);
        musicVolumeSeekBar.setProgress(seekBarValue);

        // Set up listener for volume changes
        musicVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateMusicVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
    }

    /**
     * Updates the music volume by sending a command to the background music service
     * and saves the volume setting to shared preferences
     *
     * @param progress The seekbar progress value (0-100)
     */
    private void updateMusicVolume(int progress) {
        // Convert seekbar value (0-100) to volume (0.0-1.0)
        float volume = (float) progress / 100f;

        // Send command to music service to update volume
        Intent intent = new Intent(this, BGMusicService.class);
        intent.setAction(BGMusicService.ACTION_UPDATE_VOLUME);
        intent.putExtra(BGMusicService.MUSIC_VOLUME_KEY, volume);
        startService(intent);

        // Save the volume setting
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(MUSIC_VOLUME_KEY, volume);
        editor.apply();
    }

    /**
     * Inflates the options menu with the return to main page option
     *
     * @param menu The options menu to inflate
     * @return True if the menu was successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu, menu);
        return true;
    }

    /**
     * Handles menu item selection, specifically the return to main page option
     *
     * @param item The selected menu item
     * @return True if the item selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn){
            Intent intent = new Intent(ActivitySettings.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }
}