package uk.co.sundroid.activity.data.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import android.view.View.VISIBLE
import android.view.View.GONE
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*
import java.util.*

import kotlinx.android.synthetic.main.frag_data_daysummary.*
import uk.co.sundroid.util.astro.RiseSetType.*
import uk.co.sundroid.activity.MainActivity

class DaySummaryFragment : AbstractDayFragment() {
    private val handler = Handler()
    override val layout: Int
        get() = R.layout.frag_data_daysummary

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarSubtitle(R.string.data_summary_title)
    }

    @SuppressLint("SetTextI18n")
    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar)
        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay

        Thread(Runnable {
            context?.let {
                val phase: Double = moonDay.phaseDouble
                val moonBitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, phase, location.location.latitude.doubleValue < 0, MoonPhaseImage.SIZE_MEDIUM)
                handler.post {
                    moonImage?.setImageBitmap(moonBitmap)
                }
            }
        }).start()

        modify(sunSpecial, sunEvtsRow, sunEvt0, sunEvt1, sunUptime, visibility = GONE)
        if (sunDay.riseSetType === RISEN || sunDay.riseSetType === SET) {
            modify(sunSpecial, visibility = VISIBLE, text = if (sunDay.riseSetType === RISEN) "Risen all day" else "Set all day")
        } else {
            modify(sunEvtsRow, visibility = VISIBLE)
            val events = TreeSet<RiseSetEvent>()
            sunDay.rise?.let { events.add(RiseSetEvent("Rise", it, sunDay.riseAzimuth)) }
            sunDay.set?.let { events.add(RiseSetEvent("Set", it, sunDay.setAzimuth)) }
            events.forEachIndexed { index, event ->
                val time = formatTime(requireContext(), event.time, false)
                modifyChild(view, id("sunEvt$index"), visibility = VISIBLE)
                modifyChild(view, id("sunEvt${index}Img"), image = if (event.name == "Rise") getRiseArrow() else getSetArrow())
                modifyChild(view, id("sunEvt${index}Label"), visibility = VISIBLE, text = event.name.toUpperCase(Locale.getDefault()))
                modifyChild(view, id("sunEvt${index}Time"), visibility = VISIBLE, text = time.time + time.marker)
            }

            if (sunDay.uptimeHours > 0 && sunDay.uptimeHours < 24) {
                modify(sunUptime, visibility = VISIBLE)
                modify(sunUptimeTime, text = formatDurationHMS(requireContext(), sunDay.uptimeHours, false))
            }
        }

        modify(moonSpecial, moonEvtsRow, moonEvt0, moonEvt1, visibility = GONE)

        if (moonDay.riseSetType === RISEN || moonDay.riseSetType === SET) {
            modify(moonSpecial, visibility = VISIBLE, text = if (moonDay.riseSetType === RISEN) "Risen all day" else "Set all day")
        } else {
            modify(moonEvtsRow, visibility = VISIBLE)
            val events = TreeSet<RiseSetEvent>()
            moonDay.rise?.let { events.add(RiseSetEvent("Rise", it, moonDay.riseAzimuth)) }
            moonDay.set?.let { events.add(RiseSetEvent("Set", it, moonDay.setAzimuth)) }
            events.forEachIndexed { index, event ->
                val time = formatTime(requireContext(), event.time, false)
                modifyChild(view, id("moonEvt$index"), visibility = VISIBLE)
                modifyChild(view, id("moonEvt${index}Img"), image = if (event.name == "Rise") getRiseArrow() else getSetArrow())
                modifyChild(view, id("moonEvt${index}Label"), text = event.name.toUpperCase(Locale.getDefault()))
                modifyChild(view, id("moonEvt${index}Time"), text = "$time")
            }
        }

        modify(moonPhase, text = moonDay.phase.displayName + (moonDay.phaseEvent?.let {
            " " + formatTime(requireContext(), it.time, false).toString()
        } ?: ""))
    }

}