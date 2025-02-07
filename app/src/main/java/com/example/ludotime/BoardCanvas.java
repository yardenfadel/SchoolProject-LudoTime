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
    Context context;
    private Point[] redPawnPositions;
    private Point[] greenPawnPositions;
    private Point[] bluePawnPositions;
    private Point[] yellowPawnPositions;
    private Bitmap[] redPawnBitmaps;
    private Bitmap[] greenPawnBitmaps;
    private Bitmap[] bluePawnBitmaps;
    private Bitmap[] yellowPawnBitmaps;

    private int selectedPawnIndex = -1; //0 to 3
    private int selectedPawnColor = -1; //0 to 3 is red, green, blue, yellow


    public BoardCanvas(Context context) {
        super(context);
        this.context = context;

        redPawnPositions = new Point[4];
        redPawnPositions[0] = new Point(2,2);
        redPawnPositions[1] = new Point(3,2);
        redPawnPositions[2] = new Point(2,3);
        redPawnPositions[3] = new Point(3,3);

        greenPawnPositions = new Point[4];
        greenPawnPositions[0] = new Point(11,2);
        greenPawnPositions[1] = new Point(12,2);
        greenPawnPositions[2] = new Point(11,3);
        greenPawnPositions[3] = new Point(12,3);

        bluePawnPositions = new Point[4];
        bluePawnPositions[0] = new Point(2,11);
        bluePawnPositions[1] = new Point(3,11);
        bluePawnPositions[2] = new Point(2,12);
        bluePawnPositions[3] = new Point(3,12);

        yellowPawnPositions = new Point[4];
        yellowPawnPositions[0] = new Point(11,11);
        yellowPawnPositions[1] = new Point(12,11);
        yellowPawnPositions[2] = new Point(11,12);
        yellowPawnPositions[3] = new Point(12,12);


        setClickable(true);
        redPawnBitmaps = new Bitmap[4];
        greenPawnBitmaps = new Bitmap[4];
        bluePawnBitmaps = new Bitmap[4];
        yellowPawnBitmaps = new Bitmap[4];

        for(int i = 0; i < 4; i++) redPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.red_pawn);
        for(int i = 0; i < 4; i++) greenPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.green_pawn);
        for(int i = 0; i < 4; i++) bluePawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.blue_pawn);
        for(int i = 0; i < 4; i++) yellowPawnBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_pawn);
    }

    private int getPixelsCordX(Canvas canvas, int x) {
        return (int)(canvas.getWidth() * ((float)(x + 0.5) / 16));
    }

    private int getPixelsCordY(Canvas canvas, int y) {
        return (int)(canvas.getHeight() * ((float)(y + 0.5) / 16));
    }

    private int getGridPixelX(Canvas canvas, int x) {
        return (int)(canvas.getWidth() * ((float)x / 16));
    }

    private int getGridPixelY(Canvas canvas, int y) {
        return (int)(canvas.getHeight() * ((float)y / 16));
    }

    private Point getBoardPosition(float touchX, float touchY) {
        int boardX = (int)(touchX * 16f / getWidth()-0.5);
        int boardY = (int)(touchY * 16f / getHeight()-0.5);
        return new Point(boardX, boardY);
    }

    private boolean isNearPawn(Point touch, Point pawn) {
        double distance = Math.sqrt(
                Math.pow(touch.x - pawn.x, 2) +
                        Math.pow(touch.y - pawn.y, 2)
        );
        return distance <= 0.5;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        Point touchPoint = getBoardPosition(event.getX(), event.getY());

        if (selectedPawnIndex == -1 || selectedPawnColor == -1) {
            selectedPawnColor=-1;
            selectedPawnIndex=-1;
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, redPawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=0;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, greenPawnPositions[i])) {
                    selectedPawnIndex = i;
                    selectedPawnColor = 1;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, bluePawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=2;
                }
            }
            for (int i = 0; i < 4; i++){
                if(isNearPawn(touchPoint, yellowPawnPositions[i])){
                    selectedPawnIndex=i;
                    selectedPawnColor=3;
                }
            }
        }
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int incx = canvas.getWidth()/16;
        float aspectRatio = (float) yellowPawnBitmaps[0].getHeight() / yellowPawnBitmaps[0].getWidth();
        int desiredWidth = incx;
        int desiredHeight = (int) (desiredWidth * aspectRatio);
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(redPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, redPawnPositions[i].x),
                    getPixelsCordY(canvas, redPawnPositions[i].y), null);
        }
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(greenPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, greenPawnPositions[i].x),
                    getPixelsCordY(canvas, greenPawnPositions[i].y), null);
        }
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(bluePawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, bluePawnPositions[i].x),
                    getPixelsCordY(canvas, bluePawnPositions[i].y), null);
        }
        for (int i = 0; i < 4; i++){
            Bitmap resized = Bitmap.createScaledBitmap(yellowPawnBitmaps[i], desiredWidth, desiredHeight, true);
            canvas.drawBitmap(resized, getPixelsCordX(canvas, yellowPawnPositions[i].x),
                    getPixelsCordY(canvas, yellowPawnPositions[i].y), null);
        }

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