package uk.co.sundroid.activity.data.fragments

import android.view.View
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.geometry.formatElevation
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.formatDurationHMS
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.util.time.isSameDay
import uk.co.sundroid.util.time.shortDateAndMonth
import java.util.*

class DayDetailMoonFragment : AbstractDayDetailFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_moon

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarTitle(R.string.data_detail_title)
        (activity as MainActivity).setToolbarSubtitle(R.string.data_detail_subtitle_moon)
    }

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        class Data(val day: MoonDay, val phaseEvents: List<MoonPhaseEvent>, val yearEvent: YearData.Event?)
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

                        async(
                                inBackground = { MoonPhaseImage.makeImage(resources, R.drawable.moon, day.phaseDouble, location.location.latitude.doubleValue < 0, MoonPhaseImage.SIZE_LARGE) },
                                onDone = { bitmap ->
                                    if (isSafe) {
                                        image(view, moonImage, bitmap)
                                    }
                                }
                        )

                        if (yearEvent != null) {
                            view.findViewById<View>(moonEvent).setOnClickListener(null)
                            show(view, moonEvent)
                            show(view, moonEventTitle, yearEvent.type.displayName)
                            show(view, moonEventSubtitle, "Tap to check Wikipedia for visibility")
                            view.findViewById<View>(moonEvent).setOnClickListener { browseTo(yearEvent.link) }
                        } else {
                            remove(view, moonEvent)
                        }

                        for (i in 1..4) {
                            val phaseEvent = phaseEvents[i - 1]
                            val phaseImgView = view("moonPhase${i}Img")
                            val phaseLabelView = view("moonPhase${i}Label")
                            var phaseImg = getPhaseFull()
                            when {
                                phaseEvent.phase === MoonPhase.NEW -> phaseImg = getPhaseNew()
                                phaseEvent.phase === MoonPhase.FIRST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                                phaseEvent.phase === MoonPhase.LAST_QUARTER -> phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                            }
                            image(view, phaseImgView, phaseImg)
                            text(view, phaseLabelView, shortDateAndMonth(phaseEvent.time))
                        }

                        var noTransit = true
                        var noUptime = true

                        if (day.riseSetType !== RiseSetType.SET && day.transitAppElevation > 0) {
                            val noon = formatTime(activity, day.transit!!, false)
                            noTransit = false
                            show(view, moonTransit)
                            show(view, moonTransitTime, "$noon  ${formatElevation(day.transitAppElevation)}")
                        } else {
                            remove(view, moonTransit)
                        }

                        if (day.riseSetType === RiseSetType.RISEN || day.riseSetType === RiseSetType.SET) {
                            show(view, moonSpecial, if (day.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
                            remove(view, moonEvtsRow, moonEvt0, moonEvt1, moonUptime)
                        } else {
                            remove(view, moonSpecial)
                            remove(view, moonEvt0, moonEvt1)
                            show(view, moonEvtsRow)
                            val events = TreeSet<RiseSetEvent>()
                            day.rise?.let { events.add(RiseSetEvent("Rise", it, day.riseAzimuth)) }
                            day.set?.let { events.add(RiseSetEvent("Set", it, day.setAzimuth)) }
                            events.forEachIndexed({ index, event ->
                                val rowId = view("moonEvt$index")
                                val timeId = view("moonEvt${index}Time")
                                val azId = view("moonEvt${index}Az")
                                val imgId = view("moonEvt${index}Img")

                                val time = formatTime(activity, event.time, false)
                                val az = formatBearing(activity, event.azimuth, location.location, event.time)

                                text(view, timeId, "$time")
                                text(view, azId, az)
                                show(view, rowId)
                                image(view, imgId, if (event.name == "Rise") getRiseArrow() else getSetArrow())
                            })

                            if (day.uptimeHours > 0 && day.uptimeHours < 24) {
                                noUptime = false
                                show(view, moonUptime)
                                show(view, moonUptimeTime, formatDurationHMS(activity, day.uptimeHours, false))
                            } else {
                                remove(view, moonUptime)
                            }
                        }

                        if (noTransit && noUptime) {
                            remove(view, moonTransitUptime, moonTransitUptimeDivider)
                        } else {
                            show(view, moonTransitUptime, moonTransitUptimeDivider)
                        }

                        show(view, moonPhase, day.phase.displayName + (day.phaseEvent?.let {
                            " " + formatTime(activity, it.time, false).toString()
                        } ?: ""))
                        show(view, moonIllumination, "${day.illumination}%")
                        show(view, moonDataBox)
                    }
                }
        )
    }

}
