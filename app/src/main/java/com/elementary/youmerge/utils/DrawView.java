package com.elementary.youmerge.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.elementary.youmerge.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DrawView extends View implements OnTouchListener {
    public Canvas mCanvas;
    public Path mPath;
    public Paint mPaint;
    public ArrayList<Path> paths = new ArrayList<Path>();
    public ArrayList<Path> undonePaths = new ArrayList<Path>();

    public Bitmap im;
    public Bitmap canvasBitMap;
    public Bitmap emptyBitMap;

    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(ContextCompat.getColor(context, R.color.red));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        mCanvas = new Canvas();
        mPath = new Path();
        paths.add(mPath);

        im = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
//        disableDraw();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (Path p : paths) {
            canvas.drawPath(p, mPaint);
        }
    }

    public void disableDraw() {
        setFocusable(false);
        setFocusableInTouchMode(false);
        setOnTouchListener(null);
    }

    public void enableDraw() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath = new Path();
        paths.add(mPath);
    }

    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {

        }
        //toast the user
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        } else {

        }
        //toast the user
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public File save(@NonNull File file) {
        try {
            return saveImageToGallery(file, createBitmap());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    @NonNull
    public Bitmap createBitmap() throws OutOfMemoryError {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    public static File saveImageToGallery(@NonNull File file, @NonNull Bitmap bmp) {
        if (bmp == null) {
            throw new IllegalArgumentException("bmp should not be null");
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("DrawView", "Draw View Image" + file.getAbsolutePath());
        return file;
    }

}