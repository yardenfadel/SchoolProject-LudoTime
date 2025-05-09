/**
 * FirebaseGameManager.java
 *
 * Manages multiplayer Ludo game interactions with Firebase Realtime Database.
 * Handles game creation, joining, synchronization, turn management, and game state updates.
 */
package com.example.ludotime;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class FirebaseGameManagerOutdated {
    private static final String TAG = "FirebaseGameManager";
    private static final String GAMES_REF = "games";

    private FirebaseDatabase database;
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
        void onGameJoined(MultiplayerGameOutdated game);
        void onGameUpdated(MultiplayerGameOutdated game);
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
    public FirebaseGameManagerOutdated(String userId, GameUpdateListener listener) {
        this.currentUserId = userId;
        this.updateListener = listener;
        this.database = FirebaseDatabase.getInstance();
        this.gamesRef = database.getReference(GAMES_REF);
    }

    /**
     * Creates a new multiplayer game in Firebase
     * Sets the current user as the host and initializes game settings
     *
     * @param displayName The display name of the current user
     */
    public void createGame(String displayName) {
        MultiplayerGameOutdated newGame = new MultiplayerGameOutdated(currentUserId, displayName);
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
        gamesRef.child(gameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameOutdated game = mutableData.getValue(MultiplayerGameOutdated.class);

                if (game == null) {
                    return Transaction.success(mutableData);
                }

                // Check if game is joinable
                if (game.isGameStarted() || game.getCurrentPlayers() >= game.getMaxPlayers()) {
                    return Transaction.abort();
                }

                // Determine player color
                int playerColor = -1;
                boolean[] colorsTaken = new boolean[4];

                for (MultiplayerGameOutdated.GamePlayer player : game.getPlayers().values()) {
                    colorsTaken[player.getPlayerColor()] = true;
                }

                for (int i = 0; i < 4; i++) {
                    if (!colorsTaken[i]) {
                        playerColor = i;
                        break;
                    }
                }

                // Add player to game
                MultiplayerGameOutdated.GamePlayer newPlayer = new MultiplayerGameOutdated.GamePlayer();
                newPlayer.setUserId(currentUserId);
                newPlayer.setDisplayName(displayName);
                newPlayer.setPlayerColor(playerColor);
                newPlayer.setReady(false);

                game.getPlayers().put(currentUserId, newPlayer);
                game.setCurrentPlayers(game.getCurrentPlayers() + 1);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    currentGameId = gameId;
                    attachGameListener(gameId);
                    if (updateListener != null) {
                        updateListener.onGameJoined(dataSnapshot.getValue(MultiplayerGameOutdated.class));
                    }
                } else {
                    if (updateListener != null) {
                        updateListener.onGameError("Failed to join game. It may be full or already started.");
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

        gamesRef.child(currentGameId)
                .child("players")
                .child(currentUserId)
                .child("ready")
                .setValue(ready);
    }

    /**
     * Starts the game if all conditions are met
     * Only the host can start the game and all players must be ready
     */
    public void startGame() {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameOutdated game = mutableData.getValue(MultiplayerGameOutdated.class);

                if (game == null || !game.getHostUserId().equals(currentUserId)) {
                    return Transaction.abort();
                }

                // Check if all players are ready
                boolean allReady = true;
                for (MultiplayerGameOutdated.GamePlayer player : game.getPlayers().values()) {
                    if (!player.isReady()) {
                        allReady = false;
                        break;
                    }
                }

                if (!allReady) {
                    return Transaction.abort();
                }

                // Initialize game state
                game.setGameStarted(true);
                game.setCurrentPlayerTurn(0);
                game.setLastDiceRoll(0);

                // Initialize game state
                MultiplayerGameOutdated.GameState gameState = new MultiplayerGameOutdated.GameState();
                // Set initial game state (all pawns in home, etc.)
                game.setGameState(gameState);

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to start game. Make sure all players are ready.");
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
                MultiplayerGameOutdated game = mutableData.getValue(MultiplayerGameOutdated.class);

                if (game == null || !game.isGameStarted()) {
                    return Transaction.abort();
                }

                // Check if it's this player's turn
                int playerColor = -1;
                for (MultiplayerGameOutdated.GamePlayer player : game.getPlayers().values()) {
                    if (player.getUserId().equals(currentUserId)) {
                        playerColor = player.getPlayerColor();
                        break;
                    }
                }

                if (playerColor != game.getCurrentPlayerTurn()) {
                    return Transaction.abort();
                }

                // Roll the dice
                Random random = new Random();
                int diceValue = random.nextInt(6) + 1;
                game.setLastDiceRoll(diceValue);
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to roll dice. It may not be your turn.");
                }
            }
        });
    }

    /**
     * Moves a pawn to a new position on the board
     * Updates game state and advances turn to the next player
     *
     * @param pawnIndex Index of the pawn to move (0-3)
     * @param position New position on the board
     */
    public void movePawn(int pawnIndex, int position) {
        if (currentGameId == null) return;

        gamesRef.child(currentGameId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MultiplayerGameOutdated game = mutableData.getValue(MultiplayerGameOutdated.class);

                if (game == null || !game.isGameStarted()) {
                    return Transaction.abort();
                }

                // Check if it's this player's turn
                int playerColor = -1;
                for (MultiplayerGameOutdated.GamePlayer player : game.getPlayers().values()) {
                    if (player.getUserId().equals(currentUserId)) {
                        playerColor = player.getPlayerColor();
                        break;
                    }
                }

                if (playerColor != game.getCurrentPlayerTurn()) {
                    return Transaction.abort();
                }

                // Update the pawn position
                game.getGameState().getPawnPositions()[playerColor][pawnIndex] = position;
                game.setLastUpdateTimestamp(System.currentTimeMillis());

                // Update turn to next player
                game.setCurrentPlayerTurn((game.getCurrentPlayerTurn() + 1) % 4);

                // Skip players who aren't in the game
                while (!playerInGame(game, game.getCurrentPlayerTurn())) {
                    game.setCurrentPlayerTurn((game.getCurrentPlayerTurn() + 1) % 4);
                }

                mutableData.setValue(game);
                return Transaction.success(mutableData);
            }

            /**
             * Helper method to check if a player with the given color index exists in the game
             *
             * @param game Current game object
             * @param playerIndex Color index to check
             * @return True if player exists, false otherwise
             */
            private boolean playerInGame(MultiplayerGameOutdated game, int playerIndex) {
                for (MultiplayerGameOutdated.GamePlayer player : game.getPlayers().values()) {
                    if (player.getPlayerColor() == playerIndex) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed && updateListener != null) {
                    updateListener.onGameError("Failed to move pawn.");
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
                MultiplayerGameOutdated game = mutableData.getValue(MultiplayerGameOutdated.class);

                if (game == null) {
                    return Transaction.success(mutableData);
                }

                // If host is leaving, delete the game
                if (game.getHostUserId().equals(currentUserId)) {
                    mutableData.setValue(null);
                    return Transaction.success(mutableData);
                }

                // Otherwise, remove the player
                if (game.getPlayers().containsKey(currentUserId)) {
                    game.getPlayers().remove(currentUserId);
                    game.setCurrentPlayers(game.getCurrentPlayers() - 1);
                    game.setLastUpdateTimestamp(System.currentTimeMillis());

                    mutableData.setValue(game);
                }

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
                MultiplayerGameOutdated game = dataSnapshot.getValue(MultiplayerGameOutdated.class);

                if (game == null) {
                    // Game was deleted
                    if (updateListener != null) {
                        updateListener.onGameError("Game no longer exists.");
                    }
                    detachGameListener();
                    currentGameId = null;
                    return;
                }

                if (updateListener != null) {
                    updateListener.onGameUpdated(game);
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