/**
 * ActivityGameMultiplayer.java
 *
 * Multiplayer game activity for playing Ludo with other players via Firebase.
 * Handles game board, players' turns, dice rolling mechanics, and real-time synchronization.
 */
package com.example.ludotime;

import android.content.Intent;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Random;

public class ActivityGameMultiplayer extends AppCompatActivity implements FirebaseGameManager.GameUpdateListener {

    // ===== Game Elements =====
    private BoardCanvas board;
    private MultiplayerGameLogic multiplayerGame;
    private GameLogic gameLogic;
    private FirebaseGameManager gameManager;

    // ===== UI Elements =====
    private ImageView[] turnIndicators = new ImageView[4];
    private TextView[] diceValues = new TextView[4];
    private Button[] rollButtons = new Button[4];
    private TextView[] playerNameLabels = new TextView[4];

    // ===== Game State =====
    private int currentPlayerTurn = 0;
    private int myPlayerIndex = -1;
    private Random random = new Random();
    private boolean isRolling = false;
    private boolean gameEnded = false;
    private String currentUserId;

    // Player color names for messages
    private final String[] playerColors = {"Red", "Green", "Yellow", "Blue"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_local);

        // Get current user ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase game manager
        gameManager = new FirebaseGameManager(currentUserId, this);

        // Initialize UI elements
        initializePlayerViews();

        // Get game ID from intent and join the game
        String gameId = getIntent().getStringExtra("GAME_ID");
        String displayName = getIntent().getStringExtra("PLAYER_NAME");
        boolean isHost = getIntent().getBooleanExtra("IS_HOST", false);

