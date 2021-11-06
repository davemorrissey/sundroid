package uk.co.sundroid.util.view

import android.content.Context
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import kotlin.math.abs

/**
 * Detects drag actions on year/month/date buttons so the date can be changed with a swipe.
 */
class ButtonDragGestureDetector(private val listener: ButtonDragGestureDetectorListener, context: Context) : SimpleOnGestureListener() {

    private var thresholdDist = 15
    private var thresholdVel = 100

    init {
        thresholdDist = size(context, 15)
        thresholdVel = size(context, 100)
    }

    private fun size(context: Context, size: Int): Int {
        val metrics = context.resources.displayMetrics
        return (metrics.densityDpi / 160.0 * size).toInt()
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val dx = e1.x - e2.x
        val dy = e1.y - e2.y
        val vx = abs(velocityX)
        val vy = abs(velocityY)

        if (dx > thresholdDist && vx > vy && vx > thresholdVel) {
            listener.onButtonDragLeft()
            return true
        } else if (dx < -thresholdDist && vx > vy && vx > thresholdVel) {
            listener.onButtonDragRight()
            return true
        } else if (dy > thresholdDist && vy > thresholdVel) {
            listener.onButtonDragUp()
            return true
        } else if (dy < -thresholdDist && vy > thresholdVel) {
            listener.onButtonDragDown()
            return true
        }
        return false
    }

    interface ButtonDragGestureDetectorListener {
        fun onButtonDragUp()
        fun onButtonDragDown()
        fun onButtonDragLeft()
        fun onButtonDragRight()
    }

}
