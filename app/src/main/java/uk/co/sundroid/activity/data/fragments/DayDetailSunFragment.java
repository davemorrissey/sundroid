package uk.co.sundroid.activity.data.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import uk.co.sundroid.R;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.YearData;
import uk.co.sundroid.util.astro.math.SunCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.geometry.GeometryUtils;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.Time;
import uk.co.sundroid.util.astro.YearData.Event;
import uk.co.sundroid.util.astro.YearData.EventType;
import uk.co.sundroid.util.time.TimeUtils;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import static uk.co.sundroid.util.time.TimeUtils.formatDurationHMS;
import static uk.co.sundroid.util.time.TimeUtils.formatTime;

public class DayDetailSunFragment extends AbstractDayFragment {

    private final Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return R.layout.frag_data_daydetail_sun;
	}

	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                Set<Event> yearEvents = YearData.getYearEvents(calendar.get(Calendar.YEAR), calendar.getTimeZone());
                Event yearEventToday = null;
                for (Event yearEvent : yearEvents) {
                    if (yearEvent.type.body == Body.SUN) {
                        if (TimeUtils.isSameDay(calendar, yearEvent.time)) {
                            yearEventToday = yearEvent;
                        }
                    }
                }
                final SunDay sunDay = SunCalculator.calcDay(location.getLocation(), calendar);
                final Event todayEvent = yearEventToday;

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }

                    if (todayEvent != null) {
                        view.findViewById(R.id.sunEvent).setOnClickListener(null);
                        showInView(view, R.id.sunEvent);
                        showInView(view, R.id.sunEventTitle, todayEvent.type.name);
                        if (todayEvent.type == EventType.NORTHERN_SOLSTICE && Math.abs(location.getLocation().getLatitude().getDoubleValue()) > 23.44) {
                            String localExtreme = location.getLocation().getLatitude().getDoubleValue() >= 0 ? "Longest" : "Shortest";
                            showInView(view, R.id.sunEventSubtitle, localExtreme + " day");
                        } else if (todayEvent.type == EventType.SOUTHERN_SOLSTICE && Math.abs(location.getLocation().getLatitude().getDoubleValue()) > 23.44) {
                            String localExtreme = location.getLocation().getLatitude().getDoubleValue() >= 0 ? "Shortest" : "Longest";
                            showInView(view, R.id.sunEventSubtitle, localExtreme + " day");
                        } else if (todayEvent.type == EventType.ANNULAR_SOLAR || todayEvent.type == EventType.HYBRID_SOLAR || todayEvent.type == EventType.PARTIAL_SOLAR || todayEvent.type == EventType.TOTAL_SOLAR) {
                            showInView(view, R.id.sunEventSubtitle, "Tap to check Wikipedia for visibility");
                            final String finalLink = todayEvent.link;
                            view.findViewById(R.id.sunEvent).setOnClickListener(view1 -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(finalLink));
                                startActivity(intent);
                            });
                        } else {
                            removeInView(view, R.id.sunEventSubtitle);
                        }
                    } else {
                        removeInView(view, R.id.sunEvent);
                    }

                    boolean noTransit = true;
                    boolean noUptime = true;

                    if (sunDay.getRiseSetType() != RiseSetType.SET && sunDay.getTransitAppElevation() > 0) {
                        Time noon = formatTime(getApplicationContext(), sunDay.getTransit(), false);
                        noTransit = false;
                        showInView(view, R.id.sunTransit);
                        showInView(view, R.id.sunTransitTime, noon.getTime() + noon.getMarker() + "  " + GeometryUtils.formatElevation(sunDay.getTransitAppElevation()));
                    } else {
                        removeInView(view, R.id.sunTransit);
                    }

                    if (sunDay.getRiseSetType() == RiseSetType.RISEN || sunDay.getRiseSetType() == RiseSetType.SET) {
                        showInView(view, R.id.sunSpecial, sunDay.getRiseSetType() == RiseSetType.RISEN ? "Risen all day" : "Set all day");
                        removeInView(view, R.id.sunEvtsRow, R.id.sunEvt1, R.id.sunEvt2, R.id.sunUptime);
                    } else {
                        removeInView(view, R.id.sunSpecial);
                        removeInView(view, R.id.sunEvt1, R.id.sunEvt2);
                        showInView(view, R.id.sunEvtsRow);
                        Set<SummaryEvent> events = new TreeSet<>();
                        if (sunDay.getRise() != null) {
                            events.add(new SummaryEvent("Rise", sunDay.getRise(), sunDay.getRiseAzimuth()));
                        }
                        if (sunDay.getSet() != null) {
                            events.add(new SummaryEvent("Set", sunDay.getSet(), sunDay.getSetAzimuth()));
                        }
                        int index = 1;
                        for (SummaryEvent event : events) {
                            int rowId = view("sunEvt" + index);
                            int timeId = view("sunEvt" + index + "Time");
                            int azId = view("sunEvt" + index + "Az");
                            int imgId = view("sunEvt" + index + "Img");

                            Time time = formatTime(getApplicationContext(), event.getTime(), false);
                            String az = GeometryUtils.formatBearing(getApplicationContext(), event.getAzimuth(), location.getLocation(), event.getTime());

                            textInView(view, timeId, time.getTime() + time.getMarker());
                            textInView(view, azId, az);
                            showInView(view, rowId);
                            imageInView(view, imgId, event.getName().equals("Rise") ? ThemePalette.getRiseArrow() : ThemePalette.getSetArrow());

                            index++;
                        }

                        if (sunDay.getUptimeHours() > 0 && sunDay.getUptimeHours() < 24) {
                            noUptime = false;
                            showInView(view, R.id.sunUptime);
                            showInView(view, R.id.sunUptimeTime, formatDurationHMS(getApplicationContext(), sunDay.getUptimeHours(), false));
                        } else {
                            removeInView(view, R.id.sunUptime);
                        }

                    }

                    if (noTransit && noUptime) {
                        removeInView(view, R.id.sunTransitUptime, R.id.sunTransitUptimeDivider);
                    } else {
                        showInView(view, R.id.sunTransitUptime, R.id.sunTransitUptimeDivider);
                    }

                    if (sunDay.getCivDawn() == null) {
                        textInView(view, R.id.sunCivDawnTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getCivDawn(), false);
                        textInView(view, R.id.sunCivDawnTime, time.getTime() + time.getMarker());
                    }
                    if (sunDay.getCivDusk() == null) {
                        textInView(view, R.id.sunCivDuskTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getCivDusk(), false);
                        textInView(view, R.id.sunCivDuskTime, time.getTime() + time.getMarker());
                    }
                    if (sunDay.getNtcDawn() == null) {
                        textInView(view, R.id.sunNtcDawnTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getNtcDawn(), false);
                        textInView(view, R.id.sunNtcDawnTime, time.getTime() + time.getMarker());
                    }
                    if (sunDay.getNtcDusk() == null) {
                        textInView(view, R.id.sunNtcDuskTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getNtcDusk(), false);
                        textInView(view, R.id.sunNtcDuskTime, time.getTime() + time.getMarker());
                    }
                    if (sunDay.getAstDawn() == null) {
                        textInView(view, R.id.sunAstDawnTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getAstDawn(), false);
                        textInView(view, R.id.sunAstDawnTime, time.getTime() + time.getMarker());
                    }
                    if (sunDay.getAstDusk() == null) {
                        textInView(view, R.id.sunAstDuskTime, "-");
                    } else {
                        Time time = formatTime(getApplicationContext(), sunDay.getAstDusk(), false);
                        textInView(view, R.id.sunAstDuskTime, time.getTime() + time.getMarker());
                    }

                    showInView(view, R.id.sunDataBox);
                });
            }
        };
        thread.start();
    }
	
	
}
