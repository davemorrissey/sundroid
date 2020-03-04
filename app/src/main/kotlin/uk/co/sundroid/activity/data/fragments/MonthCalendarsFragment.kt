package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import uk.co.sundroid.R
import uk.co.sundroid.R.array
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.formatDiff
import uk.co.sundroid.util.time.formatDuration
import uk.co.sundroid.util.time.formatTime
import java.math.BigDecimal
import java.util.*
import uk.co.sundroid.R.id.*
import uk.co.sundroid.activity.MainActivity

class MonthCalendarsFragment : AbstractMonthFragment<ArrayList<MonthCalendarsFragment.DayEntry?>>(), OnItemSelectedListener {

    private var selectorActive = false

    override val layout: Int
        get() = R.layout.frag_data_monthcalendars

    private val body: Body?
        get() {
            return when (Prefs.lastCalendar(requireContext())) {
                0 -> Body.SUN
                7 -> Body.MERCURY
                8 -> Body.VENUS
                9 -> Body.MARS
                10 -> Body.JUPITER
                11 -> Body.SATURN
                12 -> Body.URANUS
                13 -> Body.NEPTUNE
                else -> null
            }
        }

    private val type: String
        get() {
            return when (Prefs.lastCalendar(requireContext())) {
                0 -> "daylight"
                1 -> "civ"
                2 -> "ntc"
                3 -> "ast"
                4 -> "golden"
                5 -> "moon"
                6 -> "daylight"
                else -> ""
            }
        }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarSubtitle(R.string.data_calendars_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectorActive = false
        val selector = view.findViewById<Spinner>(monthCalSelector)
        selector.setOnTouchListener { _, _ ->
            selectorActive = true
            false
        }
        val adapter = ArrayAdapter.createFromResource(requireContext(), array.monthCalendars, R.layout.frag_data_monthcalendars_selector_selected)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val index = Prefs.lastCalendar(requireContext())
        selector.adapter = adapter
        selector.onItemSelectedListener = this
        if (selector.selectedItemPosition != index) {
            selector.setSelection(index)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View?, index: Int, l: Long) {
        if (selectorActive) {
            prefs.setLastCalendar(index)
            update()
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
            when {
                body != null -> bodyDay = BodyPositionCalculator.calcDay(body, location.location, loopCalendar, false)
                type == "daylight" -> sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.RISESET)
                type == "civ" -> sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.CIVIL)
                type == "ntc" -> sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.NAUTICAL)
                type == "ast" -> sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.ASTRONOMICAL)
                type == "golden" -> sunDay = SunCalculator.calcDay(location.location, loopCalendar, SunCalculator.Event.GOLDENHOUR)
                type == "moon" -> moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, loopCalendar, false) as MoonDay
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

    override fun post(view: View, data: ArrayList<DayEntry?>) {
        val listAdapter = DayEntryAdapter(data)
        val list = view.findViewById<ListView>(monthCalList)
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

    inner class DayEntryAdapter constructor(list: ArrayList<DayEntry?>) : ArrayAdapter<DayEntry>(requireContext(), R.layout.frag_data_monthcalendars_row, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val location = getLocation()
            val row = convertView ?: inflate(R.layout.frag_data_monthcalendars_row)
            val entry = getItem(position)
            val type = type
            val body = body

            val packageName = requireContext().packageName

            if (entry == null) {
                text(row, rowDate, "")
                text(row, rowWeekday, "")
                row.setBackgroundColor(getCalendarDefaultColor())

                show(row, dayUpAz, dayDownAz)
                remove(row, dayMoonCell, monthCalDatePhase)

                if (body != null) {
                    text(row, dayUp, "RISE")
                    text(row, dayDown, "SET")
                    text(row, dayUpAz, "CHANGE\nAZIMUTH")
                    text(row, dayDownAz, "CHANGE\nAZIMUTH")
                } else if (type == "civ" || type == "ntc" || type == "ast") {
                    text(row, dayUp, "DAWN")
                    text(row, dayDown, "DUSK")
                    text(row, dayUpAz, "CHANGE")
                    text(row, dayDownAz, "CHANGE")
                } else if (type == "golden") {
                    text(row, dayUp, "MORNING END")
                    text(row, dayDown, "EVENING START")
                    text(row, dayUpAz, "CHANGE")
                    text(row, dayDownAz, "CHANGE")
                } else if (type == "daylight") {
                    text(row, dayUp, "LENGTH")
                    text(row, dayDown, "CHANGE")
                    remove(row, dayUpAz)
                    remove(row, dayDownAz)
                } else if (type == "moon") {
                    hide(row, dayMoonCell)
                    text(row, dayUp, "RISE")
                    text(row, dayDown, "SET")
                    text(row, dayUpAz, "AZIMUTH")
                    text(row, dayDownAz, "AZIMUTH")
                }
                return row
            }

            if (entry.today) {
                row.setBackgroundColor(getCalendarHighlightColor())
            } else {
                row.setBackgroundColor(getCalendarDefaultColor())
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

            text(row, rowDate, entry.day.toString())
            text(row, rowWeekday, weekday)

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
                if (up != null && previousBodyDay?.rise != null) {
                    upAz = formatDiff(requireContext(), bodyDay.rise!!, previousBodyDay.rise!!, allowSeconds)
                }
                if (down != null && previousBodyDay?.set != null) {
                    downAz = formatDiff(requireContext(), bodyDay.set!!, previousBodyDay.set!!, allowSeconds)
                }
                if (up != null) {
                    if (isNotEmpty(upAz)) {
                        upAz += "\n"
                    } else {
                        upAz = ""
                    }
                    val azimuth = formatBearing(requireContext(), bodyDay.riseAzimuth, location.location, up)
                    upAz += azimuth
                }
                if (down != null) {
                    if (isNotEmpty(downAz)) {
                        downAz += "\n"
                    } else {
                        downAz = ""
                    }
                    val azimuth = formatBearing(requireContext(), bodyDay.setAzimuth, location.location, down)
                    downAz += azimuth
                }
                if (up == null && down == null) {
                    riseSetType = bodyDay.riseSetType
                }
                (row.findViewById<View>(dayUpCell) as LinearLayout).weightSum = 0.40f
                (row.findViewById<View>(dayDownCell) as LinearLayout).weightSum = 0.40f

            } else if (type == "civ") {
                allowSeconds = true
                val sunDay = entry.sunDay
                val previousSunDay = entry.previousSunDay
                up = sunDay!!.civDawn
                down = sunDay.civDusk
                if (up == null && down == null) {
                    riseSetType = if (sunDay.civType === TwilightType.DARK) RiseSetType.SET else RiseSetType.RISEN
                }
                if (up != null && previousSunDay?.civDawn != null) {
                    upAz = formatDiff(requireContext(), sunDay.civDawn!!, previousSunDay.civDawn!!, true)
                }
                if (down != null && previousSunDay?.civDusk != null) {
                    downAz = formatDiff(requireContext(), sunDay.civDusk!!, previousSunDay.civDusk!!, true)
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
                if (up != null && previousSunDay?.ntcDawn != null) {
                    upAz = formatDiff(requireContext(), sunDay.ntcDawn!!, previousSunDay.ntcDawn!!, true)
                }
                if (down != null && previousSunDay?.ntcDusk != null) {
                    downAz = formatDiff(requireContext(), sunDay.ntcDusk!!, previousSunDay.ntcDusk!!, true)
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
                if (up != null && previousSunDay?.astDawn != null) {
                    upAz = formatDiff(requireContext(), sunDay.astDawn!!, previousSunDay.astDawn!!, true)
                }
                if (down != null && previousSunDay?.astDusk != null) {
                    downAz = formatDiff(requireContext(), sunDay.astDusk!!, previousSunDay.astDusk!!, true)
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
                if (up != null && previousSunDay?.ghEnd != null) {
                    upAz = formatDiff(requireContext(), sunDay.ghEnd!!, previousSunDay.ghEnd!!, true)
                }
                if (down != null && previousSunDay?.ghStart != null) {
                    downAz = formatDiff(requireContext(), sunDay.ghStart!!, previousSunDay.ghStart!!, true)
                }
            } else if (type == "daylight") {
                allowSeconds = true
                val sunDay = entry.sunDay!!
                val previousSunDay = entry.previousSunDay!!
                length = sunDay.uptimeHours
                lengthDiff = sunDay.uptimeHours - previousSunDay.uptimeHours
            } else if (type == "moon") {
                val moonDay = entry.moonDay!!
                up = moonDay.rise
                down = moonDay.set
                if (up != null) {
                    upAz = formatBearing(requireContext(), moonDay.riseAzimuth, location.location, up)
                }
                if (down != null) {
                    downAz = formatBearing(requireContext(), moonDay.setAzimuth, location.location, down)
                }
                if (up == null && down == null) {
                    riseSetType = if (moonDay.riseSetType === RiseSetType.RISEN) {
                        RiseSetType.RISEN
                    } else {
                        RiseSetType.SET
                    }
                }

                val phaseDbl = moonDay.phaseDouble / 2
                var phaseBd = BigDecimal(phaseDbl)
                phaseBd = phaseBd.setScale(2, BigDecimal.ROUND_HALF_DOWN)
                phaseBd = phaseBd.multiply(BigDecimal(2))
                if (location.location.latitude.doubleValue < 0) {
                    phaseBd = BigDecimal(1).subtract(phaseBd)
                }
                val moonImg = phaseBd.toString().replace("\\.".toRegex(), "")
                image(row, dayMoonOverlay, resources.getIdentifier("$packageName:drawable/moonoverlay$moonImg", null, null))
                show(row, dayMoonCell)

                val phaseEvent = moonDay.phaseEvent
                if (phaseEvent != null) {
                    var phaseImg = getPhaseFull()
                    when {
                        phaseEvent.phase === MoonPhase.NEW -> phaseImg = getPhaseNew()
                        phaseEvent.phase === MoonPhase.FIRST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                        phaseEvent.phase === MoonPhase.LAST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                    }
                    image(row, monthCalDatePhase, phaseImg)
                    show(row, monthCalDatePhase)
                } else {
                    remove(row, monthCalDatePhase)
                }
            }

            if (length != java.lang.Double.MIN_VALUE) {
                val timeStr = formatDuration(requireContext(), length, true)
                text(row, dayUp, timeStr)
                val diffStr = formatDiff(requireContext(), lengthDiff, true)
                text(row, dayDown, diffStr)
                remove(row, dayUpAz)
                remove(row, dayDownAz)
            } else {
                if (up != null) {
                    val time = formatTime(requireContext(), up, allowSeconds)
                    var timeStr = time.time
                    if (isNotEmpty(time.marker)) {
                        timeStr += time.marker
                    }
                    text(row, dayUp, timeStr)
                    if (upAz != null) {
                        show(row, dayUpAz, upAz)
                    } else {
                        remove(row, dayUpAz)
                    }
                } else {
                    if (down == null && riseSetType != null) {
                        if (type == "sun" || type == "moon") {
                            text(row, dayUp, if (riseSetType === RiseSetType.RISEN) "Risen" else "Set")
                        } else {
                            text(row, dayUp, if (riseSetType === RiseSetType.RISEN) "Light" else "Dark")
                        }
                    } else {
                        text(row, dayUp, "None")
                    }
                    remove(row, dayUpAz)
                }

                if (down != null) {
                    val time = formatTime(requireContext(), down, allowSeconds)
                    var timeStr = time.time
                    if (isNotEmpty(time.marker)) {
                        timeStr += time.marker
                    }
                    (row.findViewById<View>(dayDown) as TextView).text = timeStr
                    if (downAz != null) {
                        show(row, dayDownAz, downAz)
                    } else {
                        remove(row, dayDownAz)
                    }
                } else {
                    text(row, dayDown, if (up == null && riseSetType != null) "" else "None")
                    remove(row, dayDownAz)
                }
            }
            return row
        }

    }

}
