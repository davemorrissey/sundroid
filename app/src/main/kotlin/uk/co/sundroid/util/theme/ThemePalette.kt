@file:JvmName("ThemePalette")
package uk.co.sundroid.util.theme

import android.app.Activity
import android.content.Intent
import android.graphics.Color

import uk.co.sundroid.R.style
import uk.co.sundroid.util.SharedPrefsHelper
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.image.TrackerImage.TrackerStyle
import uk.co.sundroid.R.drawable.*

const val THEME_DARK = "DARK"
const val THEME_LIGHT = "LIGHT"
var theme: String? = null

private val radarLight = TrackerStyle(
        Color.argb(255, 0, 0, 0),
        Color.argb(100, 0, 0, 0),
        Color.argb(255, 255, 204, 0),
        Color.argb(255, 255, 168, 0),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 255, 204, 0),
        Color.argb(255, 72, 90, 144),
        2,
        1,
        floatArrayOf( 2f, 3f ),
        true
)

private val radarDark = TrackerStyle(
        Color.argb(255, 255, 255, 255),
        Color.argb(170, 120, 120, 120),
        Color.argb(255, 255, 222, 107),
        Color.argb(255, 255, 198, 0),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 115, 142, 204),
        Color.argb(255, 255, 255, 255),
        Color.argb(255, 100, 100, 100),
        2,
        1,
        floatArrayOf( 2f, 3f ),
        true
)

fun getRiseArrow(): Int {
    return if (theme == THEME_LIGHT) l_rise else d_rise
}

fun getSetArrow(): Int {
    return if (theme == THEME_LIGHT) l_set else d_set
}

fun getAppBg(): Int {
    return if (theme == THEME_LIGHT) l_app_bg else d_app_bg
}

fun getActionBarBg(): Int {
    return if (theme == THEME_LIGHT) l_action_bar_bg else d_action_bar_bg
}

fun getPhaseFull(): Int {
    return if (theme == THEME_LIGHT) l_phase_full else d_phase_full
}

fun getPhaseNew(): Int {
    return if (theme == THEME_LIGHT) l_phase_new else d_phase_new
}

fun getPhaseLeft(): Int {
    return if (theme == THEME_LIGHT) l_phase_left else d_phase_left
}

fun getPhaseRight(): Int {
    return if (theme == THEME_LIGHT) l_phase_right else d_phase_right
}

fun getCalendarHighlightColor(): Int {
    return if (theme == THEME_LIGHT) 0xffa6dff5.toInt() else 0xff222522.toInt()
}

fun getCalendarDefaultColor(): Int {
    return if (theme == THEME_LIGHT) 0x00ffffff else 0x00000000
}

fun getCalendarHeaderColor(): Int {
    return if (theme == THEME_LIGHT) 0xffc4eaf8.toInt() else 0xff222522.toInt()
}

fun getDisclosureOpen(): Int {
    return if (theme == THEME_LIGHT) l_disclosure_open else d_disclosure_open
}

fun getDisclosureClosed(): Int {
    return if (theme == THEME_LIGHT) l_disclosure_closed else d_disclosure_closed
}

fun getBodyColor(body: Body): Int {
    return if (theme == THEME_LIGHT) body.lightColor else body.darkColor
}

fun getTrackerRadarStyle(): TrackerStyle {
    return if (theme == THEME_LIGHT) radarLight else radarDark
}

fun changeToTheme(activity: Activity, newTheme: String) {
    theme = newTheme
    activity.finish()
    activity.startActivity(Intent(activity, activity::class.java))
}

fun onActivityCreateSetTheme(activity: Activity) {
    if (theme == null) {
        val prefsTheme = SharedPrefsHelper.getTheme(activity)
        if (prefsTheme == THEME_DARK || prefsTheme == THEME_LIGHT) {
            theme = prefsTheme
        }
    }
    activity.setTheme(if (theme == THEME_LIGHT) style.SundroidLight else style.SundroidDark)
}
