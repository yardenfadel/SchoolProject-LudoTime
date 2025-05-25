package com.example.ludotime;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.example.ludotime.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

/**
 * ActivityLobby.java
 *
 * Handles creating and joining online Ludo game lobbies.
 * Manages player color selection and lobby code generation/sharing.
 * Uses Firebase user's display name instead of asking for a name.
 */
public class ActivityLobby extends AppCompatActivity implements FirebaseGameManager.GameUpdateListener {

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseGameManager gameManager;

    // UI Components
    private CardView menuCard;
    private CardView lobbyCard;
    private CardView createGameDialog;
    private CardView joinGameDialog;
    private LinearLayout playersContainer;

    private Button createGameButton;
    private Button joinGameButton;
    private Button startGameButton;
    private Button shareCodeButton;
    private Button cancelCreateButton;
    private Button confirmCreateButton;
    private Button cancelJoinButton;
    private Button confirmJoinButton;
    private Button readyButton;

    private EditText lobbyCodeInput;
    private TextView lobbyCodeDisplay;
    private TextView waitingMessage;

    private ImageView colorRed;
    private ImageView colorGreen;
    private ImageView colorYellow;
    private ImageView colorBlue;

    // Variables
    private String generatedLobbyCode;
    private String playerName;
    private int selectedColor = 0; // Default: Red
    private boolean isHost = false;
    private boolean isReady = false;
    private boolean allPlayersReady = false;
    private MultiplayerGameLogic currentGame;

    /**
     * Initializes the activity, sets up the UI components and event listeners
     *
     * @param savedInstanceState Contains data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Get player name from Firebase user
        playerName = currentUser.getDisplayName();
        if (playerName == null || playerName.isEmpty()) {
            playerName = "Player_" + currentUser.getUid().substring(0, 5);
        }

        // Initialize Firebase Game Manager
        gameManager = new FirebaseGameManager(currentUser.getUid(), this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Finds and assigns all UI components to their respective fields
     */
    private void initializeViews() {
        // Cards
        menuCard = findViewById(R.id.menu_card);
        lobbyCard = findViewById(R.id.lobby_card);
        createGameDialog = findViewById(R.id.create_game_dialog);
        joinGameDialog = findViewById(R.id.join_game_dialog);
        playersContainer = findViewById(R.id.players_container);

        // Buttons
        createGameButton = findViewById(R.id.create_game_button);
        joinGameButton = findViewById(R.id.join_game_button);
        startGameButton = findViewById(R.id.start_game_button);
        shareCodeButton = findViewById(R.id.share_code_button);
        cancelCreateButton = findViewById(R.id.cancel_create_button);
        confirmCreateButton = findViewById(R.id.confirm_create_button);
        cancelJoinButton = findViewById(R.id.cancel_join_button);
        confirmJoinButton = findViewById(R.id.confirm_join_button);
        readyButton = findViewById(R.id.ready_button);

        // Edit texts
        lobbyCodeInput = findViewById(R.id.lobby_code_input);

        // Text views
        lobbyCodeDisplay = findViewById(R.id.lobby_code_display);
        waitingMessage = findViewById(R.id.waiting_message);

        // Color selection images
        colorRed = findViewById(R.id.color_red);
        colorGreen = findViewById(R.id.color_green);
        colorYellow = findViewById(R.id.color_yellow);
        colorBlue = findViewById(R.id.color_blue);

        // Initially hide start button (only host can see it)
        startGameButton.setVisibility(View.GONE);
    }

    /**
     * Sets up click listeners for all interactive UI elements
     */
    private void setupClickListeners() {
        // Main menu buttons
        createGameButton.setOnClickListener(v -> showCreateGameDialog());
        joinGameButton.setOnClickListener(v -> processJoinGame());

        // Color selection
        colorRed.setOnClickListener(v -> selectColor(R.id.color_red, 0));
        colorGreen.setOnClickListener(v -> selectColor(R.id.color_green, 1));
        colorYellow.setOnClickListener(v -> selectColor(R.id.color_yellow, 2));
        colorBlue.setOnClickListener(v -> selectColor(R.id.color_blue, 3));

        // Create game dialog buttons
        cancelCreateButton.setOnClickListener(v -> hideCreateGameDialog());
        confirmCreateButton.setOnClickListener(v -> createNewGame());

        // Join game dialog buttons
        cancelJoinButton.setOnClickListener(v -> hideJoinGameDialog());
        confirmJoinButton.setOnClickListener(v -> joinExistingGame());

        // Lobby buttons
        shareCodeButton.setOnClickListener(v -> shareLobbyCode());
        startGameButton.setOnClickListener(v -> startGame());
        readyButton.setOnClickListener(v -> toggleReady());
    }

