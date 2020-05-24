package uk.co.sundroid.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.BodyDayEvent
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.TwilightType
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.widget.options.SunWidgetOptionsActivity
import java.util.*

class SunWidget : AbstractWidget() {

    override val widgetClass: Class<*>
        get() = SunWidget::class.java

    /**
     * Show locating message.
     */
    override fun showLocating(context: Context, id: Int) {
        val v = RemoteViews(context.packageName, R.layout.widget_sun)
        v.setViewVisibility(R.id.special, View.GONE)
        v.setViewVisibility(R.id.rise, View.GONE)
        v.setViewVisibility(R.id.set, View.GONE)
        v.setViewVisibility(R.id.location, View.GONE)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, "LOCATING ...")
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Show an error message.
     */
    override fun showError(context: Context, id: Int, message: String) {
        val v = RemoteViews(context.packageName, R.layout.widget_sun)
        v.setViewVisibility(R.id.special, View.GONE)
        v.setViewVisibility(R.id.rise, View.GONE)
        v.setViewVisibility(R.id.set, View.GONE)
        v.setViewVisibility(R.id.location, View.GONE)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, message)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Updates a widget to show data for a location.
     */
    override fun showData(context: Context, id: Int, location: LocationDetails, calendar: Calendar) {
        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar, BodyDayEvent.Event.RISESET)
        val v = RemoteViews(context.packageName, R.layout.widget_sun)
        val boxOpacity = Prefs.widgetBoxShadowOpacity(context, id)
        v.setInt(R.id.root, "setBackgroundColor", Color.argb(boxOpacity, 0, 0, 0))
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
            v.setViewVisibility(R.id.special, View.VISIBLE)
            v.setViewVisibility(R.id.rise, View.GONE)
            v.setViewVisibility(R.id.set, View.GONE)
            v.setTextViewText(R.id.special, if (lightDarkType === TwilightType.LIGHT) "RISEN" else "SET")
        } else {
            v.setViewVisibility(R.id.special, View.GONE)
            v.setViewVisibility(R.id.rise, View.VISIBLE)
            v.setViewVisibility(R.id.set, View.VISIBLE)
            if (rise == null) {
                v.setTextViewText(R.id.riseTime, "NONE")
                v.setViewVisibility(R.id.riseZone, View.GONE)
            } else {
                val time: Time = formatTime(context, rise, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.riseTime, View.VISIBLE)
                v.setTextViewText(R.id.riseTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.riseZone, View.VISIBLE)
                    v.setTextViewText(R.id.riseZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.riseZone, View.GONE)
                }
            }
            if (set == null) {
                v.setTextViewText(R.id.setTime, "NONE")
                v.setViewVisibility(R.id.setZone, View.GONE)
            } else {
                val time: Time = formatTime(context, set, allowSeconds = false, allowRounding = true)
                v.setViewVisibility(R.id.setTime, View.VISIBLE)
                v.setTextViewText(R.id.setTime, time.time)
                if (isNotEmpty(time.marker)) {
                    v.setViewVisibility(R.id.setZone, View.VISIBLE)
                    v.setTextViewText(R.id.setZone, time.marker.toUpperCase(Locale.getDefault()))
                } else {
                    v.setViewVisibility(R.id.setZone, View.GONE)
                }
            }
        }
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    companion object {

        /**
         * Sets the tap action on the widget. It has a tendency to disappear and needs regular updating.
         */
        fun staticResetTapAction(remoteViews: RemoteViews, context: Context, id: Int) {
            val opts = Intent(context, SunWidgetOptionsActivity::class.java)
            opts.addFlags(FLAG_ACTIVITY_NEW_TASK)
            opts.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            opts.putExtra(EXTRA_APPWIDGET_ID, id)
            opts.data = getWidgetUri("sundroid_moon", id)
            val pending = PendingIntent.getActivity(context, 0, opts, FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.root, pending)
        }

    }
}