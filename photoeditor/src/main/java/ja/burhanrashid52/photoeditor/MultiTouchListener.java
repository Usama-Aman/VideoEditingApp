package ja.burhanrashid52.photoeditor;

import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created on 18/01/2017.
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * <p></p>
 */
public class MultiTouchListener implements OnTouchListener {

    private static final int INVALID_POINTER_ID = -1;
    private final GestureDetector mGestureListener;
    private boolean isRotateEnabled = true;
    private boolean isTranslateEnabled = true;
    private boolean isScaleEnabled = true;
    private float minimumScale = 0.5f;
    private float maximumScale = 10.0f;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPrevX, mPrevY, mPrevRawX, mPrevRawY;
    private ScaleGestureDetector mScaleGestureDetector;

    private int[] location = new int[2];
    private Rect outRect;
    private View deleteView;
    private ImageView photoEditImageView;
    private RelativeLayout parentView;

    private OnMultiTouchListener onMultiTouchListener;
    private OnGestureControl mOnGestureControl;
    private boolean mIsTextPinchZoomable;
    private OnPhotoEditorListener mOnPhotoEditorListener;

    public float currentXposition;
    public float currentYposition;

    private PhotoEditorMoveListerner mphotoEditorMoveListerner;

    MultiTouchListener(@Nullable View deleteView, RelativeLayout parentView,
                       ImageView photoEditImageView, boolean isTextPinchZoomable,
                       OnPhotoEditorListener onPhotoEditorListener, PhotoEditorMoveListerner photoEditorMoveListerner) {
        mIsTextPinchZoomable = isTextPinchZoomable;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
        mGestureListener = new GestureDetector(new GestureListener());
        this.deleteView = deleteView;
        this.parentView = parentView;
        this.photoEditImageView = photoEditImageView;
        this.mOnPhotoEditorListener = onPhotoEditorListener;
        this.mphotoEditorMoveListerner = photoEditorMoveListerner;
        if (deleteView != null) {
            outRect = new Rect(deleteView.getLeft(), deleteView.getTop(),
                    deleteView.getRight(), deleteView.getBottom());
        } else {
            outRect = new Rect(0, 0, 0, 0);
        }
    }

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    public void move(View view, TransformInfo info) {
        computeRenderOffset(view, info.pivotX, info.pivotY);
        adjustTranslation(view, info.deltaX, info.deltaY);

        float scale = view.getScaleX() * info.deltaScale;
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
        view.setScaleX(scale);
        view.setScaleY(scale);

        float rotation = adjustAngle(view.getRotation() + info.deltaAngle);
        view.setRotation(rotation);
    }

    private void adjustTranslation(View view, float deltaX, float deltaY) {
        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);


        float viewCoard_X = view.getX() + (float) (view.getWidth()) / 2;
        float viewCoard_Y = view.getY() + (float) (view.getHeight()) / 2;

        float viewStartX = view.getX();
        float viewStartY = view.getY() - view.getHeight() / 2 - 20;

        int orignalWidth_X = parentView.getWidth();
        int orignalWidth_Y = parentView.getHeight();

        float percentageOfViewInParentXaxis = (viewCoard_X * 100) / orignalWidth_X;
        float percentageOfViewInParentYaxis = (viewCoard_Y * 100) / orignalWidth_Y;

        System.out.println("OrignalX = " + orignalWidth_X + "orignalY = " + orignalWidth_Y);

        System.out.println("View Normal X = " + view.getX() + "finalOut X = " + percentageOfViewInParentXaxis);
        System.out.println("View Normal Y = " + view.getY() + "finalOut Y = " + percentageOfViewInParentYaxis);


