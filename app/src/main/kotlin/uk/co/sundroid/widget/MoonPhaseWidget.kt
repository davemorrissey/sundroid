package uk.co.sundroid.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.widget.options.MoonPhaseWidgetOptionsActivity

class MoonPhaseWidget : AbstractWidget() {

    override val widgetClass: Class<*>
        get() = MoonPhaseWidget::class.java

    companion object {

        /**
         * Sets the tap action on the widget. It has a tendency to disappear and needs regular updating.
         */
        fun staticResetTapAction(remoteViews: RemoteViews, context: Context?, widgetId: Int) {
            val opts = Intent(context, MoonPhaseWidgetOptionsActivity::class.java)
            opts.addFlags(FLAG_ACTIVITY_NEW_TASK)
            opts.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            opts.putExtra(EXTRA_APPWIDGET_ID, widgetId)
            opts.data = getWidgetUri("sundroid_moonphase", widgetId)
            val pending = PendingIntent.getActivity(context, 0, opts, FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.root, pending)
        }

    }
}