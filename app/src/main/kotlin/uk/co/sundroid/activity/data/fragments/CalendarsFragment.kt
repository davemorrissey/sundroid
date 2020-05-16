package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_data_calendars.*
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.settings.CalendarSelectorFragment
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.DESCENDING
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.RISING
import uk.co.sundroid.util.astro.BodyDayEvent.Event.*
import uk.co.sundroid.util.astro.image.MoonPhaseImageView
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.html
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.formatDiff
import uk.co.sundroid.util.time.formatDuration
import uk.co.sundroid.util.time.formatTimeStr
import java.util.*
import kotlin.collections.LinkedHashSet


class CalendarsFragment : AbstractMonthFragment<ArrayList<CalendarsFragment.DayEntry>>() {

    override val layout: Int
        get() = R.layout.frag_data_calendars

    private fun openSettingsDialog() {
        val pickerDialog = CalendarSelectorFragment.newInstance()
        pickerDialog.setTargetFragment(this, 0)
        pickerDialog.show(requireFragmentManager(), "calendarPicker")
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarSubtitle(Prefs.lastCalendar(requireContext()).title)
            setViewConfigurationCallback({ openSettingsDialog() }, R.drawable.icn_bar_calendar)
        }
    }

    override fun updateData(view: View) {
        (activity as MainActivity).apply {
            setToolbarSubtitle(Prefs.lastCalendar(requireContext()).title)
        }
        val calendarView = Prefs.lastCalendar(requireContext())
        if (prefs.showElement("tipCalendarView", true)) {
            modify(tip, visibility = VISIBLE)
            tipHide.setOnClickListener {
                prefs.setShowElement("tipCalendarView", false)
                modify(tip, visibility = GONE)
            }
        }
        if (calendarView.grid) {
            modify(calendarList, visibility = GONE)
        } else {
            modify(calendarGrid, visibility = GONE)
        }
        async(
            inBackground = {
                calculateMonth(!calendarView.grid)
            },
            onDone = { data: ArrayList<DayEntry> ->
                if (isSafe) {
                    if (calendarView.grid) {
                        modify(calendarGrid, visibility = VISIBLE)
                        updateGrid(data)
                    } else {
                        modify(calendarList, visibility = VISIBLE)
                        updateList(data)
                    }
                }
            }
        )
    }

    private fun addEvent(dayEntry: DayEntry, event: BodyDayEvent.Direction, time: Calendar?, previousTime: Calendar?, allowSeconds: Boolean = false, azimuth: Double? = null) {
        time?.let {
            var timeHtml = if (event == RISING) {
                "<font color=\"${upColor()}\">\u25b2</font><br/>"
            } else {
                "<font color=\"${downColor()}\">\u25bc</font><br/>"
            }
            timeHtml += formatTimeStr(requireContext(), time, allowSeconds, html = true)
            val diffStr: String = previousTime?.let { formatDiff(requireContext(), time, previousTime, allowSeconds) } ?: ""
            val azStr: String = azimuth?.let { formatBearing(requireContext(), azimuth, getLocation().location, time) } ?: ""
            val detail = ("$diffStr  $azStr").trim()
            dayEntry.events.add(DayEntryEvent(timeHtml, detail))
        }
    }

    private fun addRisenSet(dayEntry: DayEntry, risen: Boolean, lightDark: Boolean) {
        val symbol = if (risen) "\u25cf" else "×"
        val color = if (risen) upColor() else downColor()
        var label = if (risen) "RISEN" else "SET"
        if (lightDark) {
            label = if (risen) "LIGHT" else "DARK"
        }
        val timeHtml = "<font color=\"$color\">$symbol</font><br/>$label"
        dayEntry.events.add(DayEntryEvent(timeHtml, null))
    }

    private fun calculateMonth(allowSeconds: Boolean): ArrayList<DayEntry> {
        val location = getLocation()
        val calendar = getDateCalendar()
        val calendarView = Prefs.lastCalendar(requireContext())
        val type = calendarView.type
        val body = calendarView.body

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

        val entries = ArrayList<DayEntry>()

        var day = 1
        while (day < 32 && loopCalendar.get(Calendar.MONTH) == month) {
            val today = todayCalendar.get(Calendar.YEAR) == loopCalendar.get(Calendar.YEAR) &&
                    todayCalendar.get(Calendar.MONTH) == loopCalendar.get(Calendar.MONTH) &&
                    todayCalendar.get(Calendar.DAY_OF_MONTH) == loopCalendar.get(Calendar.DAY_OF_MONTH)

            val dayEntry = DayEntry()
            dayEntry.day = day
            dayEntry.dayOfWeek = loopCalendar.get(Calendar.DAY_OF_WEEK)
            dayEntry.today = today

            if (type == "sun" || type == "civ" || type == "ntc" || type == "ast") {
                // Sunrise/sunset and dawn/dusk only allowing one of each per day, to allow diffs
                val event = when(type) {
                    "sun" -> RISESET
                    "civ" -> CIVIL
                    "ntc" -> NAUTICAL
                    else -> ASTRONOMICAL
                }
                val sunDay = SunCalculator.calcDay(location.location, loopCalendar, event)
                if (sunDay.eventUp[event] == null && sunDay.eventDown[event] == null) {
                    addRisenSet(dayEntry, sunDay.eventType[event] == RiseSetType.RISEN, true)
                } else {
                    addEvent(dayEntry, RISING, sunDay.eventUp[event]?.time, previousSunDay!!.eventUp[event]?.time, allowSeconds = allowSeconds, azimuth = sunDay.eventUp[event]?.azimuth)
                    addEvent(dayEntry, DESCENDING, sunDay.eventDown[event]?.time, previousSunDay.eventDown[event]?.time, allowSeconds = allowSeconds, azimuth = sunDay.eventDown[event]?.azimuth)
                }
                previousSunDay = sunDay
            } else if (body != null) {
                // Moon and planet rise and set, allowing up to 3 events per day but no diffs
                val bodyDay = BodyPositionCalculator.calcDay(body, location.location, loopCalendar, false)
                if (bodyDay.events.isEmpty()) {
                    addRisenSet(dayEntry, bodyDay.riseSetType == RiseSetType.RISEN, false)
                } else {
                    for (event in bodyDay.events) {
                        addEvent(dayEntry, event.direction, event.time, null, allowSeconds = false, azimuth = event.azimuth)
                    }
                }
                (bodyDay as? MoonDay)?.let { moonDay ->
                    val phaseEvent = moonDay.phaseEvent
                    if (phaseEvent != null) {
                        var phaseImg = getPhaseFull()
                        when {
                            phaseEvent.phase === MoonPhase.NEW -> phaseImg = getPhaseNew()
                            phaseEvent.phase === MoonPhase.FIRST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                            phaseEvent.phase === MoonPhase.LAST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                        }
                        dayEntry.phaseImg = phaseImg
                    }
                    dayEntry.orientationAngles = moonDay.orientationAngles
                }
            } else if (type == "daylight") {
                val sunDay = SunCalculator.calcDay(location.location, loopCalendar, RISESET)
                val length = sunDay.uptimeHours
                val diff = sunDay.uptimeHours - previousSunDay!!.uptimeHours
                val lengthStr = formatDuration(requireContext(), length, allowSeconds)
                val diffStr = formatDiff(requireContext(), diff, allowSeconds)
                dayEntry.events.add(DayEntryEvent(lengthStr, diffStr))
                previousSunDay = sunDay
            }

            entries.add(dayEntry)
            loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
            day++
        }
        return entries
    }

    private fun updateList(data: ArrayList<DayEntry>) {
        calendarList.adapter = DayEntryAdapter(data)
    }

    private fun updateGrid(data: ArrayList<DayEntry>) {
        val calendar = getDateCalendar()

        // Set column headers according to weekday preference.

        for (day in 1..7) {
            val dayId = id("calendarGridD$day")
            var altDay = day + Prefs.firstWeekday(requireContext()) - 1
            if (altDay > 7) {
                altDay -= 7
            }
            when (altDay) {
                1 -> modifyChild(calendarGrid, dayId, text = "SUN")
                2 -> modifyChild(calendarGrid, dayId, text = "MON")
                3 -> modifyChild(calendarGrid, dayId, text = "TUE")
                4 -> modifyChild(calendarGrid, dayId, text = "WED")
                5 -> modifyChild(calendarGrid, dayId, text = "THU")
                6 -> modifyChild(calendarGrid, dayId, text = "FRI")
                7 -> modifyChild(calendarGrid, dayId, text = "SAT")
            }
        }

        // Wipe all days.
        for (row in 1..6) {
            val datesRowId = id("calendarGridDates$row")
            (calendarGrid.findViewById<View>(datesRowId) as TableRow).removeAllViews()
            val imagesRowId = id("calendarGridCells$row")
            (calendarGrid.findViewById<View>(imagesRowId) as TableRow).removeAllViews()
        }

        val loopCalendar = Calendar.getInstance()
        loopCalendar.timeZone = calendar.timeZone
        loopCalendar.timeInMillis = calendar.timeInMillis
        loopCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val month = loopCalendar.get(Calendar.MONTH)
        var row = 1

        // Add empty cells to the first row.
        var firstCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - Prefs.firstWeekday(requireContext())) + 1
        if (firstCol > 7) {
            firstCol -= 7
        }
        for (i in 1 until firstCol) {
            val dateCell = inflate(R.layout.frag_data_calendars_grid_date, calendarGridDates1, false)
            dateCell.visibility = INVISIBLE
            calendarGridDates1.addView(dateCell)
            val cell = inflate(R.layout.frag_data_calendars_grid_cell, calendarGridCells1, false)
            cell.visibility = INVISIBLE
            calendarGridCells1.addView(cell)
        }

        run {
            var i = 0
            while (i < 32 && loopCalendar.get(Calendar.MONTH) == month) {
                val entry = data[i]
                var col = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - Prefs.firstWeekday(requireContext())) + 1
                if (col > 7) {
                    col -= 7
                }

                val datesRow = calendarGrid.findViewById<TableRow>(id("calendarGridDates$row"))
                val cellsRow = calendarGrid.findViewById<TableRow>(id("calendarGridCells$row"))

                val dateCell = inflate(R.layout.frag_data_calendars_grid_date, datesRow, false)
                modifyChild(dateCell, calendarGridTitleText, text = loopCalendar.get(Calendar.DAY_OF_MONTH).toString())

                val cell = inflate(R.layout.frag_data_calendars_grid_cell, datesRow, false)

                entry.orientationAngles?.let { orientationAngles ->
                    entry.phaseImg?.let { phaseImg ->
                        modifyChild(dateCell, calendarGridTitlePhase, visibility = VISIBLE, image = phaseImg)
                    } ?: run {
                        modifyChild(dateCell, calendarGridTitlePhase, visibility = GONE)
                    }
                    val moonImageView = cell.findViewById(calendarGridCellMoon) as MoonPhaseImageView
                    moonImageView.setMoonImage(R.drawable.moonsmall)
                    moonImageView.setOrientationAngles(orientationAngles)
                    moonImageView.visibility = VISIBLE
                } ?: run {
                    modifyChild(cell, calendarGridCellMoon, visibility = GONE)
                    modifyChild(dateCell, calendarGridTitlePhase, visibility = GONE)
                }

                val eventContainer = cell.findViewById<ViewGroup>(calendarGridCellEventsContainer)
                for (event in entry.events) {
                    val eventCell = inflate(R.layout.frag_data_calendars_grid_cell_event, eventContainer, false) as TextView
                    eventCell.text = html(event.time)
                    eventContainer.addView(eventCell)
                }

                if (entry.today) {
                    dateCell.setBackgroundColor(getCalendarGridHighlightColor())
                    cell.setBackgroundColor(getCalendarGridHighlightColor())
                } else {
                    dateCell.setBackgroundColor(getCalendarHeaderColor())
                    cell.setBackgroundColor(getCalendarGridDefaultColor())
                }

                datesRow.addView(dateCell)
                cellsRow.addView(cell)

                loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
                if (loopCalendar.get(Calendar.MONTH) == month && col == 7) {
                    row++
                }
                i++
            }
        }

        // Fill out any remaining cells in the last row.
        loopCalendar.add(Calendar.DAY_OF_MONTH, -1)
        var lastCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - Prefs.firstWeekday(requireContext())) + 1
        if (lastCol > 7) {
            lastCol -= 7
        }
        for (i in lastCol + 1..7) {
            val datesRow = calendarGrid.findViewById<TableRow>(id("calendarGridDates$row"))
            val dateCell = inflate(R.layout.frag_data_calendars_grid_date, datesRow, false)
            dateCell.visibility = INVISIBLE
            datesRow.addView(dateCell)
            val imagesRow = calendarGrid.findViewById<TableRow>(id("calendarGridCells$row"))
            val imageCell = inflate(R.layout.frag_data_calendars_grid_cell, datesRow, false)
            imageCell.visibility = INVISIBLE
            imagesRow.addView(imageCell)
        }
    }

    inner class DayEntryAdapter constructor(list: ArrayList<DayEntry>) : ArrayAdapter<DayEntry>(requireContext(), R.layout.frag_data_calendars_list_row, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: inflate(R.layout.frag_data_calendars_list_row)
            val entry = getItem(position)!!
            val calendarView = Prefs.lastCalendar(requireContext())
            val type = calendarView.type

            if (entry.today) {
                row.findViewById<View>(monthCalDateCell).setBackgroundColor(getCalendarGridHighlightColor())
                row.setBackgroundColor(getCalendarGridHighlightColor())
            } else {
                row.findViewById<View>(monthCalDateCell).setBackgroundColor(getCalendarHeaderColor())
                row.setBackgroundColor(getCalendarGridDefaultColor())
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

            modifyChild(row, rowDate, visibility = VISIBLE, text = entry.day.toString())
            modifyChild(row, rowWeekday, visibility = VISIBLE, text = weekday)

            entry.orientationAngles?.let { orientationAngles ->
                entry.phaseImg?.let { phaseImg ->
                    modifyChild(row, monthCalDatePhase, visibility = VISIBLE, image = phaseImg)
                } ?: run {
                    modifyChild(row, monthCalDatePhase, visibility = INVISIBLE)
                }
                modifyChild(row, dayMoonCell, monthCalDatePhaseCell, visibility = VISIBLE)

                val moonImageView = row.findViewById(dayMoon) as MoonPhaseImageView
                moonImageView.setMoonImage(R.drawable.moonsmall)
                moonImageView.setOrientationAngles(orientationAngles)
                moonImageView.visibility = VISIBLE
            } ?: run {
                modifyChild(row, dayMoonCell, monthCalDatePhaseCell, visibility = GONE)
            }

            val eventsRow = row.findViewById<TableRow>(monthCalEventsRow)
            eventsRow.removeAllViews()

            for (event in entry.events) {
                if (type == "daylight") {
                    val eventCell = inflate(R.layout.frag_data_calendars_list_row_length, eventsRow, false)
                    modifyChild(eventCell, lengthTime, visibility = VISIBLE, text = event.time)
                    modifyChild(eventCell, lengthDiff, visibility = VISIBLE, text = event.detail)
                    eventsRow.addView(eventCell)
                } else {
                    val eventCell = inflate(R.layout.frag_data_calendars_list_row_event, eventsRow, false)
                    modifyChild(eventCell, eventText, visibility = VISIBLE, html = event.time)
                    if (event.detail != null) {
                        modifyChild(eventCell, eventAz, visibility = VISIBLE, text = event.detail)
                    } else {
                        modifyChild(eventCell, eventAz, visibility = GONE)
                    }
                    eventsRow.addView(eventCell)
                }
            }
            return row
        }
    }

    class DayEntry {
        var events: MutableSet<DayEntryEvent> = LinkedHashSet()
        var phaseImg: Int? = null
        var orientationAngles: OrientationAngles? = null
        var day: Int = 0
        var dayOfWeek: Int = 0
        var today = false
    }

    class DayEntryEvent(val time: String, val detail: String?)

}
