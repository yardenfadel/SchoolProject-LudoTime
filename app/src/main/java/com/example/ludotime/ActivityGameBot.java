package com.example.ludotime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class ActivityGameBot extends AppCompatActivity {

    BoardCanvas board;
    private ImageView[] turnIndicators = new ImageView[4];
    private TextView[] diceValues = new TextView[4];
    private Button[] rollButtons = new Button[4];
    private int currentPlayerTurn = 0;
    private Random random = new Random();
    private boolean isRolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_bot);
        board = new BoardCanvas(this);

        FrameLayout frameLayout = findViewById(R.id.board_frame);
        frameLayout.addView(board);

        // Initialize player UI elements
        initializePlayerViews();

        // Set first player as active
        setActivePlayer(0);

        // Initialize dice appearance
        updateDiceAppearance();
    }

    private void initializePlayerViews() {
        // Find turn indicators
        turnIndicators[0] = findViewById(R.id.player1_turn_indicator);
        turnIndicators[1] = findViewById(R.id.player2_turn_indicator);
        turnIndicators[2] = findViewById(R.id.player3_turn_indicator);
        turnIndicators[3] = findViewById(R.id.player4_turn_indicator);

        // Find dice value displays
        diceValues[0] = findViewById(R.id.player1_dice_value);
        diceValues[1] = findViewById(R.id.player2_dice_value);
        diceValues[2] = findViewById(R.id.player3_dice_value);
        diceValues[3] = findViewById(R.id.player4_dice_value);

        // Find and set up roll buttons
        rollButtons[0] = findViewById(R.id.player1_roll_button);
        rollButtons[1] = findViewById(R.id.player2_roll_button);
        rollButtons[2] = findViewById(R.id.player3_roll_button);
        rollButtons[3] = findViewById(R.id.player4_roll_button);

        // Set up click listeners for roll buttons
        for (int i = 0; i < 4; i++) {
            final int playerIndex = i;
            rollButtons[i].setOnClickListener(v -> rollDice(playerIndex));

            // Initially disable all buttons except current player
            rollButtons[i].setEnabled(i == currentPlayerTurn);
        }
    }

    private void rollDice(int playerIndex) {
        // Only allow current player to roll and prevent multiple rolls in progress
        if (playerIndex != currentPlayerTurn || isRolling) return;

        // Set rolling state to true
        isRolling = true;

        // Disable roll button during animation
        rollButtons[playerIndex].setEnabled(false);

        // Create animation handler
        final Handler handler = new Handler();
        final int animationDuration = 1500; // 1.5 seconds
        final int intervalBetweenFrames = 50; // 50ms between changes

        // Generate final dice value (1-6)
        final int finalDiceValue = random.nextInt(6) + 1;

        // Create blinking animation for dice value
        AlphaAnimation blinkAnimation = new AlphaAnimation(0.2f, 1.0f);
        blinkAnimation.setDuration(100);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(3);

        // Animation runnable
        Runnable animation = new Runnable() {
            int framesLeft = animationDuration / intervalBetweenFrames;
            int lastValue = 0; // To avoid showing the same number twice

            @Override
            public void run() {
                if (framesLeft > 0) {
                    // Generate a random value different from the last one
                    int tempValue;
                    do {
                        tempValue = random.nextInt(6) + 1;
                    } while (tempValue == lastValue);
                    lastValue = tempValue;

                    // Show random number during animation
                    diceValues[playerIndex].setText(String.valueOf(tempValue));

                    // Apply blink animation to dice value
                    diceValues[playerIndex].startAnimation(blinkAnimation);

                    // Slow down the animation gradually
                    long delay = intervalBetweenFrames;
                    if (framesLeft < 10) {
                        delay = intervalBetweenFrames + (10 - framesLeft) * 20;
                    }

                    framesLeft--;
                    handler.postDelayed(this, delay);
                } else {
                    // Animation complete, show final value
                    diceValues[playerIndex].setText(String.valueOf(finalDiceValue));
                    diceValues[playerIndex].clearAnimation();

                    // Add a highlight effect for the final value
                    AlphaAnimation finalAnimation = new AlphaAnimation(0.2f, 1.0f);
                    finalAnimation.setDuration(500);
                    diceValues[playerIndex].startAnimation(finalAnimation);

                    // TODO: Handle game logic based on dice roll

                    // Move to next player after short delay
                    handler.postDelayed(() -> {
                        nextPlayerTurn();
                        isRolling = false; // Reset rolling state
                    }, 500);
                }
            }
        };

        // Start animation
        handler.post(animation);
    }

    private void nextPlayerTurn() {
        // Disable current player's roll button
        rollButtons[currentPlayerTurn].setEnabled(false);

        // Move to next player
        currentPlayerTurn = (currentPlayerTurn + 1) % 4;

        // Update active player UI
        setActivePlayer(currentPlayerTurn);

        // Enable new current player's roll button
        rollButtons[currentPlayerTurn].setEnabled(true);
    }

    private void setActivePlayer(int playerIndex) {
        // Hide all turn indicators
        for (int i = 0; i < 4; i++) {
            turnIndicators[i].setVisibility(View.INVISIBLE);
        }

        // Show current player's turn indicator
        turnIndicators[playerIndex].setVisibility(View.VISIBLE);

        // Update dice appearance
        updateDiceAppearance();
    }

    private void updateDiceAppearance() {
        // Update dice appearances based on current player
        for (int i = 0; i < 4; i++) {
            if (i == currentPlayerTurn) {
                // Active player dice
                diceValues[i].setAlpha(1.0f);
                diceValues[i].setBackgroundResource(R.drawable.dice_background);
                rollButtons[i].setEnabled(true);
                rollButtons[i].setAlpha(1.0f);
            } else {
                // Inactive player dice
                diceValues[i].setAlpha(0.5f);
                diceValues[i].setBackgroundResource(R.drawable.dice_background_inactive);
                rollButtons[i].setEnabled(false);
                rollButtons[i].setAlpha(0.5f);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn){
            Intent intent = new Intent(ActivityGameBot.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }
}