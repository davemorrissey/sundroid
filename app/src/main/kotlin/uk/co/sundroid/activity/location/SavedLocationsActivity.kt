package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.geometry.Accuracy

class SavedLocationsActivity : AbstractLocationActivity() {

    private var db: DatabaseHelper? = null

    override val layout: Int
        get() = R.layout.loc_saved

    override val viewTitle: String
        get() = "Saved locations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        populateSavedLocations()
    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
        db = null
    }

    private fun populateSavedLocations() {
        val db = this.db ?: return

        val locations = db.savedLocations
        val list = findViewById<LinearLayout>(id.savedLocationsList)
        list.removeAllViews()

        if (locations.size > 0) {
            show(id.savedLocationsList)
            remove(id.savedLocationsNone)
        } else {
            remove(id.savedLocationsList)
            show(id.savedLocationsNone)
        }

        var first = true
        for (location in locations) {
            if (!first) {
                layoutInflater.inflate(R.layout.divider, list)
            }
            val row = View.inflate(this, R.layout.loc_saved_row, null)
            row.findViewById<TextView>(R.id.savedLocName).text = location.name
            row.findViewById<TextView>(R.id.savedLocCoords).text = location.location.getPunctuatedValue(Accuracy.MINUTES)
            row.findViewById<View>(id.savedLocText).setOnClickListener { select(location) }
            row.findViewById<View>(id.savedLocDelete).setOnClickListener { confirmDelete(location) }
            list.addView(row)
            first = false
        }

    }

    private fun select(location: LocationDetails) {
        Prefs.saveSelectedLocation(this, location)
        if (location.timeZone == null) {
            val intent = Intent(this, TimeZonePickerActivity::class.java)
            intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
            startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
        } else {
            setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
            finish()
        }
    }

    private fun confirmDelete(location: LocationDetails) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Delete this saved location?")
        builder.setPositiveButton("OK", { _, _ -> delete(location) })
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }

    private fun delete(location: LocationDetails) {
        db?.deleteSavedLocation(location.id)
        populateSavedLocations()
    }

}
