package uk.co.sundroid.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.math.SunMoonCalculator
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.widget.options.SunMoonWidgetOptionsActivity
import java.util.*

class SunMoonWidget : AbstractWidget() {

    override val widgetClass: Class<*>
        get() = SunMoonWidget::class.java

    /**
     * Show locating message.
     */
    override fun showLocating(context: Context, id: Int) {
        val v = RemoteViews(context.packageName, R.layout.widget_sun_moon)
        v.setViewVisibility(R.id.sunSpecial, View.GONE)
        v.setViewVisibility(R.id.sunRise, View.GONE)
        v.setViewVisibility(R.id.sunSet, View.GONE)
        v.setViewVisibility(R.id.moonSpecial, View.GONE)
        v.setViewVisibility(R.id.moonRise, View.GONE)
        v.setViewVisibility(R.id.moonSet, View.GONE)
        v.setTextViewText(R.id.location, " ")
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, "LOCATING ...")
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Show an error message.
     */
    override fun showError(context: Context, id: Int, message: String) {
        val v = RemoteViews(context.packageName, R.layout.widget_sun_moon)
        v.setViewVisibility(R.id.sunSpecial, View.GONE)
        v.setViewVisibility(R.id.sunRise, View.GONE)
        v.setViewVisibility(R.id.sunSet, View.GONE)
        v.setViewVisibility(R.id.moonSpecial, View.GONE)
        v.setViewVisibility(R.id.moonRise, View.GONE)
        v.setViewVisibility(R.id.moonSet, View.GONE)
        v.setTextViewText(R.id.location, " ")
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, message)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Updates a widget to show data for a location.
     */
    override fun showData(context: Context, id: Int, location: LocationDetails, calendar: Calendar) {
        val v = RemoteViews(context.packageName, R.layout.widget_sun_moon)
        val boxOpacity = Prefs.widgetBoxShadowOpacity(context, id)
        v.setInt(R.id.root, "setBackgroundColor", Color.argb(boxOpacity, 0, 0, 0))

        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar, BodyDayEvent.Event.RISESET)
        var lightDarkType: TwilightType? = null
        if (sunDay.riseSetType === RiseSetType.RISEN) {
            lightDarkType = TwilightType.LIGHT
        } else if (sunDay.riseSetType === RiseSetType.SET) {
            lightDarkType = TwilightType.DARK
        }
        val rise = sunDay.rise
        val set = sunDay.set
        v.setViewVisibility(R.id.message, View.GONE)
        v.setViewVisibility(R.id.location, View.VISIBLE)
        if (isEmpty(location.name)) {
            v.setTextViewText(R.id.location, "MY LOCATION")
        } else {
            v.setTextViewText(R.id.location, location.displayName.toUpperCase(Locale.getDefault()))
        }
        if (lightDarkType != null && lightDarkType === TwilightType.LIGHT || lightDarkType === TwilightType.DARK) {
            v.setViewVisibility(R.id.sunSpecial, View.VISIBLE)
            v.setViewVisibility(R.id.sunRise, View.GONE)
            v.setViewVisibility(R.id.sunSet, View.GONE)
            v.setTextViewText(R.id.sunSpecial, if (lightDarkType === TwilightType.LIGHT) "RISEN" else "SET")
        } else {
            v.setViewVisibility(R.id.sunSpecial, View.GONE)
            v.setViewVisibility(R.id.sunRise, View.VISIBLE)
            v.setViewVisibility(R.id.sunSet, View.VISIBLE)
            if (rise == null) {
                v.setTextViewText(R.id.riseTime, "NONE")
                v.setViewVisibility(R.id.riseZone, View.GONE)
            } else {
                val time: Time = formatTime(context, rise, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.sunRiseTime, View.VISIBLE)
                v.setTextViewText(R.id.sunRiseTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.sunRiseZone, View.VISIBLE)
                    v.setTextViewText(R.id.sunRiseZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.sunRiseZone, View.GONE)
                }
            }
            if (set == null) {
                v.setTextViewText(R.id.sunSetTime, "NONE")
                v.setViewVisibility(R.id.sunSetZone, View.GONE)
            } else {
                val time: Time = formatTime(context, set, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.sunSetTime, View.VISIBLE)
                v.setTextViewText(R.id.sunSetTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.sunSetZone, View.VISIBLE)
                    v.setTextViewText(R.id.sunSetZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.sunSetZone, View.GONE)
                }
            }
        }

        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay
        if (moonDay.riseSetType === RiseSetType.RISEN || moonDay.riseSetType === RiseSetType.SET) {
            v.setViewVisibility(R.id.moonSpecial, View.VISIBLE)
            v.setViewVisibility(R.id.moonRise, View.GONE)
            v.setViewVisibility(R.id.moonSet, View.GONE)
            v.setTextViewText(R.id.special, if (moonDay.riseSetType === RiseSetType.RISEN) "RISEN" else "SET")
        } else {
            v.setViewVisibility(R.id.special, View.GONE)
            v.setViewVisibility(R.id.moonRise, View.VISIBLE)
            v.setViewVisibility(R.id.moonSet, View.VISIBLE)
            if (moonDay.rise == null) {
                v.setTextViewText(R.id.moonRiseTime, "None")
                v.setViewVisibility(R.id.moonRiseZone, View.GONE)
            } else {
                val time: Time = formatTime(context, moonDay.rise!!, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.moonRiseTime, View.VISIBLE)
                v.setTextViewText(R.id.moonRiseTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.moonRiseZone, View.VISIBLE)
                    v.setTextViewText(R.id.moonRiseZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.moonRiseZone, View.GONE)
                }
            }
            if (moonDay.set == null) {
                v.setTextViewText(R.id.moonSetTime, "None")
                v.setViewVisibility(R.id.moonSetZone, View.GONE)
            } else {
                val time: Time = formatTime(context, moonDay.set!!, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.moonSetTime, View.VISIBLE)
                v.setTextViewText(R.id.moonSetTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.moonSetZone, View.VISIBLE)
                    v.setTextViewText(R.id.moonSetZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.moonSetZone, View.GONE)
                }
            }
        }
        val orientationAngles: OrientationAngles = SunMoonCalculator.moonDiskOrientationAngles(Calendar.getInstance(), location.location)
        val moonBitmap: Bitmap = MoonPhaseImage.makeImage(context.resources, R.drawable.moon, orientationAngles)
        v.setImageViewBitmap(R.id.moonIcon, moonBitmap)

        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    companion object {

        /**
         * Sets the tap action on the widget. It has a tendency to disappear and needs regular updating.
         */
        fun staticResetTapAction(remoteViews: RemoteViews, context: Context, id: Int) {
            val opts = Intent(context, SunMoonWidgetOptionsActivity::class.java)
            opts.addFlags(FLAG_ACTIVITY_NEW_TASK)
            opts.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            opts.putExtra(EXTRA_APPWIDGET_ID, id)
            opts.data = getWidgetUri("sundroid_moon", id)
            val pending = PendingIntent.getActivity(context, 0, opts, FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.root, pending)
        }

    }
}