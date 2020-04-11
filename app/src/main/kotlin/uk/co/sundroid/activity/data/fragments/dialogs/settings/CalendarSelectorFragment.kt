package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.CalendarView
import uk.co.sundroid.activity.data.fragments.CalendarView.*
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
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
        val context = activity
        val view = View.inflate(context, R.layout.dialog_calendarselector, null)
        view.findViewById<View>(R.id.calendarSunRiseSetList).setOnClickListener { setCalendarView(SUN_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarSunRiseSetGrid).setOnClickListener { setCalendarView(SUN_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarCivilDawnDuskList).setOnClickListener { setCalendarView(CIVIL_DAWN_DUSK_LIST) }
        view.findViewById<View>(R.id.calendarCivilDawnDuskGrid).setOnClickListener { setCalendarView(CIVIL_DAWN_DUSK_GRID) }
        view.findViewById<View>(R.id.calendarNauticalDawnDuskList).setOnClickListener { setCalendarView(NAUTICAL_DAWN_DUSK_LIST) }
        view.findViewById<View>(R.id.calendarNauticalDawnDuskGrid).setOnClickListener { setCalendarView(NAUTICAL_DAWN_DUSK_GRID) }
        view.findViewById<View>(R.id.calendarAstronomicalDawnDuskList).setOnClickListener { setCalendarView(ASTRONOMICAL_DAWN_DUSK_LIST) }
        view.findViewById<View>(R.id.calendarAstronomicalDawnDuskGrid).setOnClickListener { setCalendarView(ASTRONOMICAL_DAWN_DUSK_GRID) }
        view.findViewById<View>(R.id.calendarDaylightList).setOnClickListener { setCalendarView(LENGTH_OF_DAYLIGHT_LIST) }
        view.findViewById<View>(R.id.calendarDaylightGrid).setOnClickListener { setCalendarView(LENGTH_OF_DAYLIGHT_GRID) }
        view.findViewById<View>(R.id.calendarMoonRiseSetList).setOnClickListener { setCalendarView(MOON_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarMoonRiseSetGrid).setOnClickListener { setCalendarView(MOON_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarMercuryRiseSetList).setOnClickListener { setCalendarView(MERCURY_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarMercuryRiseSetGrid).setOnClickListener { setCalendarView(MERCURY_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarVenusRiseSetList).setOnClickListener { setCalendarView(VENUS_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarVenusRiseSetGrid).setOnClickListener { setCalendarView(VENUS_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarMarsRiseSetList).setOnClickListener { setCalendarView(MARS_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarMarsRiseSetGrid).setOnClickListener { setCalendarView(MARS_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarJupiterRiseSetList).setOnClickListener { setCalendarView(JUPITER_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarJupiterRiseSetGrid).setOnClickListener { setCalendarView(JUPITER_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarSaturnRiseSetList).setOnClickListener { setCalendarView(SATURN_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarSaturnRiseSetGrid).setOnClickListener { setCalendarView(SATURN_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarUranusRiseSetList).setOnClickListener { setCalendarView(URANUS_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarUranusRiseSetGrid).setOnClickListener { setCalendarView(URANUS_RISE_SET_GRID) }
        view.findViewById<View>(R.id.calendarNeptuneRiseSetList).setOnClickListener { setCalendarView(NEPTUNE_RISE_SET_LIST) }
        view.findViewById<View>(R.id.calendarNeptuneRiseSetGrid).setOnClickListener { setCalendarView(NEPTUNE_RISE_SET_GRID) }

        return AlertDialog.Builder(activity).apply {
            setView(view)
        }.create()
    }

    companion object {
        fun newInstance(): CalendarSelectorFragment {
            return CalendarSelectorFragment()
        }
    }

}
