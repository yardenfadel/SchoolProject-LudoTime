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
import java.util.Random;

public class ActivityLeaderBoard extends AppCompatActivity implements FireBaseListener {
    FireBaseController fireBaseController;
    RecyclerView recyclerView;
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        fireBaseController = new FireBaseController();
        fireBaseController.readUsersList(this);

        ArrayList<User> users = new ArrayList<>();
        //set fake values for when offline
        Random r = new Random();
        for(int i=0; i<14; i++){
            users.add(new User("pa@gm.com", "pa200", "No real results" + i, 7654-121*i-r.nextInt(68), "male_avatar"));
        }
        recyclerView = findViewById(R.id.recyclerview_leaderboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(users);
        recyclerView.setAdapter(userAdapter);

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

    @Override
    public void onCallbackUser(User u) {

    }

    @Override
    public void onCallbackUsers(ArrayList<User> users) {
        userAdapter = new UserAdapter(users);
        recyclerView.setAdapter(userAdapter);
    }

    @Override
    public void onCallbackFromLogin(){}
}