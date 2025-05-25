package com.example.ludotime;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class FirebaseGameManager {
    private static final String TAG = "FirebaseGameManager";
    private static final String GAMES_REF = "games";

    private static FirebaseAuth mAuth;
    private static FirebaseDatabase database;
    private DatabaseReference gamesRef;
    private String currentUserId;
    private String currentGameId;
    private ValueEventListener gameListener;

    private GameUpdateListener updateListener;
    /**
     * Interface for notifying UI about game updates
     * Contains callbacks for various game events that the UI can implement
     */
    public interface GameUpdateListener {
        void onGameCreated(String gameId);
        void onGameJoined(MultiplayerGameLogic game);
        void onGameUpdated(MultiplayerGameLogic game);
        void onPlayerJoined(String playerId, String playerName);
        void onPlayerLeft(String playerId);
        void onGameError(String message);
        void onTurnChange(int playerTurn);
        void onDiceRolled(int playerIndex, int diceValue);
        void onPawnMoved(int playerIndex, int pawnIndex, int position);
        void onGameEnded(int[] winnerOrder);
    }

    /**
     * Constructor initializes Firebase connection and sets up the game manager
     *
     * @param userId User identifier for the current player
     * @param listener Callback interface for game events
     */
    public FirebaseGameManager(String userId, GameUpdateListener listener) {
        this.currentUserId = userId;
        this.updateListener = listener;
        this.database = FirebaseDatabase.getInstance();
        this.gamesRef = database.getReference(GAMES_REF);
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Creates a new multiplayer game in Firebase
     * Sets the current user as the host and initializes game settings
     *
     */
    public void createGame() {
        MultiplayerGameLogic newGame = new MultiplayerGameLogic(currentUserId, mAuth.getCurrentUser().getDisplayName(), 4, 0);
        currentGameId = newGame.getGameId();

        gamesRef.child(currentGameId).setValue(newGame)
                .addOnSuccessListener(aVoid -> {
                    if (updateListener != null) {
                        updateListener.onGameCreated(currentGameId);
                    }
                    attachGameListener(currentGameId);
                })
                .addOnFailureListener(e -> {
                    if (updateListener != null) {
                        updateListener.onGameError("Failed to create game: " + e.getMessage());
                    }
                });
    }

    /**
     * Joins an existing game with the specified game ID
     * Assigns player color and updates game state in Firebase
     *
     * @param gameId The ID of the game to join
     * @param displayName The display name of the current user
     */
    public void joinGame(String gameId, String displayName) {
        Log.d(TAG, "=== ATTEMPTING TO JOIN GAME ===");
        Log.d(TAG, "Game ID: " + gameId);
        Log.d(TAG, "Display Name: " + displayName);
        Log.d(TAG, "Current User ID: " + currentUserId);

        // First, let's check if the game exists
        gamesRef.child(gameId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                Log.d(TAG, "Game exists check - snapshot exists: " + snapshot.exists());
                if (snapshot.exists()) {
                    Log.d(TAG, "Game data: " + snapshot.getValue());

                    // Now try the transaction
                    performJoinTransaction(gameId, displayName);
                } else {
                    Log.e(TAG, "Game does not exist in database");
                    if (updateListener != null) {
                        updateListener.onGameError("Game code not found. Please check the code and try again.");
                    }
                }
            } else {
                Log.e(TAG, "Failed to check game existence: " + task.getException());
                if (updateListener != null) {
                    updateListener.onGameError("Failed to connect to database.");
                }
            }
        });
    }

    private void performJoinTransaction(String gameId, String displayName) {
        gamesRef.child(gameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Log.d(TAG, "=== TRANSACTION STARTED ===");

                Object rawValue = mutableData.getValue();
                Log.d(TAG, "Raw mutable data: " + rawValue);
                Log.d(TAG, "Raw data type: " + (rawValue != null ? rawValue.getClass().getName() : "null"));

                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);
                Log.d(TAG, "Parsed game object: " + (game != null ? "not null" : "null"));

                if (game == null) {
                    Log.e(TAG, "Game object is null after parsing");
                    return Transaction.success(mutableData);
                }

                // Log game state
                Log.d(TAG, "Game started: " + game.isGameStarted());
                Log.d(TAG, "Current players: " + game.getCurrentPlayersNumber());
                Log.d(TAG, "Max players: " + game.getMaxPlayers());
                Log.d(TAG, "Host user ID: " + game.getHostUserId());

                // Check if game is joinable
                if (game.isGameStarted()) {
                    Log.e(TAG, "ABORT: Game already started");
                    return Transaction.abort();
                }

                if (game.getCurrentPlayersNumber() >= game.getMaxPlayers()) {
                    Log.e(TAG, "ABORT: Game is full (" + game.getCurrentPlayersNumber() + "/" + game.getMaxPlayers() + ")");
                    return Transaction.abort();
                }

                // Check player arrays
                ArrayList<String> playerIDs = game.getPlayerID();
                ArrayList<String> playerNames = game.getPlayerName();

                Log.d(TAG, "Player IDs array: " + (playerIDs != null ? playerIDs.toString() : "null"));
                Log.d(TAG, "Player Names array: " + (playerNames != null ? playerNames.toString() : "null"));

                // Initialize arrays if null
                if (playerIDs == null) {
                    Log.d(TAG, "Initializing null playerIDs array");
                    playerIDs = new ArrayList<>();
                    game.setPlayerID(playerIDs);
                }
                if (playerNames == null) {
                    Log.d(TAG, "Initializing null playerNames array");
                    playerNames = new ArrayList<>();
                    game.setPlayerName(playerNames);
                }

                // Check if player is already in the game
                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        Log.e(TAG, "ABORT: Player already in game at position " + i);
                        return Transaction.abort();
                    }
                }

                // Determine player color
                int playerColor = -1;
                for (int i = 0; i < 4; i++) {
                    if (i >= playerIDs.size() || playerIDs.get(i) == null || playerIDs.get(i).isEmpty()) {
                        playerColor = i;
                        Log.d(TAG, "Found available slot at position: " + playerColor);
                        break;
                    }
                }

                if (playerColor == -1) {
                    Log.e(TAG, "ABORT: No available color slots");
                    return Transaction.abort();
                }

                // Add player to game
                while (playerIDs.size() <= playerColor) {
                    Log.d(TAG, "Expanding playerIDs array to size: " + (playerColor + 1));
                    playerIDs.add(null);
                }
                playerIDs.set(playerColor, currentUserId);
                Log.d(TAG, "Set player ID at position " + playerColor + ": " + currentUserId);

                while (playerNames.size() <= playerColor) {
                    Log.d(TAG, "Expanding playerNames array to size: " + (playerColor + 1));
                    playerNames.add("Unknown");
                }
                playerNames.set(playerColor, displayName);
                Log.d(TAG, "Set player name at position " + playerColor + ": " + displayName);

                // Initialize isReady array if needed
                ArrayList<Boolean> isReady = game.getIsReady();
                if (isReady == null) {
                    Log.d(TAG, "Initializing null isReady array");
                    isReady = new ArrayList<>();
                    game.setIsReady(isReady);
                }
                while (isReady.size() <= playerColor) {
                    Log.d(TAG, "Expanding isReady array to size: " + (playerColor + 1));
                    isReady.add(false);
                }

                // Update player count
                int newPlayerCount = game.getCurrentPlayersNumber() + 1;
                game.setCurrentPlayersNumber(newPlayerCount);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                Log.d(TAG, "Updated player count to: " + newPlayerCount);
                Log.d(TAG, "Final playerIDs: " + playerIDs.toString());
                Log.d(TAG, "Final playerNames: " + playerNames.toString());
                Log.d(TAG, "=== TRANSACTION SUCCESS ===");

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                Log.d(TAG, "=== TRANSACTION COMPLETE ===");
                Log.d(TAG, "Database error: " + (databaseError != null ? databaseError.getMessage() : "none"));
                Log.d(TAG, "Committed: " + committed);

                if (databaseError != null) {
                    Log.e(TAG, "Database error details: " + databaseError.getDetails());
                    Log.e(TAG, "Database error code: " + databaseError.getCode());
                    if (updateListener != null) {
                        updateListener.onGameError("Database error: " + databaseError.getMessage());
                    }
                    return;
                }

                if (committed) {
                    Log.d(TAG, "Join successful!");
                    currentGameId = gameId;
                    attachGameListener(gameId);
                    if (updateListener != null) {
                        MultiplayerGameLogic finalGame = dataSnapshot.getValue(MultiplayerGameLogic.class);
                        Log.d(TAG, "Final game state: " + (finalGame != null ? "valid" : "null"));
                        updateListener.onGameJoined(finalGame);
                    }
                } else {
                    Log.e(TAG, "Transaction was not committed - this means it was aborted");
                    if (updateListener != null) {
                        updateListener.onGameError("Failed to join game. It may be full, already started, or you may already be in it.");
                    }
                }
            }
        });
    }

    /**
     * Updates the ready status of the current player
     * Used to indicate when a player is ready to start the game
     *
     * @param ready Boolean indicating if player is ready
     */
    public void setPlayerReady(boolean ready) {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null) {
                    return Transaction.abort();
                }

                // Find player index
                ArrayList<String> playerIDs = game.getPlayerID();
                int playerIndex = -1;

                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        playerIndex = i;
                        break;
                    }
                }

                if (playerIndex == -1) {
                    return Transaction.abort(); // Player not found
                }

                ArrayList<Boolean> isReady = game.getIsReady();
                while (isReady.size() <= playerIndex) {
                    isReady.add(false);
                }
                isReady.set(playerIndex, ready);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to update ready status.");
                }
            }
        });
    }

    /**
     * Starts the game if all conditions are met
     * Only the host can start the game and exactly 4 players must be ready
     */
    public void startGame() {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null || !game.getHostUserId().equals(currentUserId)) {
                    return Transaction.abort();
                }

                ArrayList<Boolean> isReady = game.getIsReady();
                ArrayList<String> playerIDs = game.getPlayerID();

                // Count active players (non-null player IDs)
                int activePlayerCount = 0;
                int readyPlayerCount = 0;

                for (int i = 0; i < playerIDs.size(); i++) {
                    String playerId = playerIDs.get(i);
                    if (playerId != null && !playerId.isEmpty()) {
                        activePlayerCount++;
                        // Check if this active player is ready
                        if (i < isReady.size() && isReady.get(i)) {
                            readyPlayerCount++;
                        }
                    }
                }

                // Game requires exactly 4 players and all must be ready
                if (activePlayerCount != 4) {
                    Log.d(TAG, "Cannot start game: Need exactly 4 players, but have " + activePlayerCount);
                    return Transaction.abort();
                }

                if (readyPlayerCount != 4) {
                    Log.d(TAG, "Cannot start game: Need all 4 players ready, but only " + readyPlayerCount + " are ready");
                    return Transaction.abort();
                }

                // Initialize game logic if not already set
                if (game.getGameLogic() == null) {
                    game.setGameLogic(new GameLogic());
                }

                // Initialize game state
                game.setGameStarted(true);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to start game. Make sure exactly 4 players have joined and all are ready.");
                }
            }
        });
    }

    /**
     * Rolls the dice for the current player's turn
     * Uses Firebase transaction to ensure data consistency
     */
    public void rollDice() {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null || !game.isGameStarted() || game.getGameLogic() == null) {
                    return Transaction.abort();
                }

                GameLogic gameLogic = game.getGameLogic();

                // Check if it's this player's turn
                int playerIndex = -1;
                ArrayList<String> playerIDs = game.getPlayerID();

                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        playerIndex = i;
                        break;
                    }
                }

                if (playerIndex == -1 || gameLogic.getCurrentPlayerTurn() != playerIndex) {
                    return Transaction.abort(); // Not this player's turn
                }

                if (gameLogic.isDiceRolled()) {
                    return Transaction.abort(); // Already rolled dice this turn
                }

                // Roll the dice
                Random random = new Random();
                int diceValue = random.nextInt(6) + 1;
                gameLogic.setDiceRoll(diceValue);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to roll dice. It may not be your turn.");
                } else if (committed && updateListener != null) {
                    MultiplayerGameLogic game = dataSnapshot.getValue(MultiplayerGameLogic.class);
                    updateListener.onDiceRolled(game.getGameLogic().getCurrentPlayerTurn(),
                            game.getGameLogic().getLastDiceRoll());
                }
            }
        });
    }

    /**
     * Selects a pawn to move after dice roll
     * @param pawnIndex Index of the pawn to move (0-3)
     */
    public void selectPawn(int pawnIndex) {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null || !game.isGameStarted() || game.getGameLogic() == null) {
                    return Transaction.abort();
                }

                GameLogic gameLogic = game.getGameLogic();

                // Check if it's this player's turn
                int playerIndex = -1;
                ArrayList<String> playerIDs = game.getPlayerID();

                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        playerIndex = i;
                        break;
                    }
                }

                if (playerIndex == -1 || gameLogic.getCurrentPlayerTurn() != playerIndex) {
                    return Transaction.abort(); // Not this player's turn
                }

                if (!gameLogic.isDiceRolled() || gameLogic.isMoveMade()) {
                    return Transaction.abort(); // Dice not rolled or move already made
                }

                if (gameLogic.isWaitingForPawnSelection()) {
                    gameLogic.setPawnSelection(pawnIndex);
                    game.setLastUpdateTimestamp(System.currentTimeMillis());

                    mutableData.setValue(game);
                    return Transaction.success(mutableData);
                }

                return Transaction.abort(); // Not waiting for pawn selection
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to select pawn.");
                } else if (committed && updateListener != null) {
                    MultiplayerGameLogic game = dataSnapshot.getValue(MultiplayerGameLogic.class);
                    updateListener.onGameUpdated(game);
                }
            }
        });
    }

    /**
     * Plays a round of the game
     * First step of moving a pawn (before selection)
     */
    public void playRound() {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null || !game.isGameStarted() || game.getGameLogic() == null) {
                    return Transaction.abort();
                }

                GameLogic gameLogic = game.getGameLogic();

                // Check if it's this player's turn
                int playerIndex = -1;
                ArrayList<String> playerIDs = game.getPlayerID();

                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        playerIndex = i;
                        break;
                    }
                }

                if (playerIndex == -1 || gameLogic.getCurrentPlayerTurn() != playerIndex) {
                    return Transaction.abort(); // Not this player's turn
                }

                if (!gameLogic.isDiceRolled() || gameLogic.isMoveMade()) {
                    return Transaction.abort(); // Dice not rolled or move already made
                }

                boolean roundCompleted = gameLogic.playRound();
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to play round.");
                } else if (committed && updateListener != null) {
                    MultiplayerGameLogic game = dataSnapshot.getValue(MultiplayerGameLogic.class);
                    GameLogic gameLogic = game.getGameLogic();

                    // Check if waiting for pawn selection
                    if (gameLogic.isWaitingForPawnSelection()) {
                        // UI needs to handle pawn selection
                    } else {
                        // Turn changed
                        updateListener.onTurnChange(gameLogic.getCurrentPlayerTurn());
                    }

                    // Check for winner
                    int winner = gameLogic.getWinner();
                    if (winner != -1) {
                        if (gameLogic.isGameOver()) {
                            updateListener.onGameEnded(gameLogic.getWinnerOrder().stream().mapToInt(Integer::intValue).toArray());
                        }
                    }
                }
            }
        });
    }

    /**
     * Removes player from the current game
     * Deletes the game if the host leaves, otherwise just removes the player
     */
    public void leaveGame() {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameLogic game = mutableData.getValue(MultiplayerGameLogic.class);

                if (game == null) {
                    return Transaction.success(mutableData);
                }

                // If host is leaving, delete the game
                if (game.getHostUserId().equals(currentUserId)) {
                    mutableData.setValue(null);
                    return Transaction.success(mutableData);
                }

                // Otherwise, remove the player
                ArrayList<String> playerIDs = game.getPlayerID();
                ArrayList<String> playerNames = game.getPlayerName();
                ArrayList<Boolean> isReady = game.getIsReady();

                for (int i = 0; i < playerIDs.size(); i++) {
                    if (currentUserId.equals(playerIDs.get(i))) {
                        playerIDs.set(i, null);
                        if (i < playerNames.size()) {
                            playerNames.set(i, "Unknown");
                        }
                        if (i < isReady.size()) {
                            isReady.set(i, false);
                        }
                        game.setCurrentPlayersNumber(game.getCurrentPlayersNumber() - 1);
                        break;
                    }
                }

                game.setLastUpdateTimestamp(System.currentTimeMillis());
                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                detachGameListener();
                currentGameId = null;
            }
        });
    }

    /**
     * Sets up a listener for real-time game updates from Firebase
     * Notifies the UI of any changes to the game state
     *
     * @param gameId ID of the game to listen for updates
     */
    private void attachGameListener(String gameId) {
        detachGameListener();

        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MultiplayerGameLogic game = dataSnapshot.getValue(MultiplayerGameLogic.class);

                if (game == null) {
                    // Game was deleted
                    if (updateListener != null) {
                        updateListener.onGameError("Game no longer exists.");
                    }
                    detachGameListener();
                    currentGameId = null;
                    return;
                }

                // Check for player joined
                checkForPlayerChanges(game);

                if (updateListener != null) {
                    updateListener.onGameUpdated(game);

                    // If game is in progress, update turn information
                    if (game.isGameStarted() && game.getGameLogic() != null) {
                        GameLogic gameLogic = game.getGameLogic();
                        updateListener.onTurnChange(gameLogic.getCurrentPlayerTurn());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (updateListener != null) {
                    updateListener.onGameError("Database error: " + databaseError.getMessage());
                }
            }
        };

        gamesRef.child(gameId).addValueEventListener(gameListener);
    }

    // Keep track of known players to detect joins/leaves
    private ArrayList<String> knownPlayers = new ArrayList<>();

    /**
     * Check for player joins and leaves by comparing current state to previous
     * @param game Current game state
     */
    private void checkForPlayerChanges(MultiplayerGameLogic game) {
        ArrayList<String> playerIDs = game.getPlayerID();
        ArrayList<String> playerNames = game.getPlayerName();

        // Ensure knownPlayers has same size
        while (knownPlayers.size() < playerIDs.size()) {
            knownPlayers.add(null);
        }

        // Check for new players
        for (int i = 0; i < playerIDs.size(); i++) {
            String playerId = (i < playerIDs.size()) ? playerIDs.get(i) : null;
            String knownPlayerId = (i < knownPlayers.size()) ? knownPlayers.get(i) : null;

            if (playerId != null && !playerId.equals(knownPlayerId)) {
                if (updateListener != null) {
                    String playerName = (i < playerNames.size()) ? playerNames.get(i) : "Unknown";
                    updateListener.onPlayerJoined(playerId, playerName);
                }
            } else if (playerId == null && knownPlayerId != null) {
                if (updateListener != null) {
                    updateListener.onPlayerLeft(knownPlayerId);
                }
            }

            if (i < knownPlayers.size()) {
                knownPlayers.set(i, playerId);
            } else {
                knownPlayers.add(playerId);
            }
        }
    }

    /**
     * Removes the Firebase listener to prevent memory leaks
     * Should be called when no longer monitoring the game
     */
    private void detachGameListener() {
        if (gameListener != null && currentGameId != null) {
            gamesRef.child(currentGameId).removeEventListener(gameListener);
            gameListener = null;
        }
    }

    /**
     * Performs cleanup operations before destroying the manager
     * Should be called in onDestroy() of the hosting activity/fragment
     */
    public void cleanup() {
        detachGameListener();
    }
}