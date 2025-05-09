/**
 * GameLogic.java
 *
 * Manages the game logic for the Ludo game including:
 * - Turn management
 * - Dice rolling
 * - Pawn movement
 * - Path tracking
 * - Collision detection
 */
package com.example.ludotime;

import android.graphics.Point;

public class GameLogic {
    // ===== Constants =====
    public static final int RED_PLAYER = 0;
    public static final int GREEN_PLAYER = 1;
    public static final int YELLOW_PLAYER = 2;
    public static final int BLUE_PLAYER = 3;

    // Number required to exit the home area
    private static final int EXIT_ROLL = 6;

    // Total number of squares on the board (excluding home and final paths)
    private static final int BOARD_SQUARES = 52;

    // ===== Game State =====
    private int currentPlayerTurn;
    private int lastDiceRoll;
    private boolean diceRolled;
    private boolean moveMade;

    // Track if pawns are in home or on the board
    private boolean[][] pawnInHome;
    private boolean[][] pawnFinished;
    private boolean[][] pawnOnFinishLine; // Added: track if pawn is on finish line

    // Track pawn positions on the main track (0-51)
    private int[][] pawnPositions;
    private int[][] finalPathPositions; // Added: track positions within final path (-1 means not in final path)

    // Home coordinates for each player's pawns
    private Point[][] homeCoordinates;

    // Start positions on the main track for each player
    private int[] startPositions = {0, 13, 26, 39};

    // Entry points to final path for each player
    private int[] finalPathEntries = {50, 11, 24, 37};

    // Final path coordinates for each player
    private Point[][] finalPathCoordinates;

    // Pawn selection and it's flag, used to wait for a choice
    private boolean waitingForPawnSelection;
    private int selectedPawn = -1;