    /**
     * Shows the create game dialog and hides the main menu
     */
    private void showCreateGameDialog() {
        menuCard.setVisibility(View.GONE);
        createGameDialog.setVisibility(View.VISIBLE);

        // Default to red selected
        selectColor(R.id.color_red, 0);
    }

    /**
     * Hides the create game dialog and shows the main menu
     */
    private void hideCreateGameDialog() {
        createGameDialog.setVisibility(View.GONE);
        menuCard.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the join game dialog and updates it with the entered lobby code
     */
    private void showJoinGameDialog() {
        menuCard.setVisibility(View.GONE);
        joinGameDialog.setVisibility(View.VISIBLE);

        TextView codeTextView = findViewById(R.id.lobby_join_code_display);
        if (codeTextView != null) {
            codeTextView.setText("Game Code: " + lobbyCodeInput.getText().toString().toUpperCase());
        }
    }

    /**
     * Hides the join game dialog and shows the main menu
     */
    private void hideJoinGameDialog() {
        joinGameDialog.setVisibility(View.GONE);
        menuCard.setVisibility(View.VISIBLE);
    }

    /**
     * Validates the entered lobby code and proceeds to join game dialog if valid
     */
    private void processJoinGame() {
        String code = lobbyCodeInput.getText().toString().trim().toUpperCase();
        if (code.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed to join game dialog
        showJoinGameDialog();
    }

    /**
     * Handles color selection by updating UI and storing selected color
     *
     * @param colorId The resource ID of the selected color
     * @param colorValue The color value (0=Red, 1=Green, 2=Yellow, 3=Blue)
     */
    private void selectColor(int colorId, int colorValue) {
        // Reset previous selection (remove highlight)
        colorRed.setAlpha(1.0f);
        colorGreen.setAlpha(1.0f);
        colorYellow.setAlpha(1.0f);
        colorBlue.setAlpha(1.0f);

        // Highlight selected color
        findViewById(colorId).setAlpha(0.7f);

        // Store selected color
        selectedColor = colorValue;
    }

    /**
     * Creates a new game lobby
     */
    private void createNewGame() {
        // Set host flag
        isHost = true;

        // Create game in Firebase
        gameManager.createGame();

        // Update UI
        createGameDialog.setVisibility(View.GONE);
        lobbyCard.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE); // Only host can see start button
        readyButton.setVisibility(View.GONE); // Host doesn't need ready button
    }

    /**
     * Joins an existing game
     */
    private void joinExistingGame() {
        String gameCode = lobbyCodeInput.getText().toString().trim().toUpperCase();

        // Set as non-host
        isHost = false;

        // Join game in Firebase with player name from Firebase Auth
        gameManager.joinGame(gameCode, playerName);

        // Update UI
        joinGameDialog.setVisibility(View.GONE);
        lobbyCard.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.GONE); // Only host can see start button
        readyButton.setVisibility(View.VISIBLE); // Non-host players need ready button
    }

