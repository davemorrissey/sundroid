package uk.co.sundroid.activity.data.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.inc_monthbar.*
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AbstractMonthFragment<T> : AbstractDataFragment(), MonthPickerFragment.OnMonthSelectedListener {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()
    }

    override fun initialise() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateMonth()
        }
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateMonth()
            update(view)
        }
    }

    override fun onMonthSet(year: Int, month: Int) {
        calendarSet(year, month, getDateCalendar().get(DAY_OF_MONTH))
    }

    private fun initGestures() {
        val monthListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(YEAR, -1)
            override fun onButtonDragDown() = calendarDiff(YEAR, 1)
            override fun onButtonDragLeft() = calendarDiff(MONTH, -1)
            override fun onButtonDragRight() = calendarDiff(MONTH, 1)
        }
        val monthDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(monthListener, applicationContext!!))

        monthPrev.setOnClickListener { _ -> calendarDiff(MONTH, -1) }
        monthNext.setOnClickListener { _ -> calendarDiff(MONTH, 1) }
        zoneButton.setOnClickListener { _ -> startTimeZone() }
        monthButton.setOnClickListener { _ -> MonthPickerFragment.show(getDateCalendar(), this, activity) }
        monthButton.setOnTouchListener { _, event -> monthDetector.onTouchEvent(event) }
    }

    private fun updateMonth() {
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

        monthFormat.timeZone = calendar.timeZone
        month.text = monthFormat.format(Date(calendar.timeInMillis))
    }

    protected fun offThreadUpdate(location: LocationDetails, calendar: Calendar, view: View) {
        data class Params(val location: LocationDetails, val calendar: Calendar)
        class Task : AsyncTask<Params, Void, T>() {
            override fun doInBackground(vararg params: Params): T {
                return calculate(params[0].location, params[0].calendar)
            }
            override fun onPostExecute(data: T) {
                if (isSafe) {
                    post(view, data)
                }
            }
        }
        Task().execute(Params(location, calendar))
    }

    protected abstract fun update(view: View)

    protected abstract fun calculate(location: LocationDetails, calendar: Calendar): T

    protected open fun post(view: View, data: T) { }

}
