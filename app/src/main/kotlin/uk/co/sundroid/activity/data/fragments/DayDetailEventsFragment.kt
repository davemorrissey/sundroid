package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.widget.TableLayout
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.time.formatTime
import java.util.*
import kotlin.collections.ArrayList

class DayDetailEventsFragment : AbstractDayDetailFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_events

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        async(
                inBackground = {
                    val eventsList = ArrayList<SummaryEvent>()
                    fun add(e: String, t: Calendar, az: Double? = null) = eventsList.add(SummaryEvent(e, t, az))

                    if (prefs.showElement("evtByTimeSun")) {
                        SunCalculator.calcDay(location.location, calendar).apply {
                            rise?.let { add("Sunrise", it, riseAzimuth) }
                            set?.let { add("Sunset", it, setAzimuth) }
                            astDawn?.let { add("Astronomical dawn", it) }
                            astDusk?.let { add("Astronomical dusk", it) }
                            ntcDawn?.let { add("Nautical dawn", it) }
                            ntcDusk?.let { add("Nautical dusk", it) }
                            civDawn?.let { add("Civil dawn", it) }
                            civDusk?.let { add("Civil dusk", it) }
                            ghEnd?.let { add("Golden hour end", it) }
                            ghStart?.let { add("Golden hour start", it) }
                            transit?.let {
                                if (riseSetType !== RiseSetType.SET) {
                                    add("Solar noon", it)
                                }
                            }
                        }
                    }

                    if (prefs.showElement("evtByTimeMoon")) {
                        (BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay).apply {
                            rise?.let { add("Moonrise", it, riseAzimuth) }
                            set?.let { add("Moonset", it, setAzimuth) }
                        }
                    }

                    if (prefs.showElement("evtByTimePlanets", false)) {
                        for (planet in Body.PLANETS) {
                            BodyPositionCalculator.calcDay(planet, location.location, calendar, true).apply {
                                rise?.let { add("${planet.displayName} rise", it, riseAzimuth) }
                                set?.let { add("${planet.displayName} set", it, setAzimuth) }
                            }
                        }
                    }

                    eventsList.sort()
                    eventsList
                },
                onDone = { eventsList ->
                    if (isSafe) {
                        val table = view.findViewById<TableLayout>(eventsTable)
                        table.removeAllViews()

                        val eventsHeader = inflate(R.layout.frag_data_daydetail_events_header)
                        table.addView(eventsHeader)

                        if (eventsList.isEmpty()) {
                            toggle(view, on = eventsNone, off = eventsTable)
                        } else {
                            toggle(view, on = eventsTable, off = eventsNone)
                            for (event in eventsList) {
                                val eventRow = inflate(R.layout.frag_data_daydetail_events_row)
                                modifyChild(eventRow, eventName, text = event.name.toUpperCase(Locale.getDefault()))
                                modifyChild(eventRow, eventTime, text = "${formatTime(requireContext(), event.time, true)}")
                                modifyChild(eventRow, eventAz, text = if (event.azimuth != null) formatBearing(requireContext(), event.azimuth, location.location, calendar) else " ")
                                table.addView(eventRow)
                            }
                        }
                        show(view, eventsDataBox)
                    }
                }
        )
    }

}
