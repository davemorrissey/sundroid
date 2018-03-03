package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner

import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.prefs.Prefs

// TODO retain state on orientation change
class TrackerSettingsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val view = View.inflate(context, R.layout.dialog_trackersettings, null)

        val body = view.findViewById<Spinner>(R.id.trackerSettingBody)
        val currentBody = Prefs.sunTrackerBody(context)
        if (currentBody != null) {
            body.setSelection(currentBody.ordinal)
        } else {
            body.setSelection(Body.values().size)
        }

        val map = view.findViewById<CheckBox>(R.id.trackerSettingMap)
        map.isChecked = Prefs.sunTrackerMode(context) == "map"
        map.setOnCheckedChangeListener { _, checked ->
            view.findViewById<View>(R.id.trackerSettingCompassWrapper).visibility = if (checked) View.GONE else View.VISIBLE
            view.findViewById<View>(R.id.trackerSettingMapTypeWrapper).visibility = if (checked) View.VISIBLE else View.GONE
        }

        val mapType = view.findViewById<Spinner>(R.id.trackerSettingMapType)
        val mapTypeAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, MapType.displayNames())
        mapTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapType.adapter = mapTypeAdapter

        val currentMapType = Prefs.sunTrackerMapType(context)
        mapType.setSelection(currentMapType.ordinal)

        val compass = view.findViewById<CheckBox>(R.id.trackerSettingCompass)
        compass.isChecked = Prefs.sunTrackerCompass(context)

        val linearElevation = view.findViewById<CheckBox>(R.id.trackerSettingLinearElevation)
        linearElevation.isChecked = Prefs.sunTrackerLinearElevation(context)

        val hourMarkers = view.findViewById<CheckBox>(R.id.trackerSettingHourMarkers)
        hourMarkers.isChecked = Prefs.sunTrackerHourMarkers(context)

        val text = view.findViewById<CheckBox>(R.id.trackerSettingText)
        text.isChecked = Prefs.sunTrackerText(context)

        if (Prefs.sunTrackerMode(context) == "map") {
            view.findViewById<View>(R.id.trackerSettingCompassWrapper).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.trackerSettingMapTypeWrapper).visibility = View.GONE
        }

        return AlertDialog.Builder(activity).apply {
            setTitle("Settings")
            setView(view)
            setNegativeButton("Cancel", null)
            setPositiveButton("OK") { _, _ ->
                if (body.selectedItemPosition >= Body.values().size) {
                    Prefs.setSunTrackerBody(context, null)
                } else {
                    Prefs.setSunTrackerBody(context, Body.values()[body.selectedItemPosition])
                }

                if (map.isChecked) {
                    Prefs.setSunTrackerMode(context, "map")
                    Prefs.setSunTrackerMapType(context, MapType.values()[mapType.selectedItemPosition])
                } else {
                    Prefs.setSunTrackerMode(context, "radar")
                }
                Prefs.setSunTrackerCompass(context, compass.isChecked)
                Prefs.setSunTrackerLinearElevation(context, linearElevation.isChecked)
                Prefs.setSunTrackerHourMarkers(context, hourMarkers.isChecked)
                Prefs.setSunTrackerText(context, text.isChecked)

                val parent = targetFragment
                if (parent != null && parent is OnViewPrefsChangedListener) {
                    (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
                }
            }
        }.create()
    }

    companion object {
        fun newInstance(): TrackerSettingsFragment {
            return TrackerSettingsFragment()
        }
    }

}
