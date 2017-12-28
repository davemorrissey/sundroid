package uk.co.sundroid.activity.data.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import uk.co.sundroid.R;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.MoonDay;
import uk.co.sundroid.util.astro.MoonPhase;
import uk.co.sundroid.util.astro.MoonPhaseEvent;
import uk.co.sundroid.util.astro.YearData;
import uk.co.sundroid.util.astro.image.MoonPhaseImage;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.*;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeHelper;
import uk.co.sundroid.util.time.TimeHelper.Time;
import uk.co.sundroid.util.astro.YearData.Event;
import uk.co.sundroid.util.geometry.BearingHelper;
import uk.co.sundroid.util.time.TimeUtils;

import java.util.*;

public class DayDetailMoonFragment extends AbstractDayFragment {
	
	private static final String TAG = DayDetailMoonFragment.class.getSimpleName();
	
	private Handler handler = new Handler();
	
	@Override
	protected int getLayout() {
		return R.layout.frag_data_daydetail_moon;
	}

	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                final MoonDay moonDay = (MoonDay)BodyPositionCalculator.calcDay(Body.MOON, location.getLocation(), calendar, true);
                final List<MoonPhaseEvent> moonPhaseEvents = new ArrayList<>();
                for (MoonPhaseEvent event : MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR), calendar.getTimeZone())) {
                    if (event.getTime().get(Calendar.DAY_OF_YEAR) >= calendar.get(Calendar.DAY_OF_YEAR)) {
                        moonPhaseEvents.add(event);
                    }
                }
                if (moonPhaseEvents.size() < 4) {
                    moonPhaseEvents.addAll(MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR) + 1, calendar.getTimeZone()));
                }

                Set<Event> yearEvents = YearData.getYearEvents(calendar.get(Calendar.YEAR), calendar.getTimeZone());
                Event yearEventToday = null;
                for (Event yearEvent : yearEvents) {
                    if (yearEvent.type.body == Body.MOON) {
                        if (TimeUtils.isSameDay(calendar, yearEvent.time)) {
                            yearEventToday = yearEvent;
                        }
                    }
                }
                final Event todayEvent = yearEventToday;

                // Asynchronously generate moon graphic to speed up response.
                Thread moonThread = new Thread() {
                    @Override
                    public void run() {
                        if (!isSafe()) {
                            return;
                        }

                        try {
                            long start = System.currentTimeMillis();
                            double phase = moonDay.getPhaseDouble();
                            final Bitmap moonBitmap = MoonPhaseImage.makeImage(getResources(), R.drawable.moon, phase, location.getLocation().getLatitude().getDoubleValue() < 0, MoonPhaseImage.SIZE_LARGE);
                            long end = System.currentTimeMillis();
                            handler.post(() -> {
                                        if (!isSafe()) {
                                            return;
                                        }
                                        ImageView moon = view.findViewById(R.id.moonImage);
                                        moon.setImageBitmap(moonBitmap);
                                    });
                            LogWrapper.d(TAG, "Moon render time " + (end - start));
                        } catch (Exception e) {
                            LogWrapper.e(TAG, "Error generating moon", e);
                        }
                    }
                };
                moonThread.start();

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }

                    if (todayEvent != null) {
                        view.findViewById(R.id.moonEvent).setOnClickListener(null);
                        showInView(view, R.id.moonEvent);
                        showInView(view, R.id.moonEventTitle, todayEvent.type.name);
                        showInView(view, R.id.moonEventSubtitle, "Tap to check Wikipedia for visibility");
                        final String finalLink = todayEvent.link;
                        view.findViewById(R.id.moonEvent).setOnClickListener(view1 -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(finalLink));
                            startActivity(intent);
                        });
                    } else {
                        removeInView(view, R.id.moonEvent);
                    }

                    for (int i = 1; i < 5; i++) {
                        MoonPhaseEvent phaseEvent = moonPhaseEvents.get(i - 1);
                        int phaseImgView = view("moonPhase" + i + "Img");
                        int phaseLabelView = view("moonPhase" + i + "Label");
                        int phaseImg = ThemePalette.getPhaseFull();
                        if (phaseEvent.getPhase() == MoonPhase.NEW) {
                            phaseImg = ThemePalette.getPhaseNew();
                        } else if (phaseEvent.getPhase() == MoonPhase.FIRST_QUARTER) {
                            phaseImg = getLocation().getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseRight() : ThemePalette.getPhaseLeft();
                        } else if (phaseEvent.getPhase() == MoonPhase.LAST_QUARTER) {
                            phaseImg = getLocation().getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseLeft() : ThemePalette.getPhaseRight();
                        }
                        ((ImageView)view.findViewById(phaseImgView)).setImageResource(phaseImg);
                        ((TextView)view.findViewById(phaseLabelView)).setText(TimeUtils.shortDateAndMonth(phaseEvent.getTime()));
                    }

                    boolean noTransit = true;
                    boolean noUptime = true;

                    if (moonDay.getRiseSetType() != RiseSetType.SET && moonDay.getTransitAppElevation() > 0) {
                        Time noon = TimeHelper.formatTime(getApplicationContext(), moonDay.getTransit(), false);
                        noTransit = false;
                        showInView(view, R.id.moonTransit);
                        showInView(view, R.id.moonTransitTime, noon.time + noon.marker + "  " + BearingHelper.formatElevation(moonDay.getTransitAppElevation()));
                    } else {
                        removeInView(view, R.id.moonTransit);
                    }

                    if (moonDay.getRiseSetType() == RiseSetType.RISEN || moonDay.getRiseSetType() == RiseSetType.SET) {
                        showInView(view, R.id.moonSpecial, moonDay.getRiseSetType() == RiseSetType.RISEN ? "Risen all day" : "Set all day");
                        removeInView(view, R.id.moonEvtsRow, R.id.moonEvt1, R.id.moonEvt2, R.id.moonUptime);
                    } else {
                        removeInView(view, R.id.moonSpecial);
                        removeInView(view, R.id.moonEvt1, R.id.moonEvt2);
                        showInView(view, R.id.moonEvtsRow);
                        Set<SummaryEvent> events = new TreeSet<>();
                        if (moonDay.getRise() != null) {
                            events.add(new SummaryEvent("Rise", moonDay.getRise(), moonDay.getRiseAzimuth()));
                        }
                        if (moonDay.getSet() != null) {
                            events.add(new SummaryEvent("Set", moonDay.getSet(), moonDay.getSetAzimuth()));
                        }
                        int index = 1;
                        for (SummaryEvent event : events) {
                            int rowId = view("moonEvt" + index);
                            int timeId = view("moonEvt" + index + "Time");
                            int azId = view("moonEvt" + index + "Az");
                            int imgId = view("moonEvt" + index + "Img");

                            Time time = TimeHelper.formatTime(getApplicationContext(), event.getTime(), false);
                            String az = BearingHelper.formatBearing(getApplicationContext(), event.getAzimuth(), location.getLocation(), event.getTime());

                            textInView(view, timeId, time.time + time.marker);
                            textInView(view, azId, az);
                            showInView(view, rowId);
                            imageInView(view, imgId, event.getName().equals("Rise") ? ThemePalette.getRiseArrow() : ThemePalette.getSetArrow());

                            index++;
                        }

                        if (moonDay.getUptimeHours() > 0 && moonDay.getUptimeHours() < 24) {
                            noUptime = false;
                            showInView(view, R.id.moonUptime);
                            showInView(view, R.id.moonUptimeTime, TimeHelper.formatDurationHMS(getApplicationContext(), moonDay.getUptimeHours(), false));
                        } else {
                            removeInView(view, R.id.moonUptime);
                        }

                    }

                    if (noTransit && noUptime) {
                        removeInView(view, R.id.moonTransitUptime, R.id.moonTransitUptimeDivider);
                    } else {
                        showInView(view, R.id.moonTransitUptime, R.id.moonTransitUptimeDivider);
                    }

                    if (moonDay.getPhaseEvent() == null) {
                        showInView(view, R.id.moonPhase, moonDay.getPhase().getDisplayName());
                    } else {
                        Time time = TimeHelper.formatTime(getApplicationContext(), moonDay.getPhaseEvent().getTime(), false);
                        showInView(view, R.id.moonPhase, moonDay.getPhase().getDisplayName() + " at " + time.time + time.marker);
                    }
                    showInView(view, R.id.moonIllumination, Integer.toString(moonDay.getIllumination()) + "%");
                    showInView(view, R.id.moonDataBox);
                });
            }
        };
        thread.start();

    }

}