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

    // Default constructor for Firebase deserialization
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

    // Getters and Setters
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayersNumber() {
        return currentPlayersNumber;
    }

    public void setCurrentPlayersNumber(int currentPlayersNumber) {
        this.currentPlayersNumber = currentPlayersNumber;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public ArrayList<Boolean> getIsReady() {
        return isReady;
    }

    public void setIsReady(ArrayList<Boolean> isReady) {
        this.isReady = isReady;
    }

    public ArrayList<String> getPlayerID() {
        return playerID;
    }

    public void setPlayerID(ArrayList<String> playerID) {
        this.playerID = playerID;
    }

    public ArrayList<String> getPlayerName() {
        return playerName;
    }

    public void setPlayerName(ArrayList<String> playerName) {
        this.playerName = playerName;
    }
}