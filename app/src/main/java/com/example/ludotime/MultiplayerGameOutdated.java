/**
 * MultiplayerGame.java
 *
 * Manages the multiplayer Ludo game mechanics and state.
 * Handles player connections, turn management, game state tracking,
 * and provides methods for game progress and synchronization.
 */
package com.example.ludotime;

import static com.example.ludotime.GameLogic.RED_PLAYER;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages a multiplayer Ludo game session with remote players.
 * Tracks game state, player information, and turn sequence.
 */
public class MultiplayerGameOutdated {
    private String gameId;             // 6-character unique code
    private String hostUserId;         // ID of the player who created the game
    private int maxPlayers;            // Default 4
    private int currentPlayers;        // Number of joined players
    private Map<String, GamePlayer> players;  // Player info by userId
    private int currentPlayerTurn;     // Player index whose turn it is (0-3)
    private int lastDiceRoll;          // Last dice value rolled
    private boolean gameStarted;       // Whether game has started
    private long lastUpdateTimestamp;  // For handling disconnections
    private GameState gameState;       // Current state information

    /**
     * Inner class representing the complete game state.
     * Contains all information needed to render and update the game board.
     */
    public static class GameState {
        private int[][] pawnPositions;      // Position of each player's pawns on the board
        private boolean[][] pawnInHome;     // Tracks which pawns are still in home area
        private boolean[][] pawnFinished;   // Tracks which pawns have reached the end
        private boolean[][] pawnOnFinishLine; // Tracks pawns on their final stretch
        private int[] winnerOrder;          // Order in which players finished the game
        private int winnersCount;           // Number of players who have finished

        /**
         * Gets the count of players who have finished the game
         *
         * @return Number of players who have finished
         */
        public int getWinnersCount() {
            return winnersCount;
        }

        /**
         * Sets the number of players who have finished the game
         *
         * @param winnersCount The number of finished players
         */
        public void setWinnersCount(int winnersCount) {
            this.winnersCount = winnersCount;
        }

        /**
         * Gets the array tracking the order in which players finished
         *
         * @return Array containing player indices in order of completion
         */
        public int[] getWinnerOrder() {
            return winnerOrder;
        }

        /**
         * Sets the array tracking the finishing order of players
         *
         * @param winnerOrder Array containing player indices in order of completion
         */
        public void setWinnerOrder(int[] winnerOrder) {
            this.winnerOrder = winnerOrder;
        }

        /**
         * Gets the status of pawns on their final stretch to home
         *
         * @return 2D array of boolean values [player][pawn]
         */
        public boolean[][] getPawnOnFinishLine() {
            return pawnOnFinishLine;
        }

        /**
         * Sets the status of pawns on their final stretch to home
         *
         * @param pawnOnFinishLine 2D array of boolean values [player][pawn]
         */
        public void setPawnOnFinishLine(boolean[][] pawnOnFinishLine) {
            this.pawnOnFinishLine = pawnOnFinishLine;
        }

        /**
         * Gets the status of pawns that have completed the game
         *
         * @return 2D array of boolean values [player][pawn]
         */
        public boolean[][] getPawnFinished() {
            return pawnFinished;
        }

        /**
         * Sets the status of pawns that have completed the game
         *
         * @param pawnFinished 2D array of boolean values [player][pawn]
         */
        public void setPawnFinished(boolean[][] pawnFinished) {
            this.pawnFinished = pawnFinished;
        }

        /**
         * Gets the status of pawns still in their starting home position
         *
         * @return 2D array of boolean values [player][pawn]
         */
        public boolean[][] getPawnInHome() {
            return pawnInHome;
        }

        /**
         * Sets the status of pawns still in their starting home position
         *
         * @param pawnInHome 2D array of boolean values [player][pawn]
         */
        public void setPawnInHome(boolean[][] pawnInHome) {
            this.pawnInHome = pawnInHome;
        }

        /**
         * Gets the board positions of all pawns
         *
         * @return 2D array of position values [player][pawn]
         */
        public int[][] getPawnPositions() {
            return pawnPositions;
        }

        /**
         * Sets the board positions of all pawns
         *
         * @param pawnPositions 2D array of position values [player][pawn]
         */
        public void setPawnPositions(int[][] pawnPositions) {
            this.pawnPositions = pawnPositions;
        }
    }

    /**
     * Inner class representing a player in the game.
     * Stores player identification and game-specific attributes.
     */
    public static class GamePlayer {
        private String userId;        // Unique identifier for the player
        private String displayName;    // Player's visible name
        private int playerColor;       // RED_PLAYER, GREEN_PLAYER, etc.
        private boolean ready;         // Player ready status

        /**
         * Constructor with all player attributes
         *
         * @param ready Whether the player is ready to start
         * @param playerColor The assigned color index for this player
         * @param displayName The player's visible name
         * @param userId The player's unique identifier
         */
        public GamePlayer(boolean ready, int playerColor, String displayName, String userId) {
            this.ready = ready;
            this.playerColor = playerColor;
            this.displayName = displayName;
            this.userId = userId;
        }

