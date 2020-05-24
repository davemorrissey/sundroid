package uk.co.sundroid.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.arrayString
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.widget.service.OP_REFRESH
import uk.co.sundroid.widget.service.OP_RENDER
import java.util.*

abstract class AbstractWidget : AppWidgetProvider() {

    protected abstract val widgetClass: Class<*>

    /**
     * Scheduled refresh: start unforced refresh (uses cached location if fresh) and reset tap action.
     */
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        d(TAG, "onUpdate: " + arrayString(ids))
        ids.forEach { id -> sendUpdate(context, OP_REFRESH, id) }
    }

    /**
     * Size has changed. Perform an ordinary render.
     */
    override fun onAppWidgetOptionsChanged(context: Context?, manager: AppWidgetManager?, id: Int, options: Bundle?) {
        d(TAG, "onAppWidgetOptionsChanged: $id")
        context?.let {
            sendUpdate(it, OP_RENDER, id)
        }
    }

    /**
     * When a widget is deleted, delete the location and preferences associated with it. The single
     * alarm will be left scheduled if there are no widgets left, but cleared at the next reboot.
     */
    override fun onDeleted(context: Context, ids: IntArray) {
        d(TAG, "onDeleted: " + arrayString(ids))
        DatabaseHelper(context).use { db ->
            ids.forEach { id ->
                db.deleteWidgetLocation(id)
                Prefs.removeWidgetPrefs(context, id)
            }
        }
        super.onDeleted(context, ids)
    }

    abstract fun showLocating(context: Context, id: Int)
    abstract fun showError(context: Context, id: Int, message: String)
    abstract fun showData(context: Context, id: Int, location: LocationDetails, calendar: Calendar)

    companion object {
        private val TAG = AbstractWidget::class.java.simpleName
    }
}