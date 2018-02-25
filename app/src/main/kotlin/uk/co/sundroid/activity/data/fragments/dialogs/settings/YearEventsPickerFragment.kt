package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.util.prefs.SharedPrefsHelper

class YearEventsPickerFragment : DialogFragment(), OnClickListener, OnMultiChoiceClickListener {

    private val currentEvents = BooleanArray(8)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true// TODO Redundant?

        currentEvents[0] = SharedPrefsHelper.getShowElement(activity, "yearNewMoon", true)
        currentEvents[1] = SharedPrefsHelper.getShowElement(activity, "yearFullMoon", true)
        currentEvents[2] = SharedPrefsHelper.getShowElement(activity, "yearQuarterMoon", true)
        currentEvents[3] = SharedPrefsHelper.getShowElement(activity, "yearSolstice", true)
        currentEvents[4] = SharedPrefsHelper.getShowElement(activity, "yearEquinox", true)
        currentEvents[5] = SharedPrefsHelper.getShowElement(activity, "yearLunarEclipse", true)
        currentEvents[6] = SharedPrefsHelper.getShowElement(activity, "yearSolarEclipse", true)
        currentEvents[7] = SharedPrefsHelper.getShowElement(activity, "yearEarthApsis", true)

        val builder = AlertDialog.Builder(activity)
        builder.setMultiChoiceItems(
                arrayOf<CharSequence>("New moons", "Full moons", "Quarter moons", "Solstices", "Equinoxes", "Lunar eclipses", "Solar eclipses", "Earth aphelion and perihelion"),
                currentEvents,
                this)
        builder.setTitle("Select events")
        builder.setPositiveButton("OK", this)
        builder.setNegativeButton("Cancel", this)
        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int, value: Boolean) {
        currentEvents[id] = value
    }

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            SharedPrefsHelper.setShowElement(activity, "yearNewMoon", currentEvents[0])
            SharedPrefsHelper.setShowElement(activity, "yearFullMoon", currentEvents[1])
            SharedPrefsHelper.setShowElement(activity, "yearQuarterMoon", currentEvents[2])
            SharedPrefsHelper.setShowElement(activity, "yearSolstice", currentEvents[3])
            SharedPrefsHelper.setShowElement(activity, "yearEquinox", currentEvents[4])
            SharedPrefsHelper.setShowElement(activity, "yearLunarEclipse", currentEvents[5])
            SharedPrefsHelper.setShowElement(activity, "yearSolarEclipse", currentEvents[6])
            SharedPrefsHelper.setShowElement(activity, "yearEarthApsis", currentEvents[7])

            val parent = targetFragment
            if (parent != null && parent is OnViewPrefsChangedListener) {
                (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
            }
        }
        dismiss()
    }

    companion object {

        fun newInstance(): YearEventsPickerFragment {
            return YearEventsPickerFragment()
        }
    }

}