        if (gameId != null) {
            if (isHost) {
                // For host, the game should already be created
                Toast.makeText(this, "Game created: " + gameId, Toast.LENGTH_SHORT).show();
            } else {
                // Join existing game
                gameManager.joinGame(gameId, displayName);
            }
        } else {
            Toast.makeText(this, "No game ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initially disable all buttons until game starts
        for (Button button : rollButtons) {
            button.setEnabled(false);
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

        // Find roll buttons
        rollButtons[0] = findViewById(R.id.player1_roll_button);
        rollButtons[1] = findViewById(R.id.player2_roll_button);
        rollButtons[2] = findViewById(R.id.player3_roll_button);
        rollButtons[3] = findViewById(R.id.player4_roll_button);

        // Find player name labels
        playerNameLabels[0] = findViewById(R.id.player1_name);
        playerNameLabels[1] = findViewById(R.id.player2_name);
        playerNameLabels[2] = findViewById(R.id.player3_name);
        playerNameLabels[3] = findViewById(R.id.player4_name);

        // Set up click listeners for roll buttons
        for (int i = 0; i < 4; i++) {
            final int playerIndex = i;
            rollButtons[i].setOnClickListener(v -> rollDice(playerIndex));
            rollButtons[i].setEnabled(false);
        }
    }

    /**
     * Initialize the game board once we have the game data
     */
    private void initializeGameBoard() {
        if (multiplayerGame != null && multiplayerGame.getGameLogic() != null) {
            gameLogic = multiplayerGame.getGameLogic();

            // Initialize game board
            board = new BoardCanvas(this, false); // Not test mode for multiplayer
            board.setLogic(gameLogic);

            FrameLayout frameLayout = findViewById(R.id.board_frame);
            frameLayout.removeAllViews();
            frameLayout.addView(board);

            // Update UI
            updateGameUI();
        }
    }

    /**
     * Handle dice rolling for the given player
     */
    private void rollDice(int playerIndex) {
        // Only allow current player to roll and prevent multiple rolls
        if (playerIndex != myPlayerIndex || playerIndex != currentPlayerTurn || isRolling) {
            return;
        }

        // Set rolling state
        isRolling = true;
        rollButtons[playerIndex].setEnabled(false);

        // Create dice rolling animation
        animateDiceRoll(playerIndex, () -> {
            // After animation, send roll request to Firebase
            gameManager.rollDice();
        });
    }

    /**
     * Animate dice rolling effect
     */
    private void animateDiceRoll(int playerIndex, Runnable onComplete) {
        final Handler handler = new Handler();
        final int animationDuration = 1500;
        final int intervalBetweenFrames = 100;

        // Create blinking animation
        AlphaAnimation blinkAnimation = new AlphaAnimation(0.2f, 1.0f);
        blinkAnimation.setDuration(100);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(3);

        // Play dice roll sound
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.diceroll);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Handle case where sound file doesn't exist
        }

        // Animation runnable
        Runnable animation = new Runnable() {
            int framesLeft = animationDuration / intervalBetweenFrames;
            int lastValue = 0;

            @Override
            public void run() {
                if (framesLeft > 0) {
                    // Generate random value different from last
                    int tempValue;
                    do {
                        tempValue = random.nextInt(6) + 1;
                    } while (tempValue == lastValue);
                    lastValue = tempValue;

                    // Show random number during animation
                    diceValues[playerIndex].setText(String.valueOf(tempValue));
                    diceValues[playerIndex].startAnimation(blinkAnimation);

                    framesLeft--;
                    handler.postDelayed(this, intervalBetweenFrames);
                } else {
                    // Animation complete
                    diceValues[playerIndex].clearAnimation();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };

        handler.post(animation);
    }

    /**
     * Update the game UI based on current state
     */
    private void updateGameUI() {
        if (multiplayerGame == null) return;

        // Update player names
        ArrayList<String> playerNames = multiplayerGame.getPlayerName();
        ArrayList<String> playerIDs = multiplayerGame.getPlayerID();

        for (int i = 0; i < 4; i++) {
            if (i < playerNames.size() && i < playerIDs.size() && playerIDs.get(i) != null) {
                playerNameLabels[i].setText(playerNames.get(i));
                playerNameLabels[i].setVisibility(View.VISIBLE);

                // Check if this is the current user
                if (currentUserId.equals(playerIDs.get(i))) {
                    myPlayerIndex = i;
                }
            } else {
                playerNameLabels[i].setText("Waiting...");
                playerNameLabels[i].setVisibility(View.VISIBLE);
            }
        }

        // Update turn indicators
        setActivePlayer(currentPlayerTurn);

        // Update dice appearance
        updateDiceAppearance();

        // Update board if it exists
        if (board != null) {
            board.invalidate();
        }
    }

    /**
     * Update UI to indicate the active player
     */
    private void setActivePlayer(int playerIndex) {
        currentPlayerTurn = playerIndex;

        // Hide all turn indicators
        for (int i = 0; i < 4; i++) {
            turnIndicators[i].setVisibility(View.INVISIBLE);
        }

        // Show current player's turn indicator
        if (playerIndex >= 0 && playerIndex < 4) {
            turnIndicators[playerIndex].setVisibility(View.VISIBLE);
        }

        updateDiceAppearance();
    }

    /**
     * Update dice appearance based on current player and game state
     */
    private void updateDiceAppearance() {
        for (int i = 0; i < 4; i++) {
            boolean isActivePlayer = (i == currentPlayerTurn);
            boolean isMyTurn = (isActivePlayer && i == myPlayerIndex);
            boolean canRoll = isMyTurn && !isRolling;

            if (isActivePlayer) {
                diceValues[i].setAlpha(1.0f);
                diceValues[i].setBackgroundResource(R.drawable.dice_background);
                rollButtons[i].setAlpha(1.0f);
            } else {
                diceValues[i].setAlpha(0.5f);
                diceValues[i].setBackgroundResource(R.drawable.dice_background_inactive);
                rollButtons[i].setAlpha(0.5f);
            }

            rollButtons[i].setEnabled(canRoll && multiplayerGame != null && multiplayerGame.isGameStarted());
        }
    }

    /**
     * Display the final scoreboard
     */
    private void showScoreboard() {
        if (gameLogic == null) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Game Over - Final Standings");

        StringBuilder scoreText = new StringBuilder();
        int[] winnerOrder = gameLogic.getWinnerOrder().stream().mapToInt(Integer::intValue).toArray();
        int winnersCount = gameLogic.getWinnersCount();

        // Add winners in order
        for (int i = 0; i < winnersCount; i++) {
            if (winnerOrder[i] != -1) {
                String playerName = "Unknown";
                if (multiplayerGame != null && multiplayerGame.getPlayerName() != null &&
                        winnerOrder[i] < multiplayerGame.getPlayerName().size()) {
                    playerName = multiplayerGame.getPlayerName().get(winnerOrder[i]);
                }

                scoreText.append(i + 1).append(getOrdinalSuffix(i + 1)).append(" Place: ")
                        .append(playerName).append(" (").append(playerColors[winnerOrder[i]]).append(")")
                        .append("\n");
            }
        }

        // Add remaining players
        for (int player = 0; player < 4; player++) {
            boolean found = false;
            for (int i = 0; i < winnersCount; i++) {
                if (winnerOrder[i] == player) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String playerName = "Unknown";
                if (multiplayerGame != null && multiplayerGame.getPlayerName() != null &&
                        player < multiplayerGame.getPlayerName().size()) {
                    playerName = multiplayerGame.getPlayerName().get(player);
                }
                scoreText.append("Not Finished: ").append(playerName)
                        .append(" (").append(playerColors[player]).append(")").append("\n");
            }
        }

        builder.setMessage(scoreText.toString());
        builder.setPositiveButton("Return to Main Menu", (dialog, which) -> {
            Intent intent = new Intent(ActivityGameMultiplayer.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }

    /**
     * Get ordinal suffix for numbers
     */
    private String getOrdinalSuffix(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    // ===== Firebase Game Manager Callbacks =====

    @Override
    public void onGameCreated(String gameId) {
        Toast.makeText(this, "Game created with ID: " + gameId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGameJoined(MultiplayerGameLogic game) {
        multiplayerGame = game;
        runOnUiThread(() -> {
            Toast.makeText(this, "Joined game successfully!", Toast.LENGTH_SHORT).show();
            updateGameUI();

            if (game.isGameStarted()) {
                initializeGameBoard();
            }
        });
    }

    @Override
    public void onGameUpdated(MultiplayerGameLogic game) {
        multiplayerGame = game;
        runOnUiThread(() -> {
            updateGameUI();

            if (game.isGameStarted() && board == null) {
                initializeGameBoard();
            }

            if (board != null) {
                board.invalidate();
            }
        });
    }

    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        runOnUiThread(() -> {
            Toast.makeText(this, playerName + " joined the game", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPlayerLeft(String playerId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "A player left the game", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onGameError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Game error: " + message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onTurnChange(int playerTurn) {
        runOnUiThread(() -> {
            setActivePlayer(playerTurn);
            isRolling = false; // Reset rolling state

            if (playerTurn == myPlayerIndex) {
                Toast.makeText(this, "Your turn!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDiceRolled(int playerIndex, int diceValue) {
        runOnUiThread(() -> {
            diceValues[playerIndex].setText(String.valueOf(diceValue));

            // Add highlight effect for the final value
            AlphaAnimation finalAnimation = new AlphaAnimation(0.2f, 1.0f);
            finalAnimation.setDuration(500);
            diceValues[playerIndex].startAnimation(finalAnimation);

            isRolling = false;

            // If it's this player's turn, initiate the move
            if (playerIndex == myPlayerIndex) {
                // Small delay then play round
                new Handler().postDelayed(() -> {
                    gameManager.playRound();
                }, 1000);
            }
        });
    }

    @Override
    public void onPawnMoved(int playerIndex, int pawnIndex, int position) {
        runOnUiThread(() -> {
            if (board != null) {
                board.invalidate();
            }
        });
    }

    @Override
    public void onGameEnded(int[] winnerOrder) {
        runOnUiThread(() -> {
            gameEnded = true;
            showScoreboard();

            // Disable all roll buttons
            for (Button button : rollButtons) {
                button.setEnabled(false);
            }
        });
    }

    // ===== Menu and Lifecycle =====

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_to_main_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn) {
            // Leave game before returning to main menu
            if (gameManager != null) {
                gameManager.leaveGame();
            }
            Intent intent = new Intent(ActivityGameMultiplayer.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.cleanup();
        }
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before leaving
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Leave Game?");
        builder.setMessage("Are you sure you want to leave the game?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            if (gameManager != null) {
                gameManager.leaveGame();
            }
            super.onBackPressed();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}