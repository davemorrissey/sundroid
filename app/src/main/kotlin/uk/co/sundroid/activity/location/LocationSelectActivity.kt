package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.OnClickListener
import uk.co.sundroid.activity.Locater
import uk.co.sundroid.activity.LocaterListener
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*

class LocationSelectActivity : AbstractLocationActivity(), LocaterListener, OnClickListener, DialogInterface.OnClickListener {
    private var locater: Locater? = null

    private val handler = Handler()

    protected override val viewTitle: String
        get() = "Change location"

    protected override val layout: Int
        get() = R.layout.loc_options

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(id.locOptionMyLocation).setOnClickListener(this)
        findViewById<View>(id.locOptionMap).setOnClickListener(this)
        findViewById<View>(id.locOptionSearch).setOnClickListener(this)
        findViewById<View>(id.locOptionSavedPlaces).setOnClickListener(this)
        findViewById<View>(id.locOptionCoords).setOnClickListener(this)
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {

    }

    private fun startMap() {
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    private fun startSavedLocations() {
        val intent = Intent(this, SavedLocationsActivity::class.java)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    private fun startSearch() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    private fun startCoords() {
        val intent = Intent(this, CoordsActivity::class.java)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        if (id == DIALOG_LOCATING) {
            val progressDialog = dialog as ProgressDialog
            progressDialog.setMessage("Finding your location, please wait...")
            return
        } else if (id == DIALOG_LOCATION_TIMEOUT) {
            val alertDialog = dialog as AlertDialog
            alertDialog.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.")
            return
        }
        super.onPrepareDialog(id, dialog)
    }

    public override fun onCreateDialog(id: Int): Dialog {
        if (id == DIALOG_LOCATING) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Locating")
            progressDialog.setMessage("Finding your location, please wait...")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(true)
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { dialog, id1 ->
                if (locater != null) {
                    locater!!.cancel()
                }
                dismissDialog(DIALOG_LOCATING)
            }
            return progressDialog
        } else if (id == DIALOG_LOCATION_TIMEOUT) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Location lookup timeout")
            builder.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.")
            builder.setNeutralButton("OK", this)
            return builder.create()
        } else if (id == DIALOG_LOCATION_ERROR) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Location lookup failed")
            builder.setMessage("Location services are disabled. Enable wireless networks or GPS in your location settings.")
            builder.setNeutralButton("OK", this)
            return builder.create()
        }
        return super.onCreateDialog(id)
    }

    override fun locationError() {
        dismissDialog(DIALOG_LOCATING)
        showDialog(DIALOG_LOCATION_ERROR)
    }

    override fun locationTimeout() {
        handler.post {
            dismissDialog(DIALOG_LOCATING)
            showDialog(DIALOG_LOCATION_TIMEOUT)
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.locOptionMyLocation -> {
                if (locater != null) {
                    locater!!.cancel()
                }
                locater = Locater(this, applicationContext)
                if (locater!!.start()) {
                    showDialog(DIALOG_LOCATING)
                } else {
                    showDialog(DIALOG_LOCATION_ERROR)
                }
                return
            }
            id.locOptionMap -> {
                startMap()
                return
            }
            R.id.locOptionSearch -> {
                startSearch()
                return
            }
            R.id.locOptionSavedPlaces -> {
                startSavedLocations()
                return
            }
            R.id.locOptionCoords -> {
                startCoords()
                return
            }
        }
        super.onClick(view)
    }

    companion object {

        private val TAG = LocationSelectActivity::class.java.simpleName

        val REQUEST_LOCATION = 1110
        val RESULT_LOCATION_SELECTED = 2220
        val RESULT_CANCELLED = 2223

        val DIALOG_LOCATING = 101
        val DIALOG_LOCATION_ERROR = 103
        val DIALOG_LOCATION_TIMEOUT = 105
    }

}
