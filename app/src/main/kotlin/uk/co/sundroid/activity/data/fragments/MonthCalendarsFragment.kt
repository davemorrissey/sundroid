package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import uk.co.sundroid.R
import uk.co.sundroid.R.array
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.TwilightType
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.*
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*

import java.math.BigDecimal
import java.util.ArrayList
import java.util.Calendar

class MonthCalendarsFragment : AbstractMonthFragment<ArrayList<MonthCalendarsFragment.DayEntry?>>(), OnItemSelectedListener {

    private var selectorActive = false

    protected override val layout: Int
        get() = R.layout.frag_data_monthcalendars

    private val body: Body?
        get() {
            when (SharedPrefsHelper.getLastCalendar(activity)) {
                0 -> return Body.SUN
                7 -> return Body.MERCURY
                8 -> return Body.VENUS
                9 -> return Body.MARS
                10 -> return Body.JUPITER
                11 -> return Body.SATURN
                12 -> return Body.URANUS
                13 -> return Body.NEPTUNE
                else -> return null
            }
        }

    private val type: String
        get() {
            when (SharedPrefsHelper.getLastCalendar(activity)) {
                0 -> return "daylight"
                1 -> return "civ"
                2 -> return "ntc"
                3 -> return "ast"
                4 -> return "golden"
                5 -> return "moon"
                6 -> return "daylight"
                else -> return ""
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        d(TAG, hashCode().toString() + " on view created ")
        selectorActive = false
        val selector = view.findViewById<Spinner>(R.id.monthCalSelector)
        selector.setOnTouchListener { v, e ->
            d(TAG, "Selector activated by touch")
            selectorActive = true
            false
        }
        val adapter = ArrayAdapter.createFromResource(activity, array.monthCalendars, R.layout.frag_data_monthcalendars_selector_selected)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val index = SharedPrefsHelper.getLastCalendar(activity)
        selector.adapter = adapter
        selector.onItemSelectedListener = this
        if (selector.selectedItemPosition != index) {
            d(TAG, "set selection " + index)
            selector.setSelection(index)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View?, index: Int, l: Long) {
        if (selectorActive) {
            d(TAG, "Item selected " + index)
            SharedPrefsHelper.setLastCalendar(activity, index)
            update()
        } else {
            d(TAG, "SELECTOR NOT ACTIVE " + index)
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    override fun calculate(location: LocationDetails, calendar: Calendar): ArrayList<DayEntry?> {
        val type = type
        val body = body

        val todayCalendar = Calendar.getInstance(calendar.timeZone)

        val loopCalendar = Calendar.getInstance()
        loopCalendar.timeZone = calendar.timeZone
        loopCalendar.timeInMillis = calendar.timeInMillis
        loopCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val month = loopCalendar.get(Calendar.MONTH)

        // Calculate full details of previous day for diffs.
        val prevCalendar = Calendar.getInstance()
        prevCalendar.timeZone = calendar.timeZone
        prevCalendar.timeInMillis = calendar.timeInMillis
        prevCalendar.set(Calendar.DAY_OF_MONTH, 1)
        prevCalendar.add(Calendar.DAY_OF_MONTH, -1)
        var previousSunDay: SunDay? = SunCalculator.calcDay(location.location, prevCalendar)
        var previousBodyDay: BodyDay? = if (body == null) null else BodyPositionCalculator.calcDay(body, location.location, prevCalendar, false)

        val entries = ArrayList<DayEntry?>()

        // Placeholder entry becomes column headings.
        entries.add(null)

        var day = 1
        while (day < 32 && loopCalendar.get(Calendar.MONTH) == month) {
            var bodyDay: BodyDay? = null
            var sunDay: SunDay? = null
            var moonDay: MoonDay? = null
            if (body != null) {
                bodyDay = BodyPositionCalculator.calcDay(body, location.location, loopCalendar, false)
            } else if (type == "daylight") {
                sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.RISESET)
            } else if (type == "civ") {
                sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.CIVIL)
            } else if (type == "ntc") {
                sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.NAUTICAL)
            } else if (type == "ast") {
                sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.ASTRONOMICAL)
            } else if (type == "golden") {
                sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.GOLDENHOUR)
            } else if (type == "moon") {
                moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, loopCalendar, false) as MoonDay
            }
            val today = todayCalendar.get(Calendar.YEAR) == loopCalendar.get(Calendar.YEAR) &&
                    todayCalendar.get(Calendar.MONTH) == loopCalendar.get(Calendar.MONTH) &&
                    todayCalendar.get(Calendar.DAY_OF_MONTH) == loopCalendar.get(Calendar.DAY_OF_MONTH)

