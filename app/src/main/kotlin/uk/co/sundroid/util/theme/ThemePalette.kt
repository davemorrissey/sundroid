package uk.co.sundroid.util.theme

import android.app.Activity
import android.graphics.Color
import uk.co.sundroid.R

import uk.co.sundroid.R.style
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.image.TrackerImage.TrackerStyle
import uk.co.sundroid.R.drawable.*

const val THEME_DARK = "DARK"
const val THEME_LIGHT = "LIGHT"
const val THEME_DARKBLUE = "DARKBLUE"
var theme: String? = null

private val radarLight = TrackerStyle(
        Color.argb(255, 0, 0, 0),
        Color.argb(100, 0, 0, 0),
        Color.argb(255, 255, 204, 0),
        Color.argb(255, 255, 157, 0),
        Color.argb(255, 99, 116, 166),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 47, 65, 119),
        Color.argb(255, 26, 41, 88),
        Color.argb(255, 255, 204, 0),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 0, 0, 0),
        Color.argb(0, 0, 0, 0),
        3,
        2,
        true
)

private val radarDark = TrackerStyle(
        Color.argb(255, 255, 255, 255),
        Color.argb(170, 120, 120, 120),
        Color.argb(255, 255, 204, 0),
        Color.argb(255, 255, 157, 0),
        Color.argb(255, 99, 116, 166),
        Color.argb(255, 72, 90, 144),
        Color.argb(255, 47, 65, 119),
        Color.argb(255, 26, 41, 88),
        Color.argb(255, 255, 255, 255),
        Color.argb(255, 92, 118, 168),
        Color.argb(255, 255, 255, 255),
        Color.argb(0, 0, 0, 0),
        3,
        2,
        true
)

fun appBackground(): Int {
    return when (theme) {
        THEME_LIGHT -> R.color.l_app_background_start
        THEME_DARK -> R.color.d_app_background_start
        else -> R.color.db_app_background_start
    }
}

fun getRiseArrow(): Int {
    return if (theme == THEME_LIGHT) l_rise else d_rise
}

fun getSetArrow(): Int {
    return when (theme) {
        THEME_LIGHT -> l_set
        THEME_DARKBLUE -> db_set
        else -> d_set
    }
}

fun getRisenAllDay(): Int {
    return d_risen_all_day
}

fun getSetAllDay(): Int {
    return when (theme) {
        THEME_DARKBLUE -> db_set_all_day
        else -> d_set_all_day
    }
}

fun getAppBg(): Int {
    return when (theme) {
        THEME_LIGHT -> l_app_bg
        THEME_DARKBLUE -> db_app_bg
        else -> d_app_bg
    }
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
    return when (theme) {
        THEME_LIGHT -> 0xffa6dff5.toInt()
        THEME_DARKBLUE -> 0xff162544.toInt()
        else -> 0xff222522.toInt()
    }
}

fun getCalendarDefaultColor(): Int {
    return when (theme) {
        THEME_LIGHT -> 0x00ffffff
        THEME_DARKBLUE -> 0x00000000
        else -> 0x00000000
    }
}

fun getCalendarGridHighlightColor(): Int {
    return when (theme) {
        THEME_LIGHT -> 0xffa6dff5.toInt()
        THEME_DARKBLUE -> 0xff162544.toInt()
        else -> 0xff323632.toInt()
    }
}

fun getCalendarGridDefaultColor(): Int {
    return when (theme) {
        THEME_LIGHT -> 0x00ffffff
        THEME_DARKBLUE -> 0x810D1629.toInt()
        else -> 0x81222522.toInt()
    }
}

fun getCalendarHeaderColor(): Int {
    return when (theme) {
        THEME_LIGHT -> 0xffc4eaf8.toInt()
        THEME_DARKBLUE -> 0xff0D1629.toInt()
        else -> 0xff222522.toInt()
    }
}

fun upColor(): Int {
    return 0xffffffff.toInt()
}

fun downColor(): Int {
    return when (theme) {
        THEME_DARKBLUE -> 0xff5C76A8.toInt()
        else -> 0xff8a918a.toInt()
    }
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
    activity.recreate()
}

fun onActivityCreateSetTheme(activity: Activity) {
    if (theme == null) {
        val prefsTheme = Prefs.theme(activity)
        if (prefsTheme == THEME_DARK || prefsTheme == THEME_DARKBLUE) {
            theme = prefsTheme
        }
    }
    when (theme) {
        THEME_DARKBLUE -> activity.setTheme(style.SundroidDarkBlue)
        else -> activity.setTheme(style.SundroidDark)
    }
}
