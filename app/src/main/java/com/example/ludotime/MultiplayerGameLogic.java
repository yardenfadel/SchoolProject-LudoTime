package com.example.ludotime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * MultiplayerGameLogic.java
 *
 * Similar to GameLogic, but handles multiplayer game state management.
 * Uses ArrayLists for better Firebase compatibility and dynamic player management.
 */

public class MultiplayerGameLogic extends GameLogic {
    //game state and rules
    private GameLogic gameLogic;

    private int maxPlayers;
    private int currentPlayersNumber;
    private boolean gameStarted;
    private long lastUpdateTimestamp;
    private String gameId;             // 6-character unique code
    private String hostUserId;         // ID of the player who created the game
    private ArrayList<Boolean> isReady;
    private ArrayList<String> playerID;
    private ArrayList<String> playerName;

    /**
     * Constructor for creating a new multiplayer game
     * @param hostUserId ID of the host player
     * @param hostName Name of the host player
     * @param maxPlayers Maximum number of players allowed
     * @param hostColor Color index for the host player
     */
    public MultiplayerGameLogic(String hostUserId, String hostName, int maxPlayers, int hostColor) {
        this.hostUserId = hostUserId;
        this.maxPlayers = maxPlayers;
        this.currentPlayersNumber = 1;
        this.gameStarted = false;
        this.gameId = generateGameId();

        // Initialize ArrayLists with default values for 4 players
        this.isReady = new ArrayList<>(Collections.nCopies(4, false));
        this.playerID = new ArrayList<>(Collections.nCopies(4, (String) null));
        this.playerName = new ArrayList<>(Collections.nCopies(4, "Unknown"));

        // Set host player data
        this.playerID.set(hostColor, hostUserId);
        this.playerName.set(hostColor, hostName);
        this.isReady.set(hostColor, true);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }

    /**
     * Default constructor for Firebase deserialization
     */
    public MultiplayerGameLogic() {
        // Initialize ArrayLists with default values
        this.isReady = new ArrayList<>(Collections.nCopies(4, false));
        this.playerID = new ArrayList<>(Collections.nCopies(4, (String) null));
        this.playerName = new ArrayList<>(Collections.nCopies(4, "Unknown"));
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
     * Gets the game logic instance
     * @return GameLogic instance
     */
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    /**
     * Sets the game logic instance
     * @param gameLogic GameLogic instance to set
     */
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    /**
     * Gets the maximum number of players
     * @return Maximum players allowed
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum number of players
     * @param maxPlayers Maximum players to allow
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Gets the current number of players
     * @return Current player count
     */
    public int getCurrentPlayersNumber() {
        return currentPlayersNumber;
    }

    /**
     * Sets the current number of players
     * @param currentPlayersNumber Current player count
     */
    public void setCurrentPlayersNumber(int currentPlayersNumber) {
        this.currentPlayersNumber = currentPlayersNumber;
    }

    /**
     * Checks if the game has started
     * @return True if game started, false otherwise
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sets the game started status
     * @param gameStarted Game started status
     */
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    /**
     * Gets the last update timestamp
     * @return Last update timestamp in milliseconds
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Sets the last update timestamp
     * @param lastUpdateTimestamp Timestamp in milliseconds
     */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /**
     * Gets the host user ID
     * @return Host user ID
     */
    public String getHostUserId() {
        return hostUserId;
    }

    /**
     * Sets the host user ID
     * @param hostUserId Host user ID to set
     */
    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    /**
     * Gets the game ID
     * @return 6-character game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the game ID
     * @param gameId Game ID to set
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the ready status list for all players
     * @return ArrayList of ready status for each player
     */
    public ArrayList<Boolean> getIsReady() {
        return isReady;
    }

    /**
     * Sets the ready status list for all players
     * @param isReady ArrayList of ready status for each player
     */
    public void setIsReady(ArrayList<Boolean> isReady) {
        this.isReady = isReady;
    }

    /**
     * Gets the player ID list
     * @return ArrayList of player IDs
     */
    public ArrayList<String> getPlayerID() {
        return playerID;
    }

    /**
     * Sets the player ID list
     * @param playerID ArrayList of player IDs
     */
    public void setPlayerID(ArrayList<String> playerID) {
        this.playerID = playerID;
    }

    /**
     * Gets the player name list
     * @return ArrayList of player names
     */
    public ArrayList<String> getPlayerName() {
        return playerName;
    }

    /**
     * Sets the player name list
     * @param playerName ArrayList of player names
     */
    public void setPlayerName(ArrayList<String> playerName) {
        this.playerName = playerName;
    }
}