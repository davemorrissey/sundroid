package uk.co.sundroid.activity.data.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.*
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.*
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar.*
import android.view.View.*

import kotlinx.android.synthetic.main.inc_monthbar.*

abstract class AbstractMonthFragment<T> : AbstractDataFragment(), MonthPickerFragment.OnMonthSelectedListener {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    private var monthDetector: GestureDetector? = null

    protected abstract val layout: Int

    private val handler = Handler()

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
        getDateCalendar().set(Calendar.YEAR, year)
        getDateCalendar().set(Calendar.MONTH, month)
        getTimeCalendar().set(Calendar.YEAR, year)
        getTimeCalendar().set(Calendar.MONTH, month)
        update()
    }

    private fun initGestures() {
        if (monthDetector == null) {
            val monthListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() = changeCalendars(YEAR, -1)
                override fun onButtonDragDown() = changeCalendars(YEAR, 1)
                override fun onButtonDragLeft() = changeCalendars(MONTH, -1)
                override fun onButtonDragRight() = changeCalendars(MONTH, 1)
            }
            monthDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(monthListener, applicationContext!!))
        }

        monthPrev.setOnClickListener { _ -> changeCalendars(MONTH, -1) }
        monthNext.setOnClickListener { _ -> changeCalendars(MONTH, 1) }
        zoneButton.setOnClickListener { _ -> startTimeZone() }
        monthButton.setOnClickListener { _ -> showMonthPicker() }
        monthButton.setOnTouchListener { _, event -> monthDetector != null && monthDetector!!.onTouchEvent(event) }
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

    private fun showMonthPicker() {
        val monthPickerFragment = MonthPickerFragment.newInstance(getDateCalendar())
        monthPickerFragment.setTargetFragment(this, 0)
        monthPickerFragment.show(fragmentManager, "monthPicker")
    }

    private fun changeCalendars(field: Int, diff: Int) {
        arrayOf(getDateCalendar(), getTimeCalendar()).forEach { it.add(field, diff) }
        update()
    }

    protected abstract fun update(view: View)

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

    abstract fun calculate(location: LocationDetails, calendar: Calendar): T

    protected open fun post(view: View, data: T) {

    }

    companion object {
        private val TAG = AbstractMonthFragment::class.java.simpleName
    }

}
