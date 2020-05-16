package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.RISING
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.image.MoonPhaseImageView
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.geometry.formatElevation
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.formatDurationHMS
import uk.co.sundroid.util.time.formatTimeStr
import uk.co.sundroid.util.time.isSameDay
import uk.co.sundroid.util.time.shortDateAndMonth
import java.util.*

class DayDetailMoonFragment : AbstractDayDetailFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_moon

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        class Data(val day: MoonDay, val phaseEvents: List<MoonPhaseEvent>, val yearEvent: Event?)
        async(
                inBackground = {
                    val day = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, true) as MoonDay
                    val phaseEvents = MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR), calendar.timeZone)
                            .filter { it.time.get(Calendar.DAY_OF_YEAR) >= calendar.get(Calendar.DAY_OF_YEAR) }
                            .toMutableList()
                    if (phaseEvents.size < 4) {
                        phaseEvents.addAll(MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR) + 1, calendar.timeZone))
                    }
                    val yearEvents = YearData.getYearEvents(calendar.get(Calendar.YEAR), calendar.timeZone)
                    val yearEvent: Event? = yearEvents.lastOrNull { it.type.body === Body.MOON && isSameDay(calendar, it.time) }
                    Data(day, phaseEvents, yearEvent)
                },
                onDone = { data: Data ->
                    if (isSafe) {
                        val day = data.day
                        val phaseEvents = data.phaseEvents
                        val yearEvent = data.yearEvent

                        val moonImageView = view.findViewById(moonImage) as MoonPhaseImageView
                        moonImageView.setOrientationAngles(day.orientationAngles)

                        if (yearEvent != null) {
                            view.findViewById<View>(moonEvent).setOnClickListener(null)
                            modifyChild(view, moonEvent, visibility = VISIBLE)
                            modifyChild(view, moonEventTitle, visibility = VISIBLE, text = yearEvent.type.displayName)
                            modifyChild(view, moonEventSubtitle, visibility = VISIBLE, text = "Tap to check Wikipedia for visibility")
                            view.findViewById<View>(moonEvent).setOnClickListener { browseTo(yearEvent.link) }
                        } else {
                            modifyChild(view, moonEvent, visibility = GONE)
                        }

                        for (i in 1..4) {
                            val phaseEvent = phaseEvents[i - 1]
                            val phaseImgView = id("moonPhase${i}Img")
                            val phaseLabelView = id("moonPhase${i}Label")
                            var phaseImg = getPhaseFull()
                            when {
                                phaseEvent.phase === MoonPhase.NEW -> phaseImg = getPhaseNew()
                                phaseEvent.phase === MoonPhase.FIRST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                                phaseEvent.phase === MoonPhase.LAST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                            }
                            modifyChild(view, phaseImgView, image = phaseImg)
                            modifyChild(view, phaseLabelView, visibility = VISIBLE, html = shortDateAndMonth(phaseEvent.time, html = true, upperCase = true))
                        }

                        var noTransit = true
                        var noUptime = true

                        if (day.riseSetType !== RiseSetType.SET && day.transitAppElevation > 0) {
                            val noon = formatTimeStr(requireContext(), day.transit!!, false, html = true)
                            noTransit = false
                            modifyChild(view, moonTransit, visibility = VISIBLE)
                            modifyChild(view, moonTransitTime, html = "$noon &nbsp; ${formatElevation(day.transitAppElevation)}")
                        } else {
                            modifyChild(view, moonTransit, visibility = GONE)
                        }

                        val eventsRow = view.findViewById<ViewGroup>(moonEventsRow)
                        eventsRow.removeAllViews()

                        if (day.riseSetType === RiseSetType.RISEN || day.riseSetType === RiseSetType.SET) {
                            val eventCell = inflate(R.layout.frag_data_event, eventsRow, false)
                            modifyChild(eventCell, evtImg, image = if (day.riseSetType === RiseSetType.RISEN) getRisenAllDay() else getSetAllDay())
                            modifyChild(eventCell, evtTime, html = if (day.riseSetType === RiseSetType.RISEN) "<small>RISEN ALL DAY</small>" else "<small>SET ALL DAY</small>")
                            modifyChild(eventCell, evtAz, visibility = GONE)
                            modifyChild(view, moonUptime, visibility = GONE)
                            eventsRow.addView(eventCell)
                            noUptime = true
                        } else {
                            day.events.forEach { event ->
                                val eventCell = inflate(R.layout.frag_data_event, eventsRow, false)
                                val az = formatBearing(requireContext(), event.azimuth ?: 0.0, location.location, event.time)
                                modifyChild(eventCell, evtImg, image = if (event.direction == RISING) getRiseArrow() else getSetArrow())
                                modifyChild(eventCell, evtTime, html = formatTimeStr(requireContext(), event.time, false, html = true))
                                modifyChild(eventCell, evtAz, text = az)
                                eventsRow.addView(eventCell)
                            }

                            if (day.uptimeHours > 0 && day.uptimeHours < 24) {
                                modifyChild(view, moonUptimeTime, visibility = VISIBLE, html = formatDurationHMS(requireContext(), day.uptimeHours, false, html = true))
                                modifyChild(view, moonUptime, visibility = VISIBLE)
                            } else {
                                modifyChild(view, moonUptime, visibility = GONE)
                            }
                        }

                        modifyChild(view, moonTransitUptime, moonTransitUptimeDivider, visibility = if (noTransit && noUptime) GONE else VISIBLE)

                        modifyChild(view, moonPhase, visibility = VISIBLE, html = (day.phase.displayName.toUpperCase(Locale.getDefault()) + (day.phaseEvent?.let {
                            " at " + formatTimeStr(requireContext(), it.time, false, html = true)
                        } ?: "")))
                        modifyChild(view, moonIllumination, text = "${day.illumination}%")
                        modifyChild(view, moonDataBox, visibility = VISIBLE)
                    }
                }
        )
    }

}
