package uk.co.sundroid.activity.data.fragments;

import android.os.Handler;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.data.fragments.dialogs.settings.DayEventsPickerFragment;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.MoonDay;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.astro.math.SunCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.geometry.GeometryUtils;
import uk.co.sundroid.util.time.TimeUtils;
import uk.co.sundroid.util.time.Time;

import java.util.*;

public class DayDetailEventsFragment extends AbstractDayFragment implements ConfigurableFragment {

    private Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return R.layout.frag_data_daydetail_events;
	}

    @Override
    public void openSettingsDialog() {
        boolean[] currentEvents = new boolean[7];
        currentEvents[0] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimeSun", true);
        currentEvents[1] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimeMoon", true);
        currentEvents[2] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimePlanets", false);

        DayEventsPickerFragment settingsDialog = DayEventsPickerFragment.Companion.newInstance(currentEvents);
        settingsDialog.setTargetFragment(this, 0);
        settingsDialog.show(getFragmentManager(), "dayEventsSettings");
    }

    protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                SunDay sunDay = null;
                MoonDay moonDay = null;
                Map<Body, BodyDay> planetDays = null;

                if (SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimeSun", true)) {
                    sunDay = SunCalculator.INSTANCE.calcDay(location.getLocation(), calendar);
                }
                if (SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimeMoon", true)) {
                    moonDay = (MoonDay)BodyPositionCalculator.INSTANCE.calcDay(Body.MOON, location.getLocation(), calendar, false);
                }
                if (SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "evtByTimePlanets", false)) {
                    planetDays = new LinkedHashMap<>();
                    for (Body body : Body.values()) {
                        if (body != Body.SUN && body != Body.MOON) {
                            planetDays.put(body, BodyPositionCalculator.INSTANCE.calcDay(body, location.getLocation(), calendar, true));
                        }
                    }
                }

                Set<SummaryEvent> eventsSet = new TreeSet<>();

                if (sunDay != null) {
                    if (sunDay.getRise() != null) {
                        eventsSet.add(new SummaryEvent("Sunrise", sunDay.getRise(), sunDay.getRiseAzimuth()));
                    }
                    if (sunDay.getSet() != null) {
                        eventsSet.add(new SummaryEvent("Sunset", sunDay.getSet(), sunDay.getSetAzimuth()));
                    }
                    if (sunDay.getAstDawn() != null) {
                        eventsSet.add(new SummaryEvent("Astro. dawn", sunDay.getAstDawn(), null));
                    }
                    if (sunDay.getAstDusk() != null) {
                        eventsSet.add(new SummaryEvent("Astro. dusk", sunDay.getAstDusk(), null));
                    }
                    if (sunDay.getNtcDawn() != null) {
                        eventsSet.add(new SummaryEvent("Nautical dawn", sunDay.getNtcDawn(), null));
                    }
                    if (sunDay.getNtcDusk() != null) {
                        eventsSet.add(new SummaryEvent("Nautical dusk", sunDay.getNtcDusk(), null));
                    }
                    if (sunDay.getCivDawn() != null) {
                        eventsSet.add(new SummaryEvent("Civil dawn", sunDay.getCivDawn(), null));
                    }
                    if (sunDay.getCivDusk() != null) {
                        eventsSet.add(new SummaryEvent("Civil dusk", sunDay.getCivDusk(), null));
                    }
                    if (sunDay.getTransit() != null && sunDay.getRiseSetType() != RiseSetType.SET) {
                        eventsSet.add(new SummaryEvent("Solar noon", sunDay.getTransit(), null));
                    }
                    if (sunDay.getGhEnd() != null) {
                        eventsSet.add(new SummaryEvent("Golden hr end", sunDay.getGhEnd(), null));
                    }
                    if (sunDay.getGhStart() != null) {
                        eventsSet.add(new SummaryEvent("Golden hr start", sunDay.getGhStart(), null));
                    }
                }
                if (moonDay != null) {
                    if (moonDay.getRise() != null) {
                        eventsSet.add(new SummaryEvent("Moonrise", moonDay.getRise(), moonDay.getRiseAzimuth()));
                    }
                    if (moonDay.getSet() != null) {
                        eventsSet.add(new SummaryEvent("Moonset", moonDay.getSet(), moonDay.getSetAzimuth()));
                    }
                }
                if (planetDays != null) {
                    for (Map.Entry<Body, BodyDay> planetEntry : planetDays.entrySet()) {
                        Body planet = planetEntry.getKey();
                        BodyDay planetDay = planetEntry.getValue();
                        if (planetDay.getRise() != null) {
                            eventsSet.add(new SummaryEvent(planet.getDisplayName() + " rise", planetDay.getRise(), planetDay.getRiseAzimuth()));
                        }
                        if (planetDay.getSet() != null) {
                            eventsSet.add(new SummaryEvent(planet.getDisplayName() + " set", planetDay.getSet(), planetDay.getSetAzimuth()));
                        }
                    }
                }

                final List<SummaryEvent> eventsList = new ArrayList<>(eventsSet);

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }

                    TableLayout eventsTable = view.findViewById(id.eventsTable);
                    eventsTable.removeAllViews();

                    final View eventsHeader = getActivity().getLayoutInflater().inflate(R.layout.frag_data_daydetail_events_header, null);
                    eventsTable.addView(eventsHeader);

                    if (eventsList.isEmpty()) {
                        showInView(view, id.eventsNone);
                        removeInView(view, id.eventsTable);
                    } else {
                        showInView(view, id.eventsTable);
                        removeInView(view, id.eventsNone);
                        for (SummaryEvent event : eventsList) {
                            final View eventRow = getActivity().getLayoutInflater().inflate(R.layout.frag_data_daydetail_events_row, null);
                            ((TextView)eventRow.findViewById(id.eventName)).setText(event.getName().toUpperCase());
                            Time time = TimeUtils.formatTime(getApplicationContext(), event.getTime(), true);
                            ((TextView)eventRow.findViewById(id.eventTime)).setText(time.getTime() + time.getMarker());
                            if (event.getAzimuth() != null) {
                                String azimuth = GeometryUtils.formatBearing(getApplicationContext(), event.getAzimuth(), location.getLocation(), calendar);
                                ((TextView)eventRow.findViewById(id.eventAz)).setText(azimuth);
                            } else {
                                ((TextView)eventRow.findViewById(id.eventAz)).setText(" ");
                            }
                            eventsTable.addView(eventRow);
                        }
                    }
                    showInView(view,  id.eventsDataBox);
                });
            }
        };
        thread.start();

    	
    }
	
	
}
