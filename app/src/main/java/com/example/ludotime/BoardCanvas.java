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
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.example.ludotime.R;

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
     * @param context  Application context
     * @param testMode (false for normal game)
     */
    public BoardCanvas(Context context, boolean testMode) {
        super(context);
        this.context = context;
        gameLogic = new GameLogic(testMode);
        PawnPositions = new Point[4][4];

        if (!testMode) {
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
        } else {

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
        for (int i = 0; i < 4; i++)
            PawnBitmaps[RED_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.red_pawn);
        for (int i = 0; i < 4; i++)
            PawnBitmaps[GREEN_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.green_pawn);
        for (int i = 0; i < 4; i++)
            PawnBitmaps[BLUE_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.blue_pawn);
        for (int i = 0; i < 4; i++)
            PawnBitmaps[YELLOW_PLAYER][i] = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_pawn);
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
     *
     * @return the logic class
     */
    GameLogic getLogic() {
        return gameLogic;
    }

    /**
     * Convert grid X coordinate to pixel X coordinate (centered)
     */
    private int getPixelsCordX(Canvas canvas, int x) {
        return (int) (canvas.getWidth() * ((float) (x + 0.5) / 16));
    }

    /**
     * Convert grid Y coordinate to pixel Y coordinate (centered)
     */
    private int getPixelsCordY(Canvas canvas, int y) {
        return (int) (canvas.getHeight() * ((float) (y + 0.5) / 16));
    }

    /**
     * Convert grid X coordinate to pixel X coordinate (top-left corner)
     */
    private int getGridPixelX(Canvas canvas, int x) {
        return (int) (canvas.getWidth() * ((float) x / 16));
    }

    /**
     * Convert grid Y coordinate to pixel Y coordinate (top-left corner)
     */
    private int getGridPixelY(Canvas canvas, int y) {
        return (int) (canvas.getHeight() * ((float) y / 16));
    }

    /**
     * Convert pixel coordinates to board grid position
     */
    private Point getBoardPosition(float touchX, float touchY) {
        int boardX = (int) (touchX * 16f / getWidth() - 0.5);
        int boardY = (int) (touchY * 16f / getHeight() - 0.5);
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
            int currentPlayer = gameLogic.getCurrentPlayerTurn();

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

        int incx = canvas.getWidth() / 16;
        float aspectRatio = (float) PawnBitmaps[3][0].getHeight() / PawnBitmaps[3][0].getWidth();
        int desiredWidth = incx;
        int desiredHeight = (int) (desiredWidth * aspectRatio);

        // First, identify pawns on the same square
        int[][] pawnCountOnSquare = new int[16][16]; // Grid size is 16x16
        int[][][] pawnIndexOnSquare = new int[16][16][16]; // Store up to 16 pawns per square
        int[][][] pawnColorOnSquare = new int[16][16][16]; // Store the color of each pawn

        // Count pawns per square and store their indices
        for (int color = 0; color < 4; color++) {
            for (int i = 0; i < 4; i++) {
                Point pos = PawnPositions[color][i];
                if (pos != null && pos.x >= 0 && pos.x < 16 && pos.y >= 0 && pos.y < 16) {
                    int count = pawnCountOnSquare[pos.x][pos.y];
                    pawnIndexOnSquare[pos.x][pos.y][count] = i;
                    pawnColorOnSquare[pos.x][pos.y][count] = color;
                    pawnCountOnSquare[pos.x][pos.y]++;
                }
            }
        }

        // Draw pawns with offsets when multiple pawns are on the same square
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int count = pawnCountOnSquare[x][y];
                if (count > 0) {
                    // Calculate size reduction factor based on pawn count
                    float scaleFactor = count > 1 ? 0.7f : 1.0f;
                    int scaledWidth = (int) (desiredWidth * scaleFactor);
                    int scaledHeight = (int) (desiredHeight * scaleFactor);

                    for (int idx = 0; idx < count; idx++) {
                        int pawnIndex = pawnIndexOnSquare[x][y][idx];
                        int pawnColor = pawnColorOnSquare[x][y][idx];

                        // Calculate offset based on position in stack
                        float offsetX = 0;
                        float offsetY = 0;

                        if (count == 2) {
                            // Two pawns: diagonal arrangement
                            offsetX = idx == 0 ? -0.2f * incx : 0.2f * incx;
                            offsetY = idx == 0 ? -0.2f * incx : 0.2f * incx;
                        } else if (count == 3) {
                            // Three pawns: triangular arrangement
                            switch (idx) {
                                case 0:
                                    offsetX = -0.25f * incx;
                                    offsetY = 0;
                                    break;
                                case 1:
                                    offsetX = 0.25f * incx;
                                    offsetY = 0;
                                    break;
                                case 2:
                                    offsetX = 0;
                                    offsetY = 0.25f * incx;
                                    break;
                            }
                        } else if (count >= 4) {
                            // Four or more pawns: grid-like arrangement
                            switch (idx % 4) {
                                case 0:
                                    offsetX = -0.25f * incx;
                                    offsetY = -0.25f * incx;
                                    break;
                                case 1:
                                    offsetX = 0.25f * incx;
                                    offsetY = -0.25f * incx;
                                    break;
                                case 2:
                                    offsetX = -0.25f * incx;
                                    offsetY = 0.25f * incx;
                                    break;
                                case 3:
                                    offsetX = 0.25f * incx;
                                    offsetY = 0.25f * incx;
                                    break;
                            }
                            // For more than 4 pawns, add additional vertical offset
                            if (idx >= 4) {
                                offsetY += 0.1f * incx * (idx / 4);
                            }
                        }

                        Bitmap resized = Bitmap.createScaledBitmap(
                                PawnBitmaps[pawnColor][pawnIndex],
                                scaledWidth,
                                scaledHeight,
                                true
                        );

                        // Center point + offset - half bitmap width/height for proper centering
                        float drawX = getPixelsCordX(canvas, x) + offsetX - scaledWidth / 2f +0.5f*incx;
                        float drawY = getPixelsCordY(canvas, y) + offsetY - scaledHeight / 2f +0.5f*incx;

                        canvas.drawBitmap(resized, drawX, drawY, null);
                    }
                }
            }
        }

        // Draw highlight around selected pawn if game is waiting for selection
        if (gameLogic.isWaitingForPawnSelection()) {
            int currentPlayer = gameLogic.getCurrentPlayerTurn();
            Paint highlightPaint = new Paint();
            highlightPaint.setStyle(Paint.Style.STROKE);
            highlightPaint.setColor(Color.WHITE);
            highlightPaint.setStrokeWidth(5);

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

                    // Get the grid cell dimensions
                    float cellSize = canvas.getWidth() / 16f;
                    float x = getGridPixelX(canvas, position.x + 1);
                    float y = getGridPixelY(canvas, position.y + 1);

                    // Check if multiple pawns are on this square
                    Point pos = PawnPositions[currentPlayer][i];
                    int count = pawnCountOnSquare[pos.x][pos.y];

                    if (count > 1) {
                        // Draw a square highlight for multiple pawns
                        float padding = cellSize * 0.1f; // Add some padding around the square
                        RectF rect = new RectF(
                                x - cellSize/2 - padding,
                                y - cellSize/2 - padding,
                                x + cellSize/2 + padding,
                                y + cellSize/2 + padding
                        );
                        canvas.drawRect(rect, highlightPaint);
                    } else {
                        // Draw a circle highlight for a single pawn
                        float radius = cellSize / 2;
                        canvas.drawCircle(x, y, radius, highlightPaint);
                    }
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

                float x = getGridPixelX(canvas, selectedPosition.x + 1);
                float y = getGridPixelY(canvas, selectedPosition.y + 1);

                // Check if multiple pawns are on this square
                int count = pawnCountOnSquare[selectedPosition.x][selectedPosition.y];

                if (count > 1) {
                    // Draw a square highlight for multiple pawns
                    float cellSize = canvas.getWidth() / 16f;
                    float padding = cellSize * 0.1f; // Add some padding around the square
                    RectF rect = new RectF(
                            x - cellSize/2 - padding,
                            y - cellSize/2 - padding,
                            x + cellSize/2 + padding,
                            y + cellSize/2 + padding
                    );
                    canvas.drawRect(rect, highlightPaint);
                } else {
                    // Draw a circle highlight for a single pawn
                    float radius = canvas.getWidth() / 32;
                    canvas.drawCircle(x, y, radius, highlightPaint);
                }
            }
        }
    }
}