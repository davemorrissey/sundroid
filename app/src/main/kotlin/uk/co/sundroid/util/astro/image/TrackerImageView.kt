package uk.co.sundroid.util.astro.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import androidx.appcompat.widget.AppCompatImageView

class TrackerImageView(context: Context) : AppCompatImageView(context) {

    private var direction = 0f
    private var trackerImage: TrackerImage? = null
    private val center: Point = Point(Int.MIN_VALUE, Int.MIN_VALUE)

    public override fun onDraw(canvas: Canvas) {
        val image = trackerImage ?: return
        val width = this.width
        val height = this.height
        if (center.x == Int.MIN_VALUE) {
            center.set(width/2, height/2)
        }
        canvas.rotate(-direction, width/2F, height/2F)
        image.drawOnCanvas(this, canvas, center.x.toFloat(), center.y.toFloat())
        super.onDraw(canvas)
    }

    fun setTrackerImage(trackerImage: TrackerImage) {
        this.trackerImage = trackerImage
        this.postInvalidate()
    }

    fun setDirection(direction: Float) {
        this.direction = direction
        this.postInvalidate()
    }

    fun setCenter(center: Point) {
        this.center.set(center.x, center.y)
        this.postInvalidate()
    }

}