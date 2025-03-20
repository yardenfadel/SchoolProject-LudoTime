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
    public static final int BLUE_PLAYER = 2;
    public static final int YELLOW_PLAYER = 3;

    // Number required to exit the home area
    private static final int EXIT_ROLL = 6;

    // Total number of squares on the board (excluding home and final paths)
    private static final int BOARD_SQUARES = 52;

    // ===== Game State =====
    private int currentPlayer;
    private int lastDiceRoll;
    private boolean diceRolled;
    private boolean moveMade;

    // Track if pawns are in home or on the board
    private boolean[][] pawnInHome;
    private boolean[][] pawnFinished;

    // Track pawn positions on the main track (0-51)
    private int[][] pawnPositions;

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

    /**
     * Constructor initializes the game state
     */
    public GameLogic() {
        // Start with player 1 (RED)
        currentPlayer = RED_PLAYER;

        // Initialize tracking arrays
        pawnInHome = new boolean[4][4];
        pawnFinished = new boolean[4][4];
        pawnPositions = new int[4][4];

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

        // RED final path (vertical upward from bottom)
        finalPathCoordinates[RED_PLAYER][0] = new Point(7, 9);
        finalPathCoordinates[RED_PLAYER][1] = new Point(7, 8);
        finalPathCoordinates[RED_PLAYER][2] = new Point(7, 7);
        finalPathCoordinates[RED_PLAYER][3] = new Point(7, 6);
        finalPathCoordinates[RED_PLAYER][4] = new Point(7, 5);

        // GREEN final path (horizontal leftward from right)
        finalPathCoordinates[GREEN_PLAYER][0] = new Point(9, 7);
        finalPathCoordinates[GREEN_PLAYER][1] = new Point(8, 7);
        finalPathCoordinates[GREEN_PLAYER][2] = new Point(7, 7);
        finalPathCoordinates[GREEN_PLAYER][3] = new Point(6, 7);
        finalPathCoordinates[GREEN_PLAYER][4] = new Point(5, 7);

        // BLUE final path (vertical downward from top)
        finalPathCoordinates[BLUE_PLAYER][0] = new Point(7, 5);
        finalPathCoordinates[BLUE_PLAYER][1] = new Point(7, 6);
        finalPathCoordinates[BLUE_PLAYER][2] = new Point(7, 7);
        finalPathCoordinates[BLUE_PLAYER][3] = new Point(7, 8);
        finalPathCoordinates[BLUE_PLAYER][4] = new Point(7, 9);

        // YELLOW final path (horizontal rightward from left)
        finalPathCoordinates[YELLOW_PLAYER][0] = new Point(5, 7);
        finalPathCoordinates[YELLOW_PLAYER][1] = new Point(6, 7);
        finalPathCoordinates[YELLOW_PLAYER][2] = new Point(7, 7);
        finalPathCoordinates[YELLOW_PLAYER][3] = new Point(8, 7);
        finalPathCoordinates[YELLOW_PLAYER][4] = new Point(9, 7);

        // Set all pawns to be in home initially
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome[player][pawn] = true;
                pawnFinished[player][pawn] = false;
                pawnPositions[player][pawn] = -1;
            }
        }

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
            movePawn(currentPlayer, selectedPawn);
            checkForCaptures(currentPlayer, pawnPositions[currentPlayer][selectedPawn]);
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
    public int getCurrentPlayer() {
        return currentPlayer;
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

    /**
     * Move to the next player's turn
     */
    public void nextTurn() {
        currentPlayer = (currentPlayer + 1) % 4;
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

        // If pawn is approaching finish, check if it's in the final path
        int trackPosition = pawnPositions[player][pawnIndex];
        int relativePosition = (trackPosition - startPositions[player] + BOARD_SQUARES) % BOARD_SQUARES;

        // Check if pawn has entered final path
        if (trackPosition == finalPathEntries[player] ||
                (trackPosition > finalPathEntries[player] &&
                        trackPosition < finalPathEntries[player] + 5)) {

            int finalPathIndex = trackPosition - finalPathEntries[player];
            return finalPathCoordinates[player][finalPathIndex];
        }

        // Otherwise, pawn is on the main track
        return getMainTrackCoordinates(trackPosition);
    }

    /**
     * Convert a main track position (0-51) to board coordinates
     * @param position Track position (0-51)
     * @return Point with x,y coordinates on the board grid
     */
    private Point getMainTrackCoordinates(int position) {
        // Define the main track coordinates
        // This is a simplified representation of the board track
        // The actual implementation would map all 52 positions to coordinates

        // Red start (bottom middle)
        if (position == 0) return new Point(7, 10);
        if (position == 1) return new Point(8, 10);
        if (position == 2) return new Point(9, 10);
        if (position == 3) return new Point(10, 10);
        if (position == 4) return new Point(10, 9);
        if (position == 5) return new Point(10, 8);

        // Green start (right middle)
        if (position == 13) return new Point(10, 7);
        if (position == 14) return new Point(10, 6);
        if (position == 15) return new Point(10, 5);
        if (position == 16) return new Point(10, 4);
        if (position == 17) return new Point(9, 4);
        if (position == 18) return new Point(8, 4);

        // Blue start (top middle)
        if (position == 26) return new Point(7, 4);
        if (position == 27) return new Point(6, 4);
        if (position == 28) return new Point(5, 4);
        if (position == 29) return new Point(4, 4);
        if (position == 30) return new Point(4, 5);
        if (position == 31) return new Point(4, 6);

        // Yellow start (left middle)
        if (position == 39) return new Point(4, 7);
        if (position == 40) return new Point(4, 8);
        if (position == 41) return new Point(4, 9);
        if (position == 42) return new Point(4, 10);
        if (position == 43) return new Point(5, 10);
        if (position == 44) return new Point(6, 10);

        // Default for positions not explicitly defined
        // In a complete implementation, all 52 positions would be mapped
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
        if (player != currentPlayer || !diceRolled || moveMade) {
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

        // Move pawn on the board
        int currentPosition = pawnPositions[player][pawnIndex];
        int newPosition = (currentPosition + lastDiceRoll) %BOARD_SQUARES;

        // Check if pawn is entering or moving in final path
        if (currentPosition < finalPathEntries[player] && newPosition >= finalPathEntries[player]) {
            // Handle entering final path
            int stepsIntoFinalPath = newPosition - finalPathEntries[player];

            // Check if pawn reached the end or went beyond
            if (stepsIntoFinalPath >= 5) {
                // Pawn has reached home!
                pawnFinished[player][pawnIndex] = true;
                pawnPositions[player][pawnIndex] = finalPathEntries[player] + 4; // Last position in final path
            } else {
                // Pawn is on final path
                pawnPositions[player][pawnIndex] = newPosition;
            }

            moveMade = true;
            return true;
        }

        // Regular move on the main track
        pawnPositions[player][pawnIndex] = newPosition % BOARD_SQUARES;

        // Check if pawn landed on another pawn
        checkForCaptures(player, pawnPositions[player][pawnIndex]);

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
                // Check if opponent pawn is on the same position and not in a safe square
                if (!pawnInHome[player][pawn] && !pawnFinished[player][pawn] &&
                        pawnPositions[player][pawn] == position) {

                    //TODO: Safe squares are typically at positions 8, 21, 34, 47
                    // Simplification: we're not implementing safe squares here

                    // Send the pawn back home
                    pawnInHome[player][pawn] = true;
                    pawnPositions[player][pawn] = -1;
                }
            }
        }
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
     * Check if the game is over (any player has won)
     * @return Player index of winner, or -1 if no winner yet
     */
    //TODO: make game 3 won
    public int getWinner() {
        for (int player = 0; player < 4; player++) {
            if (hasPlayerWon(player)) {
                return player;
            }
        }
        return -1;
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
                if (pawnInHome[currentPlayer][pawn]) {
                    return true; // Can exit home with a 6
                }
            }
        }

        // Check pawns on board
        for (int pawn = 0; pawn < 4; pawn++) {
            if (!pawnInHome[currentPlayer][pawn] && !pawnFinished[currentPlayer][pawn]) {
                // This pawn can potentially move
                return true;
            }
        }

        return false;
    }
}