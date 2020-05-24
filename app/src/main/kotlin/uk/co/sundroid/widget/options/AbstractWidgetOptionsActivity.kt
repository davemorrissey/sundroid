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
import uk.co.sundroid.widget.sendUpdate
import uk.co.sundroid.widget.service.OP_TAP_REFRESH
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

        val items: MutableList<CharSequence> = ArrayList()
        items.add("Refresh")
        items.add("Options")
        items.add("Open Sundroid")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(name)
        builder.setItems(items.toTypedArray()) { _, item ->
            when (item) {
                0 -> sendUpdate(this, OP_TAP_REFRESH, rnc.widgetId)
                1 -> openConfig()
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

}