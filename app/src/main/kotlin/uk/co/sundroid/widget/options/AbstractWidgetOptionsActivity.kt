package uk.co.sundroid.widget.options

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.widget.sendUpdate
import uk.co.sundroid.widget.service.OP_TAP_REFRESH
import uk.co.sundroid.util.permission.backgroundLocationGranted
import java.util.*

abstract class AbstractWidgetOptionsActivity : Activity() {

    protected abstract val configClass: Class<*>
    protected abstract val name: String

    private inner class RetainedNonConfiguration {
        var widgetId = 0
    }

    private var rnc = RetainedNonConfiguration()
    override fun onRetainNonConfigurationInstance(): Any {
        return rnc
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (lastNonConfigurationInstance != null) {
            rnc = (lastNonConfigurationInstance as RetainedNonConfiguration?)!!
        } else if (intent.hasExtra(EXTRA_APPWIDGET_ID)) {
            rnc.widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        // If this is an auto-locate widget and background location permission is denied, skip the
        // dialog and open config activity directly.
        if (isAutoLocateWidget(rnc.widgetId) && !backgroundLocationGranted(this)) {
            openConfig()
            return
        }

        val items: MutableList<CharSequence> = ArrayList()
        items.add("Settings")
        items.add("Refresh")
        items.add("Open Sundroid")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(name)
        builder.setItems(items.toTypedArray()) { _, item ->
            when (item) {
                0 -> openConfig()
                1 -> sendUpdate(this, OP_TAP_REFRESH, rnc.widgetId)
                else -> openApp()
            }
            finish()
        }
        builder.setOnCancelListener { finish() }
        builder.show()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun openApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
//        FIXME
//        app.putExtra(MainActivity.EXTRA_WIDGET_ID, rnc.widgetId)
        startActivity(intent)
    }

    private fun openConfig() {
        val intent = Intent(applicationContext, configClass)
        intent.putExtra(EXTRA_APPWIDGET_ID, rnc.widgetId)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun isAutoLocateWidget(widgetId: Int): Boolean {
        DatabaseHelper(this).use { db ->
            return db.getWidgetLocation(widgetId) == null
        }
    }

}