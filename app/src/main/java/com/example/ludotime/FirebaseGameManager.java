package com.example.ludotime;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    /*
    public void createGame() {
        MultiplayerGameLogic newGame = new MultiplayerGameLogic(currentUserId, "name",4,0);
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
     */
}
