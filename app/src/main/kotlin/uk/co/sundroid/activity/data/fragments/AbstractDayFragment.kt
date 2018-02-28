package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.*
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.*
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import uk.co.sundroid.util.log.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class AbstractDayFragment : AbstractDataFragment(), DatePickerFragment.OnDateSelectedListener {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    private var dateDetector: GestureDetector? = null

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGestures(view)
        updateDate(view)
        update()
    }

    override fun initialise() {
//        val view = view
//        if (isSafe && view != null) {
//            initGestures(view)
//            updateDate(view)
//        }
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures(view)
            updateDate(view)
            update(view)
        }
    }

    override fun onDateSet(year: Int, month: Int, date: Int) {
        getDateCalendar().set(year, month, date)
        getTimeCalendar().set(year, month, date)
        update()
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

        view.findViewById<View>(R.id.datePrev).setOnClickListener { _ -> prevDate() }
        view.findViewById<View>(R.id.dateNext).setOnClickListener { _ -> nextDate() }
        view.findViewById<View>(R.id.zoneButton).setOnClickListener { _ -> startTimeZone() }
        view.findViewById<View>(R.id.dateButton).setOnClickListener { _ -> showDatePicker() }
        view.findViewById<View>(R.id.dateButton).setOnTouchListener { _, e -> dateDetector?.onTouchEvent(e) ?: false }
    }

    private fun updateDate(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()
        if (SharedPrefsHelper.getShowTimeZone(applicationContext!!)) {
            showInView(view, R.id.zoneButton)
            val zone = location.timeZone!!.zone
            val zoneDST = zone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
            val zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG)
            textInView(view, R.id.zoneName, zoneName)

            var zoneCities = location.timeZone!!.getOffset(calendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            if (isNotEmpty(location.timeZone!!.cities)) {
                zoneCities += " " + location.timeZone!!.cities!!
            }
            textInView(view, R.id.zoneCities, zoneCities)
        } else {
            removeInView(view, R.id.zoneButton)
        }

        dateFormat.timeZone = calendar.timeZone
        weekdayFormat.timeZone = calendar.timeZone
        val date = dateFormat.format(Date(calendar.timeInMillis))
        val weekday = weekdayFormat.format(Date(calendar.timeInMillis))
        showInView(view, R.id.dateDMY, date)
        showInView(view, R.id.dateWeekday, weekday)
    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment.newInstance(getDateCalendar())
        datePickerFragment.setTargetFragment(this, 0)
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    private fun nextDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, 1)
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, 1)
        update()
    }

    private fun nextMonth() {
        getDateCalendar().add(Calendar.MONTH, 1)
        getTimeCalendar().add(Calendar.MONTH, 1)
        update()
    }

    private fun prevDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, -1)
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, -1)
        update()
    }

    private fun prevMonth() {
        getDateCalendar().add(Calendar.MONTH, -1)
        getTimeCalendar().add(Calendar.MONTH, -1)
        update()
    }

    protected abstract fun update(view: View)

    companion object {
        private val TAG = AbstractDayFragment::class.java.simpleName
    }

}
