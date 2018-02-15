package uk.co.sundroid.activity.data.fragments;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.util.astro.MoonPhase;
import uk.co.sundroid.util.astro.MoonPhaseEvent;
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.theme.ThemePalette;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public class MonthMoonPhaseFragment extends AbstractMonthFragment {

    private final Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return layout.frag_data_monthmoonphase;
	}

    @Override
	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }

                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }

                    String packageName = getApplicationContext().getPackageName();

                    // Set column headers according to weekday preference.
                    for (int day = 1; day < 8; day++) {
                        int dayId = view("moonCalD" + day);
                        int altDay = day + SharedPrefsHelper.INSTANCE.getFirstWeekday(getApplicationContext()) - 1;
                        if (altDay > 7) { altDay -= 7; };
                        switch(altDay) {
                            case 1: showInView(view, dayId, "Sun"); break;
                            case 2: showInView(view, dayId, "Mon"); break;
                            case 3: showInView(view, dayId, "Tue"); break;
                            case 4: showInView(view, dayId, "Wed"); break;
                            case 5: showInView(view, dayId, "Thu"); break;
                            case 6: showInView(view, dayId, "Fri"); break;
                            case 7: showInView(view, dayId, "Sat"); break;
                        }
                    }

                    // Wipe all days.
                    for (int row = 1; row < 7; row++) {
                        int datesRowId = view("moonCalDates" + row);
                        ((TableRow)view.findViewById(datesRowId)).removeAllViews();
                        int imagesRowId = view("moonCalImages" + row);
                        ((TableRow)view.findViewById(imagesRowId)).removeAllViews();
                    }

                    Calendar loopCalendar = Calendar.getInstance();
                    loopCalendar.setTimeZone(calendar.getTimeZone());
                    loopCalendar.setTimeInMillis(calendar.getTimeInMillis());
                    loopCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    int month = loopCalendar.get(Calendar.MONTH);
                    int row = 1;

                    Calendar todayCalendar = Calendar.getInstance(calendar.getTimeZone());

                    // Add empty cells to the first row.
                    int firstCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.INSTANCE.getFirstWeekday(getApplicationContext())) + 1;
                    if (firstCol > 7) { firstCol -= 7; }
                    for (int i = 1; i < firstCol; i++) {
                        TableRow datesRow = view.findViewById(id.moonCalDates1);
                        View dateCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_date, datesRow, false);
                        dateCell.setVisibility(View.INVISIBLE);
                        datesRow.addView(dateCell);
                        TableRow imagesRow = view.findViewById(id.moonCalImages1);
                        View imageCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_image, imagesRow, false);
                        imageCell.setVisibility(View.INVISIBLE);
                        imagesRow.addView(imageCell);
                    }

                    List<MoonPhaseEvent> phaseEvents = MoonPhaseCalculator.INSTANCE.getYearEvents(calendar.get(Calendar.YEAR), calendar.getTimeZone());

                    for (int i = 0; i < 32 && loopCalendar.get(Calendar.MONTH) == month; i++) {
                        int col = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.INSTANCE.getFirstWeekday(getApplicationContext())) + 1;
                        if (col > 7) { col -= 7; }

                        boolean today = todayCalendar.get(Calendar.YEAR) == loopCalendar.get(Calendar.YEAR) &&
                                todayCalendar.get(Calendar.MONTH) == loopCalendar.get(Calendar.MONTH) &&
                                todayCalendar.get(Calendar.DAY_OF_MONTH) == loopCalendar.get(Calendar.DAY_OF_MONTH);

                        TableRow datesRow = view.findViewById(view("moonCalDates" + row));
                        TableRow imagesRow = view.findViewById(view("moonCalImages" + row));

                        View dateCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_date, datesRow, false);
                        ((TextView)dateCell.findViewById(id.moonCalTitleText)).setText(Integer.toString(loopCalendar.get(Calendar.DAY_OF_MONTH)));

                        MoonPhaseEvent phaseEvent = MoonPhaseCalculator.INSTANCE.getDayEvent(loopCalendar, phaseEvents);
                        if (phaseEvent != null) {
                            int phaseImg = ThemePalette.getPhaseFull();
                            if (phaseEvent.getPhase() == MoonPhase.NEW) {
                                phaseImg = ThemePalette.getPhaseNew();
                            } else if (phaseEvent.getPhase() == MoonPhase.FIRST_QUARTER) {
                                phaseImg = location.getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseRight() : ThemePalette.getPhaseLeft();
                            } else if (phaseEvent.getPhase() == MoonPhase.LAST_QUARTER) {
                                phaseImg = location.getLocation().getLatitude().getDoubleValue() >= 0 ? ThemePalette.getPhaseLeft() : ThemePalette.getPhaseRight();
                            }
                            ((ImageView)dateCell.findViewById(id.moonCalTitlePhase)).setImageResource(phaseImg);
                            dateCell.findViewById(id.moonCalTitlePhase).setVisibility(View.VISIBLE);
                        } else {
                            dateCell.findViewById(id.moonCalTitlePhase).setVisibility(View.GONE);
                        }

                        datesRow.addView(dateCell);

                        View imageCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_image, imagesRow, false);

                        double phaseDbl = MoonPhaseCalculator.INSTANCE.getNoonPhase(loopCalendar)/2;
                        BigDecimal phaseBd = new BigDecimal(phaseDbl);
                        phaseBd = phaseBd.setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        phaseBd = phaseBd.multiply(new BigDecimal(2));
                        if (location.getLocation().getLatitude().getDoubleValue() < 0) {
                            phaseBd = (new BigDecimal(1).subtract(phaseBd));
                        }
                        String moonImg = phaseBd.toString().replaceAll("\\.", "");

                        ((ImageView)imageCell.findViewById(id.moonCalImageOverlay)).setImageResource(getResources().getIdentifier(packageName + ":drawable/moonoverlay" + moonImg, null, null));

                        imagesRow.addView(imageCell);

                        if (today) {
                            dateCell.setBackgroundColor(ThemePalette.getCalendarHighlightColor());
                            imageCell.setBackgroundColor(ThemePalette.getCalendarHighlightColor());
                        } else {
                            dateCell.setBackgroundColor(ThemePalette.getCalendarHeaderColor());
                            imageCell.setBackgroundColor(ThemePalette.getCalendarDefaultColor());
                        }

                        loopCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        if (loopCalendar.get(Calendar.MONTH) == month && col == 7) {
                            row++;
                        }
                    }

                    // Fill out any remaining cells in the last row.
                    loopCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    int lastCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.INSTANCE.getFirstWeekday(getApplicationContext())) + 1;
                    if (lastCol > 7) { lastCol -= 7; }
                    for (int i = lastCol + 1; i < 8; i++) {
                        TableRow datesRow = view.findViewById(view("moonCalDates" + row));
                        View dateCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_date, datesRow, false);
                        dateCell.setVisibility(View.INVISIBLE);
                        datesRow.addView(dateCell);
                        TableRow imagesRow = view.findViewById(view("moonCalImages" + row));
                        View imageCell = getActivity().getLayoutInflater().inflate(layout.frag_data_monthmoonphase_image, imagesRow, false);
                        imageCell.setVisibility(View.INVISIBLE);
                        imagesRow.addView(imageCell);
                    }


                });
            }
        };
        thread.start();
    }



}