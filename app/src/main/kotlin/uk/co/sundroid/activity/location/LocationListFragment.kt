package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.loc_saved.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.geometry.Accuracy


class LocationListFragment : AbstractFragment() {

    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
        db = null
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Saved locations")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return when (container) {
            null -> null
            else -> inflater.inflate(R.layout.loc_saved, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateSavedLocations()
    }

    private fun populateSavedLocations() {
        val db = this.db ?: return

        val locations = db.savedLocations
        savedLocationsList.removeAllViews()
        savedLocationsList.visibility = if (locations.isEmpty()) GONE else VISIBLE
        savedLocationsNone.visibility = if (locations.isEmpty()) VISIBLE else GONE

        for (location in locations) {
            val row = View.inflate(requireContext(), R.layout.loc_saved_row, null)
            row.apply {
                findViewById<TextView>(R.id.savedLocName).text = location.name
                findViewById<TextView>(R.id.savedLocCoords).text = location.location.getPunctuatedValue(Accuracy.MINUTES)
                findViewById<View>(R.id.savedLocText).setOnClickListener { onLocationSelected(location) }
                findViewById<View>(R.id.savedLocDelete).setOnClickListener { confirmDelete(location) }
            }
            savedLocationsList.addView(row)
            layoutInflater.inflate(R.layout.divider, savedLocationsList)
        }
    }

    private fun confirmDelete(location: LocationDetails) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Delete ${location.displayName}?")
            .setPositiveButton("OK") { _, _ -> run {
                db?.deleteSavedLocation(location.id)
                populateSavedLocations()
            }}
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

}
