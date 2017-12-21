package uk.co.sundroid.activity.data.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import uk.co.sundroid.R;
import uk.co.sundroid.R.array;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.MoonDay;
import uk.co.sundroid.util.astro.MoonPhase;
import uk.co.sundroid.util.astro.MoonPhaseEvent;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.TwilightType;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.astro.math.SunCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.*;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeHelper;
import uk.co.sundroid.util.time.TimeHelper.Time;
import uk.co.sundroid.util.geometry.BearingHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

public class MonthCalendarsFragment extends AbstractMonthFragment<ArrayList<MonthCalendarsFragment.DayEntry>> implements OnItemSelectedListener {

    private static final String TAG = MonthCalendarsFragment.class.getSimpleName();

    private boolean selectorActive = false;

	@Override
	protected int getLayout() {
		return layout.frag_data_monthcalendars;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LogWrapper.d(TAG, hashCode() + " on view created ");
        selectorActive = false;
        Spinner selector = view.findViewById(id.monthCalSelector);
        selector.setOnTouchListener((v, e) -> {
            LogWrapper.d(TAG, "Selector activated by touch");
            selectorActive = true;
            return false;
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), array.monthCalendars, layout.frag_data_monthcalendars_selector_selected);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int index = SharedPrefsHelper.getLastCalendar(getActivity());
        selector.setAdapter(adapter);
        selector.setOnItemSelectedListener(this);
        if (selector.getSelectedItemPosition() != index) {
            LogWrapper.d(TAG, "set selection " + index);
            selector.setSelection(index);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
        if (selectorActive) {
            LogWrapper.d(TAG, "Item selected " + index);
            SharedPrefsHelper.setLastCalendar(getActivity(), index);
            update();
        } else {
            LogWrapper.d(TAG, "SELECTOR NOT ACTIVE " + index);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected ArrayList<DayEntry> calculate(final LocationDetails location, final Calendar calendar) {
        String type = getType();
        Body body = getBody();

        Calendar todayCalendar = Calendar.getInstance(calendar.getTimeZone());

        Calendar loopCalendar = Calendar.getInstance();
        loopCalendar.setTimeZone(calendar.getTimeZone());
        loopCalendar.setTimeInMillis(calendar.getTimeInMillis());
        loopCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int month = loopCalendar.get(Calendar.MONTH);

        // Calculate full details of previous day for diffs.
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTimeZone(calendar.getTimeZone());
        prevCalendar.setTimeInMillis(calendar.getTimeInMillis());
        prevCalendar.set(Calendar.DAY_OF_MONTH, 1);
        prevCalendar.add(Calendar.DAY_OF_MONTH, -1);
        SunDay previousSunDay = SunCalculator.calcDay(location.getLocation(), prevCalendar);
        BodyDay previousBodyDay = body == null ? null : BodyPositionCalculator.calcDay(body, location.getLocation(), prevCalendar, false);

        final ArrayList<DayEntry> entries = new ArrayList<>();

        // Placeholder entry becomes column headings.
        entries.add(null);

        for (int day = 1; day < 32 && loopCalendar.get(Calendar.MONTH) == month; day++) {
            BodyDay bodyDay = null;
            SunDay sunDay = null;
            MoonDay moonDay = null;
            if (body != null) {
                bodyDay = BodyPositionCalculator.calcDay(body, location.getLocation(), loopCalendar, false);
            } else if (type.equals("daylight")) {
                sunDay = SunCalculator.calcDay(location.getLocation(), loopCalendar, SunCalculator.Event.RISESET);
            } else if (type.equals("civ")) {
                sunDay = SunCalculator.calcDay(location.getLocation(), loopCalendar, SunCalculator.Event.CIVIL);
            } else if (type.equals("ntc")) {
                sunDay = SunCalculator.calcDay(location.getLocation(), loopCalendar, SunCalculator.Event.NAUTICAL);
            } else if (type.equals("ast")) {
                sunDay = SunCalculator.calcDay(location.getLocation(), loopCalendar, SunCalculator.Event.ASTRONOMICAL);
            } else if (type.equals("golden")) {
                sunDay = SunCalculator.calcDay(location.getLocation(), loopCalendar, SunCalculator.Event.GOLDENHOUR);
            } else if (type.equals("moon")) {
                moonDay = (MoonDay)BodyPositionCalculator.calcDay(Body.MOON, location.getLocation(), loopCalendar, false);
            }
            boolean today = todayCalendar.get(Calendar.YEAR) == loopCalendar.get(Calendar.YEAR) &&
                    todayCalendar.get(Calendar.MONTH) == loopCalendar.get(Calendar.MONTH) &&
                    todayCalendar.get(Calendar.DAY_OF_MONTH) == loopCalendar.get(Calendar.DAY_OF_MONTH);

            DayEntry dayEntry = new DayEntry();
            dayEntry.day = day;
            dayEntry.sunDay = sunDay;
            dayEntry.previousSunDay = previousSunDay;
            dayEntry.bodyDay = bodyDay;
            dayEntry.previousBodyDay = previousBodyDay;
            dayEntry.moonDay = moonDay;
            dayEntry.dayOfWeek = loopCalendar.get(Calendar.DAY_OF_WEEK);
            dayEntry.today = today;
            entries.add(dayEntry);
            loopCalendar.add(Calendar.DAY_OF_MONTH, 1);
            previousSunDay = sunDay;
            previousBodyDay = bodyDay;
        }

        return entries;

    }

    @Override
    protected void post(View view, ArrayList<DayEntry> entries) {
        DayEntryAdapter listAdapter = new DayEntryAdapter(entries);
        ListView list = view.findViewById(R.id.monthCalList);
        list.setAdapter(listAdapter);
    }

    @Override
	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {
        offThreadUpdate(location, calendar, view);
    }

    public static class DayEntry {
        private MoonDay moonDay = null;
        private SunDay sunDay = null;
        private SunDay previousSunDay = null;
        private BodyDay bodyDay = null;
        private BodyDay previousBodyDay = null;
        private int day;
        private int dayOfWeek;
        private boolean today = false;
    }

    private class DayEntryAdapter extends ArrayAdapter<DayEntry> {

        private DayEntryAdapter(ArrayList<DayEntry> list) {
            super(getApplicationContext(), layout.frag_data_monthcalendars_row, list);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(layout.frag_data_monthcalendars_row, parent, false);
            }
            DayEntry entry = getItem(position);
            String type = getType();
            Body body = getBody();

            String packageName = getApplicationContext().getPackageName();

            if (entry == null) {
                textInView(row, id.rowDate, "");
                textInView(row, id.rowWeekday, "");
                row.setBackgroundColor(ThemePalette.getCalendarDefaultColor());

                showInView(row, id.dayUpAz, id.dayDownAz);
                removeInView(row, id.dayMoonCell, id.monthCalDatePhase);

                if (body != null) {
                    textInView(row, id.dayUp, "RISE");
                    textInView(row, id.dayDown, "SET");
                    textInView(row, id.dayUpAz, "CHANGE\nAZIMUTH");
                    textInView(row, id.dayDownAz, "CHANGE\nAZIMUTH");
                } else if (type.equals("civ") || type.equals("ntc") || type.equals("ast")) {
                    textInView(row, id.dayUp, "DAWN");
                    textInView(row, id.dayDown, "DUSK");
                    textInView(row, id.dayUpAz, "CHANGE");
                    textInView(row, id.dayDownAz, "CHANGE");
                } else if (type.equals("golden")) {
                    textInView(row, id.dayUp, "MORNING END");
                    textInView(row, id.dayDown, "EVENING START");
                    textInView(row, id.dayUpAz, "CHANGE");
                    textInView(row, id.dayDownAz, "CHANGE");
                } else if (type.equals("daylight")) {
                    textInView(row, id.dayUp, "LENGTH");
                    textInView(row, id.dayDown, "CHANGE");
                    removeInView(row, id.dayUpAz);
                    removeInView(row, id.dayDownAz);
                } else if (type.equals("moon")) {
                    hideInView(row, id.dayMoonCell);
                    textInView(row, id.dayUp, "RISE");
                    textInView(row, id.dayDown, "SET");
                    textInView(row, id.dayUpAz, "AZIMUTH");
                    textInView(row, id.dayDownAz, "AZIMUTH");
                }
                return row;
            }

            if (entry.today) {
                row.setBackgroundColor(ThemePalette.getCalendarHighlightColor());
            } else {
                row.setBackgroundColor(ThemePalette.getCalendarDefaultColor());
            }

            String weekday = "";
            switch (entry.dayOfWeek) {
                case (Calendar.MONDAY): weekday = "MON"; break;
                case (Calendar.TUESDAY): weekday = "TUE"; break;
                case (Calendar.WEDNESDAY): weekday = "WED"; break;
                case (Calendar.THURSDAY): weekday = "THU"; break;
                case (Calendar.FRIDAY): weekday = "FRI"; break;
                case (Calendar.SATURDAY): weekday = "SAT"; break;
                case (Calendar.SUNDAY): weekday = "SUN"; break;
            }

            textInView(row, id.rowDate, Integer.toString(entry.day));
            textInView(row, id.rowWeekday, weekday);

            Calendar up = null;
            Calendar down = null;
            double length = Double.MIN_VALUE;
            double lengthDiff = Double.MIN_VALUE;

            String upAz = null;
            String downAz = null;
            RiseSetType riseSetType = null;
            boolean allowSeconds = false;

            if (body != null) {
                allowSeconds = body == Body.SUN;
                BodyDay bodyDay = entry.bodyDay;
                BodyDay previousBodyDay = entry.previousBodyDay;
                up = bodyDay.getRise();
                down = bodyDay.getSet();
                if (up != null && previousBodyDay.getRise() != null) {
                    upAz = TimeHelper.formatDiff(getApplicationContext(), bodyDay.getRise(), previousBodyDay.getRise(), allowSeconds);
                }
                if (down != null && previousBodyDay.getSet() != null) {
                    downAz = TimeHelper.formatDiff(getApplicationContext(), bodyDay.getSet(), previousBodyDay.getSet(), allowSeconds);
                }
                if (up != null) {
                    if (StringUtils.isNotEmpty(upAz)) { upAz += "\n"; } else { upAz = ""; }
                    String azimuth = BearingHelper.formatBearing(getApplicationContext(), bodyDay.getRiseAzimuth(), getLocation().getLocation(), up);
                    upAz += azimuth;
                }
                if (down != null) {
                    if (StringUtils.isNotEmpty(downAz)) { downAz += "\n"; } else { downAz = ""; }
                    String azimuth = BearingHelper.formatBearing(getApplicationContext(), bodyDay.getSetAzimuth(), getLocation().getLocation(), down);
                    downAz += azimuth;
                }
                if (up == null && down == null) {
                    riseSetType = bodyDay.getRiseSetType();
                }
                ((LinearLayout)row.findViewById(R.id.dayUpCell)).setWeightSum(0.40f);
                ((LinearLayout)row.findViewById(R.id.dayDownCell)).setWeightSum(0.40f);

            } else if (type.equals("civ")) {
                allowSeconds = true;
                SunDay sunDay = entry.sunDay;
                SunDay previousSunDay = entry.previousSunDay;
                up = sunDay.getCivDawn();
                down = sunDay.getCivDusk();
                if (up == null && down == null) {
                    riseSetType = sunDay.getCivType() == TwilightType.DARK ? RiseSetType.SET : RiseSetType.RISEN;
                }
                if (up != null && previousSunDay.getCivDawn() != null) {
                    upAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getCivDawn(), previousSunDay.getCivDawn(), true);
                }
                if (down != null && previousSunDay.getCivDusk() != null) {
                    downAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getCivDusk(), previousSunDay.getCivDusk(), true);
                }
            } else if (type.equals("ntc")) {
                allowSeconds = true;
                SunDay sunDay = entry.sunDay;
                SunDay previousSunDay = entry.previousSunDay;
                up = sunDay.getNtcDawn();
                down = sunDay.getNtcDusk();
                if (up == null && down == null) {
                    riseSetType = sunDay.getNtcType() == TwilightType.DARK ? RiseSetType.SET : RiseSetType.RISEN;
                }
                if (up != null && previousSunDay.getNtcDawn() != null) {
                    upAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getNtcDawn(), previousSunDay.getNtcDawn(), true);
                }
                if (down != null && previousSunDay.getNtcDusk() != null) {
                    downAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getNtcDusk(), previousSunDay.getNtcDusk(), true);
                }
            } else if (type.equals("ast")) {
                allowSeconds = true;
                SunDay sunDay = entry.sunDay;
                SunDay previousSunDay = entry.previousSunDay;
                up = sunDay.getAstDawn();
                down = sunDay.getAstDusk();
                if (up == null && down == null) {
                    riseSetType = sunDay.getAstType() == TwilightType.DARK ? RiseSetType.SET : RiseSetType.RISEN;
                }
                if (up != null && previousSunDay.getAstDawn() != null) {
                    upAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getAstDawn(), previousSunDay.getAstDawn(), true);
                }
                if (down != null && previousSunDay.getAstDusk() != null) {
                    downAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getAstDusk(), previousSunDay.getAstDusk(), true);
                }
            } else if (type.equals("golden")) {
                allowSeconds = true;
                SunDay sunDay = entry.sunDay;
                SunDay previousSunDay = entry.previousSunDay;
                up = sunDay.getGhEnd();
                down = sunDay.getGhStart();
                if (up == null && down == null) {
                    riseSetType = sunDay.getGhType() == TwilightType.DARK ? RiseSetType.SET : RiseSetType.RISEN;
                }
                if (up != null && previousSunDay.getGhEnd() != null) {
                    upAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getGhEnd(), previousSunDay.getGhEnd(), true);
                }
                if (down != null && previousSunDay.getGhStart() != null) {
                    downAz = TimeHelper.formatDiff(getApplicationContext(), sunDay.getGhStart(), previousSunDay.getGhStart(), true);
                }
            } else if (type.equals("daylight")) {
                allowSeconds = true;
                SunDay sunDay = entry.sunDay;
                SunDay previousSunDay = entry.previousSunDay;
                length = sunDay.getUptimeHours();
                lengthDiff = sunDay.getUptimeHours() - previousSunDay.getUptimeHours();
            } else if (type.equals("moon")) {
                MoonDay moonDay = entry.moonDay;
                up = moonDay.getRise();
                down = moonDay.getSet();
                if (up != null) {
                    upAz = BearingHelper.formatBearing(getApplicationContext(), moonDay.getRiseAzimuth(), getLocation().getLocation(), up);
                }
                if (down != null) {
                    downAz = BearingHelper.formatBearing(getApplicationContext(), moonDay.getSetAzimuth(), getLocation().getLocation(), down);
                }
                if (up == null && down == null) {
                    if (moonDay.getRiseSetType() == RiseSetType.RISEN) {
                        riseSetType = RiseSetType.RISEN;
                    } else {
                        riseSetType = RiseSetType.SET;
                    }
                }

                double phaseDbl = moonDay.getPhaseDouble()/2;
                BigDecimal phaseBd = new BigDecimal(phaseDbl);
                phaseBd = phaseBd.setScale(2, BigDecimal.ROUND_HALF_DOWN);
                phaseBd = phaseBd.multiply(new BigDecimal(2));
                if (getLocation().getLocation().getLatitude().getDoubleValue() < 0) {
                    phaseBd = (new BigDecimal(1).subtract(phaseBd));
                }
                String moonImg = phaseBd.toString().replaceAll("\\.", "");
                imageInView(row, id.dayMoonOverlay, getResources().getIdentifier(packageName + ":drawable/moonoverlay" + moonImg, null, null));
                showInView(row, id.dayMoonCell);

                MoonPhaseEvent phaseEvent = moonDay.getPhaseEvent();
                if (phaseEvent != null) {
                    int phaseImg = ThemePalette.getPhaseFull();
                    if (phaseEvent.getPhase() == MoonPhase.NEW) {
                        phaseImg = ThemePalette.getPhaseNew();
                    } else if (phaseEvent.getPhase() == MoonPhase.FIRST_QUARTER) {
                        phaseImg = getLocation().getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseRight() : ThemePalette.getPhaseLeft();
                    } else if (phaseEvent.getPhase() == MoonPhase.LAST_QUARTER) {
                        phaseImg = getLocation().getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseLeft() : ThemePalette.getPhaseRight();
                    }
                    imageInView(row, id.monthCalDatePhase, phaseImg);
                    showInView(row, id.monthCalDatePhase);
                } else {
                    removeInView(row, id.monthCalDatePhase);
                }
            }

            if (length != Double.MIN_VALUE) {
                String timeStr = TimeHelper.formatDuration(getApplicationContext(), length, true);
                textInView(row, id.dayUp, timeStr);
                String diffStr = TimeHelper.formatDiff(getApplicationContext(), lengthDiff, true);
                textInView(row, id.dayDown, diffStr);
                removeInView(row, id.dayUpAz);
                removeInView(row, id.dayDownAz);
            } else {
                if (up != null) {
                    Time time = TimeHelper.formatTime(getApplicationContext(), up, allowSeconds);
                    String timeStr = time.time;
                    if (StringUtils.isNotEmpty(time.marker)) {
                        timeStr += time.marker;
                    }
                    textInView(row, id.dayUp, timeStr);
                    if (upAz != null) {
                        showInView(row, id.dayUpAz, upAz);
                    } else {
                        removeInView(row, id.dayUpAz);
                    }
                } else {
                    if (down == null && riseSetType != null) {
                        if (type.equals("sun") || type.equals("moon")) {
                            textInView(row, id.dayUp, riseSetType == RiseSetType.RISEN ? "Risen" : "Set");
                        } else {
                            textInView(row, id.dayUp, riseSetType == RiseSetType.RISEN ? "Light" : "Dark");
                        }
                    } else {
                        textInView(row, id.dayUp, "None");
                    }
                    removeInView(row, id.dayUpAz);
                }

                if (down != null) {
                    Time time = TimeHelper.formatTime(getApplicationContext(), down, allowSeconds);
                    String timeStr = time.time;
                    if (StringUtils.isNotEmpty(time.marker)) {
                        timeStr += time.marker;
                    }
                    ((TextView)row.findViewById(R.id.dayDown)).setText(timeStr);
                    if (downAz != null) {
                        showInView(row, id.dayDownAz, downAz);
                    } else {
                        removeInView(row, id.dayDownAz);
                    }
                } else {
                    if (up == null && riseSetType != null) {
                        textInView(row, id.dayDown, "");
                    } else {
                        textInView(row, id.dayDown, "None");
                    }
                    removeInView(row, id.dayDownAz);
                }
            }
            return row;
        }

    }

    private Body getBody() {
        switch (SharedPrefsHelper.getLastCalendar(getActivity())) {
            case 0:
                return Body.SUN;
            case 7:
                return Body.MERCURY;
            case 8:
                return Body.VENUS;
            case 9:
                return Body.MARS;
            case 10:
                return Body.JUPITER;
            case 11:
                return Body.SATURN;
            case 12:
                return Body.URANUS;
            case 13:
                return Body.NEPTUNE;
            default:
                return null;
        }
    }

    private String getType() {
        switch (SharedPrefsHelper.getLastCalendar(getActivity())) {
            case 0:
                return "daylight";
            case 1:
                return "civ";
            case 2:
                return "ntc";
            case 3:
                return "ast";
            case 4:
                return "golden";
            case 5:
                return "moon";
            case 6:
                return "daylight";
            default:
                return "";
        }
    }


}