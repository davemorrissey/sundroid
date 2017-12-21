package uk.co.sundroid.util.theme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

import uk.co.sundroid.R.drawable;
import uk.co.sundroid.R.style;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.image.TrackerImage.TrackerStyle;

public class ThemePalette {

    public static final String THEME_DARK = "DARK";
    public static final String THEME_LIGHT = "LIGHT";
    private static String THEME = null;

    private static final TrackerStyle RADAR_LIGHT = new TrackerStyle(
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
            new float[] { 2, 3 },
            true
    );

    private static final TrackerStyle RADAR_DARK = new TrackerStyle(
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
            new float[] { 2, 3 },
            true
    );

    public static int getRiseArrow() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_rise : drawable.d_rise;
    }

    public static int getSetArrow() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_set : drawable.d_set;
    }

    public static int getAppBg() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_app_bg : drawable.d_app_bg;
    }

    public static int getActionBarBg() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_action_bar_bg : drawable.d_action_bar_bg;
    }

    public static int getPhaseFull() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_phase_full : drawable.d_phase_full;
    }

    public static int getPhaseNew() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_phase_new : drawable.d_phase_new;
    }

    public static int getPhaseLeft() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_phase_left : drawable.d_phase_left;
    }

    public static int getPhaseRight() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_phase_right : drawable.d_phase_right;
    }

    public static int getCalendarHighlightColor() {
        return THEME.equals(THEME_LIGHT) ? 0xffa6dff5 : 0xff222522;
    }

    public static int getCalendarDefaultColor() {
        return THEME.equals(THEME_LIGHT) ? 0x00ffffff : 0x00000000;
    }

    public static int getCalendarHeaderColor() {
        return THEME.equals(THEME_LIGHT) ? 0xffc4eaf8 : 0xff222522;
    }

    public static int getDisclosureOpen() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_disclosure_open : drawable.d_disclosure_open;
    }

    public static int getDisclosureClosed() {
        return THEME.equals(THEME_LIGHT) ? drawable.l_disclosure_closed : drawable.d_disclosure_closed;
    }

    public static int getBodyColor(Body body) {
        return THEME.equals(THEME_LIGHT) ? body.getLightColor() : body.getDarkColor();
    }

    public static TrackerStyle getTrackerRadarStyle() {
        if (THEME.equals(THEME_LIGHT)) {
            return RADAR_LIGHT;
        } else {
            return RADAR_DARK;
        }
    }

    public static void changeToTheme(Activity activity, String theme) {
        THEME = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        if (THEME == null) {
            String prefsTheme = SharedPrefsHelper.getTheme(activity);
            if (prefsTheme.equals(THEME_DARK) || prefsTheme.equals(THEME_LIGHT)) {
                THEME = prefsTheme;
            }
        }
        if (THEME.equals(THEME_LIGHT)) {
            activity.setTheme(style.SundroidLight);
        } else {
            activity.setTheme(style.SundroidDark);
        }
    }

}
