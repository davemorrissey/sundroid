package uk.co.sundroid.activity.location

import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast

class CoordsActivity : AbstractLocationActivity(), OnClickListener {

    protected override val layout: Int
        get() = R.layout.loc_coords

    protected override val viewTitle: String
        get() = "Enter coordinates"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        d(TAG, "onCreate()")

        val coordsSubmit = findViewById<View>(R.id.coordsSubmit)
        coordsSubmit.setOnClickListener(this)

        val coordsField = findViewById<EditText>(R.id.coordsField)
        coordsField.setOnEditorActionListener(CoordsActionListener())
        coordsField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(field: Editable) {
                try {
                    parseLocation()
                    show(R.id.coordsValid)
                    hide(R.id.coordsInvalid)
                } catch (e: Exception) {
                    show(R.id.coordsInvalid)
                    hide(R.id.coordsValid)
                }

            }

            override fun beforeTextChanged(string: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(string: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onClick(button: View) {
        when (button.id) {
            R.id.coordsSubmit -> {
                startSubmit()
                return
            }
        }
        super.onClick(button)
    }

    private fun startSubmit() {
        try {
            val location = parseLocation()
            startLookup(location)
        } catch (e: Exception) {
            e(TAG, "Parse error", e)
            Toast.makeText(applicationContext, "Invalid coordinates. Please try again.", Toast.LENGTH_LONG).show()
        }

    }

    @Throws(Exception::class)
    private fun parseLocation(): LatitudeLongitude {

        val coordsField = findViewById<EditText>(R.id.coordsField)
        val coordsValue = coordsField.text.toString().toUpperCase()

        val location: LatitudeLongitude

        if (coordsValue.matches("[NS][0-9]+(\\.[0-9]+)? [WE][0-9]+(\\.[0-9]+)?".toRegex())) {

            val lat = coordsValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val lon = coordsValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

            var latDbl = java.lang.Double.parseDouble(lat.substring(1))
            var lonDbl = java.lang.Double.parseDouble(lon.substring(1))
            if (lat.startsWith("S")) {
                latDbl = -latDbl
            }
            if (lon.startsWith("W")) {
                lonDbl = -lonDbl
            }

            location = LatitudeLongitude(latDbl, lonDbl)

        } else if (coordsValue.matches("-?[0-9]+(\\.[0-9]+)? -?[0-9]+(\\.[0-9]+)?".toRegex())) {

            val lat = coordsValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val lon = coordsValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val latDbl = java.lang.Double.parseDouble(lat)
            val lonDbl = java.lang.Double.parseDouble(lon)
            location = LatitudeLongitude(latDbl, lonDbl)

        } else {

            location = LatitudeLongitude(coordsValue)

        }

        return location

    }

    private fun startLookup(location: LatitudeLongitude) {
        d(TAG, "startLookup()")

        showDialog(DIALOG_LOOKINGUP)
        val activity = this
        // FIXME AsyncTask
        val thread = object : Thread() {
            override fun run() {
                val locationDetails = Geocoder.getLocationDetails(location, applicationContext)
                dismissDialog(DIALOG_LOOKINGUP)
                SharedPrefsHelper.saveSelectedLocation(activity, locationDetails)
                if (locationDetails.timeZone == null) {
                    val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
                    intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
                    startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
                } else {
                    setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
                    finish()
                }
            }
        }
        thread.start()
    }

    public override fun onCreateDialog(id: Int): Dialog {
        if (id == DIALOG_LOOKINGUP) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Looking up location...")
            progressDialog.setMessage("Looking up location, please wait. This can be disabled in settings.")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            return progressDialog
        }
        return super.onCreateDialog(id)
    }

    private inner class CoordsActionListener : OnEditorActionListener {
        override fun onEditorAction(view: TextView, id: Int, arg2: KeyEvent): Boolean {
            if (id == EditorInfo.IME_ACTION_GO) {
                startSubmit()
                return true
            }
            return false
        }
    }

    companion object {

        private val TAG = CoordsActivity::class.java.simpleName

        val DIALOG_LOOKINGUP = 101
    }

}