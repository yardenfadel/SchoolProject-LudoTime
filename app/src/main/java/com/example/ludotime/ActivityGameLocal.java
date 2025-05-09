/**
 * ActivityGameLocal.java
 *
 * Game activity for playing Ludo against local.
 * Handles game board, players' turns, and dice rolling mechanics.
 */
package com.example.ludotime;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ludotime.R;

import java.util.Random;

public class ActivityGameLocal extends AppCompatActivity {
    // ===== Game Elements =====
    BoardCanvas board;
    GameLogic gameLogic;
    private ImageView[] turnIndicators = new ImageView[4];
    private TextView[] diceValues = new TextView[4];
    private Button[] rollButtons = new Button[4];

    // ===== Game State =====
    private int currentPlayerTurn = 0;
    private Random random = new Random();
    private boolean isRolling = false;

    // ===== Scoreboard elements =====
    private boolean gameEnded = false;

    // ===== Vibration element =====
    private Vibrator vibrator;

    // Player color names for the toast message
    private final String[] playerColors = {"Red", "Green", "Yellow", "Blue"};

    /**
     * Initialize the game activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_local);

        // Initialize vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Set to true for quick testing, false for normal gameplay
        boolean testMode = true; // TOGGLE THIS FOR TESTING

        // Initialize game board with test mode
        board = new BoardCanvas(this, testMode);
        gameLogic = board.getLogic();
        FrameLayout frameLayout = findViewById(R.id.board_frame);
        frameLayout.addView(board);

        // Initialize player UI elements
        initializePlayerViews();

        // Set first player as active
        setActivePlayer(0);

        // Initialize dice appearance
        updateDiceAppearance();

        // Show a toast to indicate test mode is active (optional)
        if (testMode) {
            Toast.makeText(this, "Test Mode Active", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialize player UI components and set up event listeners
     */
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