        mOnPhotoEditorListener.getViewCoordinates(
                deltaVector[0], deltaVector[1], view,
                parentView, ((ViewType) view.getTag()),
                percentageOfViewInParentXaxis, percentageOfViewInParentYaxis,
                viewStartX, viewStartY
        );
    }

    private void adjustTranslationOnDrag(View view, float deltaX, float deltaY, float percentageOfViewInParentXaxis, float percentageOfViewInParentYaxis, float viewCoard_X, float viewCoard_Y) {
        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);

        mOnPhotoEditorListener.getViewCoordinates(
                deltaVector[0], deltaVector[1], view,
                parentView, ((ViewType) view.getTag()),
                percentageOfViewInParentXaxis, percentageOfViewInParentYaxis,
                viewCoard_X, viewCoard_Y
        );

    }

    private static void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() == pivotX && view.getPivotY() == pivotY) {
            return;
        }

        float[] prevPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(prevPoint);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        float[] currPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(currPoint);

        float offsetX = currPoint[0] - prevPoint[0];
        float offsetY = currPoint[1] - prevPoint[1];

        view.setTranslationX(view.getTranslationX() - offsetX);
        view.setTranslationY(view.getTranslationY() - offsetY);

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(view, event);
        mGestureListener.onTouchEvent(event);

        if (!isTranslateEnabled) {
            return true;
        }

        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (action & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                mActivePointerId = event.getPointerId(0);
                if (deleteView != null) {
                    deleteView.setVisibility(View.VISIBLE);
                }
                view.bringToFront();
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                if (pointerIndexMove != -1) {
                    float currX = event.getX(pointerIndexMove);
                    float currY = event.getY(pointerIndexMove);


//                    System.out.println("Event X = " + event.getX() / ((double) parentView.getWidth()));
//                    System.out.println("Event Y = " + event.getY()/ ((double) parentView.getHeight()));

//                    int[] location = new int[2];
//                    view.getLocationInWindow(location);
//                    int majorViewLeft = location[0];
//                    int majorViewTop = location[1];
//                    System.out.println("X view" + view.getLeft());
//                    System.out.println("Y view" + view.getTop());

//                    Float[] location = new Float[2];
//                    location = getRelativePosition(view);
//                    System.out.println("X view" + location[0]);
//                    System.out.println("Y view" + location[1]);

                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    int majorViewLeft = location[0];
                    int majorViewTop = location[1];

//
//                    System.out.println("Major View left = " + ((double) majorViewLeft / ((double) view.getWidth())));
//                    System.out.println("Major View Top = " + ((double) majorViewTop / ((double) view.getHeight())));
//
//                    int[] location1 = new int[2];
//                    view.getLocationInWindow(location1);
//                    int childViewLeft = location1[0];
//                    int childViewTop = location1[1];
//
//                    System.out.println("Child View left = " + childViewLeft);
//                    System.out.println("Child View Top= " + childViewTop);


//                    double x_ = (((double) majorViewLeft / ((double) view.getWidth())) * 100) + view.getPivotX();
//                    double y_ = (((double) majorViewTop / ((double) view.getHeight())) * 100) + view.getPivotY();

//                    double x_ = ((double)view.getPivotX()/ ((double)view.getWidth()));
//                    double y_ =  ((double)view.getPivotY()/ ((double)view.getHeight()));

//                    System.out.println("Major View left = " + view.getWidth());
//                    System.out.println("Major View Top= " + view.getHeight());
//
//                    System.out.println("View Width = " + view.getWidth());
//                    System.out.println("View Height= " + view.getHeight());

//                    System.out.println("View Pivot X = " + view.getPivotX());
//                    System.out.println("View Pivot Y= " + view.getPivotY());

//                    double x_ = view.getLeft() /*/ majorViewLeft)*/ / (view.getWidth() / 2);
//                    double y_ = view.getTop() /*/ majorViewTop)*/ / (view.getHeight() / 2);
//
//                    System.out.println("X view = " + x_);
//                    System.out.println("Y view = " + y_);


                    firePhotoEditorSDKListener(view, true);


//                    System.out.println("X view" + (int) (currX - mPrevX));
//                    System.out.println("Y view" + (int) (currY - mPrevY));

                    if (!mScaleGestureDetector.isInProgress()) {
                        /*Checking if is in bound going to zero zero*/
                        if (!isViewInBounds(photoEditImageView, x, y)) {
                            view.animate().translationX(currX - mPrevX).translationX(currX - mPrevX);
                            view.animate().translationY(currY - mPrevY).translationY(currY - mPrevY);

                        } else {
//                            adjustTranslationOnDrag(
//                                    view, currX - mPrevX, currY - mPrevY,
//                                    percentageOfViewInParentXaxis, percentageOfViewInParentYaxis,
//                                    viewStartX, viewStartY
//                            );
//                            mOnPhotoEditorListener.getViewCoordinates(currX - mPrevX, currY - mPrevY,view,parentView);

                            adjustTranslation(view, currX - mPrevX, currY - mPrevY );

                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (deleteView != null && isViewInBounds(deleteView, x, y)) {
                    if (onMultiTouchListener != null)
                        onMultiTouchListener.onRemoveViewListener(view);
                } else if (!isViewInBounds(photoEditImageView, x, y)) {
                    view.animate().translationY(0).translationY(0);
                    view.animate().translationX(0).translationX(0);
                }
                if (deleteView != null) {
                    deleteView.setVisibility(View.GONE);
                }
//                TextView toolTip = view.findViewById(R.id.toolTipTextView);
//                toolTip.setVisibility(View.GONE);

                firePhotoEditorSDKListener(view, false);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndexPointerUp = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndexPointerUp);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndexPointerUp == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }
        return true;
    }

    private Float[] getRelativePosition(View v) {
        int[] locationInScreen = new int[2]; // view's position in scrren
        int[] parentLocationInScreen = new int[2]; // parent view's position in screen
        v.getLocationOnScreen(locationInScreen);
        View parentView = (View) v.getParent();
        parentView.getLocationOnScreen(parentLocationInScreen);
        float relativeX = locationInScreen[0] - parentLocationInScreen[0];
        float relativeY = locationInScreen[1] - parentLocationInScreen[1];

        Float[] locations = new Float[2];
        locations[0] = relativeX;
        locations[1] = relativeY;

        return locations;
    }

    private void firePhotoEditorSDKListener(View view, boolean isStart) {
        Object viewTag = view.getTag();
        if (mOnPhotoEditorListener != null && viewTag != null && viewTag instanceof ViewType) {
            if (isStart)
                mOnPhotoEditorListener.onStartViewChangeListener(((ViewType) view.getTag()), view.getTranslationX(), view.getTranslationY());
            else
                mOnPhotoEditorListener.onStopViewChangeListener(((ViewType) view.getTag()));
        }
    }

    private boolean isViewInBounds(View view, int x, int y) {
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    void setOnMultiTouchListener(OnMultiTouchListener onMultiTouchListener) {
        this.onMultiTouchListener = onMultiTouchListener;
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mPivotX;
        private float mPivotY;
        private Vector2D mPrevSpanVector = new Vector2D();

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return mIsTextPinchZoomable;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            TransformInfo info = new TransformInfo();
            info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
            info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
            info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
            info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;
            move(view, info);
            return !mIsTextPinchZoomable;
        }
    }

    private class TransformInfo {
        float deltaX;
        float deltaY;
        float deltaScale;
        float deltaAngle;
        float pivotX;
        float pivotY;
        float minimumScale;
        float maximumScale;
    }

    interface OnMultiTouchListener {
        void onEditTextClickListener(String text, int colorCode);

        void onRemoveViewListener(View removedView);
    }

    interface OnGestureControl {
        void onClick();

        void onLongClick();
    }

    void setOnGestureControl(OnGestureControl onGestureControl) {
        mOnGestureControl = onGestureControl;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnGestureControl != null) {
                mOnGestureControl.onClick();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            if (mOnGestureControl != null) {
                mOnGestureControl.onLongClick();
            }
        }
    }
}