package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.widget.TableLayout
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.activity.data.fragments.dialogs.settings.DayEventsPickerFragment
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.time.formatTime
import java.util.*
import kotlin.collections.ArrayList

class DayDetailEventsFragment : AbstractDayDetailFragment(), ConfigurableFragment {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_events

    override fun openSettingsDialog() = DayEventsPickerFragment.show(this)

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
                            astDawn?.let { add("Astro. dawn", it) }
                            astDusk?.let { add("Astro. dusk", it) }
                            ntcDawn?.let { add("Nautical dawn", it) }
                            ntcDusk?.let { add("Nautical dusk", it) }
                            civDawn?.let { add("Civil dawn", it) }
                            civDusk?.let { add("Civil dusk", it) }
                            ghEnd?.let { add("Golden hr end", it) }
                            ghStart?.let { add("Golden hr start", it) }
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
                                text(eventRow, eventName, event.name.toUpperCase())
                                text(eventRow, eventTime, "${formatTime(activity, event.time, true)}")
                                text(eventRow, eventAz, if (event.azimuth != null) formatBearing(activity, event.azimuth, location.location, calendar) else " ")
                                table.addView(eventRow)
                            }
                        }
                        show(view, eventsDataBox)
                    }
                }
        )
    }

}
