
package com.example.ludotime;

import android.graphics.Point;

public class MultiplayerGameLogicOutdated extends GameLogic {
    // Reference to the current multiplayer game
    private MultiplayerGameOutdated currentGame;

    // Firebase game manager for network operations
    //private FirebaseGameManager firebaseManager;

    // Local player information
    private String localPlayerId;
    private int localPlayerColor;

    // Flag to indicate if we are waiting for network synchronization
    private boolean waitingForSync;

    // Game state update listener
    private GameStateListener stateListener;

    /**
     * Interface for notifying UI about game state changes
     */
    public interface GameStateListener {
        void onGameStateChanged();
        void onTurnChanged(int newPlayerTurn);
        void onDiceRolled(int value);
        void onPawnMoved(int player, int pawnIndex, int position);
        void onPlayerWon(int player);
        void onGameOver(int[] winnerOrder);
    }

    /**
     * Constructor initializes multiplayer game logic
     *
     * @param game The multiplayer game state
     * @param firebaseManager The Firebase manager for synchronization
     * @param localPlayerId The ID of the local player
     * @param playerColor The color assigned to the local player
     */
    public MultiplayerGameLogicOutdated(MultiplayerGameOutdated game, FirebaseGameManagerOutdated firebaseManager,
                                        String localPlayerId, int playerColor) {
        super(); // Initialize base game logic
        this.currentGame = game;
        this.firebaseManager = firebaseManager;
        this.localPlayerId = localPlayerId;
        this.localPlayerColor = playerColor;
        this.waitingForSync = false;

        // Import initial game state if available
        importGameState();
    }

    /**
     * Constructor with test mode
     */
    public MultiplayerGameLogicOutdated(MultiplayerGameOutdated game, FirebaseGameManagerOutdated firebaseManager,
                                        String localPlayerId, int playerColor, boolean testMode) {
        super(testMode); // Initialize base game logic with test mode
        this.currentGame = game;
        this.firebaseManager = firebaseManager;
        this.localPlayerId = localPlayerId;
        this.localPlayerColor = playerColor;
        this.waitingForSync = false;

        // Import initial game state if available
        importGameState();
    }

    /**
     * Set a listener to receive game state updates
     *
     * @param listener The listener to receive updates
     */
    public void setGameStateListener(GameStateListener listener) {
        this.stateListener = listener;
    }

    /**
     * Check if the current player is the local player
     *
     * @return True if it's the local player's turn
     */
    public boolean isLocalPlayerTurn() {
        return getCurrentPlayerTurn() == localPlayerColor;
    }

    /**
     * Get the local player's color
     *
     * @return The local player's color index
     */
    public int getLocalPlayerColor() {
        return localPlayerColor;
    }