    // Track winners
    private int[] winnerOrder = {-1, -1, -1, -1}; // Track order of winners
    private int winnersCount = 0; // Count of winners so far
    /**
     * Constructor initializes the game state
     * @param testMode Set to true to use the test setup for quicker finish
     */
    public GameLogic(boolean testMode) {
        // Start with player 1 (RED)
        currentPlayerTurn = RED_PLAYER;

        // Initialize tracking arrays
        pawnInHome = new boolean[4][4];
        pawnFinished = new boolean[4][4];
        pawnOnFinishLine = new boolean[4][4];
        pawnPositions = new int[4][4];
        finalPathPositions = new int[4][4];

        // Initialize home coordinates
        homeCoordinates = new Point[4][4];

        // RED home coordinates
        homeCoordinates[RED_PLAYER][0] = new Point(2, 2);
        homeCoordinates[RED_PLAYER][1] = new Point(3, 2);
        homeCoordinates[RED_PLAYER][2] = new Point(2, 3);
        homeCoordinates[RED_PLAYER][3] = new Point(3, 3);

        // GREEN home coordinates
        homeCoordinates[GREEN_PLAYER][0] = new Point(11, 2);
        homeCoordinates[GREEN_PLAYER][1] = new Point(12, 2);
        homeCoordinates[GREEN_PLAYER][2] = new Point(11, 3);
        homeCoordinates[GREEN_PLAYER][3] = new Point(12, 3);

        // BLUE home coordinates
        homeCoordinates[BLUE_PLAYER][0] = new Point(2, 11);
        homeCoordinates[BLUE_PLAYER][1] = new Point(3, 11);
        homeCoordinates[BLUE_PLAYER][2] = new Point(2, 12);
        homeCoordinates[BLUE_PLAYER][3] = new Point(3, 12);

        // YELLOW home coordinates
        homeCoordinates[YELLOW_PLAYER][0] = new Point(11, 11);
        homeCoordinates[YELLOW_PLAYER][1] = new Point(12, 11);
        homeCoordinates[YELLOW_PLAYER][2] = new Point(11, 12);
        homeCoordinates[YELLOW_PLAYER][3] = new Point(12, 12);

        // Initialize final path coordinates
        finalPathCoordinates = new Point[4][5]; // 5 steps in final path

        // RED final path (horizontal rightward from left)
        finalPathCoordinates[RED_PLAYER][0] = new Point(1, 7);
        finalPathCoordinates[RED_PLAYER][1] = new Point(2, 7);
        finalPathCoordinates[RED_PLAYER][2] = new Point(3, 7);
        finalPathCoordinates[RED_PLAYER][3] = new Point(4, 7);
        finalPathCoordinates[RED_PLAYER][4] = new Point(5, 7);

        // GREEN final path (vertical downward from top)
        finalPathCoordinates[GREEN_PLAYER][0] = new Point(7, 1);
        finalPathCoordinates[GREEN_PLAYER][1] = new Point(7, 2);
        finalPathCoordinates[GREEN_PLAYER][2] = new Point(7, 3);
        finalPathCoordinates[GREEN_PLAYER][3] = new Point(7, 4);
        finalPathCoordinates[GREEN_PLAYER][4] = new Point(7, 5);

        // BLUE final path (vertical upward from bottom)
        finalPathCoordinates[BLUE_PLAYER][0] = new Point(7, 13);
        finalPathCoordinates[BLUE_PLAYER][1] = new Point(7, 12);
        finalPathCoordinates[BLUE_PLAYER][2] = new Point(7, 11);
        finalPathCoordinates[BLUE_PLAYER][3] = new Point(7, 10);
        finalPathCoordinates[BLUE_PLAYER][4] = new Point(7, 9);

        // YELLOW final path (horizontal leftward from right)
        finalPathCoordinates[YELLOW_PLAYER][0] = new Point(13, 7);
        finalPathCoordinates[YELLOW_PLAYER][1] = new Point(12, 7);
        finalPathCoordinates[YELLOW_PLAYER][2] = new Point(11, 7);
        finalPathCoordinates[YELLOW_PLAYER][3] = new Point(10, 7);
        finalPathCoordinates[YELLOW_PLAYER][4] = new Point(9, 7);

        if (testMode) {
            for (int player = 0; player < 4; player++) {
                for (int pawn = 0; pawn < 4; pawn++) {
                    pawnInHome[player][pawn] = false;
                    pawnFinished[player][pawn] = false;
                    pawnOnFinishLine[player][pawn] = false;
                    finalPathPositions[player][pawn] = -1;

                    //put before path
                    pawnPositions[player][pawn] = (player*13+pawn+47)%52;
                    System.out.println((player*13+pawn+47)%52 + " is th location for player" + player + "pawn: " + pawn);
                }
            }
            setupTestState();
        } else {
            // Set all pawns to be in home initially
            for (int player = 0; player < 4; player++) {
                for (int pawn = 0; pawn < 4; pawn++) {
                    pawnInHome[player][pawn] = true;
                    pawnFinished[player][pawn] = false;
                    pawnOnFinishLine[player][pawn] = false;
                    pawnPositions[player][pawn] = -1;
                    finalPathPositions[player][pawn] = -1;
                }
            }
        }

        diceRolled = false;
        moveMade = false;
        lastDiceRoll = 0;
    }

    /**
     * Default constructor for normal game mode
     */
    public GameLogic() {
        this(false);
    }

