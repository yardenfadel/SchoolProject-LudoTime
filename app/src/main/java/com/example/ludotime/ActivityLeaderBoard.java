package com.example.ludotime;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ActivityLeaderBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        ArrayList<User> users = new ArrayList<>();
        for(int i=0; i<15; i++){
            users.add(new User("Ploni Almoni", 1200, "male_avatar"));
        }

        RecyclerView recyclerview = findViewById(R.id.recyclerview_leaderboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);

        UserAdapter userAdapter = new UserAdapter(users);
        recyclerview.setAdapter(userAdapter);

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
            Intent intent = new Intent(ActivityLeaderBoard.this, MainActivity.class);
            startActivity(intent);
        }


        return true;
    }
}