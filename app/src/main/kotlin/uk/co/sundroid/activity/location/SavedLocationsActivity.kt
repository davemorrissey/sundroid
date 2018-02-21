package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import uk.co.sundroid.R
import uk.co.sundroid.R.attr
import uk.co.sundroid.R.id
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.geometry.Accuracy

class SavedLocationsActivity : AbstractLocationActivity(), OnClickListener, DialogInterface.OnClickListener {

    private var db: DatabaseHelper? = null

    private var contextSavedLocationId: Long = 0

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
    }

    override fun onClick(view: View) {
        val savedLocationId = view.getTag(TAG_LOCATION_ID) as String?
        val action = view.getTag(TAG_ACTION) as String?
        if (savedLocationId != null && action != null && db != null) {
            val locationDetails = db?.getSavedLocation(Integer.parseInt(savedLocationId))
            if (action == ACTION_VIEW) {
                SharedPrefsHelper.saveSelectedLocation(this, locationDetails!!)
                if (locationDetails.timeZone == null) {
                    val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
                    intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
                    startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
                } else {
                    setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
                    finish()
                }
            } else if (action == ACTION_DELETE) {
                confirmDelete(Integer.parseInt(savedLocationId))
            }
            return
        }
        super.onClick(view)
    }

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            delete()
        }
        dialogInterface.dismiss()
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
            val name = row.findViewById<TextView>(R.id.savedLocName)
            name.text = location.name
            val coords = row.findViewById<TextView>(R.id.savedLocCoords)
            coords.text = location.location.getPunctuatedValue(Accuracy.MINUTES)

            row.findViewById<View>(id.savedLocText).setTag(TAG_LOCATION_ID, location.id.toString())
            row.findViewById<View>(id.savedLocText).setTag(TAG_ACTION, ACTION_VIEW)
            row.findViewById<View>(id.savedLocText).setOnClickListener(this)
            row.findViewById<View>(id.savedLocDelete).setTag(TAG_LOCATION_ID, location.id.toString())
            row.findViewById<View>(id.savedLocDelete).setTag(TAG_ACTION, ACTION_DELETE)
            row.findViewById<View>(id.savedLocDelete).setOnClickListener(this)
            list.addView(row)

            first = false
        }

    }

    private fun confirmDelete(savedLocationId: Int) {
        contextSavedLocationId = savedLocationId.toLong()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Delete this saved location?")
        builder.setPositiveButton("OK", this)
        builder.setNegativeButton("Cancel", this)
        builder.create().show()
    }

    private fun delete() {
        if (contextSavedLocationId > 0) {
            db?.deleteSavedLocation(contextSavedLocationId)
            populateSavedLocations()
        }
    }

    companion object {
        private const val TAG_ACTION = attr.sundroid_custom_1
        private const val TAG_LOCATION_ID = attr.sundroid_custom_2
        private const val ACTION_DELETE = "delete"
        private const val ACTION_VIEW = "view"
    }

}
