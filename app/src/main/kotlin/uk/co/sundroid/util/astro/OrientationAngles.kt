package uk.co.sundroid.util.astro

/**
 * Angles used when rendering the moon: angle of bright limb, angle of axis relative to celestial
 * north, and parallactic angle for observer.
 */
class OrientationAngles {

    var axis = 0.0
    var brightLimb = 0.0
    var parallactic = 0.0
    var librationLongitude = 0.0
    var librationLatitude = 0.0

    /**
     * The rotation that should be applied to the moon image before rendering the bright limb.
     */
    fun brightLimbRotationAngle(): Float {
        var result = brightLimb - 90.0
        if (brightLimb > 180) {
            result = brightLimb - 270
        }
        return (result - axis).toFloat()
    }

    /**
     * The rotation that should be applied to the rendered image, which is generated rotated by the
     * rotation angle above, to orient it for the observer's location.
     */
    fun imageRotationAngle(): Float {
        return -brightLimbRotationAngle() + parallactic.toFloat() - axis.toFloat()
    }

}