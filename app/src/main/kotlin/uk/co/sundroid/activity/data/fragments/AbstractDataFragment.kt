package uk.co.sundroid.activity.data.fragments

import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.inc_datebar.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.activity.location.TimeZonePickerActivity
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.log.e
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import java.util.*

/**
 * Parent class for fragments that show data.
 */
abstract class AbstractDataFragment : AbstractFragment(), OnViewPrefsChangedListener {

    protected var isSafe: Boolean = true
//        get() = activity != null && !isDetached

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

    fun getTimeCalendar(): Calendar {
        return (activity as MainActivity).timeCalendar
    }

    fun calendarDiff(field: Int, diff: Int) {
        arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.add(field, diff) }
        update()
    }

    fun calendarSet(year: Int, month: Int, day: Int) {
        arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.set(year, month, day) }
        update()
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

    open fun update() {}

    override fun onViewPrefsUpdated() {
        try {
            initialise()
            update()
        } catch (e: Exception) {
            e(TAG, "Initialise for settings change failed", e)
        }
    }

    protected fun startTimeZone() {
        val intent = Intent(activity, TimeZonePickerActivity::class.java)
        intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_CHANGE)
        requireActivity().startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
    }

    companion object {
        private val TAG = AbstractDataFragment::class.java.simpleName
    }

}
