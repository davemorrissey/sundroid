package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.settings.DayEventsPickerFragment
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.*

import java.util.*
import kotlin.collections.ArrayList

class DayDetailEventsFragment : AbstractDayFragment(), ConfigurableFragment {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_daydetail_events

    override fun openSettingsDialog() = DayEventsPickerFragment.show(this)

    override fun initialise() {

    }

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                var sunDay: SunDay? = null
                var moonDay: MoonDay? = null
                var planetDays: MutableMap<Body, BodyDay>? = null

                if (Prefs.showElement(activity, "evtByTimeSun", true)) {
                    sunDay = SunCalculator.calcDay(location.location, calendar)
                }
                if (Prefs.showElement(activity, "evtByTimeMoon", true)) {
                    moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay
                }
                if (Prefs.showElement(activity, "evtByTimePlanets", false)) {
                    planetDays = LinkedHashMap()
                    for (body in Body.values()) {
                        if (body !== Body.SUN && body !== Body.MOON) {
                            planetDays[body] = BodyPositionCalculator.calcDay(body, location.location, calendar, true)
                        }
                    }
                }

                val eventsList = ArrayList<SummaryEvent>()
                fun add(e: String, t: Calendar, az: Double? = null) = eventsList.add(SummaryEvent(e, t, az))
                sunDay?.rise?.let { add("Sunrise", it, sunDay.riseAzimuth) }
                sunDay?.set?.let { add("Sunset", it, sunDay.setAzimuth) }
                sunDay?.astDawn?.let { add("Astro. dawn", it) }
                sunDay?.astDusk?.let { add("Astro. dusk", it) }
                sunDay?.ntcDawn?.let { add("Nautical dawn", it) }
                sunDay?.ntcDusk?.let { add("Nautical dusk", it) }
                sunDay?.civDawn?.let { add("Civil dawn", it) }
                sunDay?.civDusk?.let { add("Civil dusk", it) }
                sunDay?.ghEnd?.let { add("Golden hr end", it) }
                sunDay?.ghStart?.let {add("Golden hr start", it) }
                sunDay?.transit?.let { if (sunDay.riseSetType !== RiseSetType.SET) { add("Solar noon", it) }}
                moonDay?.rise?.let { add("Moonrise", it, moonDay.riseAzimuth) }
                moonDay?.set?.let { add("Moonset", it, moonDay.setAzimuth) }
                planetDays?.forEach { (planet, planetDay) -> run {
                    planetDay.rise?.let { add(planet.displayName + " rise", it, planetDay.riseAzimuth) }
                    planetDay.set?.let { add(planet.displayName + " set", it, planetDay.setAzimuth) }
                }}
                Collections.sort(eventsList)

                handler.post {
                    if (isSafe) {
                        val eventsTable = view.findViewById<TableLayout>(R.id.eventsTable)
                        eventsTable.removeAllViews()

                        val eventsHeader = activity.layoutInflater.inflate(R.layout.frag_data_daydetail_events_header, null)
                        eventsTable.addView(eventsHeader)

                        if (eventsList.isEmpty()) {
                            showInView(view, R.id.eventsNone)
                            removeInView(view, R.id.eventsTable)
                        } else {
                            showInView(view, R.id.eventsTable)
                            removeInView(view, R.id.eventsNone)
                            for (event in eventsList) {
                                val eventRow = activity.layoutInflater.inflate(R.layout.frag_data_daydetail_events_row, null)
                                (eventRow.findViewById<View>(R.id.eventName) as TextView).text = event.name.toUpperCase()
                                val time = formatTime(applicationContext!!, event.time, true)
                                (eventRow.findViewById<View>(R.id.eventTime) as TextView).text = time.time + time.marker
                                if (event.azimuth != null) {
                                    val azimuth = formatBearing(applicationContext!!, event.azimuth, location.location, calendar)
                                    (eventRow.findViewById<View>(R.id.eventAz) as TextView).text = azimuth
                                } else {
                                    (eventRow.findViewById<View>(R.id.eventAz) as TextView).text = " "
                                }
                                eventsTable.addView(eventRow)
                            }
                        }
                        showInView(view, R.id.eventsDataBox)
                    }
                }
            }
        }
        thread.start()


    }


}
