package uk.co.sundroid.widget.config

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import uk.co.sundroid.databinding.WidgetPrefsBinding
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.permission.backgroundLocationGranted
import uk.co.sundroid.util.permission.backgroundLocationPermission
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.widget.sendUpdate
import uk.co.sundroid.widget.service.OP_TAP_REFRESH
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

abstract class AbstractWidgetConfigurationActivity : Activity(), SeekBar.OnSeekBarChangeListener {

    protected var rnc = RetainedNonConfiguration()
    protected lateinit var b: WidgetPrefsBinding
    private lateinit var db: DatabaseHelper

    protected inner class RetainedNonConfiguration {
        var widgetId = 0
    }

    override fun onRetainNonConfigurationInstance(): Any {
        return rnc
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        d(TAG, "onCreate()")
        db = DatabaseHelper(this)
        if (lastNonConfigurationInstance != null) {
            rnc = (lastNonConfigurationInstance as RetainedNonConfiguration?)!!
        } else if (intent.hasExtra(EXTRA_APPWIDGET_ID)) {
            rnc.widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        val savedLocations = db.savedLocations
        b = WidgetPrefsBinding.inflate(layoutInflater)
        b.permissionWarningButton.setOnClickListener {
            if (SDK_INT > 23) {
                requestPermissions(backgroundLocationPermission(), REQUEST_LOCATION)
            }
        }
        b.locationButton.setOnClickListener {
            if (savedLocations.isEmpty()) {
                noSavedLocationsDialog()
            } else {
                selectLocationDialog()
            }
        }
        b.done.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        updateLocationOption()
    }

    override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
        when (seekBar.id) {
            b.boxOpacitySeek.id -> Prefs.setWidgetBoxShadowOpacity(this, rnc.widgetId, min(255, max(0, value)))
            b.shadowOpacitySeek.id -> Prefs.setWidgetPhaseShadowOpacity(this, rnc.widgetId, min(255, max(0, value)))
            b.shadowSizeSeek.id -> Prefs.setWidgetPhaseShadowSize(this, rnc.widgetId, min(40, max(0, value)))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) { }
    override fun onStopTrackingTouch(seekBar: SeekBar) { }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PERMISSION_DENIED) {
            permissionDeniedDialog()
        }
        updateLocationOption()
    }

    override fun onStop() {
        sendUpdate(this, OP_TAP_REFRESH, rnc.widgetId)
        super.onStop()
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    private fun updateLocationOption() {
        db.getWidgetLocation(rnc.widgetId)?.let { it ->
            b.locationValue.text = it.displayName
            b.permissionWarning.visibility = View.GONE
        } ?: run {
            b.locationValue.text = "My location (auto update)"
            if (!backgroundLocationGranted(applicationContext) && SDK_INT > 23) {
                b.permissionWarning.visibility = View.VISIBLE
            } else {
                b.permissionWarning.visibility = View.GONE
            }
        }
    }

    private fun setAutoLocation() {
        db.deleteWidgetLocation(rnc.widgetId)
        updateLocationOption()
    }

    private fun setSavedLocation(locationDetails: LocationDetails, acceptTimezone: Boolean) {
        val deviceTimeZone: TimeZoneDetail = TimeZoneResolver.getTimeZone(null)

        // Saved locations will in almost all cases have a timezone. Just in case, default to the device zone.
        if (locationDetails.timeZone == null) {
            locationDetails.timeZone = deviceTimeZone
        }

        // Ask user to select between device zone and location zone if they are different.
        if (!acceptTimezone && locationDetails.timeZone != null && locationDetails.timeZone!!.id != deviceTimeZone.id) {
            confirmTimeZoneDialog(locationDetails)
        } else {
            db.setWidgetLocation(rnc.widgetId, locationDetails)
        }
        updateLocationOption()
    }

    private fun permissionDeniedDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Permission denied")
            setMessage("Apparent orientation will not work without this permission. To fix this, you can grant this app background location permission from Android settings.")
            setPositiveButton(android.R.string.ok, null)
            setNeutralButton("Permissions") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }.show()
    }

    private fun noSavedLocationsDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("No saved locations")
            setMessage("To set a different location for this widget, you need to find and save the location in the app. Open Sundroid, change to your desired location, and tap the star icon in the top right of the screen.")
            setPositiveButton("OK") { _, _ -> }
            setNegativeButton("Cancel") { _, _ -> }
        }.show()
    }

    private fun selectLocationDialog() {
        val savedLocations = db.savedLocations
        val names: MutableList<CharSequence> = ArrayList()
        names.add("My location (auto update)")
        names.addAll(savedLocations.map { l -> l.displayName })
        AlertDialog.Builder(this).apply {
            setTitle("Select location")
            setItems(names.toTypedArray()) { _, item ->
                if (item == 0) {
                    setAutoLocation()
                } else {
                    setSavedLocation(savedLocations[item - 1], false)
                }
            }
            setNegativeButton("Cancel") { _, _ -> }
        }.show()
    }

    private fun confirmTimeZoneDialog(locationDetails: LocationDetails) {
        val deviceZone: TimeZoneDetail = TimeZoneResolver.getTimeZone(null)
        val locationZone = locationDetails.timeZone
        val names: MutableList<CharSequence> = ArrayList()
        names.add(deviceZone.getOffset(System.currentTimeMillis()) + " " + if (isEmpty(deviceZone.cities)) deviceZone.zone.displayName else deviceZone.cities)
        names.add(locationZone!!.getOffset(System.currentTimeMillis()) + " " + if (isEmpty(locationZone.cities)) locationZone.zone.displayName else locationZone.cities)
        AlertDialog.Builder(this).apply {
            setTitle("Select display time zone")
            setItems(names.toTypedArray()) { _, item ->
                if (item == 0) {
                    locationDetails.timeZone = deviceZone
                } else {
                    locationDetails.timeZone = locationZone
                }
                setSavedLocation(locationDetails, true)
            }
            setNegativeButton("Cancel") { _, _ -> }
        }.show()
    }

    companion object {
        private val TAG = AbstractWidgetConfigurationActivity::class.java.simpleName
        const val REQUEST_LOCATION = 1110
    }

}