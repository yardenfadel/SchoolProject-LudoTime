/**
 * GameLogic.java - Firebase Compatible Version
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
import java.util.ArrayList;
import java.util.Arrays;

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

    // Track if pawns are in home or on the board (ArrayList<ArrayList<Boolean>>)
    private ArrayList<ArrayList<Boolean>> pawnInHome;
    private ArrayList<ArrayList<Boolean>> pawnFinished;
    private ArrayList<ArrayList<Boolean>> pawnOnFinishLine;

    // Track pawn positions on the main track (0-51)
    private ArrayList<ArrayList<Integer>> pawnPositions;
    private ArrayList<ArrayList<Integer>> finalPathPositions;

    // Home coordinates for each player's pawns
    private ArrayList<ArrayList<Point>> homeCoordinates;

    // Start positions on the main track for each player
    private ArrayList<Integer> startPositions;

    // Entry points to final path for each player
    private ArrayList<Integer> finalPathEntries;

    // Final path coordinates for each player
    private ArrayList<ArrayList<Point>> finalPathCoordinates;

    // Pawn selection and it's flag, used to wait for a choice
    private boolean waitingForPawnSelection;
    private int selectedPawn;

    // Track winners
    private ArrayList<Integer> winnerOrder;
    private int winnersCount;

    /**
     * Default constructor for Firebase
     */
    public GameLogic() {
        // Initialize all ArrayLists
        initializeArrayLists();

        // Set default values
        currentPlayerTurn = RED_PLAYER;
        lastDiceRoll = 0;
        diceRolled = false;
        moveMade = false;
        waitingForPawnSelection = false;
        selectedPawn = -1;
        winnersCount = 0;

        // Initialize coordinates and positions
        initializeCoordinates();

        // Set all pawns to be in home initially
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome.get(player).set(pawn, true);
                pawnFinished.get(player).set(pawn, false);
                pawnOnFinishLine.get(player).set(pawn, false);
                pawnPositions.get(player).set(pawn, -1);
                finalPathPositions.get(player).set(pawn, -1);
            }
        }
    }

    /**
     * Constructor with test mode
     * @param testMode Set to true to use the test setup for quicker finish
     */
    public GameLogic(boolean testMode) {
        this(); // Call default constructor first

        if (testMode) {
            for (int player = 0; player < 4; player++) {
                for (int pawn = 0; pawn < 4; pawn++) {
                    pawnInHome.get(player).set(pawn, false);
                    pawnFinished.get(player).set(pawn, false);
                    pawnOnFinishLine.get(player).set(pawn, false);
                    finalPathPositions.get(player).set(pawn, -1);

                    //put before path
                    int position = (player * 13 + pawn + 47) % 52;
                    pawnPositions.get(player).set(pawn, position);
                    System.out.println(position + " is th location for player" + player + "pawn: " + pawn);
                }
            }
            setupTestState();
        }
    }

    /**
     * Initialize all ArrayLists with proper size
     */
    private void initializeArrayLists() {
        // Initialize 4x4 boolean ArrayLists
        pawnInHome = new ArrayList<>();
        pawnFinished = new ArrayList<>();
        pawnOnFinishLine = new ArrayList<>();
        pawnPositions = new ArrayList<>();
        finalPathPositions = new ArrayList<>();
        homeCoordinates = new ArrayList<>();
        finalPathCoordinates = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            pawnInHome.add(new ArrayList<>(Arrays.asList(false, false, false, false)));
            pawnFinished.add(new ArrayList<>(Arrays.asList(false, false, false, false)));
            pawnOnFinishLine.add(new ArrayList<>(Arrays.asList(false, false, false, false)));
            pawnPositions.add(new ArrayList<>(Arrays.asList(-1, -1, -1, -1)));
            finalPathPositions.add(new ArrayList<>(Arrays.asList(-1, -1, -1, -1)));

            // Initialize Point ArrayLists
            homeCoordinates.add(new ArrayList<>());
            for (int j = 0; j < 4; j++) {
                homeCoordinates.get(i).add(new Point(0, 0));
            }

            // Initialize final path coordinates (5 steps)
            finalPathCoordinates.add(new ArrayList<>());
            for (int j = 0; j < 5; j++) {
                finalPathCoordinates.get(i).add(new Point(0, 0));
            }
        }

        // Initialize other ArrayLists
        startPositions = new ArrayList<>(Arrays.asList(0, 13, 26, 39));
        finalPathEntries = new ArrayList<>(Arrays.asList(50, 11, 24, 37));
        winnerOrder = new ArrayList<>(Arrays.asList(-1, -1, -1, -1));
    }

    /**
     * Initialize coordinates for homes and final paths
     */
    private void initializeCoordinates() {
        // RED home coordinates
        homeCoordinates.get(RED_PLAYER).set(0, new Point(2, 2));
        homeCoordinates.get(RED_PLAYER).set(1, new Point(3, 2));
        homeCoordinates.get(RED_PLAYER).set(2, new Point(2, 3));
        homeCoordinates.get(RED_PLAYER).set(3, new Point(3, 3));

        // GREEN home coordinates
        homeCoordinates.get(GREEN_PLAYER).set(0, new Point(11, 2));
        homeCoordinates.get(GREEN_PLAYER).set(1, new Point(12, 2));
        homeCoordinates.get(GREEN_PLAYER).set(2, new Point(11, 3));
        homeCoordinates.get(GREEN_PLAYER).set(3, new Point(12, 3));

        // BLUE home coordinates
        homeCoordinates.get(BLUE_PLAYER).set(0, new Point(2, 11));
        homeCoordinates.get(BLUE_PLAYER).set(1, new Point(3, 11));
        homeCoordinates.get(BLUE_PLAYER).set(2, new Point(2, 12));
        homeCoordinates.get(BLUE_PLAYER).set(3, new Point(3, 12));

        // YELLOW home coordinates
        homeCoordinates.get(YELLOW_PLAYER).set(0, new Point(11, 11));
        homeCoordinates.get(YELLOW_PLAYER).set(1, new Point(12, 11));
        homeCoordinates.get(YELLOW_PLAYER).set(2, new Point(11, 12));
        homeCoordinates.get(YELLOW_PLAYER).set(3, new Point(12, 12));

        // Initialize final path coordinates
        // RED final path (horizontal rightward from left)
        finalPathCoordinates.get(RED_PLAYER).set(0, new Point(1, 7));
        finalPathCoordinates.get(RED_PLAYER).set(1, new Point(2, 7));
        finalPathCoordinates.get(RED_PLAYER).set(2, new Point(3, 7));
        finalPathCoordinates.get(RED_PLAYER).set(3, new Point(4, 7));
        finalPathCoordinates.get(RED_PLAYER).set(4, new Point(5, 7));

        // GREEN final path (vertical downward from top)
        finalPathCoordinates.get(GREEN_PLAYER).set(0, new Point(7, 1));
        finalPathCoordinates.get(GREEN_PLAYER).set(1, new Point(7, 2));
        finalPathCoordinates.get(GREEN_PLAYER).set(2, new Point(7, 3));
        finalPathCoordinates.get(GREEN_PLAYER).set(3, new Point(7, 4));
        finalPathCoordinates.get(GREEN_PLAYER).set(4, new Point(7, 5));

        // BLUE final path (vertical upward from bottom)
        finalPathCoordinates.get(BLUE_PLAYER).set(0, new Point(7, 13));
        finalPathCoordinates.get(BLUE_PLAYER).set(1, new Point(7, 12));
        finalPathCoordinates.get(BLUE_PLAYER).set(2, new Point(7, 11));
        finalPathCoordinates.get(BLUE_PLAYER).set(3, new Point(7, 10));
        finalPathCoordinates.get(BLUE_PLAYER).set(4, new Point(7, 9));

        // YELLOW final path (horizontal leftward from right)
        finalPathCoordinates.get(YELLOW_PLAYER).set(0, new Point(13, 7));
        finalPathCoordinates.get(YELLOW_PLAYER).set(1, new Point(12, 7));
        finalPathCoordinates.get(YELLOW_PLAYER).set(2, new Point(11, 7));
        finalPathCoordinates.get(YELLOW_PLAYER).set(3, new Point(10, 7));
        finalPathCoordinates.get(YELLOW_PLAYER).set(4, new Point(9, 7));
    }

    // ===== Firebase-required Getters and Setters =====

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public int getLastDiceRoll() {
        return lastDiceRoll;
    }

    public void setLastDiceRoll(int lastDiceRoll) {
        this.lastDiceRoll = lastDiceRoll;
    }

    public boolean isDiceRolled() {
        return diceRolled;
    }

    public void setDiceRolled(boolean diceRolled) {
        this.diceRolled = diceRolled;
    }

    public boolean isMoveMade() {
        return moveMade;
    }

    public void setMoveMade(boolean moveMade) {
        this.moveMade = moveMade;
    }

    public ArrayList<ArrayList<Boolean>> getPawnInHome() {
        return pawnInHome;
    }

    public void setPawnInHome(ArrayList<ArrayList<Boolean>> pawnInHome) {
        this.pawnInHome = pawnInHome;
    }

    public ArrayList<ArrayList<Boolean>> getPawnFinished() {
        return pawnFinished;
    }

    public void setPawnFinished(ArrayList<ArrayList<Boolean>> pawnFinished) {
        this.pawnFinished = pawnFinished;
    }

    public ArrayList<ArrayList<Boolean>> getPawnOnFinishLine() {
        return pawnOnFinishLine;
    }

    public void setPawnOnFinishLine(ArrayList<ArrayList<Boolean>> pawnOnFinishLine) {
        this.pawnOnFinishLine = pawnOnFinishLine;
    }

    public ArrayList<ArrayList<Integer>> getPawnPositions() {
        return pawnPositions;
    }

    public void setPawnPositions(ArrayList<ArrayList<Integer>> pawnPositions) {
        this.pawnPositions = pawnPositions;
    }

    public ArrayList<ArrayList<Integer>> getFinalPathPositions() {
        return finalPathPositions;
    }

    public void setFinalPathPositions(ArrayList<ArrayList<Integer>> finalPathPositions) {
        this.finalPathPositions = finalPathPositions;
    }

    public boolean isWaitingForPawnSelection() {
        return waitingForPawnSelection;
    }

    public void setWaitingForPawnSelection(boolean waitingForPawnSelection) {
        this.waitingForPawnSelection = waitingForPawnSelection;
    }

    public int getSelectedPawn() {
        return selectedPawn;
    }

    public void setSelectedPawn(int selectedPawn) {
        this.selectedPawn = selectedPawn;
    }

    public ArrayList<Integer> getWinnerOrder() {
        return winnerOrder;
    }

    public void setWinnerOrder(ArrayList<Integer> winnerOrder) {
        this.winnerOrder = winnerOrder;
    }

    public int getWinnersCount() {
        return winnersCount;
    }

    public void setWinnersCount(int winnersCount) {
        this.winnersCount = winnersCount;
    }

    // ===== Game Logic Methods =====

    /**
     * Method to set up a test state where players are close to finishing
     */
    public void setupTestState() {
        // Reset the current state first
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome.get(player).set(pawn, false);
                pawnFinished.get(player).set(pawn, false);
                pawnOnFinishLine.get(player).set(pawn, false);
                pawnPositions.get(player).set(pawn, -1);
                finalPathPositions.get(player).set(pawn, -1);
            }
        }

        // RED player - one pawn already finished, three on finish line
        pawnFinished.get(RED_PLAYER).set(0, true);
        pawnOnFinishLine.get(RED_PLAYER).set(1, true);
        pawnOnFinishLine.get(RED_PLAYER).set(2, true);
        pawnOnFinishLine.get(RED_PLAYER).set(3, true);
        finalPathPositions.get(RED_PLAYER).set(1, 4);
        finalPathPositions.get(RED_PLAYER).set(2, 4);
        finalPathPositions.get(RED_PLAYER).set(3, 4);

        // GREEN player - two pawns already finished, two on finish line
        pawnFinished.get(GREEN_PLAYER).set(0, true);
        pawnFinished.get(GREEN_PLAYER).set(1, true);
        pawnOnFinishLine.get(GREEN_PLAYER).set(2, true);
        pawnOnFinishLine.get(GREEN_PLAYER).set(3, true);
        finalPathPositions.get(GREEN_PLAYER).set(2, 3);
        finalPathPositions.get(GREEN_PLAYER).set(3, 3);

        // YELLOW player - three pawns already finished, one on finish line
        pawnFinished.get(YELLOW_PLAYER).set(0, true);
        pawnFinished.get(YELLOW_PLAYER).set(1, true);
        pawnFinished.get(YELLOW_PLAYER).set(2, true);
        pawnOnFinishLine.get(YELLOW_PLAYER).set(3, true);
        finalPathPositions.get(YELLOW_PLAYER).set(3, 3);

        // BLUE player - all pawns on main track, but close to final path
        pawnPositions.get(BLUE_PLAYER).set(0, 36);
        pawnPositions.get(BLUE_PLAYER).set(1, 36);
        pawnPositions.get(BLUE_PLAYER).set(2, 35);
        pawnPositions.get(BLUE_PLAYER).set(3, 35);

        // Reset winners
        winnersCount = 0;
        for (int i = 0; i < 4; i++) {
            winnerOrder.set(i, -1);
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
            if (!pawnOnFinishLine.get(currentPlayerTurn).get(selectedPawn)) {
                checkForCaptures(currentPlayerTurn, pawnPositions.get(currentPlayerTurn).get(selectedPawn));
            }

            nextTurn();
        }
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
        return pawnInHome.get(player).get(pawnIndex);
    }

    /**
     * Check if a pawn has finished the game
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if pawn has finished
     */
    public boolean isPawnFinished(int player, int pawnIndex) {
        return pawnFinished.get(player).get(pawnIndex);
    }

    /**
     * Check if a pawn is on the finish line (final path)
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return True if pawn is on finish line
     */
    public boolean isPawnOnFinishLine(int player, int pawnIndex) {
        return pawnOnFinishLine.get(player).get(pawnIndex);
    }

    /**
     * Get the board position for a specific pawn
     * @param player Player index (0-3)
     * @param pawnIndex Pawn index (0-3)
     * @return Point with x,y coordinates on the board grid
     */
    public Point getPawnBoardPosition(int player, int pawnIndex) {
        // If pawn is in home, return its home coordinates
        if (pawnInHome.get(player).get(pawnIndex)) {
            return homeCoordinates.get(player).get(pawnIndex);
        }

        // If pawn has finished, return the center position
        if (pawnFinished.get(player).get(pawnIndex)) {
            return new Point(7, 7); // Center of board
        }

        // If pawn is on finish line, return its position in the final path
        if (pawnOnFinishLine.get(player).get(pawnIndex)) {
            int finalPathIndex = finalPathPositions.get(player).get(pawnIndex);
            return finalPathCoordinates.get(player).get(finalPathIndex);
        }

        // Otherwise, pawn is on the main track
        return getMainTrackCoordinates(pawnPositions.get(player).get(pawnIndex));
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
        if (pawnInHome.get(player).get(pawnIndex)) {
            // Can only exit home with a 6
            if (lastDiceRoll == EXIT_ROLL) {
                exitPawnFromHome(player, pawnIndex);
                moveMade = true;
                return true;
            }
            return false;
        }

        // Check if pawn has already finished
        if (pawnFinished.get(player).get(pawnIndex)) {
            return false;
        }

        // Check if pawn is already on finish line (final path)
        if (pawnOnFinishLine.get(player).get(pawnIndex)) {
            int currentFinalPathPosition = finalPathPositions.get(player).get(pawnIndex);
            int newFinalPathPosition = currentFinalPathPosition + lastDiceRoll;

            // Check if pawn reaches or exceeds end of final path
            if (newFinalPathPosition >= 5) {
                // Pawn has reached home!
                pawnFinished.get(player).set(pawnIndex, true);
                pawnOnFinishLine.get(player).set(pawnIndex, false);
                finalPathPositions.get(player).set(pawnIndex, -1);
            } else {
                // Pawn advances on final path
                finalPathPositions.get(player).set(pawnIndex, newFinalPathPosition);
            }

            moveMade = true;
            return true;
        }

        // Move pawn on the board
        int currentPosition = pawnPositions.get(player).get(pawnIndex);

        // Check if the pawn will cross or land on its final path entry point
        boolean willEnterFinalPath = false;
        int stepsIntoFinalPath = 0;

        // Define final path entry for current player
        int entryPoint = finalPathEntries.get(player);

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
            pawnOnFinishLine.get(player).set(pawnIndex, true);
            finalPathPositions.get(player).set(pawnIndex, stepsIntoFinalPath);
        } else {
            // Regular move on the main track
            int newPosition = (currentPosition + lastDiceRoll) % BOARD_SQUARES;
            pawnPositions.get(player).set(pawnIndex, newPosition);

            // Check if pawn landed on another pawn
            checkForCaptures(player, pawnPositions.get(player).get(pawnIndex));
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
        pawnInHome.get(player).set(pawnIndex, false);
        pawnPositions.get(player).set(pawnIndex, startPositions.get(player));

        // Check if pawn landed on another pawn
        checkForCaptures(player, startPositions.get(player));
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
                int currentPos = pawnPositions.get(player).get(pawn);
                if (!pawnInHome.get(player).get(pawn) && !pawnFinished.get(player).get(pawn) &&
                        !pawnOnFinishLine.get(player).get(pawn) && currentPos == position
                        && currentPos != 8 && currentPos !=21 && currentPos !=34 && currentPos != 47 ) {

                    if(player==0 && currentPos == 0) break;
                    if(player==1 && currentPos == 13) break;
                    if(player==2 && currentPos == 26) break;
                    if(player==3 && currentPos == 39) break;

                    // Send the pawn back home
                    pawnInHome.get(player).set(pawn, true);
                    pawnPositions.get(player).set(pawn, -1);
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
     * @return ArrayList with player indices in order of winning (-1 for not finished)
     */
    public ArrayList<Integer> getWinnerOrderList() {
        return new ArrayList<>(winnerOrder);
    }

    /**
     * Get the position a player finished in (1st, 2nd, 3rd, or not finished)
     * @param player Player index to check
     * @return Position (1, 2, 3) or 0 if not finished yet or 4 if last
     */
    public int getPlayerPosition(int player) {
        for (int i = 0; i < winnerOrder.size(); i++) {
            if (winnerOrder.get(i) == player) {
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
            if (!pawnFinished.get(player).get(pawn)) {
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
            if (winnerOrder.get(i) == player) {
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
                winnerOrder.set(winnersCount, player);
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
            if (!pawnInHome.get(player).get(pawn) && !pawnFinished.get(player).get(pawn)) {
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
                if (pawnInHome.get(currentPlayerTurn).get(pawn)) {
                    return true; // Can exit home with a 6
                }
            }
        }

        // Check pawns on board or on finish line
        for (int pawn = 0; pawn < 4; pawn++) {
            if ((!pawnInHome.get(currentPlayerTurn).get(pawn) && !pawnFinished.get(currentPlayerTurn).get(pawn)) ||
                    pawnOnFinishLine.get(currentPlayerTurn).get(pawn)) {
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
            info.append(winnerOrder.get(i)).append(", ");
        }
        info.append("Count: ").append(winnersCount);
        return info.toString();
    }

    /**
     * Get list of movable pawns for current player
     * @return ArrayList of pawn indices that can be moved
     */
    public ArrayList<Integer> getMovablePawns() {
        ArrayList<Integer> movablePawns = new ArrayList<>();

        if (!diceRolled || moveMade) {
            return movablePawns; // Empty list if no dice rolled or move already made
        }

        // Check pawns in home (can only move with a 6)
        if (lastDiceRoll == EXIT_ROLL) {
            for (int pawn = 0; pawn < 4; pawn++) {
                if (pawnInHome.get(currentPlayerTurn).get(pawn)) {
                    movablePawns.add(pawn);
                }
            }
        }

        // Check pawns on board or on finish line
        for (int pawn = 0; pawn < 4; pawn++) {
            if (!pawnInHome.get(currentPlayerTurn).get(pawn) && !pawnFinished.get(currentPlayerTurn).get(pawn)) {
                // Check if pawn on finish line can move
                if (pawnOnFinishLine.get(currentPlayerTurn).get(pawn)) {
                    int currentFinalPathPosition = finalPathPositions.get(currentPlayerTurn).get(pawn);
                    int newFinalPathPosition = currentFinalPathPosition + lastDiceRoll;
                    if (newFinalPathPosition <= 5) { // Can move within or to finish
                        movablePawns.add(pawn);
                    }
                } else {
                    // Pawn is on main board and can move
                    movablePawns.add(pawn);
                }
            }
        }

        return movablePawns;
    }

    /**
     * Get coordinates for all pawns of a specific player
     * @param player Player index (0-3)
     * @return ArrayList of Points representing pawn positions
     */
    public ArrayList<Point> getPlayerPawnCoordinates(int player) {
        ArrayList<Point> coordinates = new ArrayList<>();

        for (int pawn = 0; pawn < 4; pawn++) {
            coordinates.add(getPawnBoardPosition(player, pawn));
        }

        return coordinates;
    }

    /**
     * Get all active pawns (not in home, not finished) for a player
     * @param player Player index (0-3)
     * @return ArrayList of pawn indices that are active on the board
     */
    public ArrayList<Integer> getActivePawns(int player) {
        ArrayList<Integer> activePawns = new ArrayList<>();

        for (int pawn = 0; pawn < 4; pawn++) {
            if (!pawnInHome.get(player).get(pawn) && !pawnFinished.get(player).get(pawn)) {
                activePawns.add(pawn);
            }
        }

        return activePawns;
    }

    /**
     * Get home coordinates for a specific player
     * @param player Player index (0-3)
     * @return ArrayList of Points representing home positions
     */
    public ArrayList<Point> getPlayerHomeCoordinates(int player) {
        return new ArrayList<>(homeCoordinates.get(player));
    }

    /**
     * Get final path coordinates for a specific player
     * @param player Player index (0-3)
     * @return ArrayList of Points representing final path positions
     */
    public ArrayList<Point> getPlayerFinalPathCoordinates(int player) {
        return new ArrayList<>(finalPathCoordinates.get(player));
    }

    /**
     * Reset the game to initial state
     */
    public void resetGame() {
        currentPlayerTurn = RED_PLAYER;
        lastDiceRoll = 0;
        diceRolled = false;
        moveMade = false;
        waitingForPawnSelection = false;
        selectedPawn = -1;
        winnersCount = 0;

        // Reset all pawn states
        for (int player = 0; player < 4; player++) {
            for (int pawn = 0; pawn < 4; pawn++) {
                pawnInHome.get(player).set(pawn, true);
                pawnFinished.get(player).set(pawn, false);
                pawnOnFinishLine.get(player).set(pawn, false);
                pawnPositions.get(player).set(pawn, -1);
                finalPathPositions.get(player).set(pawn, -1);
            }
            winnerOrder.set(player, -1);
        }
    }

    /**
     * Get game state summary
     * @return String with current game state information
     */
    public String getGameStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Current Player: ").append(currentPlayerTurn).append("\n");
        summary.append("Last Dice Roll: ").append(lastDiceRoll).append("\n");
        summary.append("Dice Rolled: ").append(diceRolled).append("\n");
        summary.append("Move Made: ").append(moveMade).append("\n");
        summary.append("Waiting for Pawn Selection: ").append(waitingForPawnSelection).append("\n");
        summary.append("Winners Count: ").append(winnersCount).append("\n");

        for (int player = 0; player < 4; player++) {
            summary.append("Player ").append(player).append(" - ");
            summary.append("Pawns in Home: ").append(getPawnsInHome(player)).append(", ");
            summary.append("Pawns on Board: ").append(getPawnsOnBoard(player)).append(", ");
            summary.append("Pawns Finished: ").append(getPawnsFinished(player)).append("\n");
        }

        return summary.toString();
    }

    /**
     * Get number of pawns in home for a player
     * @param player Player index (0-3)
     * @return Number of pawns still in home
     */
    public int getPawnsInHome(int player) {
        int count = 0;
        for (int pawn = 0; pawn < 4; pawn++) {
            if (pawnInHome.get(player).get(pawn)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get number of finished pawns for a player
     * @param player Player index (0-3)
     * @return Number of pawns that have finished
     */
    public int getPawnsFinished(int player) {
        int count = 0;
        for (int pawn = 0; pawn < 4; pawn++) {
            if (pawnFinished.get(player).get(pawn)) {
                count++;
            }
        }
        return count;
    }
}