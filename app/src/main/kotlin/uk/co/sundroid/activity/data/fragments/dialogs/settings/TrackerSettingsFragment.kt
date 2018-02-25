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
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.prefs.SharedPrefsHelper

import java.util.Arrays

class TrackerSettingsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true

        val context = activity
        val view = View.inflate(context, R.layout.dialog_trackersettings, null)

        val body = view.findViewById<Spinner>(R.id.trackerSettingBody)
        val currentBody = SharedPrefsHelper.getSunTrackerBody(context)
        if (currentBody != null) {
            body.setSelection(currentBody.ordinal)
        } else {
            body.setSelection(Body.values().size)
        }

        val map = view.findViewById<CheckBox>(R.id.trackerSettingMap)
        map.isChecked = SharedPrefsHelper.getSunTrackerMode(context) == "map"
        map.setOnCheckedChangeListener { b, checked ->
            if (checked) {
                view.findViewById<View>(R.id.trackerSettingCompassWrapper).visibility = View.GONE
                view.findViewById<View>(R.id.trackerSettingMapModeWrapper).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.trackerSettingMapModeWrapper).visibility = View.GONE
                view.findViewById<View>(R.id.trackerSettingCompassWrapper).visibility = View.VISIBLE
            }
        }

        val availableMapModes = Arrays.asList("Map", "Satellite", "Terrain", "Hybrid")
        val mapMode = view.findViewById<Spinner>(R.id.trackerSettingMapMode)
        val mapModeAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, availableMapModes)
        mapModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapMode.adapter = mapModeAdapter

        val currentMapMode = SharedPrefsHelper.getSunTrackerMapMode(context)
        when (currentMapMode) {
            "normal" -> mapMode.setSelection(0)
            "satellite" -> mapMode.setSelection(1)
            "terrain" -> mapMode.setSelection(2)
            "hybrid" -> mapMode.setSelection(3)
            else -> mapMode.setSelection(0)
        }

        val compass = view.findViewById<CheckBox>(R.id.trackerSettingCompass)
        compass.isChecked = SharedPrefsHelper.getSunTrackerCompass(context)

        val linearElevation = view.findViewById<CheckBox>(R.id.trackerSettingLinearElevation)
        linearElevation.isChecked = SharedPrefsHelper.getSunTrackerLinearElevation(context)

        val hourMarkers = view.findViewById<CheckBox>(R.id.trackerSettingHourMarkers)
        hourMarkers.isChecked = SharedPrefsHelper.getSunTrackerHourMarkers(context)

        val text = view.findViewById<CheckBox>(R.id.trackerSettingText)
        text.isChecked = SharedPrefsHelper.getSunTrackerText(context)

        if (SharedPrefsHelper.getSunTrackerMode(context) == "map") {
            view.findViewById<View>(R.id.trackerSettingCompassWrapper).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.trackerSettingMapModeWrapper).visibility = View.GONE
        }

        val builder = AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Settings")
        builder.setView(view)
        builder.setNegativeButton("Cancel", null)
        builder.setPositiveButton("OK") { _, _ ->
            if (body.selectedItemPosition >= Body.values().size) {
                SharedPrefsHelper.setSunTrackerBody(context, null)
            } else {
                SharedPrefsHelper.setSunTrackerBody(context, Body.values()[body.selectedItemPosition])
            }

            if (map.isChecked) {
                SharedPrefsHelper.setSunTrackerMode(context, "map")
                if (mapMode.selectedItemPosition == 0) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "normal")
                } else if (mapMode.selectedItemPosition == 1) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "satellite")
                } else if (mapMode.selectedItemPosition == 2) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "terrain")
                } else if (mapMode.selectedItemPosition == 3) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "hybrid")
                }
            } else {
                SharedPrefsHelper.setSunTrackerMode(context, "radar")
            }
            SharedPrefsHelper.setSunTrackerCompass(context, compass.isChecked)
            SharedPrefsHelper.setSunTrackerLinearElevation(context, linearElevation.isChecked)
            SharedPrefsHelper.setSunTrackerHourMarkers(context, hourMarkers.isChecked)
            SharedPrefsHelper.setSunTrackerText(context, text.isChecked)

            val parent = targetFragment
            if (parent != null && parent is OnViewPrefsChangedListener) {
                (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
            }
            dismiss()
        }
        return builder.create()
    }

    companion object {

        fun newInstance(): TrackerSettingsFragment {
            return TrackerSettingsFragment()
        }
    }

}