        /**
         * Empty constructor for Firebase deserialization
         */
        public GamePlayer() {
        }

        /**
         * Gets the player's unique identifier
         *
         * @return Player's user ID
         */
        public String getUserId() {
            return userId;
        }

        /**
         * Sets the player's unique identifier
         *
         * @param userId The player's user ID
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }

        /**
         * Gets the player's display name
         *
         * @return Player's visible name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Sets the player's display name
         *
         * @param displayName Player's visible name
         */
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Gets the player's assigned color
         *
         * @return Color constant (RED_PLAYER, GREEN_PLAYER, etc.)
         */
        public int getPlayerColor() {
            return playerColor;
        }

        /**
         * Sets the player's assigned color
         *
         * @param playerColor Color constant (RED_PLAYER, GREEN_PLAYER, etc.)
         */
        public void setPlayerColor(int playerColor) {
            this.playerColor = playerColor;
        }

        /**
         * Checks if the player is ready to start the game
         *
         * @return true if ready, false otherwise
         */
        public boolean isReady() {
            return ready;
        }

        /**
         * Sets the player's ready status
         *
         * @param ready true if ready to start, false otherwise
         */
        public void setReady(boolean ready) {
            this.ready = ready;
        }
    }

    /**
     * Empty constructor for Firebase deserialization
     */
    public MultiplayerGameOutdated() {}

    /**
     * Creates a new multiplayer game with the specified host
     *
     * @param hostUserId Unique identifier for the host player
     * @param hostDisplayName Display name for the host player
     */
    public MultiplayerGameOutdated(String hostUserId, String hostDisplayName) {
        this.gameId = generateGameId();
        this.hostUserId = hostUserId;
        this.maxPlayers = 4;
        this.currentPlayers = 1;
        this.gameStarted = false;
        this.lastUpdateTimestamp = System.currentTimeMillis();

        // Initialize players map
        this.players = new HashMap<>();
        GamePlayer host = new GamePlayer();
        host.userId = hostUserId;
        host.displayName = hostDisplayName;
        host.playerColor = RED_PLAYER; // Host always gets red
        host.ready = false;

        this.players.put(hostUserId, host);
    }

    /**
     * Generates a random 6-character game ID for room identification
     *
     * @return Alphanumeric 6-character game code
     */
    private String generateGameId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder gameId = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            gameId.append(chars.charAt(random.nextInt(chars.length())));
        }

        return gameId.toString();
    }

    /**
     * Gets the unique identifier for this game session
     *
     * @return The 6-character game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the unique identifier for this game session
     *
     * @param gameId The 6-character game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the user ID of the game host
     *
     * @return Host player's unique identifier
     */
    public String getHostUserId() {
        return hostUserId;
    }

    /**
     * Sets the user ID of the game host
     *
     * @param hostUserId Host player's unique identifier
     */
    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    /**
     * Gets the maximum number of players allowed in this game
     *
     * @return Maximum player count (default 4)
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum number of players allowed in this game
     *
     * @param maxPlayers Maximum player count
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Gets the current number of connected players
     *
     * @return Current player count
     */
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    /**
     * Sets the current number of connected players
     *
     * @param currentPlayers Current player count
     */
    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    /**
     * Gets the map of all players in the game
     *
     * @return Map of players with user IDs as keys
     */
    public Map<String, GamePlayer> getPlayers() {
        return players;
    }

    /**
     * Sets the map of all players in the game
     *
     * @param players Map of players with user IDs as keys
     */
    public void setPlayers(Map<String, GamePlayer> players) {
        this.players = players;
    }

    /**
     * Gets the index of the player whose turn it currently is
     *
     * @return Current player turn index (0-3)
     */
    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    /**
     * Sets the index of the player whose turn it currently is
     *
     * @param currentPlayerTurn Current player turn index (0-3)
     */
    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    /**
     * Gets the value of the last dice roll
     *
     * @return Last rolled dice value (1-6)
     */
    public int getLastDiceRoll() {
        return lastDiceRoll;
    }

    /**
     * Sets the value of the last dice roll
     *
     * @param lastDiceRoll Dice value (1-6)
     */
    public void setLastDiceRoll(int lastDiceRoll) {
        this.lastDiceRoll = lastDiceRoll;
    }

    /**
     * Checks if the game has been started
     *
     * @return true if game is in progress, false if in lobby
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sets whether the game has been started
     *
     * @param gameStarted true to start game, false for lobby state
     */
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    /**
     * Gets the current game state containing all pawn positions and status
     *
     * @return Current game state object
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the current game state
     *
     * @param gameState Updated game state object
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets the timestamp of the last game state update
     * Used to detect disconnected players and handle timeouts
     *
     * @return Timestamp in milliseconds
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Updates the timestamp of the last game state change
     *
     * @param lastUpdateTimestamp Timestamp in milliseconds
     */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}