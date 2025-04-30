/**
 * BoardCanvas.java
 *
 * Custom View class that handles the rendering and interaction with the Ludo game board.
 * Manages the positions and drawing of pawns for all four players.
 */
package com.example.ludotime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class BoardCanvas extends View {
    // ===== Context =====
    Context context;

    //===== Reference to Logic Class =====
    private GameLogic gameLogic;

    //===== Pawn Constants =====
    public static final int RED_PLAYER = 0;
    public static final int GREEN_PLAYER = 1;
    public static final int YELLOW_PLAYER = 2;
    public static final int BLUE_PLAYER = 3;

    // ===== Pawn Positions =====
    private Point[][] PawnPositions; //player, 0-3

    // ===== Pawn Bitmaps =====
    private Bitmap[][] PawnBitmaps; //player, 0-3


    // ===== Pawn Selection =====
    private int selectedPawnIndex = -1; //0 to 3
    private int selectedPawnColor = -1; //0 to 3 is red, green, blue, yellow

    /**
     * Constructor initializes the game board with pawns in their starting positions
     *
     * @param context Application context
     * @param testMode (false for normal game)
     */
    public BoardCanvas(Context context, boolean testMode) {
        super(context);
        this.context = context;
        gameLogic = new GameLogic(testMode);
        PawnPositions = new Point[4][4];

        if(!testMode) {
            // Initialize red pawn positions
            PawnPositions[RED_PLAYER][0] = new Point(2, 2);
            PawnPositions[RED_PLAYER][1] = new Point(3, 2);
            PawnPositions[RED_PLAYER][2] = new Point(2, 3);
            PawnPositions[RED_PLAYER][3] = new Point(3, 3);

            // Initialize green pawn positions
            PawnPositions[GREEN_PLAYER][0] = new Point(11, 2);
            PawnPositions[GREEN_PLAYER][1] = new Point(12, 2);
            PawnPositions[GREEN_PLAYER][2] = new Point(11, 3);
            PawnPositions[GREEN_PLAYER][3] = new Point(12, 3);

            // Initialize blue pawn positions
            PawnPositions[BLUE_PLAYER][0] = new Point(2, 11);
            PawnPositions[BLUE_PLAYER][1] = new Point(3, 11);
            PawnPositions[BLUE_PLAYER][2] = new Point(2, 12);
            PawnPositions[BLUE_PLAYER][3] = new Point(3, 12);

            // Initialize yellow pawn positions
            PawnPositions[YELLOW_PLAYER][0] = new Point(11, 11);
            PawnPositions[YELLOW_PLAYER][1] = new Point(12, 11);
            PawnPositions[YELLOW_PLAYER][2] = new Point(11, 12);
            PawnPositions[YELLOW_PLAYER][3] = new Point(12, 12);
        }
        else{

            PawnPositions[RED_PLAYER][0] = new Point(2, 8);
            PawnPositions[RED_PLAYER][1] = new Point(1, 8);
            PawnPositions[RED_PLAYER][2] = new Point(0, 8);
            PawnPositions[RED_PLAYER][3] = new Point(0, 7);

            // Initialize green pawn positions
            PawnPositions[GREEN_PLAYER][0] = new Point(6, 2);
            PawnPositions[GREEN_PLAYER][1] = new Point(6, 1);
            PawnPositions[GREEN_PLAYER][2] = new Point(6, 0);
            PawnPositions[GREEN_PLAYER][3] = new Point(7, 0);

            // Initialize yellow pawn positions
            PawnPositions[YELLOW_PLAYER][0] = new Point(12, 6);
            PawnPositions[YELLOW_PLAYER][1] = new Point(13, 6);
            PawnPositions[YELLOW_PLAYER][2] = new Point(14, 6);
            PawnPositions[YELLOW_PLAYER][3] = new Point(14, 7);

            // Initialize blue pawn positions
            PawnPositions[BLUE_PLAYER][0] = new Point(8, 12);
            PawnPositions[BLUE_PLAYER][1] = new Point(8, 13);
            PawnPositions[BLUE_PLAYER][2] = new Point(8, 14);
            PawnPositions[BLUE_PLAYER][3] = new Point(7, 14);

        }
        // Enable touch events
        setClickable(true);

        // Initialize bitmap arrays
        PawnBitmaps = new Bitmap[4][4];

        // Load pawn bitmap resources
        for(int i = 0; i < 4; i++) PawnBitmaps[RED_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.red_pawn);
        for(int i = 0; i < 4; i++) PawnBitmaps[GREEN_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.green_pawn);
        for(int i = 0; i < 4; i++) PawnBitmaps[BLUE_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.blue_pawn);
        for(int i = 0; i < 4; i++) PawnBitmaps[YELLOW_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_pawn);
    }

    /**
     * Constructor initializes the game board with pawns in their starting positions
     *
     * @param context Application context
     */
    public BoardCanvas(Context context) {
        this(context, false);
    }

    /**
     * Get a reference to the logic class
     * @return the logic class
     */
    GameLogic getLogic(){
        return gameLogic;
    }

    /**
     * Convert grid X coordinate to pixel X coordinate (centered)
     */
    private int getPixelsCordX(Canvas canvas, int x) {
        return (int)(canvas.getWidth() * ((float)(x + 0.5) / 16));
    }

    /**
     * Convert grid Y coordinate to pixel Y coordinate (centered)
     */
    private int getPixelsCordY(Canvas canvas, int y) {
        return (int)(canvas.getHeight() * ((float)(y + 0.5) / 16));
    }

    /**
     * Convert grid X coordinate to pixel X coordinate (top-left corner)
     */
    private int getGridPixelX(Canvas canvas, int x) {
        return (int)(canvas.getWidth() * ((float)x / 16));
    }

    /**
     * Convert grid Y coordinate to pixel Y coordinate (top-left corner)
     */
    private int getGridPixelY(Canvas canvas, int y) {
        return (int)(canvas.getHeight() * ((float)y / 16));
    }

    /**
     * Convert pixel coordinates to board grid position
     */
    private Point getBoardPosition(float touchX, float touchY) {
        int boardX = (int)(touchX * 16f / getWidth()-0.5);
        int boardY = (int)(touchY * 16f / getHeight()-0.5);
        return new Point(boardX, boardY);
    }

    /**
     * Check if touch point is near a pawn
     */
    private boolean isNearPawn(Point touch, Point pawn) {
        double distance = Math.sqrt(
                Math.pow(touch.x - pawn.x, 2) +
                        Math.pow(touch.y - pawn.y, 2)
        );
        return distance <= 0.5;
    }

    /**
     * Handle touch events on the game board
     * Handles pawn selection when game logic is waiting for it
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        Point touchPoint = getBoardPosition(event.getX(), event.getY());

        // Check if we're waiting for pawn selection during a game round
        if (gameLogic.isWaitingForPawnSelection()) {
            // Only allow selection of current player's pawns
            int currentPlayer = gameLogic.getCurrentPlayer();

            for (int i = 0; i < 4; i++) {
                if (isNearPawn(touchPoint, PawnPositions[currentPlayer][i])) {
                    // Set the selected pawn in game logic
                    gameLogic.setPawnSelection(i);

                    // Redraw the board
                    invalidate();
                    return true;
                }
            }

            return true;
        }

        // Handle default behavior otherwise (for debugging/testing)
        // This code would only run when not actively playing a round

        invalidate();
        return true;
    }

    /**
     * Draw the game board and all pawns
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // First, synchronize pawn positions with the game logic
        for (int color = 0; color < 4; color++) {
            for (int i = 0; i < 4; i++) {
                // Get the current position from game logic
                Point logicPosition = gameLogic.getPawnBoardPosition(color, i);
                // Update our position tracking array
                if (logicPosition != null) {
                    PawnPositions[color][i] = logicPosition;
                }
            }
        }

        int incx = canvas.getWidth()/16;
        float aspectRatio = (float) PawnBitmaps[3][0].getHeight() / PawnBitmaps[3][0].getWidth();
        int desiredWidth = incx;
        int desiredHeight = (int) (desiredWidth * aspectRatio);

        // Draw pawns
        for (int color = 0; color < 4; color++) {
            for (int i = 0; i < 4; i++) {
                Bitmap resized = Bitmap.createScaledBitmap(PawnBitmaps[color][i], desiredWidth, desiredHeight, true);
                canvas.drawBitmap(resized, getPixelsCordX(canvas, PawnPositions[color][i].x),
                        getPixelsCordY(canvas, PawnPositions[color][i].y), null);
            }
        }

        // Draw highlight around selected pawn if game is waiting for selection
        if (gameLogic.isWaitingForPawnSelection()) {
            int currentPlayer = gameLogic.getCurrentPlayer();

            // Highlight movable pawns
            for (int i = 0; i < 4; i++) {
                boolean canMove = false;

                // Check if this pawn can be moved
                if (gameLogic.isPawnInHome(currentPlayer, i) && gameLogic.getLastDiceRoll() == 6) {
                    canMove = true;
                } else if (!gameLogic.isPawnInHome(currentPlayer, i) && !gameLogic.isPawnFinished(currentPlayer, i)) {
                    canMove = true;
                }

                if (canMove) {
                    Point position = PawnPositions[currentPlayer][i];
                    Paint highlightPaint = new Paint();
                    highlightPaint.setStyle(Paint.Style.STROKE);
                    highlightPaint.setColor(Color.WHITE);
                    highlightPaint.setStrokeWidth(5);

                    float x = getGridPixelX(canvas, position.x+1);
                    float y = getGridPixelY(canvas, position.y+1);
                    float radius = canvas.getWidth() / 32;
                    canvas.drawCircle(x, y, radius, highlightPaint);
                }
            }
        }

        // Draw highlight around selected pawn (existing code)
        if (selectedPawnIndex != -1 && selectedPawnColor != -1) {
            Point selectedPosition = PawnPositions[selectedPawnColor][selectedPawnIndex];
            if (selectedPosition != null) {
                Paint highlightPaint = new Paint();
                highlightPaint.setStyle(Paint.Style.STROKE);
                highlightPaint.setColor(Color.WHITE);
                highlightPaint.setStrokeWidth(5);

                float x = getGridPixelX(canvas, selectedPosition.x+1);
                float y = getGridPixelY(canvas, selectedPosition.y+1);
                float radius = canvas.getWidth() / 32;
                canvas.drawCircle(x, y, radius, highlightPaint);
            }
        }
    }
}