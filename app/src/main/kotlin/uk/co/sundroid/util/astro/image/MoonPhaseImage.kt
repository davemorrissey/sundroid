package uk.co.sundroid.util.astro.image

import android.content.res.Resources
import android.graphics.*
import uk.co.sundroid.util.astro.OrientationAngles

object MoonPhaseImage {

    const val SIZE_MEDIUM = 1
    const val SIZE_LARGE = 2

    @Throws(Exception::class)
    fun makeImage(resources: Resources, drawable: Int, phase: Double, orientationAngles: OrientationAngles, size: Int): Bitmap {
        val metrics = resources.displayMetrics
        val densityDpi = metrics.densityDpi
        val source = BitmapFactory.decodeResource(resources, drawable)

        var targetSize = if (size == SIZE_MEDIUM) 61 else 121
        when {
            densityDpi > 320 -> targetSize = if (size == SIZE_MEDIUM) 181 else 361
            densityDpi > 160 -> targetSize = if (size == SIZE_MEDIUM) 91 else 181
            densityDpi < 160 -> targetSize = if (size == SIZE_MEDIUM) 45 else 91
        }

        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(orientationAngles.brightLimbRotationAngle(), source.width/2f, source.height/2f)
        rotateMatrix.postScale(targetSize.toFloat() / source.width, targetSize.toFloat() / source.height)

        var bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)

        if (!bitmap.isMutable) {
            // createBitmap returns same immutable image for identity matrix so we need to copy it.
            bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(source, 0f, 0f, Paint())
        }

        val canvas = Canvas(bitmap)
        canvas.drawBitmap(source, rotateMatrix, Paint())

        var radius = targetSize / 2
        radius++


        val leftEdge = Array(radius + 1) { IntArray(14) }
        val rightEdge = Array(radius + 1) { IntArray(14) }

        var position = 0
        run {
            var p = phase - 0.02
            while (p <= phase + 0.02 && position < 14) {
                var pa = if (p < 0) p + 1.0 else p
                pa = if (pa > 1) pa - 1 else p
                if (pa < 0.02 || pa > 0.98 || Math.abs(phase - pa) > 0.1) {
                    for (r in 0..radius) {
                        leftEdge[r][position] = 0
                        rightEdge[r][position] = 0
                    }
                } else {
                    val s = Math.cos(2.0 * Math.PI * pa)
                    for (r in 0..radius) {
                        val limb = (s * radius.toDouble() * Math.cos(Math.asin(r.toDouble() / radius))).toInt()
                        leftEdge[r][position] = if (pa <= 0.5) radius + limb else 0
                        rightEdge[r][position] = if (pa <= 0.5) targetSize else radius - limb
                    }
                }
                position++
                p += 0.003
            }
        }
        for (y in 0 until targetSize) {
            val r = Math.abs(y + 1 - radius)
            if (r < radius) {
                for (x in 0 until targetSize) {
                    var brightness = 0.0
                    val left = leftEdge[r]
                    val right = rightEdge[r]
                    left.indices
                            .filter { x >= left[it] && x <= right[it] }
                            .forEach { brightness += 1.0 }
                    brightness /= left.size.toDouble()
                    brightness = brightness * 0.75 + 0.25
                    if (phase < 0.01 || phase > 0.99) {
                        brightness = 0.25
                    }

                    if (brightness < 1) {
                        val sourcePixelColor = bitmap.getPixel(x, y)
                        if (Color.alpha(sourcePixelColor) > 0) {
                            val red = (Color.red(sourcePixelColor) * brightness).toInt()
                            val green = (Color.green(sourcePixelColor) * brightness).toInt()
                            val blue = (Color.blue(sourcePixelColor) * brightness).toInt()
                            bitmap.setPixel(x, y, Color.argb(Color.alpha(sourcePixelColor), red, green, blue))
                        }
                    }
                }
            }
        }
        return bitmap
    }

}