    /**
     * Imports game state from the MultiplayerGame object
     */
    public void importGameState() {
        if (currentGame == null || currentGame.getGameState() == null) {
            return; // No game state to import
        }

        MultiplayerGameOutdated.GameState state = currentGame.getGameState();

        // Set current player turn
        setCurrentPlayerTurn(currentGame.getCurrentPlayerTurn());

        // Import pawn positions and states
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                // Set pawn home status
                if (state.getPawnInHome()[player][pawn]) {
                    // Pawn is in home - reset any position
                    setPawnInHome(player, pawn, true);
                } else if (state.getPawnFinished()[player][pawn]) {
                    // Pawn has finished
                    setPawnFinished(player, pawn, true);
                } else if (state.getPawnOnFinishLine()[player][pawn]) {
                    // Pawn is on finish line
                    setPawnOnFinishLine(player, pawn, true, state.getPawnPositions()[player][pawn]);
                } else {
                    // Pawn is on main track
                    setPawnPosition(player, pawn, state.getPawnPositions()[player][pawn]);
                }
            }
        }

        // Import winner data
        int[] winnerOrder = state.getWinnerOrder();
        int winnersCount = state.getWinnersCount();
        importWinnerData(winnerOrder, winnersCount);

        // Import last dice roll
        setDiceRoll(currentGame.getLastDiceRoll());

        // Notify listener if available
        if (stateListener != null) {
            stateListener.onGameStateChanged();
        }
    }

    /**
     * Export the current game state to the MultiplayerGame object
     * for Firebase synchronization
     */
    public void exportGameState() {
        if (currentGame == null) {
            return;
        }

        // Create game state if it doesn't exist
        if (currentGame.getGameState() == null) {
            currentGame.setGameState(new MultiplayerGameOutdated.GameState());
        }

        MultiplayerGameOutdated.GameState state = currentGame.getGameState();

        // Create arrays to hold exported state
        boolean[][] pawnInHome = new boolean[4][4];
        boolean[][] pawnFinished = new boolean[4][4];
        boolean[][] pawnOnFinishLine = new boolean[4][4];
        int[][] pawnPositions = new int[4][4];

        // Export pawn status and positions
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome[player][pawn] = isPawnInHome(player, pawn);
                pawnFinished[player][pawn] = isPawnFinished(player, pawn);
                pawnOnFinishLine[player][pawn] = isPawnOnFinishLine(player, pawn);

                if (!pawnInHome[player][pawn] && !pawnFinished[player][pawn]) {
                    // Get the position for pawns on the board
                    Point boardPosition = getPawnBoardPosition(player, pawn);

                    // Determine the track position based on board coordinates
                    // For simplicity, store the raw position value
                    if (isPawnOnFinishLine(player, pawn)) {
                        // Store final path position
                        pawnPositions[player][pawn] = getFinalPathPosition(player, pawn);
                    } else {
                        // Store main track position
                        pawnPositions[player][pawn] = getMainTrackPosition(player, pawn);
                    }
                } else {
                    // For home or finished pawns, use -1 as position marker
                    pawnPositions[player][pawn] = -1;
                }
            }
        }

        // Update the game state
        state.setPawnInHome(pawnInHome);
        state.setPawnFinished(pawnFinished);
        state.setPawnOnFinishLine(pawnOnFinishLine);
        state.setPawnPositions(pawnPositions);
        state.setWinnerOrder(getWinnerOrder());
        state.setWinnersCount(getWinnersCount());

        // Update the current game
        currentGame.setGameState(state);
        currentGame.setCurrentPlayerTurn(getCurrentPlayerTurn());
        currentGame.setLastDiceRoll(getLastDiceRoll());
    }

    /**
     * Roll the dice and synchronize with Firebase
     */
    public void rollDice() {
        if (!isLocalPlayerTurn() || waitingForSync) {
            return; // Not the local player's turn or waiting for network
        }

        // Generate random roll (1-6) using the base class method
        int roll = new java.util.Random().nextInt(6) + 1;
        setDiceRoll(roll);

        // Send the roll to Firebase
        waitingForSync = true;
        firebaseManager.rollDice();

        // Notify listener
        if (stateListener != null) {
            stateListener.onDiceRolled(roll);
        }
    }

    /**
     * Handle dice roll from Firebase
     *
     * @param roll The dice value rolled
     */
    public void onRemoteDiceRoll(int roll) {
        // Update the local dice roll
        setDiceRoll(roll);
        waitingForSync = false;

        // Notify listener
        if (stateListener != null) {
            stateListener.onDiceRolled(roll);
        }
    }

    /**
     * Move a pawn and synchronize with Firebase
     *
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if the move was valid and completed
     */
    @Override
    public boolean movePawn(int player, int pawnIndex) {
        if (!isLocalPlayerTurn() || waitingForSync || player != localPlayerColor) {
            return false; // Not local player's turn, waiting for network, or not local player's pawn
        }

        // Check if move is valid using parent class logic
        boolean moved = super.movePawn(player, pawnIndex);

        if (moved) {
            // Get updated position for Firebase
            int position;
            if (isPawnOnFinishLine(player, pawnIndex)) {
                position = getFinalPathPosition(player, pawnIndex);
            } else if (!isPawnInHome(player, pawnIndex) && !isPawnFinished(player, pawnIndex)) {
                position = getMainTrackPosition(player, pawnIndex);
            } else {
                position = -1; // Home or finished
            }

            // Send move to Firebase
            waitingForSync = true;
            firebaseManager.movePawn(pawnIndex, position);

            // Export game state for synchronization
            exportGameState();

            // Notify listener
            if (stateListener != null) {
                stateListener.onPawnMoved(player, pawnIndex, position);
            }

            // Check for winner
            checkForWinner();
        }

        return moved;
    }

    /**
     * Handle remote pawn movement from Firebase
     *
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @param position New position
     */
    public void onRemotePawnMoved(int player, int pawnIndex, int position) {
        // Update the local game state based on the move
        if (position == -1) {
            // Either in home or finished - check current state
            if (isPawnFinished(player, pawnIndex)) {
                // Already finished, do nothing
                return;
            } else {
                // Must be in home
                setPawnInHome(player, pawnIndex, true);
            }
        } else {
            // On board or finish line
            if (isOnFinishLine(player, position)) {
                setPawnOnFinishLine(player, pawnIndex, true, position);
            } else {
                setPawnPosition(player, pawnIndex, position);
            }
        }

        waitingForSync = false;

        // Notify listener
        if (stateListener != null) {
            stateListener.onPawnMoved(player, pawnIndex, position);
            stateListener.onGameStateChanged();
        }

        // Check for winner
        checkForWinner();
    }

    /**
     * Move to the next player's turn and synchronize
     */
    @Override
    public void nextTurn() {
        // Use parent class to advance the turn
        super.nextTurn();

        // Update the game state
        currentGame.setCurrentPlayerTurn(getCurrentPlayerTurn());

        // Notify listener
        if (stateListener != null) {
            stateListener.onTurnChanged(getCurrentPlayerTurn());
        }
    }

    /**
     * Handle turn change from Firebase
     *
     * @param newPlayerTurn The new player turn
     */
    public void onRemoteTurnChanged(int newPlayerTurn) {
        // Update local turn
        setCurrentPlayerTurn(newPlayerTurn);

        // Notify listener
        if (stateListener != null) {
            stateListener.onTurnChanged(newPlayerTurn);
        }
    }

    /**
     * Check if there is a new winner and notify Firebase
     */
    private void checkForWinner() {
        int winner = getWinner();

        if (winner != -1) {
            // A new winner was found
            if (stateListener != null) {
                stateListener.onPlayerWon(winner);
            }

            // Check if game is over
            if (isGameOver()) {
                if (stateListener != null) {
                    stateListener.onGameOver(getWinnerOrder());
                }
            }
        }
    }

    /**
     * Helper method to determine if a position is on a player's finish line
     *
     * @param player Player index
     * @param position Position value
     * @return True if the position is on the player's finish line
     */
    private boolean isOnFinishLine(int player, int position) {
        // Implementation depends on how positions are encoded
        // For this example, we'll assume positions 100-105 represent finish line positions
        return position >= 100 && position < 100 + 6;
    }

    /**
     * Get the main track position for a pawn
     * This is a helper method since the base class might not expose this directly
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @return Main track position (0-51)
     */
    private int getMainTrackPosition(int player, int pawnIndex) {
        // This would be implemented based on how the parent class stores positions
        // For now, return a placeholder value
        return 0; // Placeholder
    }

    /**
     * Get the final path position for a pawn
     * This is a helper method since the base class might not expose this directly
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @return Final path position (0-5)
     */
    private int getFinalPathPosition(int player, int pawnIndex) {
        // This would be implemented based on how the parent class stores positions
        // For now, return a placeholder value
        return 0; // Placeholder
    }

    /**
     * Helper method to set a pawn in home state
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @param inHome True if pawn should be in home
     */
    private void setPawnInHome(int player, int pawnIndex, boolean inHome) {
        // This would modify the parent class's internal state
        // Implementation depends on parent class structure
    }

    /**
     * Helper method to set a pawn in finished state
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @param finished True if pawn has finished
     */
    private void setPawnFinished(int player, int pawnIndex, boolean finished) {
        // This would modify the parent class's internal state
        // Implementation depends on parent class structure
    }

    /**
     * Helper method to set a pawn on finish line
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @param onFinishLine True if pawn is on finish line
     * @param position Position on the finish line
     */
    private void setPawnOnFinishLine(int player, int pawnIndex, boolean onFinishLine, int position) {
        // This would modify the parent class's internal state
        // Implementation depends on parent class structure
    }

    /**
     * Helper method to set a pawn position on the main track
     *
     * @param player Player index
     * @param pawnIndex Pawn index
     * @param position Position on the main track
     */
    private void setPawnPosition(int player, int pawnIndex, int position) {
        // This would modify the parent class's internal state
        // Implementation depends on parent class structure
    }

    /**
     * Helper method to import winner data
     *
     * @param winnerOrder Array of player indices in order of winning
     * @param winnersCount Number of winners so far
     */
    private void importWinnerData(int[] winnerOrder, int winnersCount) {
        // This would modify the parent class's internal state
        // Implementation depends on parent class structure
    }

    /**
     * Updates the game state from Firebase
     * Called when remote updates are received
     *
     * @param game Updated game object from Firebase
     */
    public void updateFromFirebase(MultiplayerGameOutdated game) {
        if (game == null) {
            return;
        }

        // Update the reference
        this.currentGame = game;

        // Import the updated state
        importGameState();
    }

    /**
     * Send current game state to Firebase
     * Called when local state changes and needs to be synchronized
     */
    public void syncToFirebase() {
        if (currentGame == null || firebaseManager == null) {
            return;
        }

        // Export current state to the game object
        exportGameState();

        // Firebase manager would handle the actual sending
        // This might be implemented in FirebaseGameManager
    }
}