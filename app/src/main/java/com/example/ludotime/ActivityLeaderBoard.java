package com.example.ludotime;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Activity that displays the leaderboard showing user rankings.
 * Implements FireBaseListener to handle Firebase data callbacks.
 * Shows both online data from Firebase and offline fallback data.
 */
public class ActivityLeaderBoard extends AppCompatActivity implements FireBaseListener {
    FirebaseController fireBaseController;
    RecyclerView recyclerView;
    UserAdapter userAdapter;

    /**
     * Called when the activity is first created.
     * Sets up the leaderboard with Firebase data and fallback offline data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down then this Bundle contains the data it most
     *                          recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        fireBaseController = new FirebaseController();
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
            Intent intent = new Intent(ActivityLeaderBoard.this, MainActivity.class);
            startActivity(intent);
        }


        return true;
    }

    /**
     * Callback method called when a single user is retrieved from Firebase.
     * Part of the FireBaseListener interface implementation.
     *
     * @param u The user object retrieved from Firebase
     */
    @Override
    public void onCallbackUser(User u) {

    }

    /**
     * Callback method called when the list of users is retrieved from Firebase.
     * Updates the RecyclerView adapter with the new user data.
     * Part of the FireBaseListener interface implementation.
     *
     * @param users ArrayList of User objects retrieved from Firebase
     */
    @Override
    public void onCallbackUsers(ArrayList<User> users) {
        userAdapter = new UserAdapter(users);
        recyclerView.setAdapter(userAdapter);
    }

    /**
     * Callback method called after login or signup operations.
     * Part of the FireBaseListener interface implementation.
     * Currently empty as no specific action is needed for this activity.
     */
    @Override
    public void onCallbackFromLoginOrSignup(){}
}