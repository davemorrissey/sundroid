package uk.co.sundroid.util.astro.image

import android.content.res.Resources
import android.graphics.*
import uk.co.sundroid.util.astro.OrientationAngles
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos

object MoonPhaseImage {

    /**
     * Generates a moon phase image from the source moon by darkening pixels. The phase angle is taken
     * into account by rotating the source first. The returned image is still rotated because it is
     * more efficient to rotate it back in an image view than to create another bitmap.
     */
    @Throws(Exception::class)
    fun makeImage(resources: Resources, drawable: Int, orientationAngles: OrientationAngles, shadowSizePercent: Float = 0f, shadowOpacity: Int = 0): Bitmap {
        val source = BitmapFactory.decodeResource(resources, drawable)
        val size = source.width
        val sizeF = size.toFloat()
        var phase = orientationAngles.phase
        if (phase >= 0.98 || phase < 0.02) {
            phase = 0.0
        }
        val shadowSize = ((shadowSizePercent/100f) * size).toInt()

        var brightLimbRotate = orientationAngles.brightLimb - 90
        if (orientationAngles.brightLimb > 180) {
            brightLimbRotate = orientationAngles.brightLimb - 270
        }
        brightLimbRotate -= orientationAngles.axis
        val preRotate = orientationAngles.parallactic - orientationAngles.axis
        val postRotate = brightLimbRotate + preRotate

        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(preRotate.toFloat(), size / 2f, size / 2f)
        rotateMatrix.postScale(size / (size + (4f * shadowSize)), size / (size + (4f * shadowSize)), size / 2f, size / 2f)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (shadowSize > 0 && shadowOpacity > 0) {
            val blur = Paint()
            blur.color = Color.argb(shadowOpacity, 0, 0, 0)
            blur.maskFilter = BlurMaskFilter(shadowSize.toFloat(), BlurMaskFilter.Blur.NORMAL)
            canvas.drawOval(shadowSize.toFloat(), shadowSize.toFloat(), sizeF - shadowSize, sizeF - shadowSize, blur)
        }

        canvas.drawBitmap(source, rotateMatrix, Paint())

        val path = Path()
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        paint.color = Color.argb(255, 75, 75, 75)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        val radius = (size / 2) + 1

        val limb = (cos(2.0 * Math.PI * phase) * radius.toFloat() * cos(asin(0.0))).toFloat()
        if (phase >= 0.5) {
            if (limb < 0) {
                path.moveTo(size / 2f, 0f)
                path.arcTo(radius - abs(limb), 0f, radius + abs(limb), sizeF, 270f, 180f, false)
                path.lineTo(sizeF, sizeF)
                path.lineTo(sizeF, 0f)
            } else {
                path.moveTo(size / 2f, sizeF)
                path.arcTo(radius - abs(limb), 0f, radius + abs(limb), sizeF, 90f, 180f, false)
                path.lineTo(sizeF, 0f)
                path.lineTo(sizeF, sizeF)
            }
        } else if (limb < 0) {
            path.moveTo(size / 2f, sizeF)
            path.arcTo(radius - abs(limb), 0f, radius + abs(limb), sizeF, 90f, 180f, false)
            path.lineTo(0f, 0f)
            path.lineTo(0f, sizeF)
        } else {
            path.moveTo(size / 2f, 0f)
            path.arcTo(radius - abs(limb), 0f, radius + abs(limb), sizeF, 270f, 180f, false)
            path.lineTo(0f, sizeF)
            path.lineTo(0f, 0f)
        }
        canvas.scale(size / (size + (4f * shadowSize)), size / (size + (4f * shadowSize)), size / 2f, size / 2f)
        canvas.rotate(postRotate.toFloat(), radius.toFloat(), radius.toFloat())
        canvas.drawPath(path, paint)
        return bitmap
    }

}