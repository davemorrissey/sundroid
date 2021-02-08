package uk.co.sundroid.activity.data.fragments

import android.view.View
import kotlinx.android.synthetic.main.inc_zonebutton.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.log.e
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.util.time.clone
import java.util.*
import java.util.Calendar.*

/**
 * Parent class for fragments that show data.
 */
abstract class AbstractDataFragment : AbstractFragment(), OnViewPrefsChangedListener {

    protected var isSafe: Boolean = true
        get() = activity != null && !isDetached

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarTitle(getLocation().name ?: getLocation().location.toString())
    }

    fun getLocation(): LocationDetails {
        return Prefs.selectedLocation(requireContext())!!
    }

    fun getDateCalendar(): Calendar {
        return (activity as MainActivity).dateCalendar
    }

    fun getDateCalendarClone(): Calendar {
        return clone(getDateCalendar())
    }

    fun getTimeCalendar(): Calendar {
        return (activity as MainActivity).timeCalendar
    }

    fun calendarDiff(field: Int, diff: Int) {
        var dateChanged = false
        var timeChanged = false
        if (field == DAY_OF_MONTH || field == MONTH || field == YEAR) {
            dateChanged = true
            arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.add(field, diff) }
        } else if (field == MINUTE || field == HOUR_OF_DAY) {
            val dayOfYear = getTimeCalendar().get(DAY_OF_YEAR)
            getTimeCalendar().add(field, diff)
            timeChanged = true
            if (getTimeCalendar().get(DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(DAY_OF_MONTH, if (diff > 0) 1 else -1)
                dateChanged = true
            }
        }
        update(dateChanged, timeChanged)
    }

    fun calendarSet(year: Int, month: Int, day: Int) {
        arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.set(year, month, day) }
        update(dateChanged = true, timeChanged = false)
    }

    protected fun updateTimeZone() {
        val location = getLocation()
        val timeZone = location.timeZone ?: TimeZoneResolver.getTimeZone(null)
        val calendar = getDateCalendar()
        if (Prefs.showTimeZone(requireContext())) {
            zoneButton?.visibility = View.VISIBLE
            val zone = timeZone.zone
            val dst = zone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
            val name = zone.getDisplayName(dst, TimeZone.LONG)
            zoneName?.text = name

            var cities = timeZone.getOffset(calendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            timeZone.cities?.let {
                cities += " $it"
            }
            zoneCities?.text = cities
        } else {
            zoneButton?.visibility = View.GONE
        }
    }

    open fun initialise() {}

    open fun update(dateChanged: Boolean = true, timeChanged: Boolean = true) {}

    override fun onViewPrefsUpdated() {
        try {
            initialise()
            update()
        } catch (e: Exception) {
            e(TAG, "Initialise for settings change failed", e)
        }
    }

    companion object {
        private val TAG = AbstractDataFragment::class.java.simpleName
    }

}
