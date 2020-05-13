package uk.co.sundroid.activity.data.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.view.View
import android.view.View.VISIBLE
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.RISING
import uk.co.sundroid.util.astro.BodyDayEvent.Event.RISESET
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*

import kotlinx.android.synthetic.main.frag_data_daysummary.*
import uk.co.sundroid.util.astro.RiseSetType.*
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.geometry.formatBearing
import java.util.*

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

        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar, RISESET)
        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay

        Thread(Runnable {
            context?.let {
                val bitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, moonDay.phaseDouble, moonDay.orientationAngles)
                handler.post {
                    moonImage.scaleType = ImageView.ScaleType.MATRIX
                    val size = ((requireContext().resources.displayMetrics.densityDpi/160.0) * 100).toInt().toFloat()
                    val matrix = Matrix()
                    matrix.postRotate(moonDay.orientationAngles.imageRotationAngle(), bitmap.width/2f, bitmap.height/2f)
                    matrix.postScale(size/bitmap.width, size/bitmap.height)
                    moonImage.imageMatrix = matrix
                    moonImage?.setImageBitmap(bitmap)
                }
            }
        }).start()

        val sunEventsRow = view.findViewById<ViewGroup>(R.id.sunEventsRow)
        modify(sunEventsRow, visibility = VISIBLE)
        sunEventsRow.removeAllViews()

        if (sunDay.riseSetType === RISEN || sunDay.riseSetType === SET) {
            val eventCell = inflate(R.layout.frag_data_event, sunEventsRow, false)
            eventCell.setTag(R.attr.eventIndex, "sunSpecial")
            modifyChild(eventCell, evtImg, image = if (sunDay.riseSetType === RISEN) getRisenAllDay() else getSetAllDay())
            modifyChild(eventCell, evtTime, html = if (sunDay.riseSetType === RISEN) "<small>RISEN ALL DAY</small>" else "<small>SET ALL DAY</small>")
            modifyChild(eventCell, evtAz, visibility = GONE)
            modifyChild(view, R.id.sunUptime, visibility = GONE)
            sunEventsRow.addView(eventCell)
        } else {
            sunDay.events.forEachIndexed { i, event ->
                val eventCell = inflate(R.layout.frag_data_event, sunEventsRow, false)
                eventCell.setTag(R.attr.eventIndex, "sunEvt$i")
                eventCell.setTag(R.attr.eventType, event.direction.name)
                val az = formatBearing(requireContext(), event.azimuth ?: 0.0, location.location, event.time)
                modifyChild(eventCell, evtImg, image = if (event.direction == RISING) getRiseArrow() else getSetArrow())
                modifyChild(eventCell, evtTime, html = formatTimeStr(requireContext(), event.time, false, html = true))
                modifyChild(eventCell, evtAz, text = az)
                sunEventsRow.addView(eventCell)
            }

            if (sunDay.uptimeHours > 0 && sunDay.uptimeHours < 24) {
                modifyChild(view, R.id.sunUptimeTime, visibility = VISIBLE, html = formatDurationHMS(requireContext(), sunDay.uptimeHours, false, html = true))
                modifyChild(view, R.id.sunUptime, visibility = VISIBLE)
            } else {
                modifyChild(view, R.id.sunUptime, visibility = GONE)
            }
        }

        val moonEventsRow = view.findViewById<ViewGroup>(R.id.moonEventsRow)
        modify(moonEventsRow, visibility = VISIBLE)
        moonEventsRow.removeAllViews()

        if (moonDay.riseSetType === RISEN || moonDay.riseSetType === SET) {
            val eventCell = inflate(R.layout.frag_data_event, moonEventsRow, false)
            eventCell.setTag(R.attr.eventIndex, "mmonSpecial")
            modifyChild(eventCell, evtImg, image = if (moonDay.riseSetType === RISEN) getRisenAllDay() else getSetAllDay())
            modifyChild(eventCell, evtTime, html = if (moonDay.riseSetType === RISEN) "<small>RISEN ALL DAY</small>" else "<small>SET ALL DAY</small>")
            modifyChild(eventCell, evtAz, visibility = GONE)
            moonEventsRow.addView(eventCell)
        } else {
            moonDay.events.forEachIndexed { i, event ->
                val eventCell = inflate(R.layout.frag_data_event, moonEventsRow, false)
                eventCell.setTag(R.attr.eventIndex, "moonEvt$i")
                eventCell.setTag(R.attr.eventType, event.direction.name)
                val az = formatBearing(requireContext(), event.azimuth ?: 0.0, location.location, event.time)
                modifyChild(eventCell, evtImg, image = if (event.direction == RISING) getRiseArrow() else getSetArrow())
                modifyChild(eventCell, evtTime, html = formatTimeStr(requireContext(), event.time, false, html = true))
                modifyChild(eventCell, evtAz, text = az)
                moonEventsRow.addView(eventCell)
            }
        }
        modify(moonPhase, html = (moonDay.phase.displayName.toUpperCase(Locale.getDefault()) + (moonDay.phaseEvent?.let {
            " at " + formatTimeStr(requireContext(), it.time, false, html = true)
        } ?: "")))
    }

}