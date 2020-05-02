package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.LocSavedBinding
import uk.co.sundroid.databinding.LocSavedRowBinding
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.geometry.Accuracy
import uk.co.sundroid.util.prefs.Prefs


class LocationListFragment : AbstractFragment() {

    private lateinit var b: LocSavedBinding

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
        b = LocSavedBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateSavedLocations()
    }

    private fun populateSavedLocations() {
        val db = this.db ?: return

        val locations = db.savedLocations
        b.savedLocationsList.removeAllViews()
        b.savedLocationsList.visibility = if (locations.isEmpty()) GONE else VISIBLE
        b.savedLocationsNone.visibility = if (locations.isEmpty()) VISIBLE else GONE

        for (location in locations) {
            val row = LocSavedRowBinding.inflate(layoutInflater)
            row.apply {
                savedLocName.text = location.name
                savedLocCoords.text = location.location.getPunctuatedValue(Accuracy.MINUTES)
                savedLocText.setOnClickListener { onLocationSelected(location) }
                savedLocDelete.setOnClickListener { confirmDelete(location) }
            }
            b.savedLocationsList.addView(row.root)
            layoutInflater.inflate(R.layout.divider, b.savedLocationsList)
        }
    }

    private fun confirmDelete(location: LocationDetails) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Delete ${location.displayName}?")
            .setPositiveButton("OK") { _, _ -> run {
                db?.deleteSavedLocation(location.id)
                populateSavedLocations()
                val currentLocation = Prefs.selectedLocation(requireContext())
                currentLocation?.let {
                    if (it.id == location.id) {
                        currentLocation.id = 0
                        Prefs.saveSelectedLocation(requireContext(), currentLocation)
                    }
                }
            }}
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

}
