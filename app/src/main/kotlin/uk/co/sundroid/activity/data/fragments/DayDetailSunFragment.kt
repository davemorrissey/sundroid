package uk.co.sundroid.activity.data.fragments

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.View
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.YearData
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.YearData.EventType
import uk.co.sundroid.util.time.*

import java.util.Calendar
import java.util.TreeSet

import uk.co.sundroid.util.time.formatDurationHMS

class DayDetailSunFragment : AbstractDayFragment() {

    private val handler = Handler()

    protected override val layout: Int
        get() = R.layout.frag_data_daydetail_sun

    @Throws(Exception::class)
    override fun update(location: LocationDetails, calendar: Calendar, view: View) {

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                val yearEvents = YearData.getYearEvents(calendar.get(Calendar.YEAR), calendar.timeZone)
                var yearEventToday: Event? = null
                for (yearEvent in yearEvents) {
                    if (yearEvent.type.body === Body.SUN) {
                        if (isSameDay(calendar, yearEvent.time)) {
                            yearEventToday = yearEvent
                        }
                    }
                }
                val sunDay = SunCalculator.calcDay(location.location, calendar)
                val todayEvent = yearEventToday

                handler.post {
                    if (isSafe) {
                        if (todayEvent != null) {
                            view.findViewById<View>(R.id.sunEvent).setOnClickListener(null)
                            showInView(view, R.id.sunEvent)
                            showInView(view, R.id.sunEventTitle, todayEvent.type.displayName)
                            if (todayEvent.type === EventType.NORTHERN_SOLSTICE && Math.abs(location.location.latitude.doubleValue) > 23.44) {
                                val localExtreme = if (location.location.latitude.doubleValue >= 0) "Longest" else "Shortest"
                                showInView(view, R.id.sunEventSubtitle, localExtreme + " day")
                            } else if (todayEvent.type === EventType.SOUTHERN_SOLSTICE && Math.abs(location.location.latitude.doubleValue) > 23.44) {
                                val localExtreme = if (location.location.latitude.doubleValue >= 0) "Shortest" else "Longest"
                                showInView(view, R.id.sunEventSubtitle, localExtreme + " day")
                            } else if (todayEvent.type === EventType.ANNULAR_SOLAR || todayEvent.type === EventType.HYBRID_SOLAR || todayEvent.type === EventType.PARTIAL_SOLAR || todayEvent.type === EventType.TOTAL_SOLAR) {
                                showInView(view, R.id.sunEventSubtitle, "Tap to check Wikipedia for visibility")
                                val finalLink = todayEvent.link
                                view.findViewById<View>(R.id.sunEvent).setOnClickListener { view1 ->
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(finalLink)
                                    startActivity(intent)
                                }
                            } else {
                                removeInView(view, R.id.sunEventSubtitle)
                            }
                        } else {
                            removeInView(view, R.id.sunEvent)
                        }

                        var noTransit = true
                        var noUptime = true

                        if (sunDay.riseSetType !== RiseSetType.SET && sunDay.transitAppElevation > 0) {
                            val noon = formatTime(applicationContext!!, sunDay.transit!!, false)
                            noTransit = false
                            showInView(view, R.id.sunTransit)
                            showInView(view, R.id.sunTransitTime, noon.time + noon.marker + "  " + formatElevation(sunDay.transitAppElevation))
                        } else {
                            removeInView(view, R.id.sunTransit)
                        }

                        if (sunDay.riseSetType === RiseSetType.RISEN || sunDay.riseSetType === RiseSetType.SET) {
                            showInView(view, R.id.sunSpecial, if (sunDay.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
                            removeInView(view, R.id.sunEvtsRow, R.id.sunEvt1, R.id.sunEvt2, R.id.sunUptime)
                        } else {
                            removeInView(view, R.id.sunSpecial)
                            removeInView(view, R.id.sunEvt1, R.id.sunEvt2)
                            showInView(view, R.id.sunEvtsRow)
                            val events = TreeSet<SummaryEvent>()
                            if (sunDay.rise != null) {
                                events.add(SummaryEvent("Rise", sunDay.rise!!, sunDay.riseAzimuth))
                            }
                            if (sunDay.set != null) {
                                events.add(SummaryEvent("Set", sunDay.set!!, sunDay.setAzimuth))
                            }
                            var index = 1
                            for (event in events) {
                                val rowId = view("sunEvt" + index)
                                val timeId = view("sunEvt" + index + "Time")
                                val azId = view("sunEvt" + index + "Az")
                                val imgId = view("sunEvt" + index + "Img")

                                val time = formatTime(applicationContext!!, event.time, false)
                                val az = formatBearing(applicationContext!!, event.azimuth!!, location.location, event.time)

                                textInView(view, timeId, time.time + time.marker)
                                textInView(view, azId, az)
                                showInView(view, rowId)
                                imageInView(view, imgId, if (event.name == "Rise") getRiseArrow() else getSetArrow())

                                index++
                            }

                            if (sunDay.uptimeHours > 0 && sunDay.uptimeHours < 24) {
                                noUptime = false
                                showInView(view, R.id.sunUptime)
                                showInView(view, R.id.sunUptimeTime, formatDurationHMS(applicationContext!!, sunDay.uptimeHours, false))
                            } else {
                                removeInView(view, R.id.sunUptime)
                            }

                        }

                        if (noTransit && noUptime) {
                            removeInView(view, R.id.sunTransitUptime, R.id.sunTransitUptimeDivider)
                        } else {
                            showInView(view, R.id.sunTransitUptime, R.id.sunTransitUptimeDivider)
                        }

                        if (sunDay.civDawn == null) {
                            textInView(view, R.id.sunCivDawnTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.civDawn!!, false)
                            textInView(view, R.id.sunCivDawnTime, time.time + time.marker)
                        }
                        if (sunDay.civDusk == null) {
                            textInView(view, R.id.sunCivDuskTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.civDusk!!, false)
                            textInView(view, R.id.sunCivDuskTime, time.time + time.marker)
                        }
                        if (sunDay.ntcDawn == null) {
                            textInView(view, R.id.sunNtcDawnTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.ntcDawn!!, false)
                            textInView(view, R.id.sunNtcDawnTime, time.time + time.marker)
                        }
                        if (sunDay.ntcDusk == null) {
                            textInView(view, R.id.sunNtcDuskTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.ntcDusk!!, false)
                            textInView(view, R.id.sunNtcDuskTime, time.time + time.marker)
                        }
                        if (sunDay.astDawn == null) {
                            textInView(view, R.id.sunAstDawnTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.astDawn!!, false)
                            textInView(view, R.id.sunAstDawnTime, time.time + time.marker)
                        }
                        if (sunDay.astDusk == null) {
                            textInView(view, R.id.sunAstDuskTime, "-")
                        } else {
                            val time = formatTime(applicationContext!!, sunDay.astDusk!!, false)
                            textInView(view, R.id.sunAstDuskTime, time.time + time.marker)
                        }

                        showInView(view, R.id.sunDataBox)
                    }
                }
            }
        }
        thread.start()
    }


}
