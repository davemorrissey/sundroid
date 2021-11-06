package uk.co.sundroid.util.astro

import android.graphics.Color

enum class Body constructor(val displayName: String, val darkColor: Int) {

    SUN("Sun", Color.argb(255, 255, 204, 0)),
    MOON("Moon", Color.argb(255, 255, 255, 255)),
    MERCURY("Mercury", Color.argb(255, 220, 151, 110)),
    VENUS("Venus", Color.argb(255, 255, 181, 85)),
    MARS("Mars", Color.argb(255, 229, 144, 128)),
    JUPITER("Jupiter", Color.argb(255, 244, 211, 172)),
    SATURN("Saturn", Color.argb(255, 249, 210, 147)),
    URANUS("Uranus", Color.argb(255, 98, 221, 222)),
    NEPTUNE("Neptune", Color.argb(255, 98, 171, 222));

    companion object {
        val PLANETS = arrayOf(MERCURY, VENUS, MARS, JUPITER, SATURN, URANUS, NEPTUNE)
    }

}
