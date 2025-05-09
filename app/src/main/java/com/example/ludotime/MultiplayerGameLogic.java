package com.example.ludotime;

import java.util.Random;

/**
 * MultiplayerGameLogic.java
 *
 * Similar to GameLogic, but handles multiplayer game state management.
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
    private boolean[] isReady;
    private String[] playerID;
    private String[] playerName;

    public MultiplayerGameLogic(String hostUserId, String hostName, int maxPlayers, int hostColor) {
        this.hostUserId = hostUserId;
        this.maxPlayers = maxPlayers;
        this.currentPlayersNumber = 1;
        this.gameStarted = false;
        this.gameId = generateGameId();
        this.isReady = new boolean[4];
        this.playerID = new String[4];
        this.playerName = new String[4];
        for(int i = 0; i < 4; i++){
            this.isReady[i] = false;
            this.playerID[i] = null;
            this.playerName[i] = "Unknown";

        }
        this.playerID[hostColor] = hostUserId;
        this.playerName[hostColor] = hostName;
        this.isReady[hostColor] = true;
        this.lastUpdateTimestamp = System.currentTimeMillis();
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

    public boolean[] getIsReady() {
        return isReady;
    }

    public void setIsReady(boolean[] isReady) {
        this.isReady = isReady;
    }

    public String[] getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String[] playerID) {
        this.playerID = playerID;
    }

    public String[] getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String[] playerName) {
        this.playerName = playerName;
    }
}
