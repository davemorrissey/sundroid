package uk.co.sundroid.activity.data.fragments;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import uk.co.sundroid.R;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeHelper;
import uk.co.sundroid.util.time.TimeHelper.Time;
import uk.co.sundroid.util.geometry.BearingHelper;

import java.util.*;

public class DayDetailPlanetsFragment extends AbstractDayFragment {

    private final Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return R.layout.frag_data_daydetail_planets;
	}

	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                final Map<Body, BodyDay> planetDays = new LinkedHashMap<>();
                for (Body body : Body.values()) {
                    if (body != Body.SUN && body != Body.MOON) {
                        planetDays.put(body, BodyPositionCalculator.calcDay(body, location.getLocation(), calendar, true));
                    }
                }

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }
                    ViewGroup planetsDataBox = view.findViewById(R.id.planetsDataBox);
                    for (Map.Entry<Body, BodyDay> planetEntry : planetDays.entrySet()) {
                        Body planet = planetEntry.getKey();
                        BodyDay planetDay = planetEntry.getValue();
                        View planetRow = getActivity().getLayoutInflater().inflate(R.layout.frag_data_daydetail_planets_planet, planetsDataBox);

                        textInView(planetRow, R.id.planetName, planet.name());

                        boolean noTransit = false;
                        boolean noUptime = false;

                        if (planetDay.getRiseSetType() != RiseSetType.SET && planetDay.getTransitAppElevation() > 0) {
                            Time noon = TimeHelper.formatTime(getApplicationContext(), planetDay.getTransit(), false);
                            showInView(planetRow, R.id.planetTransit);
                            showInView(planetRow, R.id.planetTransitTime, noon.time + noon.marker + "  " + BearingHelper.formatElevation(planetDay.getTransitAppElevation()));
                        } else {
                            removeInView(planetRow, R.id.planetTransit);
                            noTransit = true;
                        }

                        if (planetDay.getRiseSetType() == RiseSetType.RISEN || planetDay.getRiseSetType() == RiseSetType.SET) {
                            showInView(planetRow, R.id.planetSpecial, planetDay.getRiseSetType() == RiseSetType.RISEN ? "Risen all day" : "Set all day");
                            removeInView(planetRow, R.id.planetEvtsRow, R.id.planetEvt1, R.id.planetEvt2, R.id.planetUptime);
                            noUptime = true;
                        } else {
                            removeInView(planetRow, R.id.planetSpecial);
                            removeInView(planetRow, R.id.planetEvt1, R.id.planetEvt2);
                            showInView(planetRow, R.id.planetEvtsRow);
                            Set<SummaryEvent> events = new TreeSet<>();
                            if (planetDay.getRise() != null) {
                                events.add(new SummaryEvent("Rise", planetDay.getRise(), planetDay.getRiseAzimuth()));
                            }
                            if (planetDay.getSet() != null) {
                                events.add(new SummaryEvent("Set", planetDay.getSet(), planetDay.getSetAzimuth()));
                            }
                            int index = 1;
                            for (SummaryEvent event : events) {
                                int rowId = view("planetEvt" + index);
                                int timeId = view("planetEvt" + index + "Time");
                                int azId = view("planetEvt" + index + "Az");
                                int imgId = view("planetEvt" + index + "Img");

                                Time time = TimeHelper.formatTime(getApplicationContext(), event.getTime(), false);
                                String az = BearingHelper.formatBearing(getApplicationContext(), event.getAzimuth(), location.getLocation(), event.getTime());

                                textInView(planetRow, timeId, time.time + time.marker);
                                textInView(planetRow, azId, az);
                                showInView(planetRow, rowId);

                                if (event.getName().equals("Rise")) {
                                    ((ImageView)planetRow.findViewById(imgId)).setImageResource(ThemePalette.getRiseArrow());
                                } else {
                                    ((ImageView)planetRow.findViewById(imgId)).setImageResource(ThemePalette.getSetArrow());
                                }

                                index++;
                            }

                            if (planetDay.getUptimeHours() > 0 && planetDay.getUptimeHours() < 24) {
                                showInView(planetRow, R.id.planetUptime);
                                showInView(planetRow, R.id.planetUptimeTime, TimeHelper.formatDurationHMS(getApplicationContext(), planetDay.getUptimeHours(), false));
                            } else {
                                removeInView(planetRow, R.id.planetUptime);
                            }

                        }

                        if (noTransit && noUptime) {
                            removeInView(planetRow, R.id.planetTransitUptime);
                        } else {
                            showInView(planetRow, R.id.planetTransitUptime);
                        }

                    }
                    showInView(view, R.id.planetsDataBox);
                });
            }
        };
        thread.start();
    }

}