            val dayEntry = DayEntry()
            dayEntry.day = day
            dayEntry.sunDay = sunDay
            dayEntry.previousSunDay = previousSunDay
            dayEntry.bodyDay = bodyDay
            dayEntry.previousBodyDay = previousBodyDay
            dayEntry.moonDay = moonDay
            dayEntry.dayOfWeek = loopCalendar.get(Calendar.DAY_OF_WEEK)
            dayEntry.today = today
            entries.add(dayEntry)
            loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
            previousSunDay = sunDay
            previousBodyDay = bodyDay
            day++
        }

        return entries

    }

    override fun post(view: View, data: ArrayList<MonthCalendarsFragment.DayEntry?>) {
        val listAdapter = DayEntryAdapter(data)
        val list = view.findViewById<ListView>(R.id.monthCalList)
        list.adapter = listAdapter
    }

    override fun update(view: View) {
        asyncCalculate(getLocation(), getDateCalendar(), view)
    }

    class DayEntry {
        var moonDay: MoonDay? = null
        var sunDay: SunDay? = null
        var previousSunDay: SunDay? = null
        var bodyDay: BodyDay? = null
        var previousBodyDay: BodyDay? = null
        var day: Int = 0
        var dayOfWeek: Int = 0
        var today = false
    }

    inner class DayEntryAdapter constructor(list: ArrayList<DayEntry?>) : ArrayAdapter<DayEntry>(applicationContext, R.layout.frag_data_monthcalendars_row, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val location = getLocation()
            var row = convertView
            if (row == null) {
                val inflater = activity.layoutInflater
                row = inflater.inflate(R.layout.frag_data_monthcalendars_row, parent, false)
            }
            val entry = getItem(position)
            val type = type
            val body = body

            val packageName = applicationContext!!.packageName

            if (entry == null) {
                textInView(row!!, R.id.rowDate, "")
                textInView(row, R.id.rowWeekday, "")
                row.setBackgroundColor(getCalendarDefaultColor())

                showInView(row, R.id.dayUpAz, R.id.dayDownAz)
                removeInView(row, R.id.dayMoonCell, R.id.monthCalDatePhase)

                if (body != null) {
                    textInView(row, R.id.dayUp, "RISE")
                    textInView(row, R.id.dayDown, "SET")
                    textInView(row, R.id.dayUpAz, "CHANGE\nAZIMUTH")
                    textInView(row, R.id.dayDownAz, "CHANGE\nAZIMUTH")
                } else if (type == "civ" || type == "ntc" || type == "ast") {
                    textInView(row, R.id.dayUp, "DAWN")
                    textInView(row, R.id.dayDown, "DUSK")
                    textInView(row, R.id.dayUpAz, "CHANGE")
                    textInView(row, R.id.dayDownAz, "CHANGE")
                } else if (type == "golden") {
                    textInView(row, R.id.dayUp, "MORNING END")
                    textInView(row, R.id.dayDown, "EVENING START")
                    textInView(row, R.id.dayUpAz, "CHANGE")
                    textInView(row, R.id.dayDownAz, "CHANGE")
                } else if (type == "daylight") {
                    textInView(row, R.id.dayUp, "LENGTH")
                    textInView(row, R.id.dayDown, "CHANGE")
                    removeInView(row, R.id.dayUpAz)
                    removeInView(row, R.id.dayDownAz)
                } else if (type == "moon") {
                    hideInView(row, R.id.dayMoonCell)
                    textInView(row, R.id.dayUp, "RISE")
                    textInView(row, R.id.dayDown, "SET")
                    textInView(row, R.id.dayUpAz, "AZIMUTH")
                    textInView(row, R.id.dayDownAz, "AZIMUTH")
                }
                return row
            }

            if (entry.today) {
                row!!.setBackgroundColor(getCalendarHighlightColor())
            } else {
                row!!.setBackgroundColor(getCalendarDefaultColor())
            }

            var weekday = ""
            when (entry.dayOfWeek) {
                Calendar.MONDAY -> weekday = "MON"
                Calendar.TUESDAY -> weekday = "TUE"
                Calendar.WEDNESDAY -> weekday = "WED"
                Calendar.THURSDAY -> weekday = "THU"
                Calendar.FRIDAY -> weekday = "FRI"
                Calendar.SATURDAY -> weekday = "SAT"
                Calendar.SUNDAY -> weekday = "SUN"
            }

            textInView(row, R.id.rowDate, Integer.toString(entry.day))
            textInView(row, R.id.rowWeekday, weekday)

            var up: Calendar? = null
            var down: Calendar? = null
            var length = java.lang.Double.MIN_VALUE
            var lengthDiff = java.lang.Double.MIN_VALUE

            var upAz: String? = null
            var downAz: String? = null
            var riseSetType: RiseSetType? = null
            var allowSeconds = false

            if (body != null) {
                allowSeconds = body === Body.SUN
                val bodyDay = entry.bodyDay
                val previousBodyDay = entry.previousBodyDay
                up = bodyDay!!.rise
                down = bodyDay.set
                if (up != null && previousBodyDay!!.rise != null) {
                    upAz = formatDiff(applicationContext!!, bodyDay.rise!!, previousBodyDay.rise!!, allowSeconds)
                }
                if (down != null && previousBodyDay!!.set != null) {
                    downAz = formatDiff(applicationContext!!, bodyDay.set!!, previousBodyDay.set!!, allowSeconds)
                }
                if (up != null) {
                    if (isNotEmpty(upAz)) {
                        upAz += "\n"
                    } else {
                        upAz = ""
                    }
                    val azimuth = formatBearing(applicationContext!!, bodyDay.riseAzimuth, location!!.location, up)
                    upAz += azimuth
                }
                if (down != null) {
                    if (isNotEmpty(downAz)) {
                        downAz += "\n"
                    } else {
                        downAz = ""
                    }
                    val azimuth = formatBearing(applicationContext!!, bodyDay.setAzimuth, location!!.location, down)
                    downAz += azimuth
                }
                if (up == null && down == null) {
                    riseSetType = bodyDay.riseSetType
                }
                (row.findViewById<View>(R.id.dayUpCell) as LinearLayout).weightSum = 0.40f
                (row.findViewById<View>(R.id.dayDownCell) as LinearLayout).weightSum = 0.40f

            } else if (type == "civ") {
                allowSeconds = true
                val sunDay = entry.sunDay
                val previousSunDay = entry.previousSunDay
                up = sunDay!!.civDawn
                down = sunDay.civDusk
                if (up == null && down == null) {
                    riseSetType = if (sunDay.civType === TwilightType.DARK) RiseSetType.SET else RiseSetType.RISEN
                }
                if (up != null && previousSunDay!!.civDawn != null) {
                    upAz = formatDiff(applicationContext!!, sunDay.civDawn!!, previousSunDay.civDawn!!, true)
                }
                if (down != null && previousSunDay!!.civDusk != null) {
                    downAz = formatDiff(applicationContext!!, sunDay.civDusk!!, previousSunDay.civDusk!!, true)
                }
            } else if (type == "ntc") {
                allowSeconds = true
                val sunDay = entry.sunDay
                val previousSunDay = entry.previousSunDay
                up = sunDay!!.ntcDawn
                down = sunDay.ntcDusk
                if (up == null && down == null) {
                    riseSetType = if (sunDay.ntcType === TwilightType.DARK) RiseSetType.SET else RiseSetType.RISEN
                }
                if (up != null && previousSunDay!!.ntcDawn != null) {
                    upAz = formatDiff(applicationContext!!, sunDay.ntcDawn!!, previousSunDay.ntcDawn!!, true)
                }
                if (down != null && previousSunDay!!.ntcDusk != null) {
                    downAz = formatDiff(applicationContext!!, sunDay.ntcDusk!!, previousSunDay.ntcDusk!!, true)
                }
            } else if (type == "ast") {
                allowSeconds = true
                val sunDay = entry.sunDay
                val previousSunDay = entry.previousSunDay
                up = sunDay!!.astDawn
                down = sunDay.astDusk
                if (up == null && down == null) {
                    riseSetType = if (sunDay.astType === TwilightType.DARK) RiseSetType.SET else RiseSetType.RISEN
                }
                if (up != null && previousSunDay!!.astDawn != null) {
                    upAz = formatDiff(applicationContext!!, sunDay.astDawn!!, previousSunDay.astDawn!!, true)
                }
                if (down != null && previousSunDay!!.astDusk != null) {
                    downAz = formatDiff(applicationContext!!, sunDay.astDusk!!, previousSunDay.astDusk!!, true)
                }
            } else if (type == "golden") {
                allowSeconds = true
                val sunDay = entry.sunDay
                val previousSunDay = entry.previousSunDay
                up = sunDay!!.ghEnd
                down = sunDay.ghStart
                if (up == null && down == null) {
                    riseSetType = if (sunDay.ghType === TwilightType.DARK) RiseSetType.SET else RiseSetType.RISEN
                }
                if (up != null && previousSunDay!!.ghEnd != null) {
                    upAz = formatDiff(applicationContext!!, sunDay.ghEnd!!, previousSunDay.ghEnd!!, true)
                }
                if (down != null && previousSunDay!!.ghStart != null) {
                    downAz = formatDiff(applicationContext!!, sunDay.ghStart!!, previousSunDay.ghStart!!, true)
                }
            } else if (type == "daylight") {
                allowSeconds = true
                val sunDay = entry.sunDay!!
                val previousSunDay = entry.previousSunDay!!
                length = sunDay!!.uptimeHours
                lengthDiff = sunDay.uptimeHours - previousSunDay!!.uptimeHours
            } else if (type == "moon") {
                val moonDay = entry.moonDay!!
                up = moonDay!!.rise
                down = moonDay.set
                if (up != null) {
                    upAz = formatBearing(applicationContext!!, moonDay.riseAzimuth, location!!.location, up)
                }
                if (down != null) {
                    downAz = formatBearing(applicationContext!!, moonDay.setAzimuth, location!!.location, down)
                }
                if (up == null && down == null) {
                    if (moonDay.riseSetType === RiseSetType.RISEN) {
                        riseSetType = RiseSetType.RISEN
                    } else {
                        riseSetType = RiseSetType.SET
                    }
                }

                val phaseDbl = moonDay.phaseDouble / 2
                var phaseBd = BigDecimal(phaseDbl)
                phaseBd = phaseBd.setScale(2, BigDecimal.ROUND_HALF_DOWN)
                phaseBd = phaseBd.multiply(BigDecimal(2))
                if (location!!.location.latitude.doubleValue < 0) {
                    phaseBd = BigDecimal(1).subtract(phaseBd)
                }
                val moonImg = phaseBd.toString().replace("\\.".toRegex(), "")
                imageInView(row, R.id.dayMoonOverlay, resources.getIdentifier(packageName + ":drawable/moonoverlay" + moonImg, null, null))
                showInView(row, R.id.dayMoonCell)

                val phaseEvent = moonDay.phaseEvent
                if (phaseEvent != null) {
                    var phaseImg = getPhaseFull()
                    if (phaseEvent.phase === MoonPhase.NEW) {
                        phaseImg = getPhaseNew()
                    } else if (phaseEvent.phase === MoonPhase.FIRST_QUARTER) {
                        phaseImg = if (location!!.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                    } else if (phaseEvent.phase === MoonPhase.LAST_QUARTER) {
                        phaseImg = if (location!!.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                    }
                    imageInView(row, R.id.monthCalDatePhase, phaseImg)
                    showInView(row, R.id.monthCalDatePhase)
                } else {
                    removeInView(row, R.id.monthCalDatePhase)
                }
            }

            if (length != java.lang.Double.MIN_VALUE) {
                val timeStr = formatDuration(applicationContext!!, length, true)
                textInView(row, R.id.dayUp, timeStr)
                val diffStr = formatDiff(applicationContext!!, lengthDiff, true)
                textInView(row, R.id.dayDown, diffStr)
                removeInView(row, R.id.dayUpAz)
                removeInView(row, R.id.dayDownAz)
            } else {
                if (up != null) {
                    val time = formatTime(applicationContext!!, up, allowSeconds)
                    var timeStr = time.time
                    if (isNotEmpty(time.marker)) {
                        timeStr += time.marker
                    }
                    textInView(row, R.id.dayUp, timeStr)
                    if (upAz != null) {
                        showInView(row, R.id.dayUpAz, upAz)
                    } else {
                        removeInView(row, R.id.dayUpAz)
                    }
                } else {
                    if (down == null && riseSetType != null) {
                        if (type == "sun" || type == "moon") {
                            textInView(row, R.id.dayUp, if (riseSetType === RiseSetType.RISEN) "Risen" else "Set")
                        } else {
                            textInView(row, R.id.dayUp, if (riseSetType === RiseSetType.RISEN) "Light" else "Dark")
                        }
                    } else {
                        textInView(row, R.id.dayUp, "None")
                    }
                    removeInView(row, R.id.dayUpAz)
                }

                if (down != null) {
                    val time = formatTime(applicationContext!!, down, allowSeconds)
                    var timeStr = time.time
                    if (isNotEmpty(time.marker)) {
                        timeStr += time.marker
                    }
                    (row.findViewById<View>(R.id.dayDown) as TextView).text = timeStr
                    if (downAz != null) {
                        showInView(row, R.id.dayDownAz, downAz)
                    } else {
                        removeInView(row, R.id.dayDownAz)
                    }
                } else {
                    if (up == null && riseSetType != null) {
                        textInView(row, R.id.dayDown, "")
                    } else {
                        textInView(row, R.id.dayDown, "None")
                    }
                    removeInView(row, R.id.dayDownAz)
                }
            }
            return row
        }

    }

    companion object {

        private val TAG = MonthCalendarsFragment::class.java.simpleName
    }


}