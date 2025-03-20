/**
 * MainActivity.java
 *
 * Main entry point for the LudoTime application.
 * Handles user authentication (login/signup) and navigation to game modes.
 */
package com.example.ludotime;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FireBaseListener {
    // ===== UI Elements =====
    // Dialogs
    Dialog dialogLogin;
    Dialog dialogSignUp;

    // Buttons
    Button bLogin;
    Button bSignUp;

    // Text Views
    TextView tvWelcome;

    // ===== Sign-up dialog elements =====
    EditText etSignUpEmail;
    EditText etSignUpName;
    EditText etSignUpPassword;
    EditText etSignUpVerifyPassword;
    TextView tvSignUpInfo;

    // ===== Login dialog elements =====
    EditText etLogInEmail;
    EditText etLoginPassword;

    // ===== Firebase =====
    FireBaseController fireBaseController;

    /**
     * Initialize the activity, set up UI elements and event listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        tvWelcome = findViewById(R.id.tvWelcome);
        fireBaseController = new FireBaseController();

        // Set up game mode buttons
        setupGameModeButtons();

        // Start background music service
        startService(new Intent(this, BGMusicService.class));
    }

    /**
     * Set up navigation buttons for different game modes
     */
    private void setupGameModeButtons() {
        // Online mode button
        Button bAgainstOnline = findViewById(R.id.homePage_onlineMode);
        bAgainstOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityGameBot.class);
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "Online Mode", Toast.LENGTH_SHORT).show();
            }
        });

        // Bot mode button
        Button bAgainstBot = findViewById(R.id.homePage_botMode);
        bAgainstBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityGameBot.class);
                startActivity(intent);
            }
        });

        // Local mode button
        Button bAgainstLocal = findViewById(R.id.homePage_localMode);
        bAgainstLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityGameBot.class);
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "Local Mode", Toast.LENGTH_SHORT).show();
            }
        });

        // About button
        Button bAbout = findViewById(R.id.homePage_about);
        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityAbout.class);
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the activity is started
     * Check if user is connected to Firebase and update UI accordingly
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (fireBaseController.isConnected()) {
            fireBaseController.readUser(this);
        }
    }

    /**
     * Prepare options menu before it's displayed
     * Show/hide menu items based on user login status
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        System.out.println("on prepare options menu");

        MenuItem menuLogin = menu.findItem(R.id.menuLogin);
        MenuItem menuSignUp = menu.findItem(R.id.menuSignUp);
        MenuItem menuLogOut = menu.findItem(R.id.menuLogOut);

        // Show/hide menu items based on login status
        if (fireBaseController.isConnected()) {
            menuLogin.setVisible(false);
            menuSignUp.setVisible(false);
            menuLogOut.setVisible(true);
        } else {
            menuLogin.setVisible(true);
            menuSignUp.setVisible(true);
            menuLogOut.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Initialize the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Clean up resources when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        // Stop the background music service
        Intent intent = new Intent(this, BGMusicService.class);
        intent.setAction(BGMusicService.ACTION_STOP_MUSIC);
        startService(intent);

        super.onDestroy();
    }

    /**
     * Handle menu item selections
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Login menu item
        if (item.getItemId() == R.id.menuLogin) {
            //Toast.makeText(this, "Login", Toast.LENGTH_SHORT).show();
            showLoginDialog();
        }

        // Sign up menu item
        if (item.getItemId() == R.id.menuSignUp) {
            //Toast.makeText(this, "Sign Up", Toast.LENGTH_SHORT).show();
            showSignUpDialog();
        }

        // Log out menu item
        if (item.getItemId() == R.id.menuLogOut) {
            //Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();
            FireBaseController.logOut();
            supportInvalidateOptionsMenu();
            tvWelcome.setText("Log in or sign up to play");
        }

        // Settings menu item
        if (item.getItemId() == R.id.menuSettings) {
            //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ActivitySettings.class);
            startActivity(intent);
        }

        // Leaderboard menu item
        if (item.getItemId() == R.id.menuLeaderBoard) {
            //Toast.makeText(this, "LeaderBoard", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ActivityLeaderBoard.class);
            startActivity(intent);
        }

        return true;
    }

    /**
     * Display the login dialog
     */
    private void showLoginDialog() {
        dialogLogin = new Dialog(this);
        dialogLogin.setContentView(R.layout.login);

        etLogInEmail = dialogLogin.findViewById(R.id.etLoginEmail);
        etLoginPassword = dialogLogin.findViewById(R.id.etLoginPassword);

        bLogin = dialogLogin.findViewById(R.id.bLogin);
        bLogin.setOnClickListener(this);
        dialogLogin.setCancelable(true);
        dialogLogin.show();
    }

    /**
     * Display the sign up dialog
     */
    private void showSignUpDialog() {
        dialogSignUp = new Dialog(this);
        dialogSignUp.setContentView(R.layout.signup);

        etSignUpEmail = dialogSignUp.findViewById(R.id.etSignUpEmail);
        etSignUpName = dialogSignUp.findViewById(R.id.etSignUpName);
        etSignUpPassword = dialogSignUp.findViewById(R.id.etSignUpPassword);
        etSignUpVerifyPassword = dialogSignUp.findViewById(R.id.etSignUpVerifyPassword);
        tvSignUpInfo = dialogSignUp.findViewById(R.id.tvSignup);

        bSignUp = dialogSignUp.findViewById(R.id.bSignUp);
        bSignUp.setOnClickListener(this);
        dialogSignUp.setCancelable(true);
        dialogSignUp.show();
    }

    /**
     * Handle button clicks
     */
    @Override
    public void onClick(View v) {
        // Login button clicked
        if (v == bLogin) {
            fireBaseController.loginUser(etLogInEmail.getText().toString(), etLoginPassword.getText().toString(), this);
            //fireBaseController.readUser(this);
            //supportInvalidateOptionsMenu();
            dialogLogin.dismiss();
        }

        // Sign up button clicked
        if (v == bSignUp) {
            handleSignUp();
        }
    }

    /**
     * Process sign up form submission with validation
     */
    private void handleSignUp() {
        // Validate sign-up inputs
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Email validation
        String email = etSignUpEmail.getText().toString().trim();
        if (!email.contains("@")) {
            errorMessage.append("Email must contain '@'. ");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.append("Email is invalid. ");
            isValid = false;
        }

        // Username validation
        String username = etSignUpName.getText().toString().trim();
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            errorMessage.append("Username can only contain English letters and digits. ");
            isValid = false;
        }

        // Password validation
        String password = etSignUpPassword.getText().toString();
        String verifyPassword = etSignUpVerifyPassword.getText().toString();

        if (password.length() < 8) {
            errorMessage.append("Password must be at least 8 characters long. ");
            isValid = false;
        }

        if (!password.equals(verifyPassword)) {
            errorMessage.append("Passwords must match. ");
            isValid = false;
        }

        // Display validation results
        if (isValid) {
            tvSignUpInfo.setText("Sign-up successful!");
            tvWelcome.setText("Hello " + username);
            User u = new User(email, password, username, 0, "male_avatar");
            fireBaseController.createUser(u, this);
            dialogSignUp.dismiss();
        } else {
            tvSignUpInfo.setText(errorMessage.toString());
        }
    }

    // ===== FireBaseListener Interface Methods =====

    /**
     * Callback when user data is loaded
     */
    @Override
    public void onCallbackUser(User u) {
        tvWelcome.setText("Hello " + u.getName());
        System.out.println("name set 4");
    }

    /**
     * Callback when multiple users are loaded
     */
    @Override
    public void onCallbackUsers(ArrayList<User> users) {
        // Currently not used in MainActivity
    }

    /**
     * Callback after login or signup is completed
     */
    @Override
    public void onCallbackFromLoginOrSignup() {
        invalidateOptionsMenu();
        System.out.println("callback 3");
        fireBaseController.readUser(this);
    }
}