    /**
     * Provides vibration feedback of different patterns
     *
     * @param pattern The vibration pattern to use (1 = dice roll, 2 = turn change)
     */
    private void vibrateDevice(int pattern) {
        if (vibrator != null && vibrator.hasVibrator()) {
            switch (pattern) {
                case 1: // Dice roll feedback - single strong vibration
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(100);
                    }
                    break;
                case 2: // Turn change feedback - double pulse vibration
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        long[] pattern2 = {0, 80, 80, 80};
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern2, -1));
                    } else {
                        long[] pattern2 = {0, 80, 80, 80};
                        vibrator.vibrate(pattern2, -1);
                    }
                    break;
            }
        }
    }

    /**
     * Handle dice rolling for the given player
     *
     * @param playerIndex Index of the player (0-3)
     */
    private void rollDice(int playerIndex) {
        // Only allow current player to roll and prevent multiple rolls in progress
        if (playerIndex != currentPlayerTurn || isRolling) return;

        // Set rolling state to true
        isRolling = true;

        // Disable roll button during animation
        rollButtons[playerIndex].setEnabled(false);

        // Create animation handler
        final Handler handler = new Handler();
        final int animationDuration = 200; // 1.5 seconds
        final int intervalBetweenFrames = 50; // 50ms between changes

        // Generate final dice value (1-6)
        //final int finalDiceValue = random.nextInt(6) + 1;
        final int finalDiceValue = random.nextInt(2) + 5;

        // Create blinking animation for dice value
        AlphaAnimation blinkAnimation = new AlphaAnimation(0.2f, 1.0f);
        blinkAnimation.setDuration(100);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(3);

        // Play dice roll sound
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.diceroll);
        mediaPlayer.start();

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
                    finalAnimation.setDuration(50);
                    diceValues[playerIndex].startAnimation(finalAnimation);

                    // Vibrate to indicate dice roll completion
                    vibrateDevice(1);

                    // Make sure the game logic knows whose turn it is
                    gameLogic.setCurrentPlayerTurn(currentPlayerTurn);

                    // Handle game logic based on dice roll value
                    gameLogic.setDiceRoll(finalDiceValue);

                    // Call playRound, which returns false if waiting for selection
                    boolean roundComplete = gameLogic.playRound();

                    // Update the board display
                    board.invalidate();

                    // Check if any player has won the game
                    int winnerIndex = gameLogic.getWinner();
                    if (winnerIndex != -1) {
                        // A player has won, show toast message
                        String winnerMessage = playerColors[winnerIndex] + " player finished in position " +
                                gameLogic.getPlayerPosition(winnerIndex) + "!";
                        Toast.makeText(ActivityGameLocal.this, winnerMessage, Toast.LENGTH_LONG).show();

                        // Debug toast to show winners count
                        Toast.makeText(ActivityGameLocal.this,
                                "Winners count: " + gameLogic.getWinnersDebugInfo(),
                                Toast.LENGTH_SHORT).show();

                        // Check if this is the 3rd winner (game over)
                        if (gameLogic.isGameOver()) {
                            // Game is over, show scoreboard
                            gameEnded = true;
                            showScoreboard();

                            // Disable all roll buttons as game is over
                            for (Button button : rollButtons) {
                                button.setEnabled(false);
                            }

                            // Reset rolling state
                            isRolling = false;
                            return;
                        }

                    }

                    // Only proceed to next turn if round is complete (no selection needed)
                    if (roundComplete) {
                        // Move to next player after short delay
                        handler.postDelayed(() -> {
                            nextPlayerTurn();
                            isRolling = false; // Reset rolling state
                        }, 500);
                    } else {
                        // Let user select a pawn
                        isRolling = false; // Reset rolling state to allow board interaction

                        // Set up a listener to wait for pawn selection to complete
                        final Handler selectionHandler = new Handler();
                        Runnable selectionCheck = new Runnable() {
                            @Override
                            public void run() {
                                if (!gameLogic.isWaitingForPawnSelection()) {
                                    // Selection is complete, check if this move resulted in a win
                                    int winnerIndex = gameLogic.getWinner();
                                    if (winnerIndex != -1) {
                                        // A player has won, show toast message
                                        String winnerMessage = playerColors[winnerIndex] + " player finished in position " +
                                                gameLogic.getPlayerPosition(winnerIndex) + "!";
                                        Toast.makeText(ActivityGameLocal.this, winnerMessage, Toast.LENGTH_LONG).show();

                                        // Check if this is the 3rd winner (game over)
                                        if (gameLogic.isGameOver()) {
                                            // Game is over, show scoreboard
                                            gameEnded = true;
                                            showScoreboard();

                                            // Disable all roll buttons as game is over
                                            for (Button button : rollButtons) {
                                                button.setEnabled(false);
                                            }
                                        } else {
                                            // Continue to next player
                                            nextPlayerTurn();
                                        }
                                    } else {
                                        // No winner, move to next player
                                        nextPlayerTurn();
                                    }
                                } else {
                                    // Still waiting, check again in 500ms
                                    selectionHandler.postDelayed(this, 500);
                                }
                            }
                        };
                        selectionHandler.postDelayed(selectionCheck, 500);
                    }
                }
            }
        };

        // Start animation
        handler.post(animation);
    }

    /**
     * Switch to the next player's turn
     */
    private void nextPlayerTurn() {
        // Disable current player's roll button
        rollButtons[currentPlayerTurn].setEnabled(false);

        // Find the next player who hasn't won yet
        int nextPlayer = (currentPlayerTurn + 1) % 4;
        int checkedPlayers = 0;

        // Keep searching until we find a player who hasn't won yet or we've checked all players
        while (gameLogic.hasPlayerWon(nextPlayer) && checkedPlayers < 4) {
            nextPlayer = (nextPlayer + 1) % 4;
            checkedPlayers++;
        }

        // If all players have won or game is over, end the game
        if (checkedPlayers >= 4 || gameLogic.isGameOver()) {
            gameEnded = true;
            showScoreboard();
            return;
        }

        // Update current player
        currentPlayerTurn = nextPlayer;

        // Vibrate to indicate turn change
        vibrateDevice(2);

        // Update active player UI
        setActivePlayer(currentPlayerTurn);

        // Enable new current player's roll button
        rollButtons[currentPlayerTurn].setEnabled(true);
    }

    /**
     * Update UI to indicate the active player
     *
     * @param playerIndex Index of the active player (0-3)
     */
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

    /**
     * Update the appearance of dice based on current player
     */
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

    /**
     * Initialize the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu, menu);
        return true;
    }

    /**
     * Handle menu item selections
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn) {
            Intent intent = new Intent(ActivityGameLocal.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }


    /**
     * Display the final scoreboard showing player rankings
     */
    private void showScoreboard() {
        // Create a dialog to show the scoreboard
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Game Over - Final Standings");

        // Build scoreboard text
        StringBuilder scoreText = new StringBuilder();
        int[] winnerOrder = gameLogic.getWinnerOrder();
        int winnersCount = gameLogic.getWinnersCount(); // Get the count from gameLogic

        // Add the winners in order
        for (int i = 0; i < winnersCount; i++) {
            if (winnerOrder[i] != -1) {
                scoreText.append(i + 1).append(getOrdinalSuffix(i + 1)).append(" Place: ")
                        .append(playerColors[winnerOrder[i]])
                        .append("\n");
            }
        }

        // Add any remaining players as "Not Finished"
        for (int player = 0; player < 4; player++) {
            boolean found = false;
            for (int i = 0; i < winnersCount; i++) {
                if (winnerOrder[i] == player) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                scoreText.append("Not Finished: ").append(playerColors[player]).append("\n");
            }
        }

        builder.setMessage(scoreText.toString());
        builder.setPositiveButton("Return to Main Menu", (dialog, which) -> {
            Intent intent = new Intent(ActivityGameLocal.this, MainActivity.class);
            startActivity(intent);
        });

        builder.setCancelable(false); // Prevent dismissing the dialog
        builder.show();
    }

    /**
     * Get the ordinal suffix for a number (1st, 2nd, 3rd, etc.)
     *
     * @param n The number to get a suffix for
     * @return The ordinal suffix
     */
    private String getOrdinalSuffix(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}