package uk.co.sundroid.activity.data.fragments

import android.app.Activity
import android.app.Fragment
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.R.layout
import uk.co.sundroid.activity.data.fragments.dialogs.settings.TrackerSettingsFragment
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.astro.image.TrackerImageView
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.Position
import uk.co.sundroid.util.astro.image.TrackerImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.time.*

import java.math.BigDecimal
import java.util.Calendar
import java.util.TreeSet
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TrackerFragment : AbstractTimeFragment(), ConfigurableFragment, SensorEventListener, TrackerMapFragment.MapCenterListener {

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

    protected override val layout: Int
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
                ?: return
        val display = windowManager.defaultDisplay ?: return
        rotation = display.orientation * Surface.ROTATION_90
    }

    override fun onPause() {
        if (compassActive) {
            val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            sensorManager?.unregisterListener(this)
        }
        val mapFragment = fragmentManager.findFragmentByTag(MAP_TAG)
        if (mapFragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(mapFragment)
                    .commit()
        }
        super.onPause()
    }

    @Throws(Exception::class)
    override fun initialise(location: LocationDetails, newDateCalendar: Calendar, newTimeCalendar: Calendar, view: View) {
        this.trackerLcation = location
        this.trackerDateCalendar = newDateCalendar
        this.trackerTimeCalendar = newTimeCalendar

        val body = SharedPrefsHelper.getSunTrackerBody(applicationContext!!)
        val mode = SharedPrefsHelper.getSunTrackerMode(applicationContext!!)
        val mapType = SharedPrefsHelper.getSunTrackerMapType(applicationContext!!)

        if (mode == "radar" && SharedPrefsHelper.getSunTrackerCompass(applicationContext!!)) {
            magneticDeclination = getMagneticDeclination(location.location, trackerDateCalendar!!)
        }

        if (compassActive) {
            val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            sensorManager?.unregisterListener(this)
        }
        compassActive = false

        if (SharedPrefsHelper.getSunTrackerText(applicationContext!!) && body != null) {
            showInView(view, R.id.trackerText)
        } else {
            removeInView(view, R.id.trackerText)
        }

        trackerImage = TrackerImage(TrackerImage.TrackerStyle.forMode(mode, mapType), applicationContext!!, location.location)
        trackerImage!!.setDate(trackerDateCalendar!!, trackerTimeCalendar!!)
        trackerImageView = TrackerImageView(applicationContext!!)
        trackerImageView!!.setTrackerImage(trackerImage!!)
        trackerImageView!!.layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        if (mode != "radar") {

            showInView(view, R.id.trackerMapContainer)

            val mapFragment = TrackerMapFragment()
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.trackerMapContainer, mapFragment, MAP_TAG)
                    .commit()
            showInView(view, R.id.trackerRadarContainer)

            trackerImageView!!.setDirection(0f)

        } else {

            removeInView(view, R.id.trackerMapContainer)
            showInView(view, R.id.trackerRadarContainer)

            val mapFragment = fragmentManager.findFragmentByTag(MAP_TAG)
            if (mapFragment != null) {
                fragmentManager
                        .beginTransaction()
                        .remove(mapFragment)
                        .commit()
            }

            if (SharedPrefsHelper.getSunTrackerCompass(applicationContext!!)) {
                val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
                if (sensorManager != null) {
                    val orientationSensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION)
                    if (!orientationSensors.isEmpty()) {
                        compassActive = sensorManager.registerListener(this, orientationSensors[0], SensorManager.SENSOR_DELAY_GAME)
                    }
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

    @Throws(Exception::class)
    override fun update(location: LocationDetails, newDateCalendar: Calendar, newTimeCalendar: Calendar, view: View, timeOnly: Boolean) {
        this.trackerLcation = location
        this.trackerDateCalendar = newDateCalendar
        this.trackerTimeCalendar = newTimeCalendar
        startImageUpdate(timeOnly)
    }

    override fun openSettingsDialog() {
        val settingsDialog = TrackerSettingsFragment.newInstance()
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(fragmentManager, "trackerSettings")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ORIENTATION || applicationContext == null) {
            return
        }
        val mode = SharedPrefsHelper.getSunTrackerMode(applicationContext!!)
        if ("radar" != mode) {
            return
        }
        d(TAG, "Compass event: " + event.values[0] + " orientation: " + rotation + " declination: " + magneticDeclination)
        if (trackerImageView != null && SharedPrefsHelper.getSunTrackerCompass(applicationContext!!)) {
            trackerImageView!!.setDirection(event.values[0] + rotation.toFloat() + java.lang.Double.valueOf(magneticDeclination)!!.toFloat())
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
        val body = SharedPrefsHelper.getSunTrackerBody(applicationContext!!)

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

                var tempEventsSet: MutableSet<Event>? = null
                val position = if (body != null && SharedPrefsHelper.getSunTrackerText(applicationContext!!)) BodyPositionCalculator.calcPosition(body, trackerLcation!!.location, timeCalendar) else null

                // Get the first two rise/set events that happen on this calendar day,
                // midnight to midnight.

                if (!timeOnly && body != null && SharedPrefsHelper.getSunTrackerText(applicationContext!!)) {
                    tempEventsSet = TreeSet()
                    val loopCalendar = clone(dateCalendar)
                    loopCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    for (i in 0..2) {
                        val bodyDay = BodyPositionCalculator.calcDay(body, trackerLcation!!.location, loopCalendar, false)
                        if (bodyDay.rise != null && isSameDay(bodyDay.rise!!, dateCalendar) && tempEventsSet.size < 2) {
                            tempEventsSet.add(Event("RISE", bodyDay.rise!!, bodyDay.riseAzimuth))
                        }
                        if (bodyDay.set != null && isSameDay(bodyDay.set!!, dateCalendar) && tempEventsSet.size < 2) {
                            tempEventsSet.add(Event("SET", bodyDay.set!!, bodyDay.setAzimuth))
                        }
                        loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                val eventsSet = tempEventsSet


                trackerImage!!.generate()

                handler.post {
                    if (isSafe) {
                        if (position != null && SharedPrefsHelper.getSunTrackerText(applicationContext!!)) {

                            if (eventsSet != null) {
                                if (eventsSet.size > 0) {
                                    val event1 = eventsSet.toTypedArray()[0]
                                    val time = formatTime(applicationContext!!, event1.time!!, false)
                                    val az = formatBearing(applicationContext!!, event1.azimuth!!, trackerLcation!!.location, event1.time)
                                    textInView(view!!, R.id.trackerEvt1Name, event1.name)
                                    textInView(view!!, R.id.trackerEvt1Time, time.time + time.marker)
                                    textInView(view!!, R.id.trackerEvt1Az, az)
                                } else {
                                    textInView(view!!, R.id.trackerEvt1Name, "")
                                    textInView(view!!, R.id.trackerEvt1Time, "")
                                    textInView(view!!, R.id.trackerEvt1Az, "")
                                }

                                if (eventsSet.size > 1) {
                                    val event2 = eventsSet.toTypedArray()[1]
                                    val time = formatTime(applicationContext!!, event2.time, false)
                                    val az = formatBearing(applicationContext!!, event2.azimuth!!, trackerLcation!!.location, event2.time)
                                    textInView(view!!, R.id.trackerEvt2Name, event2.name)
                                    textInView(view!!, R.id.trackerEvt2Time, time.time + time.marker)
                                    textInView(view!!, R.id.trackerEvt2Az, az)
                                } else {
                                    textInView(view!!, R.id.trackerEvt2Name, "")
                                    textInView(view!!, R.id.trackerEvt2Time, "")
                                    textInView(view!!, R.id.trackerEvt2Az, "")
                                }
                            }

                            var elBd = BigDecimal(position.appElevation)
                            elBd = elBd.setScale(1, BigDecimal.ROUND_HALF_DOWN)
                            val el = elBd.toString() + "\u00b0"
                            val az = formatBearing(applicationContext!!, position.azimuth, trackerLcation!!.location, timeCalendar)

                            textInView(view!!, R.id.trackerAz, az)
                            textInView(view!!, R.id.trackerEl, el)
                            textInView(view!!, R.id.trackerBodyAndLight, body!!.name.substring(0, 1) + body.name.substring(1).toLowerCase() + ": " + getLight(body, position.appElevation))
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
            if (elevation >= 6) {
                "Day"
            } else if (elevation >= -0.833) {
                "Golden hour"
            } else if (elevation >= -6) {
                "Civil twilight"
            } else if (elevation >= -12) {
                "Nautical twilight"
            } else if (elevation >= -18) {
                "Astronomical twilight"
            } else {
                "Night"
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

        private val TAG = TrackerFragment::class.java.simpleName

        private val MAP_TAG = "map"
    }

}
