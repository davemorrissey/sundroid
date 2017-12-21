package uk.co.sundroid.util.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Detects drag actions on year/month/date buttons so the date can be changed with a swipe.
 */
public class ButtonDragGestureDetector extends SimpleOnGestureListener {
    
    private int thresholdDist = 15;
    private int thresholdVel = 100;
    
    private ButtonDragGestureDetectorListener listener;
    
    public ButtonDragGestureDetector(ButtonDragGestureDetectorListener listener, Context context) {
        thresholdDist = size(context, 15);
        thresholdVel = size(context, 100);
        this.listener = listener;
    }

    private int size(Context context, int size) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int)((metrics.densityDpi/160d) * size);
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float dx = e1.getX() - e2.getX();
        float dy = e1.getY() - e2.getY();
        float vx = Math.abs(velocityX);
        float vy = Math.abs(velocityY);
        
        if (dx > thresholdDist && vx > vy && vx > thresholdVel) {
            listener.onButtonDragLeft();
            return true;
        } else if (dx < -thresholdDist && vx > vy && vx > thresholdVel) {
            listener.onButtonDragRight();
            return true;
        } else if (dy > thresholdDist && vy > thresholdVel) {
            listener.onButtonDragUp();
            return true;
        } else if (dy < -thresholdDist && vy > thresholdVel) {
            listener.onButtonDragDown();
            return true;
        }
        return false;
    }

    public interface ButtonDragGestureDetectorListener {
        void onButtonDragUp();
        void onButtonDragDown();
        void onButtonDragLeft();
        void onButtonDragRight();
    }
    
}
