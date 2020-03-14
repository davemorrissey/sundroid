package uk.co.sundroid.activity.data.fragments

import android.app.Activity
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.settings.TrackerSettingsFragment
import uk.co.sundroid.util.astro.image.TrackerImageView
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.image.TrackerImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.*

import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TrackerFragment : AbstractTimeFragment(), SensorEventListener, TrackerMapFragment.MapCenterListener {

    private var trackerLcation: LocationDetails? = null
    private var trackerDateCalendar: Calendar? = null
    private var trackerTimeCalendar: Calendar? = null

    private var compassActive: Boolean = false

    private var trackerImage: TrackerImage? = null
    private var trackerImageView: TrackerImageView? = null

    private val handler = Handler()
    private var executor: ThreadPoolExecutor? = null
    private var queue: BlockingQueue<Runnable>? = null

    private var magneticDeclination = 0.0
    private var rotation: Int = 0

    override val layout: Int
        get() = R.layout.frag_data_tracker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queue = ArrayBlockingQueue(1)
        executor = ThreadPoolExecutor(1, 1, 10000, TimeUnit.MILLISECONDS, queue)
    }

    override fun onResume() {
        super.onResume()
        val activity = activity ?: return
        val windowManager = activity.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay ?: return
        rotation = display.rotation * Surface.ROTATION_90
        (activity as MainActivity).apply {
            setToolbarSubtitle(R.string.data_tracker_title)
            setViewConfigurationCallback { openSettingsDialog() }
        }
    }

    override fun onPause() {
        if (compassActive) {
            val sensorManager = requireContext().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(this)
        }
        val mapFragment = requireFragmentManager().findFragmentByTag(MAP_TAG)
        if (mapFragment != null) {
            requireFragmentManager()
                    .beginTransaction()
                    .remove(mapFragment)
                    .commit()
        }
        super.onPause()
    }

    override fun initialise(location: LocationDetails, dateCalendar: Calendar, timeCalendar: Calendar, view: View) {
        this.trackerLcation = location
        this.trackerDateCalendar = dateCalendar
        this.trackerTimeCalendar = timeCalendar

        val body = Prefs.sunTrackerBody(requireContext())
        val mode = Prefs.sunTrackerMode(requireContext())
        val mapType = Prefs.sunTrackerMapType(requireContext())

        if (mode == "radar" && Prefs.sunTrackerCompass(requireContext())) {
            magneticDeclination = getMagneticDeclination(location.location, trackerDateCalendar!!)
        }

        if (compassActive) {
            val sensorManager = requireContext().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(this)
        }
        compassActive = false

        if (Prefs.sunTrackerText(requireContext()) && body != null) {
            show(view, R.id.trackerText)
        } else {
            remove(view, R.id.trackerText)
        }

        trackerImage = TrackerImage(TrackerImage.TrackerStyle.forMode(mode, mapType), requireContext(), location.location)
        trackerImage!!.setDate(trackerDateCalendar!!, trackerTimeCalendar!!)
        trackerImageView = TrackerImageView(requireContext())
        trackerImageView!!.setTrackerImage(trackerImage!!)
        trackerImageView!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        if (mode != "radar") {

            show(view, R.id.trackerMapContainer)

            val mapFragment = TrackerMapFragment()
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.trackerMapContainer, mapFragment, MAP_TAG)
                    .commit()
            show(view, R.id.trackerRadarContainer)

            trackerImageView!!.setDirection(0f)

        } else {

            remove(view, R.id.trackerMapContainer)
            show(view, R.id.trackerRadarContainer)

            val mapFragment = requireFragmentManager().findFragmentByTag(MAP_TAG)
            if (mapFragment != null) {
                requireFragmentManager()
                        .beginTransaction()
                        .remove(mapFragment)
                        .commit()
            }

            if (Prefs.sunTrackerCompass(requireContext())) {
                val sensorManager = requireContext().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
                val orientationSensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION)
                if (orientationSensors.isNotEmpty()) {
                    compassActive = sensorManager.registerListener(this, orientationSensors[0], SensorManager.SENSOR_DELAY_GAME)
                }
            } else {
                trackerImageView!!.setDirection(0f)
            }

        }

        // TODO Try embedding this view in the layout
        (view.findViewById<View>(R.id.trackerRadarContainer) as ViewGroup).removeAllViews()
        (view.findViewById<View>(R.id.trackerRadarContainer) as ViewGroup).addView(trackerImageView)

        startImageUpdate(false)

    }

    override fun update(location: LocationDetails, dateCalendar: Calendar, timeCalendar: Calendar, view: View, timeOnly: Boolean) {
        this.trackerLcation = location
        this.trackerDateCalendar = dateCalendar
        this.trackerTimeCalendar = timeCalendar
        startImageUpdate(timeOnly)
    }

    fun openSettingsDialog() {
        val settingsDialog = TrackerSettingsFragment.newInstance()
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(requireFragmentManager(), "trackerSettings")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isSafe) {
            return
        }
        val mode = Prefs.sunTrackerMode(requireContext())
        if (mode != "radar") {
            return
        }
        if (trackerImageView != null && Prefs.sunTrackerCompass(requireContext())) {
            trackerImageView!!.setDirection(event.values[0] + rotation.toFloat() + java.lang.Double.valueOf(magneticDeclination).toFloat())
        }
    }

    override fun setLocationPoint(point: Point) {
        if (trackerImageView != null) {
            trackerImageView!!.setCenter(point)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }


    @Synchronized
    private fun startImageUpdate(timeOnly: Boolean) {
        if (view == null || trackerDateCalendar == null || trackerTimeCalendar == null) {
            return
        }
        val dateCalendar = clone(this.trackerDateCalendar!!)
        val timeCalendar = clone(this.trackerTimeCalendar!!)
        val body = Prefs.sunTrackerBody(requireContext())

        if (trackerImage != null) {
            if (timeOnly) {
                trackerImage!!.setTime(timeCalendar)
            } else {
                trackerImage!!.setDate(dateCalendar, timeCalendar)
            }
        }

        queue!!.clear()
        executor!!.submit {
            if (isSafe) {

                var eventsSet: MutableSet<Event>? = null
                val position = if (body != null && Prefs.sunTrackerText(requireContext())) BodyPositionCalculator.calcPosition(body, trackerLcation!!.location, timeCalendar) else null

                // Get the first two rise/set events that happen on this calendar day,
                // midnight to midnight.

                if (!timeOnly && body != null && Prefs.sunTrackerText(requireContext())) {
                    eventsSet = TreeSet()
                    val loopCalendar = clone(dateCalendar)
                    loopCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    for (i in 0..2) {
                        val bodyDay = BodyPositionCalculator.calcDay(body, trackerLcation!!.location, loopCalendar, false)
                        if (bodyDay.rise != null && isSameDay(bodyDay.rise!!, dateCalendar) && eventsSet.size < 2) {
                            eventsSet.add(Event("RISE", bodyDay.rise!!, bodyDay.riseAzimuth))
                        }
                        if (bodyDay.set != null && isSameDay(bodyDay.set!!, dateCalendar) && eventsSet.size < 2) {
                            eventsSet.add(Event("SET", bodyDay.set!!, bodyDay.setAzimuth))
                        }
                        loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }

                trackerImage!!.generate()

                handler.post {
                    if (isSafe) {
                        if (position != null && Prefs.sunTrackerText(requireContext())) {

                            if (eventsSet != null) {
                                if (eventsSet.size > 0) {
                                    val event1 = eventsSet.toTypedArray()[0]
                                    val time = formatTime(requireContext(), event1.time, false)
                                    val az = formatBearing(requireContext(), event1.azimuth!!, trackerLcation!!.location, event1.time)
                                    text(view!!, R.id.trackerEvt1Name, event1.name)
                                    text(view!!, R.id.trackerEvt1Time, time.time + time.marker)
                                    text(view!!, R.id.trackerEvt1Az, az)
                                } else {
                                    text(view!!, R.id.trackerEvt1Name, "")
                                    text(view!!, R.id.trackerEvt1Time, "")
                                    text(view!!, R.id.trackerEvt1Az, "")
                                }

                                if (eventsSet.size > 1) {
                                    val event2 = eventsSet.toTypedArray()[1]
                                    val time = formatTime(requireContext(), event2.time, false)
                                    val az = formatBearing(requireContext(), event2.azimuth!!, trackerLcation!!.location, event2.time)
                                    text(view!!, R.id.trackerEvt2Name, event2.name)
                                    text(view!!, R.id.trackerEvt2Time, time.time + time.marker)
                                    text(view!!, R.id.trackerEvt2Az, az)
                                } else {
                                    text(view!!, R.id.trackerEvt2Name, "")
                                    text(view!!, R.id.trackerEvt2Time, "")
                                    text(view!!, R.id.trackerEvt2Az, "")
                                }
                            }

                            var elBd = BigDecimal(position.appElevation)
                            elBd = elBd.setScale(1, BigDecimal.ROUND_HALF_DOWN)
                            val el = elBd.toString() + "\u00b0"
                            val az = formatBearing(requireContext(), position.azimuth, trackerLcation!!.location, timeCalendar)

                            text(view!!, R.id.trackerAz, az)
                            text(view!!, R.id.trackerEl, el)
                            text(view!!, R.id.trackerBodyAndLight, body!!.name.substring(0, 1) + body.name.substring(1).toLowerCase(Locale.getDefault()) + ": " + getLight(body, position.appElevation))
                        }

                        trackerImageView!!.invalidate()
                    }
                }
            }

        }

    }

    class Event(val name: String, val time: Calendar, val azimuth: Double?) : Comparable<Event> {
        override fun compareTo(other: Event): Int {
            val result = time.compareTo(other.time)
            return if (result == 0) {
                1
            } else result
        }
    }

    private fun getLight(body: Body?, elevation: Double): String {
        return if (body === Body.SUN) {
            when {
                elevation >= 6 -> "Day"
                elevation >= -0.833 -> "Golden hour"
                elevation >= -6 -> "Civil twilight"
                elevation >= -12 -> "Nautical twilight"
                elevation >= -18 -> "Astronomical twilight"
                else -> "Night"
            }
        } else if (body === Body.MOON) {
            if (elevation >= -0.833) {
                "Risen"
            } else {
                "Set"
            }
        } else {
            if (elevation >= 0.0) {
                "Risen"
            } else {
                "Set"
            }

        }
    }

    companion object {
        private const val MAP_TAG = "map"
    }

}
