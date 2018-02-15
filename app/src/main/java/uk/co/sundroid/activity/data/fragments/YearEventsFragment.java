package uk.co.sundroid.activity.data.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.activity.data.fragments.dialogs.settings.YearEventsPickerFragment;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.astro.MoonPhaseEvent;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator;
import uk.co.sundroid.util.astro.math.SunCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeUtils;
import uk.co.sundroid.util.time.Time;
import uk.co.sundroid.util.astro.YearData;
import uk.co.sundroid.util.astro.YearData.Event;
import uk.co.sundroid.util.astro.YearData.EventType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class YearEventsFragment extends AbstractYearFragment implements ConfigurableFragment {

    private final Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return layout.frag_data_yearevents;
	}

    @Override
    public void openSettingsDialog() {
//        boolean[] currentEvents = new boolean[8];
//        currentEvents[0] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearNewMoon", true);
//        currentEvents[1] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearFullMoon", true);
//        currentEvents[2] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearQuarterMoon", true);
//        currentEvents[3] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearSolstice", true);
//        currentEvents[4] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearEquinox", true);
//        currentEvents[5] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearLunarEclipse", true);
//        currentEvents[6] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearSolarEclipse", true);
//        currentEvents[7] = SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearEarthApsis", true);

        YearEventsPickerFragment settingsDialog = YearEventsPickerFragment.newInstance();
        settingsDialog.setTargetFragment(this, 0);
        settingsDialog.show(getFragmentManager(), "yearEventsSettings");
    }

    @Override
	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                final Calendar todayCalendar = Calendar.getInstance(calendar.getTimeZone());
                final Set<Event> eventsSet = YearData.INSTANCE.getYearEvents(calendar.get(Calendar.YEAR), location.getTimeZone().getZone());
                List<MoonPhaseEvent> moonPhases = MoonPhaseCalculator.INSTANCE.getYearEvents(calendar.get(Calendar.YEAR), location.getTimeZone().getZone());
                for (MoonPhaseEvent moonPhase : moonPhases) {
                    eventsSet.add(new Event(EventType.PHASE, moonPhase, moonPhase.getTime(), null));
                }

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }

                    List<Event> eventsList = new ArrayList<>(eventsSet);
                    ViewGroup eventsBox = view.findViewById(id.yearEventsBox);
                    eventsBox.removeAllViews();

                    boolean first = true;
                    for (Event event : eventsList) {
                        Time eventTime = TimeUtils.formatTime(getApplicationContext(), event.getTime(), false);
                        String title = "";
                        String time = eventTime.getTime() + eventTime.getMarker().toLowerCase();
                        String subtitle = "";
                        String link = null;
                        int image = 0;
                        switch (event.getType()) {
                            case EARTH_APHELION:
                            case EARTH_PERIHELION:
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearEarthApsis", true)) { continue; }
                                title = event.getType().getDisplayName();
                                link = event.getLink();
                                break;
                            case PARTIAL_LUNAR:
                            case TOTAL_LUNAR:
                            case PENUMBRAL_LUNAR:
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearLunarEclipse", true)) { continue; }
                                title = event.getType().getDisplayName();
                                time = "Greatest eclipse: " + time;
                                link = event.getLink();
                                break;
                            case PARTIAL_SOLAR:
                            case TOTAL_SOLAR:
                            case ANNULAR_SOLAR:
                            case HYBRID_SOLAR:
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearSolarEclipse", true)) { continue; }
                                title = event.getType().getDisplayName();
                                time = "Greatest eclipse: " + time;
                                subtitle = (String) event.getExtra();
                                link = event.getLink();
                                break;
                            case MARCH_EQUINOX:
                            case SEPTEMBER_EQUINOX:
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearEquinox", true)) { continue; }
                                title = event.getType().getDisplayName();
                                break;
                            case NORTHERN_SOLSTICE:
                            {
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearSolstice", true)) { continue; }
                                title = event.getType().getDisplayName();
                                if (Math.abs(location.getLocation().getLatitude().getDoubleValue()) > 23.44) {
                                    SunDay sunDay = SunCalculator.INSTANCE.calcDay(location.getLocation(), event.getTime(), SunCalculator.Event.RISESET);
                                    String localExtreme = location.getLocation().getLatitude().getDoubleValue() >= 0 ? "Longest" : "Shortest";
                                    subtitle = localExtreme + " day: " + TimeUtils.formatDurationHMS(getApplicationContext(), sunDay.getUptimeHours(), true);
                                }
                                break;
                            }
                            case SOUTHERN_SOLSTICE:
                            {
                                if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearSolstice", true)) { continue; }
                                title = event.getType().getDisplayName();
                                if (Math.abs(location.getLocation().getLatitude().getDoubleValue()) > 23.44) {
                                    SunDay sunDay = SunCalculator.INSTANCE.calcDay(location.getLocation(), event.getTime(), SunCalculator.Event.RISESET);
                                    String localExtreme = location.getLocation().getLatitude().getDoubleValue() >= 0 ? "Shortest" : "Longest";
                                    subtitle = localExtreme + " day: " + TimeUtils.formatDurationHMS(getApplicationContext(), sunDay.getUptimeHours(), true);
                                }
                                break;
                            }
                            case PHASE:
                                MoonPhaseEvent moonPhase = (MoonPhaseEvent) event.getExtra();
                                switch (moonPhase.getPhase()) {
                                    case FULL:
                                        if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearFullMoon", true)) { continue; }
                                        title = "Full Moon";
                                        image = ThemePalette.getPhaseFull();
                                        break;
                                    case NEW:
                                        if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearNewMoon", true)) { continue; }
                                        title = "New Moon";
                                        image = ThemePalette.getPhaseNew();
                                        break;
                                    case FIRST_QUARTER:
                                        if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearQuarterMoon", true)) { continue; }
                                        title = "First Quarter";
                                        image = location.getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseRight() : ThemePalette.getPhaseLeft();
                                        break;
                                    case LAST_QUARTER:
                                        if (!SharedPrefsHelper.INSTANCE.getShowElement(getApplicationContext(), "yearQuarterMoon", true)) { continue; }
                                        title = "Last Quarter";
                                        image = location.getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseLeft() : ThemePalette.getPhaseRight();
                                        break;
                                }
                                break;


                        }

                        if (!first) {
                            getActivity().getLayoutInflater().inflate(layout.divider, eventsBox);
                        }
                        View eventRow = View.inflate(getActivity(), layout.frag_data_yearevents_event, null);

                        boolean today = todayCalendar.get(Calendar.YEAR) == event.getTime().get(Calendar.YEAR) &&
                                todayCalendar.get(Calendar.MONTH) == event.getTime().get(Calendar.MONTH) &&
                                todayCalendar.get(Calendar.DAY_OF_MONTH) == event.getTime().get(Calendar.DAY_OF_MONTH);
                        if (today) {
                            eventRow.setBackgroundColor(ThemePalette.getCalendarHighlightColor());
                        } else {
                            eventRow.setBackgroundColor(ThemePalette.getCalendarDefaultColor());
                        }

                        if (image > 0) {
                            imageInView(eventRow, id.yearEventImg, image);
                            showInView(eventRow, id.yearEventImg);
                        }
                        textInView(eventRow, id.yearEventDate, Integer.toString(event.getTime().get(Calendar.DAY_OF_MONTH)));
                        textInView(eventRow, id.yearEventMonth, getShortMonth(event.getTime()));
                        textInView(eventRow, id.yearEventTitle, Html.fromHtml(title));
                        textInView(eventRow, id.yearEventTime, Html.fromHtml(time));
                        if (StringUtils.isNotEmpty(subtitle)) {
                            textInView(eventRow, id.yearEventSubtitle, Html.fromHtml(subtitle));
                            showInView(eventRow, id.yearEventSubtitle);
                        }
                        if (StringUtils.isNotEmpty(link)) {
                            final String finalLink = link;
                            showInView(eventRow, id.yearEventLink);
                            eventRow.setOnClickListener(view1 -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(finalLink));
                                startActivity(intent);
                            });
                        } else {
                            eventRow.setClickable(false);
                            eventRow.setFocusable(false);
                        }

                        eventsBox.addView(eventRow);
                        first = false;
                    }
                    if (first) {
                        removeInView(view, id.yearEventsBox);
                        showInView(view, id.yearEventsNone);
                    } else {
                        showInView(view, id.yearEventsBox);
                        removeInView(view, id.yearEventsNone);
                    }
                });
            }
        };
        thread.start();
    }

    private String getShortMonth(Calendar calendar) {
        switch (calendar.get(Calendar.MONTH)) {
            case 0: return "JAN";
            case 1: return "FEB";
            case 2: return "MAR";
            case 3: return "APR";
            case 4: return "MAY";
            case 5: return "JUN";
            case 6: return "JUL";
            case 7: return "AUG";
            case 8: return "SEP";
            case 9: return "OCT";
            case 10: return "NOV";
            case 11: return "DEC";
        }
        return "";

    }

}