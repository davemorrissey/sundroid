package uk.co.sundroid.util.astro.image

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.OrientationAngles

class MoonPhaseImageView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var orientationAngles = OrientationAngles()
    private var bitmap: Bitmap? = null
    private var paint = Paint()
    private var sourceRect = Rect()
    private var destRect = RectF()
    private var moon = R.drawable.moon

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    public override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            return
        }
        val image = bitmap ?: MoonPhaseImage.makeImage(context.resources, moon, orientationAngles)
        sourceRect.set(0, 0, image.width, image.height)
        destRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(image, sourceRect, destRect, paint)
        super.onDraw(canvas)
    }

    fun setOrientationAngles(orientationAngles: OrientationAngles) {
        this.orientationAngles = orientationAngles
        this.bitmap = null
        this.postInvalidate()
    }

    fun setMoonImage(moonImage: Int) {
        this.moon = moonImage
        this.bitmap = null
        this.postInvalidate()
    }

}