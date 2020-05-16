package uk.co.sundroid.util.astro.image

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.OrientationAngles
import uk.co.sundroid.util.log.d

class MoonPhaseImageView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var orientationAngles = OrientationAngles()
    private var bitmap: Bitmap? = null
    private var paint = Paint()
    private var sourceRect = Rect()
    private var destRect = RectF()

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    public override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            return
        }
        if (bitmap == null) {
            d(TAG, "Draw fresh image")
        } else {
            d(TAG, "Draw existing image")
        }
        val start = System.currentTimeMillis()
        val image = bitmap ?: MoonPhaseImage.makeImage(context.resources, R.drawable.moon, orientationAngles)
        sourceRect.set(0, 0, image.width, image.height)
        destRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.rotate(orientationAngles.imageRotationAngle(), width / 2F, height / 2F)
        canvas.drawBitmap(image, sourceRect, destRect, paint)
        val end = System.currentTimeMillis()
        d(TAG, "Took " + (end - start))
        super.onDraw(canvas)
    }

    fun setOrientationAngles(orientationAngles: OrientationAngles) {
        this.orientationAngles = orientationAngles
        this.bitmap = null
        this.postInvalidate()
    }

    companion object {
        val TAG = MoonPhaseImageView::class.java.simpleName
    }

}