package com.example.ludotime;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ludotime.R;

public class ActivityGameMultiplayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_multiplayer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn){
            //Toast.makeText(this, "return to home", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ActivityGameMultiplayer.this, MainActivity.class);
            startActivity(intent);
        }


        return true;
    }
}