package uk.co.sundroid.activity.data.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.activity.data.fragments.dialogs.settings.TrackerSettingsFragment;
import uk.co.sundroid.domain.MapType;
import uk.co.sundroid.util.astro.image.TrackerImageView;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.Position;
import uk.co.sundroid.util.astro.image.TrackerImage;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.geometry.GeometryUtils;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.log.LogWrapper;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.time.Time;
import uk.co.sundroid.util.time.TimeUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TrackerFragment extends AbstractTimeFragment implements ConfigurableFragment, SensorEventListener, TrackerMapFragment.MapCenterListener {

    private static final String TAG = TrackerFragment.class.getSimpleName();

    private static final String MAP_TAG = "map";

    private LocationDetails location;
    private Calendar dateCalendar;
    private Calendar timeCalendar;
    private View view;

    private boolean compassActive;

    private TrackerImage trackerImage;
    private TrackerImageView trackerImageView;

    private Handler handler = new Handler();
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> queue;

    private double magneticDeclination = 0;
    private int rotation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = new ArrayBlockingQueue<>(1);
        executor = new ThreadPoolExecutor(1, 1, 10000, TimeUnit.MILLISECONDS, queue);
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        WindowManager windowManager = (WindowManager)activity.getSystemService(Activity.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        Display display = windowManager.getDefaultDisplay();
        if (display == null) {
            return;
        }
        rotation = display.getOrientation() * Surface.ROTATION_90;
    }

    @Override
    public void onPause() {
        if (compassActive) {
            SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Activity.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        }
        Fragment mapFragment = getFragmentManager().findFragmentByTag(MAP_TAG);
        if (mapFragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(mapFragment)
                    .commit();
        }
        super.onPause();
    }

    @Override
    protected int getLayout() {
        return layout.frag_data_tracker;
    }

    @Override
    protected void initialise(LocationDetails location, Calendar newDateCalendar, Calendar newTimeCalendar, View view) throws Exception {
        this.location = location;
        this.dateCalendar = newDateCalendar;
        this.timeCalendar = newTimeCalendar;
        this.view = view;

        Body body = SharedPrefsHelper.INSTANCE.getSunTrackerBody(getApplicationContext());
        String mode = SharedPrefsHelper.INSTANCE.getSunTrackerMode(getApplicationContext());
        MapType mapType = SharedPrefsHelper.INSTANCE.getSunTrackerMapType(getApplicationContext());

        if (mode.equals("radar") && SharedPrefsHelper.INSTANCE.getSunTrackerCompass(getApplicationContext())) {
            magneticDeclination = GeometryUtils.getMagneticDeclination(location.getLocation(), dateCalendar);
        }

        if (compassActive) {
            SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Activity.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        }
        compassActive = false;

        if (SharedPrefsHelper.INSTANCE.getSunTrackerText(getApplicationContext()) && body != null) {
            showInView(view, id.trackerText);
        } else {
            removeInView(view, id.trackerText);
        }

        trackerImage = new TrackerImage(TrackerImage.TrackerStyle.Companion.forMode(mode, mapType), getApplicationContext(), location.getLocation());
        trackerImage.setDate(dateCalendar, timeCalendar);
        trackerImageView = new TrackerImageView(getApplicationContext());
        trackerImageView.setTrackerImage(trackerImage);
        trackerImageView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (!mode.equals("radar")) {

            showInView(view, id.trackerMapContainer);

            TrackerMapFragment mapFragment = new TrackerMapFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(id.trackerMapContainer, mapFragment, MAP_TAG)
                    .commit();
            showInView(view, id.trackerRadarContainer);

            trackerImageView.setDirection(0f);

        } else {

            removeInView(view, id.trackerMapContainer);
            showInView(view, id.trackerRadarContainer);

            Fragment mapFragment = getFragmentManager().findFragmentByTag(MAP_TAG);
            if (mapFragment != null) {
                getFragmentManager()
                        .beginTransaction()
                        .remove(mapFragment)
                        .commit();
            }

            if (SharedPrefsHelper.INSTANCE.getSunTrackerCompass(getApplicationContext())) {
                SensorManager sensorManager = (SensorManager)getActivity().getSystemService(Activity.SENSOR_SERVICE);
                if (sensorManager != null) {
                    List<Sensor> orientationSensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
                    if (!orientationSensors.isEmpty()) {
                        compassActive = sensorManager.registerListener(this, orientationSensors.get(0), SensorManager.SENSOR_DELAY_GAME);
                    }
                }
            } else {
                trackerImageView.setDirection(0f);
            }

        }

        // TODO Try embedding this view in the layout
        ((ViewGroup)view.findViewById(id.trackerRadarContainer)).removeAllViews();
        ((ViewGroup)view.findViewById(id.trackerRadarContainer)).addView(trackerImageView);

        startImageUpdate(false);

    }

    @Override
    protected void update(LocationDetails location, Calendar newDateCalendar, Calendar newTimeCalendar, View view, boolean timeOnly) throws Exception {
        this.location = location;
        this.dateCalendar = newDateCalendar;
        this.timeCalendar = newTimeCalendar;
        this.view = view;
        startImageUpdate(timeOnly);
    }

    @Override
    public void openSettingsDialog() {
        TrackerSettingsFragment settingsDialog = TrackerSettingsFragment.Companion.newInstance();
        settingsDialog.setTargetFragment(this, 0);
        settingsDialog.show(getFragmentManager(), "trackerSettings");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ORIENTATION || getApplicationContext() == null) {
            return;
        }
        String mode = SharedPrefsHelper.INSTANCE.getSunTrackerMode(getApplicationContext());
        if (!"radar".equals(mode)) {
            return;
        }
        LogWrapper.d(TAG, "Compass event: " + event.values[0] + " orientation: " + rotation + " declination: " + magneticDeclination);
        if (trackerImageView != null && SharedPrefsHelper.INSTANCE.getSunTrackerCompass(getApplicationContext())) {
            trackerImageView.setDirection(event.values[0] + rotation + Double.valueOf(magneticDeclination).floatValue());
        }
    }

    @Override
    public void setLocationPoint(Point point) {
        if (trackerImageView != null) {
            trackerImageView.setCenter(point);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private synchronized void startImageUpdate(final boolean timeOnly) {
        if (view == null || dateCalendar == null || timeCalendar == null) {
            return;
        }
        final Calendar dateCalendar = TimeUtils.clone(this.dateCalendar);
        final Calendar timeCalendar = TimeUtils.clone(this.timeCalendar);
        final Body body = SharedPrefsHelper.INSTANCE.getSunTrackerBody(getApplicationContext());

        if (trackerImage != null) {
            if (timeOnly) {
                trackerImage.setTime(timeCalendar);
            } else {
                trackerImage.setDate(dateCalendar, timeCalendar);
            }
        }

        queue.clear();
        executor.submit(() -> {
            if (!isSafe()) {
                return;
            }

            Set<Event> tempEventsSet = null;
            final Position position = body != null && SharedPrefsHelper.INSTANCE.getSunTrackerText(getApplicationContext()) ? BodyPositionCalculator.INSTANCE.calcPosition(body, location.getLocation(), timeCalendar) : null;

            // Get the first two rise/set events that happen on this calendar day,
            // midnight to midnight.

            if (!timeOnly && body != null && SharedPrefsHelper.INSTANCE.getSunTrackerText(getApplicationContext())) {
                tempEventsSet = new TreeSet<>();
                Calendar loopCalendar = TimeUtils.clone(dateCalendar);
                loopCalendar.add(Calendar.DAY_OF_MONTH, -1);
                for (int i = 0; i < 3; i++) {
                    BodyDay bodyDay = BodyPositionCalculator.INSTANCE.calcDay(body, location.getLocation(), loopCalendar, false);
                    if (bodyDay.getRise() != null && TimeUtils.isSameDay(bodyDay.getRise(), dateCalendar) && tempEventsSet.size() < 2) {
                        tempEventsSet.add(new Event("RISE", bodyDay.getRise(), bodyDay.getRiseAzimuth()));
                    }
                    if (bodyDay.getSet() != null && TimeUtils.isSameDay(bodyDay.getSet(), dateCalendar) && tempEventsSet.size() < 2) {
                        tempEventsSet.add(new Event("SET", bodyDay.getSet(), bodyDay.getSetAzimuth()));
                    }
                    loopCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            final Set<Event> eventsSet = tempEventsSet;


            trackerImage.generate();

            handler.post(() -> {
                if (!isSafe()) {
                    return;
                }

                if (position != null && SharedPrefsHelper.INSTANCE.getSunTrackerText(getApplicationContext())) {

                    if (eventsSet != null) {
                        if (eventsSet.size() > 0) {
                            Event event1 = eventsSet.toArray(new Event[eventsSet.size()])[0];
                            Time time = TimeUtils.formatTime(getApplicationContext(), event1.time, false);
                            String az = GeometryUtils.formatBearing(getApplicationContext(), event1.azimuth, location.getLocation(), event1.time);
                            textInView(view, id.trackerEvt1Name, event1.name);
                            textInView(view, id.trackerEvt1Time, time.getTime() + time.getMarker());
                            textInView(view, id.trackerEvt1Az, az);
                        } else {
                            textInView(view, id.trackerEvt1Name, "");
                            textInView(view, id.trackerEvt1Time, "");
                            textInView(view, id.trackerEvt1Az, "");
                        }

                        if (eventsSet.size() > 1) {
                            Event event2 = eventsSet.toArray(new Event[eventsSet.size()])[1];
                            Time time = TimeUtils.formatTime(getApplicationContext(), event2.time, false);
                            String az = GeometryUtils.formatBearing(getApplicationContext(), event2.azimuth, location.getLocation(), event2.time);
                            textInView(view, id.trackerEvt2Name, event2.name);
                            textInView(view, id.trackerEvt2Time, time.getTime() + time.getMarker());
                            textInView(view, id.trackerEvt2Az, az);
                        } else {
                            textInView(view, id.trackerEvt2Name, "");
                            textInView(view, id.trackerEvt2Time, "");
                            textInView(view, id.trackerEvt2Az, "");
                        }
                    }

                    BigDecimal elBd = new BigDecimal(position.getAppElevation());
                    elBd = elBd.setScale(1, BigDecimal.ROUND_HALF_DOWN);
                    String el = elBd.toString() + "\u00b0";
                    String az = GeometryUtils.formatBearing(getApplicationContext(), position.getAzimuth(), location.getLocation(), timeCalendar);

                    textInView(view, id.trackerAz, az);
                    textInView(view, id.trackerEl, el);
                    textInView(view, id.trackerBodyAndLight, body.name().substring(0, 1) + body.name().substring(1).toLowerCase() + ": " + getLight(body, position.getAppElevation()));
                }

                trackerImageView.invalidate();
            });

        });

    }

    private static class Event implements Comparable<Event> {
        private String name;
        private Calendar time;
        private Double azimuth;
        public Event(String name, Calendar time, Double azimuth) {
            this.name = name;
            this.time = time;
            this.azimuth = azimuth;
        }
        public int compareTo(@NonNull Event other) {
            int result = time.compareTo(other.time);
            if (result == 0) {
                return 1;
            }
            return result;
        }
    }

    private String getLight(Body body, double elevation) {
        if (body == Body.SUN) {
            if (elevation >= 6) {
                return "Day";
            } else if (elevation >= -0.833) {
                return "Golden hour";
            } else if (elevation >= -6) {
                return "Civil twilight";
            } else if (elevation >= -12) {
                return "Nautical twilight";
            } else if (elevation >= -18) {
                return "Astronomical twilight";
            } else {
                return "Night";
            }
        } else if (body == Body.MOON) {
            if (elevation >= -0.833) {
                return "Risen";
            } else {
                return "Set";
            }
        } else {
            if (elevation >= 0.0) {
                return "Risen";
            } else {
                return "Set";
            }

        }
    }

}
