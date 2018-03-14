package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import uk.co.sundroid.R
import uk.co.sundroid.R.drawable
import uk.co.sundroid.activity.data.fragments.dialogs.date.YearPickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.*
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import uk.co.sundroid.util.log.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class AbstractYearFragment : AbstractDataFragment(), OnClickListener, OnTouchListener, YearPickerFragment.OnYearSelectedListener {

    private val yearFormat = SimpleDateFormat("yyyy", Locale.US)

    private var yearDetector: GestureDetector? = null

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(layout, container, false)
        safeUpdate(view)
        return view
    }

    override fun initialise() {
        d(TAG, hashCode().toString() + " initialise")
        if (view != null) {
            safeInit(view)
        }
    }

    override fun update() {
        if (view != null) {
            safeUpdate(view)
        }
    }

    override fun onYearSet(year: Int) {
        getDateCalendar().set(Calendar.YEAR, year)
        getTimeCalendar().set(Calendar.YEAR, year)
        update()
    }

    override fun onClick(button: View) {
        when (button.id) {
            R.id.yearPrev -> {
                prevYear()
                return
            }
            R.id.yearNext -> {
                nextYear()
                return
            }
            R.id.yearButton -> {
                showYearPicker()
                return
            }
            R.id.zoneButton -> {
                startTimeZone()
                return
            }
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return if (view.id == R.id.yearButton && yearDetector != null) {
            yearDetector!!.onTouchEvent(event)
        } else false
    }

    private fun safeInit(view: View?) {
        val location = getLocation()
        val calendar = getDateCalendar()
        try {
            if (view != null && !isDetached) {
                initGestures(view)
                updateYear(location, calendar, view)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update data view", e)
        }

    }

    private fun safeUpdate(view: View?) {
        val location = getLocation()
        val calendar = getDateCalendar()
        try {
            if (view != null && !isDetached) {
                initGestures(view)
                updateYear(location, calendar, view)
                update(location, calendar, view)
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update data view", e)
        }

    }

    private fun initGestures(view: View) {
        if (yearDetector == null) {
            val yearListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() {
                    prevYear()
                }

                override fun onButtonDragDown() {
                    nextYear()
                }

                override fun onButtonDragLeft() {
                    prevYear()
                }

                override fun onButtonDragRight() {
                    nextYear()
                }
            }
            yearDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(yearListener, applicationContext!!))
        }
        view.findViewById<View>(R.id.yearPrev).setOnClickListener(this)
        view.findViewById<View>(R.id.yearNext).setOnClickListener(this)
        view.findViewById<View>(R.id.yearButton).setOnClickListener(this)
        view.findViewById<View>(R.id.yearButton).setOnTouchListener(this)
        view.findViewById<View>(R.id.zoneButton).setOnClickListener(this)
    }

    private fun updateYear(location: LocationDetails, calendar: Calendar, view: View) {
        if (Prefs.showTimeZone(applicationContext!!)) {
            show(view, R.id.zoneButton)
            val zone = location.timeZone!!.zone
            val zoneDST = zone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
            val zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG)
            text(view, R.id.zoneName, zoneName)

            var zoneCities = location.timeZone!!.getOffset(calendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            if (isNotEmpty(location.timeZone!!.cities)) {
                zoneCities += " " + location.timeZone!!.cities!!
            }
            text(view, R.id.zoneCities, zoneCities)
        } else {
            remove(view, R.id.zoneButton)
        }

        yearFormat.timeZone = calendar.timeZone
        val year = yearFormat.format(Date(calendar.timeInMillis))
        show(view, R.id.year, year)

        if (calendar.get(Calendar.YEAR) <= 2000) {
            view.findViewById<View>(R.id.yearPrev).isEnabled = false
            image(view, R.id.yearPrev, drawable.navigation_previous_item_disabled)
        } else {
            view.findViewById<View>(R.id.yearPrev).isEnabled = true
            image(view, R.id.yearPrev, drawable.navigation_previous_item)
        }
        if (calendar.get(Calendar.YEAR) >= 2020) {
            view.findViewById<View>(R.id.yearNext).isEnabled = false
            image(view, R.id.yearNext, drawable.navigation_next_item_disabled)
        } else {
            view.findViewById<View>(R.id.yearNext).isEnabled = true
            image(view, R.id.yearNext, drawable.navigation_next_item)
        }
    }

    private fun showYearPicker() {
        val yearPickerFragment = YearPickerFragment.newInstance(getDateCalendar())
        yearPickerFragment.setTargetFragment(this, 0)
        yearPickerFragment.show(fragmentManager, "yearPicker")
    }

    private fun nextYear() {
        if (getDateCalendar().get(Calendar.YEAR) < 2020) {
            getDateCalendar().add(Calendar.YEAR, 1)
            getTimeCalendar().add(Calendar.YEAR, 1)
            update()
        }
    }

    private fun prevYear() {
        if (getDateCalendar().get(Calendar.YEAR) > 2000) {
            getDateCalendar().add(Calendar.YEAR, -1)
            getTimeCalendar().add(Calendar.YEAR, -1)
            update()
        }
    }

    @Throws(Exception::class)
    protected abstract fun update(location: LocationDetails, calendar: Calendar, view: View)

    companion object {

        private val TAG = AbstractYearFragment::class.java.simpleName
    }

}
