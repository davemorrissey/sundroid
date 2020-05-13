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
    fun makeImage(resources: Resources, drawable: Int, phase: Double, orientationAngles: OrientationAngles): Bitmap {
        val source = BitmapFactory.decodeResource(resources, drawable)
        val size = source.width
        val sizeF = size.toFloat()

        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(orientationAngles.brightLimbRotationAngle(), size / 2f, size / 2f)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(source, rotateMatrix, Paint())

        val path = Path()
        val paint = Paint()
        paint.blendMode = BlendMode.MODULATE
        paint.color = Color.argb(150, 75, 75, 75)
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
        canvas.drawPath(path, paint)
        return bitmap
    }

}