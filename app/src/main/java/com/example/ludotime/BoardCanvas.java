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

    //===== Pawn Constants =====
    public static final int RED_PLAYER = 0;
    public static final int GREEN_PLAYER = 1;
    public static final int BLUE_PLAYER = 2;
    public static final int YELLOW_PLAYER = 3;

    // ===== Pawn Positions =====
    private Point[] redPawnPositions;
    private Point[] greenPawnPositions;
    private Point[] bluePawnPositions;
    private Point[] yellowPawnPositions;

    // ===== Pawn Bitmaps =====
    private Bitmap[] redPawnBitmaps;
    private Bitmap[] greenPawnBitmaps;
    private Bitmap[] bluePawnBitmaps;
    private Bitmap[] yellowPawnBitmaps;

    // ===== Pawn Selection =====
    private int selectedPawnIndex = -1; //0 to 3
    private int selectedPawnColor = -1; //0 to 3 is red, green, blue, yellow

    /**
     * Constructor initializes the game board with pawns in their starting positions
     *
     * @param context Application context
     */
    public BoardCanvas(Context context) {
        super(context);
        this.context = context;

        // Initialize red pawn positions
        redPawnPositions = new Point[4];
        redPawnPositions[0] = new Point(2,2);
        redPawnPositions[1] = new Point(3,2);
        redPawnPositions[2] = new Point(2,3);
        redPawnPositions[3] = new Point(3,3);

        // Initialize green pawn positions
        greenPawnPositions = new Point[4];
        greenPawnPositions[0] = new Point(11,2);
        greenPawnPositions[1] = new Point(12,2);
        greenPawnPositions[2] = new Point(11,3);
        greenPawnPositions[3] = new Point(12,3);

        // Initialize blue pawn positions
        bluePawnPositions = new Point[4];
        bluePawnPositions[0] = new Point(2,11);
        bluePawnPositions[1] = new Point(3,11);
        bluePawnPositions[2] = new Point(2,12);
        bluePawnPositions[3] = new Point(3,12);

        // Initialize yellow pawn positions
        yellowPawnPositions = new Point[4];
        yellowPawnPositions[0] = new Point(11,11);
        yellowPawnPositions[1] = new Point(12,11);
        yellowPawnPositions[2] = new Point(11,12);
        yellowPawnPositions[3] = new Point(12,12);

        // Enable touch events
        setClickable(true);

        // Initialize bitmap arrays
        redPawnBitmaps = new Bitmap[4];
        greenPawnBitmaps = new Bitmap[4];
        bluePawnBitmaps = new Bitmap[4];
        yellowPawnBitmaps = new Bitmap[4];

        // Load pawn bitmap resources
        for(int i = 0; i < 4; i++) redPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.red_pawn);
        for(int i = 0; i < 4; i++) greenPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.green_pawn);
        for(int i = 0; i < 4; i++) bluePawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.blue_pawn);
        for(int i = 0; i < 4; i++) yellowPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_pawn);
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
     * First touch selects a pawn, second touch moves the selected pawn
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        Point touchPoint = getBoardPosition(event.getX(), event.getY());

        // If no pawn is selected, try to select one
        if (selectedPawnIndex == -1 || selectedPawnColor == -1) {
            selectedPawnColor=-1;
            selectedPawnIndex=-1;

            // Check all pawns to see if one was touched
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, redPawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=RED_PLAYER;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, greenPawnPositions[i])) {
                    selectedPawnIndex = i;
                    selectedPawnColor = GREEN_PLAYER;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, bluePawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=BLUE_PLAYER;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, yellowPawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=YELLOW_PLAYER;
                }
            }
        }
        // If a pawn is already selected, move it
        else {
            switch (selectedPawnColor) {
                case 0:
                    redPawnPositions[selectedPawnIndex] = touchPoint;
                    break;
                case 1:
                    greenPawnPositions[selectedPawnIndex] = touchPoint;
                    break;
                case 2:
                    bluePawnPositions[selectedPawnIndex] = touchPoint;
                    break;
                case 3:
                    yellowPawnPositions[selectedPawnIndex] = touchPoint;
                    break;
            }
            selectedPawnIndex = -1;
            selectedPawnColor = -1;
        }

        invalidate();
        return true;
    }

    /**
     * Draw the game board and all pawns
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int incx = canvas.getWidth()/16;
        float aspectRatio = (float) yellowPawnBitmaps[0].getHeight() / yellowPawnBitmaps[0].getWidth();
        int desiredWidth = incx;
        int desiredHeight = (int) (desiredWidth * aspectRatio);

        // Draw red pawns
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(redPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, redPawnPositions[i].x),
                    getPixelsCordY(canvas, redPawnPositions[i].y), null);
        }

        // Draw green pawns
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(greenPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, greenPawnPositions[i].x),
                    getPixelsCordY(canvas, greenPawnPositions[i].y), null);
        }

        // Draw blue pawns
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(bluePawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, bluePawnPositions[i].x),
                    getPixelsCordY(canvas, bluePawnPositions[i].y), null);
        }

        // Draw yellow pawns
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(yellowPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, yellowPawnPositions[i].x),
                    getPixelsCordY(canvas, yellowPawnPositions[i].y), null);
        }

        // Draw highlight around selected pawn
        if (selectedPawnIndex != -1 && selectedPawnColor != -1) {
            Point selectedPosition = null;
            switch (selectedPawnColor) {
                case 0: selectedPosition = redPawnPositions[selectedPawnIndex]; break;
                case 1: selectedPosition = greenPawnPositions[selectedPawnIndex]; break;
                case 2: selectedPosition = bluePawnPositions[selectedPawnIndex]; break;
                case 3: selectedPosition = yellowPawnPositions[selectedPawnIndex]; break;
            }
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