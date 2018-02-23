package uk.co.sundroid.activity.location

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import uk.co.sundroid.activity.Locater
import uk.co.sundroid.activity.LocaterListener
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*

import kotlinx.android.synthetic.main.loc_options.*
import uk.co.sundroid.util.view.SimpleAlertFragment

class LocationSelectActivity : AbstractLocationActivity(), LocaterListener {
    private var locater: Locater? = null

    private val handler = Handler()

    override val viewTitle: String
        get() = "Change location"

    override val layout: Int
        get() = R.layout.loc_options

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locOptionMyLocation.setOnClickListener { startLocater() }
        locOptionMap.setOnClickListener { start(MapActivity::class.java) }
        locOptionSearch.setOnClickListener { start(SearchActivity::class.java) }
        locOptionSavedPlaces.setOnClickListener { start(SavedLocationsActivity::class.java) }
        locOptionCoords.setOnClickListener { start(CoordsActivity::class.java) }
    }

    private fun startLocater() {
        locater?.cancel()
        val locater = Locater(this, applicationContext)
        if (locater.start()) {
            showDialog(DIALOG_LOCATING)
        } else {
            locationError()
        }
    }

    private fun start(activity: Class<out Activity>) {
        val intent = Intent(this, activity)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        if (id == DIALOG_LOCATING) {
            val progressDialog = dialog as ProgressDialog
            progressDialog.setMessage("Finding your location, please wait...")
            return
        }
        super.onPrepareDialog(id, dialog)
    }

    public override fun onCreateDialog(id: Int): Dialog {
        if (id == DIALOG_LOCATING) {
            return ProgressDialog(this).apply {
                isIndeterminate = true
                setTitle("Locating")
                setMessage("Finding your location, please wait...")
                setCancelable(true)
                setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                    locater?.cancel()
                    dismissDialog(DIALOG_LOCATING)
                }
                create()
            }
        }
        return super.onCreateDialog(id)
    }

    override fun locationError() {
        SimpleAlertFragment.newInstance(
                "Location lookup failed",
                "Location services are disabled. Enable wireless networks or GPS in your location settings."
        ).show(fragmentManager, "ALERT")
    }

    override fun locationTimeout() {
        handler.post {
            if (!isDestroyed) {
                SimpleAlertFragment.newInstance(
                        "Location lookup timeout",
                        "Couldn't find your location. Make sure you have a good signal or a clear view of the sky."
                ).show(fragmentManager, "ALERT")
            }
        }
    }

    override fun locationReceived(locationDetails: LocationDetails) {
        try {
            dismissDialog(DIALOG_LOCATING)
        } catch (e: Exception) {
            // May not have been shown yet.
        }

        d(TAG, "Location received: " + locationDetails)
        SharedPrefsHelper.saveSelectedLocation(this, locationDetails)

        if (locationDetails.timeZone == null) {
            val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
            intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
            startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
        } else {
            setResult(RESULT_LOCATION_SELECTED)
            finish()
        }
    }

    companion object {

        private val TAG = LocationSelectActivity::class.java.simpleName

        const val REQUEST_LOCATION = 1110
        const val RESULT_LOCATION_SELECTED = 2220
        const val RESULT_CANCELLED = 2223

        const val DIALOG_LOCATING = 101
    }

}