    /**
     * Toggle ready status for non-host players
     */
    private void toggleReady() {
        isReady = !isReady;
        gameManager.setPlayerReady(isReady);

        if (isReady) {
            readyButton.setText("Not Ready");
            Toast.makeText(this, "You are ready!", Toast.LENGTH_SHORT).show();
        } else {
            readyButton.setText("Ready");
            Toast.makeText(this, "You are not ready", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shares the lobby code via clipboard and share intent
     */
    private void shareLobbyCode() {
        // Copy code to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Lobby Code", generatedLobbyCode);
        clipboard.setPrimaryClip(clip);

        // Share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my LudoTime game!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my LudoTime game with code: " + generatedLobbyCode);
        startActivity(Intent.createChooser(shareIntent, "Share code via"));
    }

    /**
     * Starts the game by launching the game activity with player data
     */
    private void startGame() {
        if (!isHost) {
            Toast.makeText(this, "Only the host can start the game", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentGame.getCurrentPlayersNumber() < 2) {
            Toast.makeText(this, "Need at least 2 players to start", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if all players are ready
        if (!allPlayersReady) {
            Toast.makeText(this, "All players must be ready to start", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tell Firebase to start the game
        gameManager.startGame();
    }

    /**
     * Launches the multiplayer game activity
     */
    private void launchGameActivity() {
        Intent intent = new Intent(ActivityLobby.this, ActivityGameMultiplayer.class);
        intent.putExtra("GAME_ID", currentGame.getGameId());
        intent.putExtra("PLAYER_NAME", playerName);
        intent.putExtra("IS_HOST", isHost);
        intent.putExtra("USER_ID", currentUser.getUid());
        startActivity(intent);
        finish(); // Close lobby activity
    }

    /**
     * Updates the player list UI based on current game state
     */
    private void updatePlayerList() {
        if (currentGame == null) return;

        // Clear existing player views
        playersContainer.removeAllViews();

        // Check if all players are ready
        allPlayersReady = true;

        // Add player entries
        ArrayList<String> playerIDs = currentGame.getPlayerID();
        ArrayList<String> playerNames = currentGame.getPlayerName();
        ArrayList<Boolean> isReady = currentGame.getIsReady();

        for (int i = 0; i < playerIDs.size(); i++) {
            if (playerIDs.get(i) == null) continue;

            // Create player entry view
            View playerView = getLayoutInflater().inflate(R.layout.player_list_item, playersContainer, false);

            // Set player info
            TextView nameText = playerView.findViewById(R.id.player_name);
            ImageView statusIcon = playerView.findViewById(R.id.player_status);
            ImageView colorIcon = playerView.findViewById(R.id.player_color);

            nameText.setText(playerNames.get(i));

            // Set color indicator based on player color (which is their index)
            int colorRes;
            switch (i) {
                case 0: // Red
                    colorRes = R.drawable.red_pawn;
                    break;
                case 1: // Green
                    colorRes = R.drawable.green_pawn;
                    break;
                case 2: // Yellow
                    colorRes = R.drawable.yellow_pawn;
                    break;
                case 3: // Blue
                    colorRes = R.drawable.blue_pawn;
                    break;
                default:
                    colorRes = R.drawable.red_pawn;
            }
            colorIcon.setImageResource(colorRes);

            // Set ready status icon
            statusIcon.setImageResource(isReady.get(i) ?
                    R.drawable.ic_check_circle : R.drawable.ic_pending);

            // Track if all non-host players are ready
            if (!playerIDs.get(i).equals(currentGame.getHostUserId()) && !isReady.get(i)) {
                allPlayersReady = false;
            }

            // Add to container
            playersContainer.addView(playerView);
        }

        // Update start button status if host
        if (isHost) {
            startGameButton.setEnabled(allPlayersReady && currentGame.getCurrentPlayersNumber() >= 2);
        }
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
     * Handles menu item selection
     *
     * @param item The selected menu item
     * @return True if the item selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuReturn) {
            // Leave game if in a lobby
            if (currentGame != null) {
                gameManager.leaveGame();
            }

            Intent intent = new Intent(ActivityLobby.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    /**
     * Handles back button press based on current visible UI state
     */
    @Override
    public void onBackPressed() {
        // Handle back button based on current visible card
        if (createGameDialog.getVisibility() == View.VISIBLE) {
            hideCreateGameDialog();
        } else if (joinGameDialog.getVisibility() == View.VISIBLE) {
            hideJoinGameDialog();
        } else if (lobbyCard.getVisibility() == View.VISIBLE) {
            // Leave game if in a lobby
            if (currentGame != null) {
                gameManager.leaveGame();
            }

            lobbyCard.setVisibility(View.GONE);
            menuCard.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Clean up resources when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.cleanup();
        }
    }

    // FirebaseGameManager.GameUpdateListener Implementation

    @Override
    public void onGameCreated(String gameId) {
        generatedLobbyCode = gameId;
        lobbyCodeDisplay.setText(gameId);

        // Host is always ready
        isReady = true;
        gameManager.setPlayerReady(isReady);

        Toast.makeText(this, "Game created with code: " + gameId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGameJoined(MultiplayerGameLogic game) {
        currentGame = game;
        generatedLobbyCode = game.getGameId();
        lobbyCodeDisplay.setText(game.getGameId());
        Toast.makeText(this, "Joined game successfully!", Toast.LENGTH_SHORT).show();
        updatePlayerList();
    }

    @Override
    public void onGameUpdated(MultiplayerGameLogic game) {
        currentGame = game;
        updatePlayerList();

        // If game started, launch game activity
        if (game.isGameStarted()) {
            launchGameActivity();
        }
    }

    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        Toast.makeText(this, playerName + " joined the game", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerLeft(String playerId) {
        if (currentGame != null) {
            ArrayList<String> playerIDs = currentGame.getPlayerID();
            ArrayList<String> playerNames = currentGame.getPlayerName();

            for (int i = 0; i < playerIDs.size(); i++) {
                if (playerId.equals(playerIDs.get(i))) {
                    Toast.makeText(this, playerNames.get(i) + " left the game", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    @Override
    public void onGameError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTurnChange(int playerTurn) {
        // Not used in lobby
    }

    @Override
    public void onDiceRolled(int playerIndex, int diceValue) {
        // Not used in lobby
    }

    @Override
    public void onPawnMoved(int playerIndex, int pawnIndex, int position) {
        // Not used in lobby
    }

    @Override
    public void onGameEnded(int[] winnerOrder) {
        // Not used in lobby
    }
}