    /**
     * Method to set up a test state where players are close to finishing
     */
    public void setupTestState() {
        // Reset the current state first
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome[player][pawn] = false;
                pawnFinished[player][pawn] = false;
                pawnOnFinishLine[player][pawn] = false;
                pawnPositions[player][pawn] = -1;
                finalPathPositions[player][pawn] = -1;
            }
        }

        // RED player - one pawn already finished, three on finish line
        pawnFinished[RED_PLAYER][0] = true;
        pawnOnFinishLine[RED_PLAYER][1] = true;
        pawnOnFinishLine[RED_PLAYER][2] = true;
        pawnOnFinishLine[RED_PLAYER][3] = true;
        finalPathPositions[RED_PLAYER][1] = 4; // One step away from finish
        finalPathPositions[RED_PLAYER][2] = 4; // One step away from finish
        finalPathPositions[RED_PLAYER][3] = 4; // One step away from finish

        // GREEN player - two pawns already finished, two on finish line
        pawnFinished[GREEN_PLAYER][0] = true;
        pawnFinished[GREEN_PLAYER][1] = true;
        pawnOnFinishLine[GREEN_PLAYER][2] = true;
        pawnOnFinishLine[GREEN_PLAYER][3] = true;
        finalPathPositions[GREEN_PLAYER][2] = 3; // Two steps away from finish
        finalPathPositions[GREEN_PLAYER][3] = 3; // Two steps away from finish

        // YELLOW player - three pawns already finished, one on finish line
        pawnFinished[YELLOW_PLAYER][0] = true;
        pawnFinished[YELLOW_PLAYER][1] = true;
        pawnFinished[YELLOW_PLAYER][2] = true;
        pawnOnFinishLine[YELLOW_PLAYER][3] = true;
        finalPathPositions[YELLOW_PLAYER][3] = 3; // Two steps away from finish

        // BLUE player - all pawns on main track, but close to final path
        pawnPositions[BLUE_PLAYER][0] = 36; // One step away from final path entry
        pawnPositions[BLUE_PLAYER][1] = 36; // One step away from final path entry
        pawnPositions[BLUE_PLAYER][2] = 35; // Two steps away from final path entry
        pawnPositions[BLUE_PLAYER][3] = 35; // Two steps away from final path entry

        // Reset winners
        winnersCount = 0;
        for (int i = 0; i < 4; i++) {
            winnerOrder[i] = -1;
        }

        // Reset the game state
        currentPlayerTurn = RED_PLAYER;
        diceRolled = false;
        moveMade = false;
        lastDiceRoll = 0;
    }

    /**
     *Plays the next round (setPawnSelection() might be called later to complete)
     *@return Did the round end
     */
    public boolean playRound() {
        if (hasValidMoves()) {
            // Begin waiting for pawn selection
            waitingForPawnSelection = true;
            selectedPawn = -1;

            // Return false to indicate we're waiting for selection
            return false;
        }

        nextTurn();
        return true;
    }

    /**
     *Continues the round after a pawn choice
     */
    public void setPawnSelection(int pawnIndex) {
        this.selectedPawn = pawnIndex;
        this.waitingForPawnSelection = false;

        // Now that we have a selection, complete the move
        if (selectedPawn != -1) {
            movePawn(currentPlayerTurn, selectedPawn);

            // Only check for captures if pawn is not on final path
            if (!pawnOnFinishLine[currentPlayerTurn][selectedPawn]) {
                checkForCaptures(currentPlayerTurn, pawnPositions[currentPlayerTurn][selectedPawn]);
            }

            nextTurn();
        }
    }

    /**
     * Find out if the game waits for pawn selection
     * @return Is the game waiting for a pawn selection
     */
    public boolean isWaitingForPawnSelection() {
        return waitingForPawnSelection;
    }

    /**
     * Get the current player's turn
     * @return Player index (0-3)
     */
    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    /**
     * Get the last dice roll value
     * @return Last dice roll (1-6)
     */
    public int getLastDiceRoll() {
        return lastDiceRoll;
    }

    /**
     * Check if the dice has been rolled this turn
     * @return True if dice has been rolled
     */
    public boolean isDiceRolled() {
        return diceRolled;
    }

    /**
     * Check if a move has been made this turn
     * @return True if move has been made
     */
    public boolean isMoveMade() {
        return moveMade;
    }

    /**
     * Set the dice roll value for the current turn
     * @param value Dice roll value (1-6)
     */
    public void setDiceRoll(int value) {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Dice value must be between 1 and 6");
        }

        lastDiceRoll = value;
        diceRolled = true;
        moveMade = false;
    }

    public void setCurrentPlayerTurn(int player){
        currentPlayerTurn =player;
    }

    /**
     * Move to the next player's turn
     */
    public void nextTurn() {
        currentPlayerTurn = (currentPlayerTurn + 1) % 4;
        diceRolled = false;
        moveMade = false;
        lastDiceRoll = 0;
    }

    /**
     * Check if a pawn is in its home area
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if pawn is in home
     */
    public boolean isPawnInHome(int player, int pawnIndex) {
        return pawnInHome[player][pawnIndex];
    }

    /**
     * Check if a pawn has finished the game
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if pawn has finished
     */
    public boolean isPawnFinished(int player, int pawnIndex) {
        return pawnFinished[player][pawnIndex];
    }

    /**
     * Check if a pawn is on the finish line (final path)
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if pawn is on finish line
     */
    public boolean isPawnOnFinishLine(int player, int pawnIndex) {
        return pawnOnFinishLine[player][pawnIndex];
    }

    /**
     * Get the board position for a specific pawn
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return Point with x,y coordinates on the board grid
     */
    public Point getPawnBoardPosition(int player, int pawnIndex) {
        // If pawn is in home, return its home coordinates
        if (pawnInHome[player][pawnIndex]) {
            return homeCoordinates[player][pawnIndex];
        }

        // If pawn has finished, return the center position
        if (pawnFinished[player][pawnIndex]) {
            return new Point(7, 7); // Center of board
        }

        // If pawn is on finish line, return its position in the final path
        if (pawnOnFinishLine[player][pawnIndex]) {
            int finalPathIndex = finalPathPositions[player][pawnIndex];
            return finalPathCoordinates[player][finalPathIndex];
        }

        // Otherwise, pawn is on the main track
        return getMainTrackCoordinates(pawnPositions[player][pawnIndex]);
    }

    /**
     * Convert a main track position (0-51) to board coordinates
     * @param position Track position (0-51)
     * @return Point with x,y coordinates on the board grid
     */
    private Point getMainTrackCoordinates(int position) {
        // Define full track coordinates in a clockwise pattern
        // RED start (left to top)
        if (position == 0) return new Point(1, 6);
        if (position == 1) return new Point(2, 6);
        if (position == 2) return new Point(3, 6);
        if (position == 3) return new Point(4, 6);
        if (position == 4) return new Point(5, 6);
        if (position == 5) return new Point(6, 5);
        if (position == 6) return new Point(6, 4);
        if (position == 7) return new Point(6, 3);
        if (position == 8) return new Point(6, 2);
        if (position == 9) return new Point(6, 1);
        if (position == 10) return new Point(6, 0);
        if (position == 11) return new Point(7, 0);
        if (position == 12) return new Point(8, 0);

        // GREEN start (top to right)
        if (position == 13) return new Point(8, 1);
        if (position == 14) return new Point(8, 2);
        if (position == 15) return new Point(8, 3);
        if (position == 16) return new Point(8, 4);
        if (position == 17) return new Point(8, 5);
        if (position == 18) return new Point(9, 6);
        if (position == 19) return new Point(10, 6);
        if (position == 20) return new Point(11, 6);
        if (position == 21) return new Point(12, 6);
        if (position == 22) return new Point(13, 6);
        if (position == 23) return new Point(14, 6);
        if (position == 24) return new Point(14, 7);
        if (position == 25) return new Point(14, 8);

        // YELLOW start (right to bottom)
        if (position == 26) return new Point(13, 8);
        if (position == 27) return new Point(12, 8);
        if (position == 28) return new Point(11, 8);
        if (position == 29) return new Point(10, 8);
        if (position == 30) return new Point(9, 8);
        if (position == 31) return new Point(8, 9);
        if (position == 32) return new Point(8, 10);
        if (position == 33) return new Point(8, 11);
        if (position == 34) return new Point(8, 12);
        if (position == 35) return new Point(8, 13);
        if (position == 36) return new Point(8, 14);
        if (position == 37) return new Point(7, 14);
        if (position == 38) return new Point(6, 14);

        // BLUE start (bottom to left)
        if (position == 39) return new Point(6, 13);
        if (position == 40) return new Point(6, 12);
        if (position == 41) return new Point(6, 11);
        if (position == 42) return new Point(6, 10);
        if (position == 43) return new Point(6, 9);
        if (position == 44) return new Point(5, 8);
        if (position == 45) return new Point(4, 8);
        if (position == 46) return new Point(3, 8);
        if (position == 47) return new Point(2, 8);
        if (position == 48) return new Point(1, 8);
        if (position == 49) return new Point(0, 8);
        if (position == 50) return new Point(0, 7);
        if (position == 51) return new Point(0, 6);

        // Default fallback (should never happen in proper implementation)
        return new Point(7, 7);
    }

    /**
     * Try to move a pawn by the last dice roll
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if the move was valid and completed
     */
    public boolean movePawn(int player, int pawnIndex) {
        // Can only move current player's pawns after rolling dice
        if (player != currentPlayerTurn || !diceRolled || moveMade) {
            return false;
        }

        // Check if pawn is in home
        if (pawnInHome[player][pawnIndex]) {
            // Can only exit home with a 6
            if (lastDiceRoll == EXIT_ROLL) {
                exitPawnFromHome(player, pawnIndex);
                moveMade = true;
                return true;
            }
            return false;
        }

        // Check if pawn has already finished
        if (pawnFinished[player][pawnIndex]) {
            return false;
        }

        // Check if pawn is already on finish line (final path)
        if (pawnOnFinishLine[player][pawnIndex]) {
            int currentFinalPathPosition = finalPathPositions[player][pawnIndex];
            int newFinalPathPosition = currentFinalPathPosition + lastDiceRoll;

            // Check if pawn reaches or exceeds end of final path
            if (newFinalPathPosition >= 5) {
                // Pawn has reached home!
                pawnFinished[player][pawnIndex] = true;
                pawnOnFinishLine[player][pawnIndex] = false;
                finalPathPositions[player][pawnIndex] = -1;
            } else {
                // Pawn advances on final path
                finalPathPositions[player][pawnIndex] = newFinalPathPosition;
            }

            moveMade = true;
            return true;
        }

        // Move pawn on the board
        int currentPosition = pawnPositions[player][pawnIndex];

        // Check if the pawn will cross or land on its final path entry point
        boolean willEnterFinalPath = false;
        int stepsIntoFinalPath = 0;

        // Define final path entry for current player
        int entryPoint = finalPathEntries[player];

        // Calculate the start position for this player (to determine full lap)
        int startPos = startPositions[player];

        // Check entry conditions by player color
        if (player == RED_PLAYER) {
            // Needs special handling for positions 47-50 going to final path
            if (currentPosition >= 47 && currentPosition <= 50) {
                int distanceToEntry = (50 - currentPosition);
                // Will either land on or cross the entry point
                if (lastDiceRoll > distanceToEntry) {
                    willEnterFinalPath = true;
                    stepsIntoFinalPath = lastDiceRoll - distanceToEntry - 1;
                }
            }
        }
        else if (player == GREEN_PLAYER) {
            // Needs special handling for positions 8-11 going to final path
            if (currentPosition >= 8 && currentPosition <= 11) {
                int distanceToEntry = (11 - currentPosition);
                // Will either land on or cross the entry point
                if (lastDiceRoll > distanceToEntry) {
                    willEnterFinalPath = true;
                    stepsIntoFinalPath = lastDiceRoll - distanceToEntry - 1;
                }
            }
        }
        else if (player == YELLOW_PLAYER) {
            // Needs special handling for positions 21-24 going to final path
            if (currentPosition >= 21 && currentPosition <= 24) {
                int distanceToEntry = (24 - currentPosition);
                // Will either land on or cross the entry point
                if (lastDiceRoll > distanceToEntry) {
                    willEnterFinalPath = true;
                    stepsIntoFinalPath = lastDiceRoll - distanceToEntry - 1;
                }
            }
        }
        else if (player == BLUE_PLAYER) {
            // Needs special handling for positions 34-37 going to final path
            if (currentPosition >= 34 && currentPosition <= 37) {
                int distanceToEntry = (37 - currentPosition);
                // Will either land on or cross the entry point
                if (lastDiceRoll > distanceToEntry) {
                    willEnterFinalPath = true;
                    stepsIntoFinalPath = lastDiceRoll - distanceToEntry - 1;
                }
            }
        }

        if (willEnterFinalPath && stepsIntoFinalPath < 5) {
            // Enter final path
            pawnOnFinishLine[player][pawnIndex] = true;
            finalPathPositions[player][pawnIndex] = stepsIntoFinalPath;
        } else {
            // Regular move on the main track
            int newPosition = (currentPosition + lastDiceRoll) % BOARD_SQUARES;
            pawnPositions[player][pawnIndex] = newPosition;

            // Check if pawn landed on another pawn
            checkForCaptures(player, pawnPositions[player][pawnIndex]);
        }

        moveMade = true;
        return true;
    }

    /**
     * Move a pawn from home to the start position
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     */
    private void exitPawnFromHome(int player, int pawnIndex) {
        pawnInHome[player][pawnIndex] = false;
        pawnPositions[player][pawnIndex] = startPositions[player];

        // Check if pawn landed on another pawn
        checkForCaptures(player, startPositions[player]);
    }

    /**
     * Check if the moving pawn captures any opponent pawns
     * @param movingPlayer The player who is moving
     * @param position The position to check for captures
     */
    private void checkForCaptures(int movingPlayer, int position) {
        for (int player = 0; player < 4; player++) {
            // Don't capture your own pawns
            if (player == movingPlayer) continue;

            for (int pawn = 0; pawn < 4; pawn++) {
                // Check if opponent pawn is on the same position and not in home, finished or on finish line
                int currentPos = pawnPositions[player][pawn];
                if (!pawnInHome[player][pawn] && !pawnFinished[player][pawn] &&
                        !pawnOnFinishLine[player][pawn] && currentPos == position
                            && currentPos != 8 && currentPos !=21 && currentPos !=34 && currentPos != 47 ) {


                    if(player==0 && currentPos == 0) break;
                    if(player==1 && currentPos == 13) break;
                    if(player==2 && currentPos == 26) break;
                    if(player==3 && currentPos == 39) break;

                    // Send the pawn back home
                    pawnInHome[player][pawn] = true;
                    pawnPositions[player][pawn] = -1;
                }
            }
        }
    }

    /**
     * Check if the game is over (3 players have won)
     * @return True if the game is over with 3 winners
     */
    public boolean isGameOver() {
        return winnersCount >= 3; // Exactly 3 winners
    }

    /**
     * Get the winner order array
     * @return Array with player indices in order of winning (-1 for not finished)
     */
    public int[] getWinnerOrder() {
        return winnerOrder;
    }

    /**
     * Get the position a player finished in (1st, 2nd, 3rd, or not finished)
     * @param player Player index to check
     * @return Position (1, 2, 3) or 0 if not finished yet or 4 if last
     */
    public int getPlayerPosition(int player) {
        for (int i = 0; i < winnerOrder.length; i++) {
            if (winnerOrder[i] == player) {
                return i + 1;
            }
        }
        return 0; // Not finished yet
    }

    /**
     * Check if a player has won the game
     * @param player Player index (0-3)
     * @return True if all pawns have finished
     */
    public boolean hasPlayerWon(int player) {
        for (int pawn = 0; pawn < 4; pawn++) {
            if (!pawnFinished[player][pawn]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a player is already in the winners list
     * @param player Player index to check
     * @return True if player is already in winners list
     */
    private boolean isInWinnersList(int player) {
        for (int i = 0; i < winnersCount; i++) {
            if (winnerOrder[i] == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Track all winners
     * @return Player index of the latest winner, or -1 if no new winner
     */
    public int getWinner() {
        for (int player = 0; player < 4; player++) {
            // Check if player has won and isn't already in winners list
            if (hasPlayerWon(player) && !isInWinnersList(player)) {
                // Add player to winners list
                winnerOrder[winnersCount] = player;
                winnersCount++;

                // Debug message
                System.out.println("Player " + player + " won! Total winners: " + winnersCount);

                return player;
            }
        }
        return -1; // No new winner
    }

    /**
     * Get the number of pawns out of home for a player
     * @param player Player index (0-3)
     * @return Number of pawns on the board
     */
    public int getPawnsOnBoard(int player) {
        int count = 0;
        for (int pawn = 0; pawn < 4; pawn++) {
            if (!pawnInHome[player][pawn] && !pawnFinished[player][pawn]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if player can make any valid moves with current dice roll
     * @return True if any valid moves exist
     */
    public boolean hasValidMoves() {
        // If no dice has been rolled, no valid moves
        if (!diceRolled) {
            return false;
        }

        // If move already made, no more moves
        if (moveMade) {
            return false;
        }

        // Check pawns in home (can only move with a 6)
        if (lastDiceRoll == EXIT_ROLL) {
            for (int pawn = 0; pawn < 4; pawn++) {
                if (pawnInHome[currentPlayerTurn][pawn]) {
                    return true; // Can exit home with a 6
                }
            }
        }

        // Check pawns on board or on finish line
        for (int pawn = 0; pawn < 4; pawn++) {
            if ((!pawnInHome[currentPlayerTurn][pawn] && !pawnFinished[currentPlayerTurn][pawn]) ||
                    pawnOnFinishLine[currentPlayerTurn][pawn]) {
                // This pawn can potentially move
                return true;
            }
        }

        return false;
    }

    /**
     * Get debug information
     */
    public String getWinnersDebugInfo() {
        StringBuilder info = new StringBuilder("Winners: ");
        for (int i = 0; i < winnersCount; i++) {
            info.append(winnerOrder[i]).append(", ");
        }
        info.append("Count: ").append(winnersCount);
        return info.toString();
    }

    /**
     * Get the current number of winners
     * @return Number of players who have won so far
     */
    public int getWinnersCount() {
        return winnersCount;
    }
}