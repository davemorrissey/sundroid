package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

import uk.co.sundroid.activity.data.fragments.CalendarView
import uk.co.sundroid.activity.data.fragments.CalendarView.*
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.databinding.DialogCalendarselectorBinding
import uk.co.sundroid.util.prefs.Prefs

class CalendarSelectorFragment : DialogFragment() {

    private fun setCalendarView(calendarView: CalendarView) {
        Prefs.setLastCalendar(requireContext(), calendarView)
        val parent = targetFragment
        if (parent != null && parent is OnViewPrefsChangedListener) {
            (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
        }
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = DialogCalendarselectorBinding.inflate(layoutInflater)
        mapOf(
            view.calendarSunRiseSetList to SUN_RISE_SET_LIST,
            view.calendarSunRiseSetGrid to SUN_RISE_SET_GRID,
            view.calendarCivilDawnDuskList to CIVIL_DAWN_DUSK_LIST,
            view.calendarCivilDawnDuskGrid to CIVIL_DAWN_DUSK_GRID,
            view.calendarNauticalDawnDuskList to NAUTICAL_DAWN_DUSK_LIST,
            view.calendarNauticalDawnDuskGrid to NAUTICAL_DAWN_DUSK_GRID,
            view.calendarAstronomicalDawnDuskList to ASTRONOMICAL_DAWN_DUSK_LIST,
            view.calendarAstronomicalDawnDuskGrid to ASTRONOMICAL_DAWN_DUSK_GRID,
            view.calendarDaylightList to LENGTH_OF_DAYLIGHT_LIST,
            view.calendarDaylightGrid to LENGTH_OF_DAYLIGHT_GRID,
            view.calendarMoonRiseSetList to MOON_RISE_SET_LIST,
            view.calendarMoonRiseSetGrid to MOON_RISE_SET_GRID,
            view.calendarMercuryRiseSetList to MERCURY_RISE_SET_LIST,
            view.calendarMercuryRiseSetGrid to MERCURY_RISE_SET_GRID,
            view.calendarVenusRiseSetList to VENUS_RISE_SET_LIST,
            view.calendarVenusRiseSetGrid to VENUS_RISE_SET_GRID,
            view.calendarMarsRiseSetList to MARS_RISE_SET_LIST,
            view.calendarMarsRiseSetGrid to MARS_RISE_SET_GRID,
            view.calendarJupiterRiseSetList to JUPITER_RISE_SET_LIST,
            view.calendarJupiterRiseSetGrid to JUPITER_RISE_SET_GRID,
            view.calendarSaturnRiseSetList to SATURN_RISE_SET_LIST,
            view.calendarSaturnRiseSetGrid to SATURN_RISE_SET_GRID,
            view.calendarUranusRiseSetList to URANUS_RISE_SET_LIST,
            view.calendarUranusRiseSetGrid to URANUS_RISE_SET_GRID,
            view.calendarNeptuneRiseSetList to NEPTUNE_RISE_SET_LIST,
            view.calendarNeptuneRiseSetGrid to NEPTUNE_RISE_SET_GRID
        ).forEach { (v, cv) -> v.setOnClickListener { setCalendarView(cv) } }

        return AlertDialog.Builder(activity).apply {
            setView(view.root)
        }.create()
    }

    companion object {
        fun newInstance(): CalendarSelectorFragment {
            return CalendarSelectorFragment()
        }
    }

}
