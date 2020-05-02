package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment

import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.databinding.DialogTrackersettingsBinding
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.prefs.Prefs

// TODO retain state on orientation change
class TrackerSettingsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = DialogTrackersettingsBinding.inflate(layoutInflater)

        val body = view.trackerSettingBody
        val bodyAdapter = ArrayAdapter(requireContext(), R.layout.dialog_trackersettings_spinneritem, resources.getStringArray(R.array.trackerSettingBodyNames))
        bodyAdapter.setDropDownViewResource(R.layout.dialog_trackersettings_spinneritem_dropdown)
        body.adapter = bodyAdapter

        val currentBody = Prefs.sunTrackerBody(requireContext())
        if (currentBody != null) {
            body.setSelection(currentBody.ordinal)
        } else {
            body.setSelection(Body.values().size)
        }

        view.trackerSettingMap.isChecked = Prefs.sunTrackerMode(requireContext()) == "map"
        view.trackerSettingMap.setOnCheckedChangeListener { _, checked ->
            view.trackerSettingCompassWrapper.visibility = if (checked) View.GONE else View.VISIBLE
            view.trackerSettingMapTypeWrapper.visibility = if (checked) View.VISIBLE else View.GONE
        }

        val mapTypeAdapter = ArrayAdapter(requireContext(), R.layout.dialog_trackersettings_spinneritem, MapType.displayNames())
        mapTypeAdapter.setDropDownViewResource(R.layout.dialog_trackersettings_spinneritem_dropdown)
        view.trackerSettingMapType.adapter = mapTypeAdapter

        val currentMapType = Prefs.sunTrackerMapType(requireContext())
        view.trackerSettingMapType.setSelection(currentMapType.ordinal)

        view.trackerSettingCompass.isChecked = Prefs.sunTrackerCompass(requireContext())
        view.trackerSettingLinearElevation.isChecked = Prefs.sunTrackerLinearElevation(requireContext())
        view.trackerSettingHourMarkers.isChecked = Prefs.sunTrackerHourMarkers(requireContext())
        view.trackerSettingText.isChecked = Prefs.sunTrackerText(requireContext())

        if (Prefs.sunTrackerMode(requireContext()) == "map") {
            view.trackerSettingCompassWrapper.visibility = View.GONE
        } else {
            view.trackerSettingMapTypeWrapper.visibility = View.GONE
        }

        return AlertDialog.Builder(activity).apply {
            setTitle("Settings")
            setView(view.root)
            setNegativeButton("Cancel", null)
            setPositiveButton("OK") { _, _ ->
                if (body.selectedItemPosition >= Body.values().size) {
                    Prefs.setSunTrackerBody(requireContext(), null)
                } else {
                    Prefs.setSunTrackerBody(requireContext(), Body.values()[body.selectedItemPosition])
                }

                if (view.trackerSettingMap.isChecked) {
                    Prefs.setSunTrackerMode(requireContext(), "map")
                    Prefs.setSunTrackerMapType(requireContext(), MapType.values()[view.trackerSettingMapType.selectedItemPosition])
                } else {
                    Prefs.setSunTrackerMode(requireContext(), "radar")
                }
                Prefs.setSunTrackerCompass(requireContext(), view.trackerSettingCompass.isChecked)
                Prefs.setSunTrackerLinearElevation(requireContext(), view.trackerSettingLinearElevation.isChecked)
                Prefs.setSunTrackerHourMarkers(requireContext(), view.trackerSettingHourMarkers.isChecked)
                Prefs.setSunTrackerText(requireContext(), view.trackerSettingText.isChecked)

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
