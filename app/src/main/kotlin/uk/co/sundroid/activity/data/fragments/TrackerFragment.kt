package uk.co.sundroid.activity.data.fragments

import android.app.Activity
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.view.Surface
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.settings.TrackerSettingsFragment
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.image.TrackerImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.*

import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.frag_data_tracker.*

class TrackerFragment : AbstractTimeFragment(), SensorEventListener, TrackerMapFragment.MapCenterListener {

    private var trackerImage: TrackerImage? = null

    private val handler = Handler()
    private var queue: BlockingQueue<Runnable> = ArrayBlockingQueue(1)
    private var executor: ThreadPoolExecutor = ThreadPoolExecutor(1, 1, 10000, TimeUnit.MILLISECONDS, queue)

    private var magneticDeclination = 0.0
    private var rotation: Int = 0

    override val layout: Int
        get() = R.layout.frag_data_tracker

    override fun onResume() {
        super.onResume()
        val activity = activity ?: return
        val windowManager = activity.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay ?: return
        rotation = display.rotation * Surface.ROTATION_90
        setSubtitle()
        (activity as MainActivity).apply {
            setViewConfigurationCallback({ openSettingsDialog() })
        }
        initialise()
        registerCompass()
    }

    override fun onPause() {
        unregisterCompass()
        val mapFragment = requireFragmentManager().findFragmentByTag(MAP_TAG)
        if (mapFragment != null) {
            requireFragmentManager()
                    .beginTransaction()
                    .remove(mapFragment)
                    .commit()
        }
        super.onPause()
    }

    override fun initialise() {
        val body = Prefs.sunTrackerBody(requireContext())
        val mode = Prefs.sunTrackerMode(requireContext())
        val mapType = Prefs.sunTrackerMapType(requireContext())

        if (mode == "radar" && Prefs.sunTrackerCompass(requireContext())) {
            magneticDeclination = getMagneticDeclination(getLocation().location, getDateCalendar())
        }

        unregisterCompass()

        if (Prefs.sunTrackerText(requireContext()) && body != null) {
            modify(trackerText, visibility = VISIBLE)
        } else {
            modify(trackerText, visibility = GONE)
        }

        trackerImage = TrackerImage(TrackerImage.TrackerStyle.forMode(mode, mapType), requireContext(), getLocation().location)
        trackerImage!!.setDate(getDateCalendar(), getTimeCalendar())
        trackerImageView.setTrackerImage(trackerImage!!)

        if (mode != "radar") {
            modify(trackerMapContainer, visibility = VISIBLE)
            modify(trackerRadarContainer, visibility = VISIBLE)

            val mapFragment = TrackerMapFragment(getLocation(), this)
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.trackerMapContainer, mapFragment, MAP_TAG)
                    .commit()
            trackerImageView.setDirection(0f)
        } else {
            modify(trackerMapContainer, visibility = GONE)
            modify(trackerRadarContainer, visibility = VISIBLE)
            trackerImageView.setCenter(Point(Int.MIN_VALUE, Int.MIN_VALUE))

            val mapFragment = requireFragmentManager().findFragmentByTag(MAP_TAG)
            if (mapFragment != null) {
                requireFragmentManager()
                        .beginTransaction()
                        .remove(mapFragment)
                        .commit()
            }

            if (Prefs.sunTrackerCompass(requireContext())) {
                registerCompass()
            } else {
                trackerImageView.setDirection(0f)
            }
        }

        startImageUpdate(false)
    }

    override fun updateData(view: View, timeOnly: Boolean) {
        startImageUpdate(timeOnly)
        setSubtitle()
    }

    private fun setSubtitle() {
        val body = Prefs.sunTrackerBody(requireContext())
        val bodyName = if (body == null) "All" else body.name.substring(0, 1) + body.name.substring(1).toLowerCase(Locale.getDefault())
        (activity as MainActivity).apply {
            setToolbarSubtitle("Tracker: $bodyName")
        }
    }

    private fun openSettingsDialog() {
        val settingsDialog = TrackerSettingsFragment.newInstance()
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(requireFragmentManager(), "trackerSettings")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isSafe || Prefs.sunTrackerMode(requireContext()) != "radar") {
            return
        }
        if (Prefs.sunTrackerCompass(requireContext())) {
            trackerImageView.setDirection(event.values[0] + rotation.toFloat() + magneticDeclination.toFloat())
        }
    }

    override fun setLocationPoint(point: Point) {
        trackerImageView.setCenter(point)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    @Synchronized
    private fun startImageUpdate(timeOnly: Boolean) {
        val dateCalendar = clone(getDateCalendar())
        val timeCalendar = clone(getTimeCalendar())
        val body = Prefs.sunTrackerBody(requireContext())

        trackerImage?.apply {
            if (timeOnly) {
                setTime(timeCalendar)
            } else {
                setDate(dateCalendar, timeCalendar)
            }
        }

        queue.clear()
        executor.submit {
            if (isSafe) {
                var eventsSet: MutableSet<Event>? = null
                val position = if (body != null && Prefs.sunTrackerText(requireContext())) BodyPositionCalculator.calcPosition(body, getLocation().location, timeCalendar) else null

                // Get the first two rise/set events that happen on this calendar day,
                // midnight to midnight.

                if (!timeOnly && body != null && Prefs.sunTrackerText(requireContext())) {
                    eventsSet = TreeSet()
                    val loopCalendar = clone(dateCalendar)
                    loopCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    for (i in 0..2) {
                        val bodyDay = BodyPositionCalculator.calcDay(body, getLocation().location, loopCalendar, false)
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
                                    val time = formatTimeStr(requireContext(), event1.time, allowSeconds = false, html = true)
                                    modify(trackerEvt1Name, text = event1.name)
                                    modify(trackerEvt1Time, html = time)
                                } else {
                                    modify(trackerEvt1Name, text = "")
                                    modify(trackerEvt1Time, text = "")
                                }

                                if (eventsSet.size > 1) {
                                    val event2 = eventsSet.toTypedArray()[1]
                                    val time = formatTimeStr(requireContext(), event2.time, allowSeconds = false, html = true)
                                    modify(trackerEvt2Name, text = event2.name)
                                    modify(trackerEvt2Time, html = time)
                                } else {
                                    modify(trackerEvt2Name, text = "")
                                    modify(trackerEvt2Time, text = "")
                                }
                            }

                            var elBd = BigDecimal(position.appElevation)
                            elBd = elBd.setScale(1, BigDecimal.ROUND_HALF_DOWN)
                            val el = elBd.toString() + "\u00b0"
                            val az = formatBearing(requireContext(), position.azimuth, getLocation().location, timeCalendar)

                            modify(trackerAz, text = az)
                            modify(trackerEl, text = el)
                            modify(trackerBody, text = body!!.name)
                            modify(trackerLight, text = getLight(body, position.appElevation).toUpperCase(Locale.getDefault()))
                        }
                        trackerImageView.invalidate()
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

    private fun registerCompass() {
        if (Prefs.sunTrackerMode(requireContext()) == "radar" && Prefs.sunTrackerCompass(requireContext())) {
            val sensorManager = requireContext().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            val orientationSensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION)
            if (orientationSensors.isNotEmpty()) {
                sensorManager.registerListener(this, orientationSensors[0], SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    private fun unregisterCompass() {
        val sensorManager = requireContext().getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    companion object {
        private const val MAP_TAG = "map"
    }

}
