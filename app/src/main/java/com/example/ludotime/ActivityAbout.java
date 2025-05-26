package com.example.ludotime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Activity that displays information about the LudoTime application.
 * Provides a GitHub link button and menu options to return to the main page.
 */
public class ActivityAbout extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets up the layout and initializes the GitHub link button with a click listener.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down then this Bundle contains the data it most
     *                          recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        Button btnGitHubLink = findViewById(R.id.btnGitHubLink);
        btnGitHubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace with your actual GitHub repository URL
                String githubUrl = "https://github.com/yardenfadel/SchoolProject-LudoTime";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
                startActivity(browserIntent);
            }
        });
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * Inflates the return to main page menu.
     *
     * @param menu The options menu in which you place your items.
     * @return true for the menu to be displayed; false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu,menu);
        return true;
    }

    /**
     * Called whenever an item in the options menu is selected.
     * Handles the return to main page menu item selection.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed,
     *                 true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn){
            //Toast.makeText(this, "return to home", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ActivityAbout.this, MainActivity.class);
            startActivity(intent);
        }


        return true;
    }
}