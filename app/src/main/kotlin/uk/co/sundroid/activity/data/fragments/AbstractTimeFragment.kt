package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.android.synthetic.main.inc_timebar.*
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment
import uk.co.sundroid.activity.data.fragments.dialogs.date.TimePickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.log.e
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH

abstract class AbstractTimeFragment : AbstractDataFragment(), OnSeekBarChangeListener, DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    protected abstract val layout: Int

    protected abstract fun initialise(location: LocationDetails, dateCalendar: Calendar, timeCalendar: Calendar, view: View)

    protected abstract fun update(location: LocationDetails, dateCalendar: Calendar, timeCalendar: Calendar, view: View, timeOnly: Boolean)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(layout, container, false)
        safeInitialise(view)
        safeUpdate(view, false)
        return view
    }

    override fun initialise() {
        if (view != null) {
            safeInitialise(view)
        }
    }

    override fun update() {
        if (view != null) {
            safeUpdate(view, false)
        }
    }

    override fun onDateSet(year: Int, month: Int, date: Int) {
        getDateCalendar().set(year, month, date)
        getTimeCalendar().set(year, month, date)
        update()
    }

    override fun onTimeSet(hour: Int, minute: Int) {
        getTimeCalendar().set(Calendar.HOUR_OF_DAY, hour)
        getTimeCalendar().set(Calendar.MINUTE, minute)
        update()
    }

    private fun safeInitialise(view: View?) {
        val location = getLocation()
        val dateCalendar = getDateCalendar()
        val timeCalendar = getTimeCalendar()
        try {
            if (view != null && !isDetached) {
                initGestures(view)
                updateDateAndTime(view, dateCalendar, timeCalendar)
                initialise(location, dateCalendar, timeCalendar, view)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to init data view", e)
        }

    }

    private fun safeUpdate(view: View?, timeOnly: Boolean) {
        val location = getLocation()
        val dateCalendar = getDateCalendar()
        val timeCalendar = getTimeCalendar()
        try {
            if (view != null && !isDetached) {
                updateDateAndTime(view, dateCalendar, timeCalendar)
                update(location, dateCalendar, timeCalendar, view, timeOnly)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update data view", e)
        }

    }

    private fun initGestures(view: View) {
        val dateListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = changeCalendars(MONTH, -1)
            override fun onButtonDragDown() = changeCalendars(MONTH, 1)
            override fun onButtonDragLeft() = changeCalendars(DAY_OF_MONTH, -1)
            override fun onButtonDragRight() = changeCalendars(DAY_OF_MONTH, 1)
        }
        val dateDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(dateListener, applicationContext!!))
        zoneButton.setOnClickListener { startTimeZone() }
        dateButton.setOnClickListener { showDatePicker() }
        dateButton.setOnTouchListener { _, e -> dateDetector.onTouchEvent(e) }

        val timeListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = prevHour()
            override fun onButtonDragDown() = nextHour()
            override fun onButtonDragLeft() = prevMinute()
            override fun onButtonDragRight() = nextMinute()
        }
        val timeDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(timeListener, applicationContext!!))
        timeButton.setOnClickListener { showTimePicker() }
        timeButton.setOnTouchListener { _, e -> timeDetector.onTouchEvent(e) }
        timeSeeker.setOnSeekBarChangeListener(this)
    }

    private fun updateDateAndTime(view: View?, dateCalendar: Calendar, timeCalendar: Calendar) {
        val location = getLocation()
        if (view == null) {
            return
        }


        if (SharedPrefsHelper.getShowTimeZone(applicationContext!!)) {
            showInView(view, R.id.zoneButton)
            val zone = location.timeZone!!.zone
            val zoneDST = zone.inDaylightTime(Date(dateCalendar.timeInMillis + 12 * 60 * 60 * 1000))
            val zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG)
            textInView(view, R.id.zoneName, zoneName)

            var zoneCities = location.timeZone!!.getOffset(dateCalendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            if (isNotEmpty(location.timeZone!!.cities)) {
                zoneCities += " " + location.timeZone!!.cities!!
            }
            textInView(view, R.id.zoneCities, zoneCities)
        } else {
            removeInView(view, R.id.zoneButton)
        }

        val time = formatTime(applicationContext!!, timeCalendar, false, false)
        showInView(view, R.id.timeHM, time.time + time.marker)

        dateFormat.timeZone = dateCalendar.timeZone
        weekdayFormat.timeZone = dateCalendar.timeZone
        val date = dateFormat.format(Date(dateCalendar.timeInMillis))
        val weekday = weekdayFormat.format(Date(dateCalendar.timeInMillis))
        showInView(view, R.id.dateDMY, date)
        showInView(view, R.id.dateWeekday, weekday)

        val minutes = timeCalendar.get(Calendar.HOUR_OF_DAY) * 60 + timeCalendar.get(Calendar.MINUTE)
        (view.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment.newInstance(getDateCalendar())
        datePickerFragment.setTargetFragment(this, 0)
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    private fun showTimePicker() {
        val timePickerFragment = TimePickerFragment.newInstance(getTimeCalendar())
        timePickerFragment.setTargetFragment(this, 0)
        timePickerFragment.show(fragmentManager, "timePicker")
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val hours = progress / 60
            val minutes = progress - hours * 60
            getTimeCalendar().set(Calendar.HOUR_OF_DAY, hours)
            getTimeCalendar().set(Calendar.MINUTE, minutes)
            safeUpdate(view, false)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // No action.
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // No action.
    }

    private fun changeCalendars(field: Int, diff: Int) {
        arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.add(field, diff) }
        update()
    }

    private fun nextMinute() {
        if (view != null) {
            val dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR)
            getTimeCalendar().add(Calendar.MINUTE, 1)
            val minutes = getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60 + getTimeCalendar().get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, 1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun prevMinute() {
        if (view != null) {
            val dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR)
            getTimeCalendar().add(Calendar.MINUTE, -1)
            val minutes = getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60 + getTimeCalendar().get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, -1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun nextHour() {
        if (view != null) {
            val dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR)
            getTimeCalendar().add(Calendar.HOUR_OF_DAY, 1)
            val minutes = getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60 + getTimeCalendar().get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, 1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun prevHour() {
        if (view != null) {
            val dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR)
            getTimeCalendar().add(Calendar.HOUR_OF_DAY, -1)
            val minutes = getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60 + getTimeCalendar().get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, -1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    companion object {

        private val TAG = AbstractTimeFragment::class.java.simpleName
    }

}
