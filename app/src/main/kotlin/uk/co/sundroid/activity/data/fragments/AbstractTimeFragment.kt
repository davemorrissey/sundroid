package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment
import uk.co.sundroid.activity.data.fragments.dialogs.date.TimePickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.*
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.time.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class AbstractTimeFragment : AbstractDataFragment(), OnClickListener, OnTouchListener, OnSeekBarChangeListener, DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    private var dateDetector: GestureDetector? = null
    private var timeDetector: GestureDetector? = null

    protected abstract val layout: Int

    @Throws(Exception::class)
    protected abstract fun initialise(location: LocationDetails, dateCalendar: Calendar, timeCalendar: Calendar, view: View)

    @Throws(Exception::class)
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
        dateCalendar!!.set(year, month, date)
        timeCalendar!!.set(year, month, date)
        update()
    }

    override fun onTimeSet(hour: Int, minute: Int) {
        timeCalendar!!.set(Calendar.HOUR_OF_DAY, hour)
        timeCalendar!!.set(Calendar.MINUTE, minute)
        update()
    }

    override fun onClick(button: View) {
        when (button.id) {
            R.id.timeButton -> {
                showTimePicker()
                return
            }
            R.id.dateButton -> {
                showDatePicker()
                return
            }
            R.id.zoneButton -> {
                startTimeZone()
                return
            }
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (view.id == R.id.dateButton) {
            return dateDetector!!.onTouchEvent(event)
        } else if (view.id == R.id.timeButton) {
            return timeDetector!!.onTouchEvent(event)
        }
        return false
    }

    private fun safeInitialise(view: View?) {
        val location = location
        val dateCalendar = dateCalendar
        val timeCalendar = timeCalendar
        try {
            if (location != null && dateCalendar != null && timeCalendar != null && view != null && !isDetached) {
                initGestures(view)
                updateDateAndTime(view, dateCalendar, timeCalendar)
                initialise(location, dateCalendar, timeCalendar, view)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to init data view", e)
        }

    }

    private fun safeUpdate(view: View?, timeOnly: Boolean) {
        val location = location
        val dateCalendar = dateCalendar
        val timeCalendar = timeCalendar
        try {
            if (location != null && dateCalendar != null && timeCalendar != null && view != null && !isDetached) {
                updateDateAndTime(view, dateCalendar, timeCalendar)
                update(location, dateCalendar, timeCalendar, view, timeOnly)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update data view", e)
        }

    }

    private fun initGestures(view: View) {
        if (dateDetector == null) {
            val dateListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() {
                    prevMonth()
                }

                override fun onButtonDragDown() {
                    nextMonth()
                }

                override fun onButtonDragLeft() {
                    prevDate()
                }

                override fun onButtonDragRight() {
                    nextDate()
                }
            }
            dateDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(dateListener, applicationContext!!))
        }
        view.findViewById<View>(R.id.dateButton).setOnClickListener(this)
        view.findViewById<View>(R.id.dateButton).setOnTouchListener(this)
        view.findViewById<View>(R.id.zoneButton).setOnClickListener(this)

        if (timeDetector == null) {
            val timeListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() {
                    prevHour()
                }

                override fun onButtonDragDown() {
                    nextHour()
                }

                override fun onButtonDragLeft() {
                    prevMinute()
                }

                override fun onButtonDragRight() {
                    nextMinute()
                }
            }
            timeDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(timeListener, applicationContext!!))
        }
        view.findViewById<View>(R.id.timeButton).setOnClickListener(this)
        view.findViewById<View>(R.id.timeButton).setOnTouchListener(this)
        (view.findViewById<View>(R.id.timeSeeker) as SeekBar).setOnSeekBarChangeListener(this)
    }

    private fun updateDateAndTime(view: View?, dateCalendar: Calendar, timeCalendar: Calendar) {
        val location = location
        if (location == null || view == null) {
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
        val datePickerFragment = DatePickerFragment.newInstance(dateCalendar!!)
        datePickerFragment.setTargetFragment(this, 0)
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    private fun showTimePicker() {
        val timePickerFragment = TimePickerFragment.newInstance(timeCalendar!!)
        timePickerFragment.setTargetFragment(this, 0)
        timePickerFragment.show(fragmentManager, "timePicker")
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && timeCalendar != null) {
            val hours = progress / 60
            val minutes = progress - hours * 60
            timeCalendar!!.set(Calendar.HOUR_OF_DAY, hours)
            timeCalendar!!.set(Calendar.MINUTE, minutes)
            safeUpdate(view, false)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // No action.
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // No action.
    }

    private fun nextDate() {
        dateCalendar!!.add(Calendar.DAY_OF_MONTH, 1)
        timeCalendar!!.add(Calendar.DAY_OF_MONTH, 1)
        safeUpdate(view, false)
    }

    private fun nextMonth() {
        dateCalendar!!.add(Calendar.MONTH, 1)
        timeCalendar!!.add(Calendar.MONTH, 1)
        safeUpdate(view, false)
    }

    private fun prevDate() {
        dateCalendar!!.add(Calendar.DAY_OF_MONTH, -1)
        timeCalendar!!.add(Calendar.DAY_OF_MONTH, -1)
        safeUpdate(view, false)
    }

    private fun prevMonth() {
        dateCalendar!!.add(Calendar.MONTH, -1)
        timeCalendar!!.add(Calendar.MONTH, -1)
        safeUpdate(view, false)
    }

    private fun nextMinute() {
        if (view != null) {
            val dayOfYear = timeCalendar!!.get(Calendar.DAY_OF_YEAR)
            timeCalendar!!.add(Calendar.MINUTE, 1)
            val minutes = timeCalendar!!.get(Calendar.HOUR_OF_DAY) * 60 + timeCalendar!!.get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (timeCalendar!!.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                dateCalendar!!.add(Calendar.DAY_OF_MONTH, 1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun prevMinute() {
        if (view != null) {
            val dayOfYear = timeCalendar!!.get(Calendar.DAY_OF_YEAR)
            timeCalendar!!.add(Calendar.MINUTE, -1)
            val minutes = timeCalendar!!.get(Calendar.HOUR_OF_DAY) * 60 + timeCalendar!!.get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (timeCalendar!!.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                dateCalendar!!.add(Calendar.DAY_OF_MONTH, -1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun nextHour() {
        if (view != null) {
            val dayOfYear = timeCalendar!!.get(Calendar.DAY_OF_YEAR)
            timeCalendar!!.add(Calendar.HOUR_OF_DAY, 1)
            val minutes = timeCalendar!!.get(Calendar.HOUR_OF_DAY) * 60 + timeCalendar!!.get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (timeCalendar!!.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                dateCalendar!!.add(Calendar.DAY_OF_MONTH, 1)
                safeUpdate(view, false)
            } else {
                safeUpdate(view, true)
            }
        }
    }

    private fun prevHour() {
        if (view != null) {
            val dayOfYear = timeCalendar!!.get(Calendar.DAY_OF_YEAR)
            timeCalendar!!.add(Calendar.HOUR_OF_DAY, -1)
            val minutes = timeCalendar!!.get(Calendar.HOUR_OF_DAY) * 60 + timeCalendar!!.get(Calendar.MINUTE)
            (view!!.findViewById<View>(R.id.timeSeeker) as SeekBar).progress = minutes
            if (timeCalendar!!.get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                dateCalendar!!.add(Calendar.DAY_OF_MONTH, -1)
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
