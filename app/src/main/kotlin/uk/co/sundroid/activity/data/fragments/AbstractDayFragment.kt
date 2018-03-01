package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.inc_datebar.*
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH

abstract class AbstractDayFragment : AbstractDataFragment(), DatePickerFragment.OnDateSelectedListener {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    private var dateDetector: GestureDetector? = null

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGestures()
        updateDate()
        update()
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateDate()
            update(view)
        }
    }

    override fun onDateSet(year: Int, month: Int, date: Int) {
        getDateCalendar().set(year, month, date)
        getTimeCalendar().set(year, month, date)
        update()
    }

    private fun initGestures() {
        if (dateDetector == null) {
            val dateListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() = calendarDiff(MONTH, -1)
                override fun onButtonDragDown() = calendarDiff(MONTH, 1)
                override fun onButtonDragLeft() = calendarDiff(DAY_OF_MONTH, -1)
                override fun onButtonDragRight() = calendarDiff(DAY_OF_MONTH, 1)
            }
            dateDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(dateListener, applicationContext!!))
        }

        datePrev.setOnClickListener { _ -> calendarDiff(DAY_OF_MONTH, -1) }
        dateNext.setOnClickListener { _ -> calendarDiff(DAY_OF_MONTH, 1) }
        zoneButton.setOnClickListener { _ -> startTimeZone() }
        dateButton.setOnClickListener { _ -> showDatePicker() }
        dateButton.setOnTouchListener { _, e -> dateDetector?.onTouchEvent(e) ?: false }
    }

    private fun updateDate() {
        val location = getLocation()
        val calendar = getDateCalendar()
        if (SharedPrefsHelper.getShowTimeZone(applicationContext!!)) {
            zoneButton.visibility = VISIBLE
            val zone = location.timeZone!!.zone
            val dst = zone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
            val name = zone.getDisplayName(dst, TimeZone.LONG)
            zoneName.text = name

            var cities = location.timeZone!!.getOffset(calendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            if (isNotEmpty(location.timeZone!!.cities)) {
                cities += " " + location.timeZone!!.cities!!
            }
            zoneCities.text = cities
        } else {
            zoneButton.visibility = GONE
        }

        dateFormat.timeZone = calendar.timeZone
        weekdayFormat.timeZone = calendar.timeZone
        val date = dateFormat.format(Date(calendar.timeInMillis))
        val weekday = weekdayFormat.format(Date(calendar.timeInMillis))
        dateDMY.text = date
        dateWeekday.text = weekday
    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment.newInstance(getDateCalendar())
        datePickerFragment.setTargetFragment(this, 0)
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    protected abstract fun update(view: View)

}
