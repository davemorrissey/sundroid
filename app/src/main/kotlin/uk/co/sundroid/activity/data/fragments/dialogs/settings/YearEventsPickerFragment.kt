package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.util.prefs.Prefs

class YearEventsPickerFragment : DialogFragment() {

    private enum class Setting(val ref: String, val displayName: String) {
        NEW_MOON("yearNewMoon", "New moons"),
        FULL_MOON("yearFullMoon", "Full moons"),
        QUARTER_MOON("yearQuarterMoon", "Quarter moons"),
        SOLSTICE("yearSolstice", "Solstices"),
        EQUINOX("yearEquinox", "Equinoxes"),
        LUNAR_ECLIPSE("yearLunarEclipse", "Lunar eclipses"),
        SOLAR_ECLIPSE("yearSolarEclipse", "Solar eclipses"),
        EARTH_APSIS("yearEarthApsis", "Earth aphelion and perihelion")
    }

    private val currentEvents = BooleanArray(Setting.values().size, { true })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restore(arguments)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Companion.save(currentEvents, outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restore(savedInstanceState)

        val builder = AlertDialog.Builder(activity)
        builder.setMultiChoiceItems(
                Setting.values().map { it.displayName }.toTypedArray(),
                currentEvents,
                { _, i, value -> currentEvents[i] = value })
        builder.setTitle("Select events")
        builder.setPositiveButton("OK", { _, _ -> run {
            Setting.values().forEachIndexed { i, setting -> Prefs.setShowElement(activity, setting.ref, currentEvents[i]) }
            val parent = targetFragment
            if (parent is OnViewPrefsChangedListener) {
                (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
            }
        }})
        builder.setNegativeButton("Cancel", null)
        return builder.create()
    }

    private fun restore(bundle: Bundle?) {
        if (bundle != null) {
            val events = bundle.getBooleanArray("events")
            events?.forEachIndexed { i, on -> currentEvents[i] = on }
        }
    }

    companion object {
        fun newInstance(context: Context): YearEventsPickerFragment {
            val currentEvents = BooleanArray(Setting.values().size)
            Setting.values().forEachIndexed { i, setting -> currentEvents[i] = Prefs.showElement(context, setting.ref, true) }
            return YearEventsPickerFragment().apply {
                arguments = save(currentEvents)
            }
        }

        private fun save(events: BooleanArray, bundle: Bundle? = Bundle()): Bundle? {
            return bundle?.apply {
                putBooleanArray("events", events)
            }
        }
    }

}