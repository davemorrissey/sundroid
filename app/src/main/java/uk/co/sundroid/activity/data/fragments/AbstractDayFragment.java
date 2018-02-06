package uk.co.sundroid.activity.data.fragments;

import android.os.Bundle;
import android.view.*;
import uk.co.sundroid.R;
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.view.ButtonDragGestureDetector;
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener;
import uk.co.sundroid.util.log.LogWrapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class AbstractDayFragment extends AbstractDataFragment implements DatePickerFragment.OnDateSelectedListener{
	
	private static final String TAG = AbstractDayFragment.class.getSimpleName();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    private SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.US);

    private GestureDetector dateDetector;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		View view = inflater.inflate(getLayout(), container, false);
		safeUpdate(view);
		return view;
	}

    @Override
    public void initialise() {
        if (getView() != null) {
            safeInit(getView());
        }
    }

    @Override
    public void update() {
        if (getView() != null) {
            safeUpdate(getView());
        }
    }

    @Override
    public void onDateSet(int year, int month, int date) {
        getDateCalendar().set(year, month, date);
        getTimeCalendar().set(year, month, date);
        update();
    }

    private void safeInit(View view) {
        LocationDetails location = getLocation();
        Calendar calendar = getDateCalendar();
        try {
            if (location != null && calendar != null && view != null && !isDetached()) {
                initGestures(view);
                updateDate(location, calendar, view);
            }
        } catch (Exception e) {
            LogWrapper.e(TAG, "Failed to update data view", e);
        }
    }

	private void safeUpdate(View view) {
		LocationDetails location = getLocation();
		Calendar calendar = getDateCalendar();
		try {
			if (location != null && calendar != null && view != null && !isDetached()) {
                initGestures(view);
			    updateDate(location, calendar, view);
				update(location, calendar, view);
            }
		} catch (Exception e) {
			LogWrapper.e(TAG, "Failed to update data view", e);
		}
	}

    private void initGestures(View view) {
        if (dateDetector == null) {
            ButtonDragGestureDetectorListener dateListener = new ButtonDragGestureDetectorListener() {
                @Override public void onButtonDragUp() { prevMonth(); }
                @Override public void onButtonDragDown() { nextMonth(); }
                @Override public void onButtonDragLeft() { prevDate(); }
                @Override public void onButtonDragRight() { nextDate(); }
            };
            dateDetector = new GestureDetector(getApplicationContext(), new ButtonDragGestureDetector(dateListener, getApplicationContext()));
        }

        view.findViewById(R.id.datePrev).setOnClickListener(v -> prevDate());
        view.findViewById(R.id.dateNext).setOnClickListener(v -> nextDate());
        view.findViewById(R.id.zoneButton).setOnClickListener(v -> startTimeZone());
        view.findViewById(R.id.dateButton).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.dateButton).setOnTouchListener((v, e) -> {
            if (dateDetector != null) {
                if (dateDetector.onTouchEvent(e)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });
    }

    private void updateDate(LocationDetails location, Calendar calendar, View view) {
    	if (SharedPrefsHelper.INSTANCE.getShowTimeZone(getApplicationContext())) {
    		showInView(view, R.id.zoneButton);
			TimeZone zone = location.getTimeZone().getZone();
			boolean zoneDST = zone.inDaylightTime(new Date(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)));
			String zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG);
			textInView(view, R.id.zoneName, zoneName);

			String zoneCities = location.getTimeZone().getOffset(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)); // Get day's main offset.
			if (StringUtils.isNotEmpty(location.getTimeZone().getCities())) {
				zoneCities += " " + location.getTimeZone().getCities();
			}
			textInView(view, R.id.zoneCities, zoneCities);
    	} else {
    		removeInView(view, R.id.zoneButton);
    	}

		dateFormat.setTimeZone(calendar.getTimeZone());
		weekdayFormat.setTimeZone(calendar.getTimeZone());
		String date = dateFormat.format(new Date(calendar.getTimeInMillis()));
		String weekday = weekdayFormat.format(new Date(calendar.getTimeInMillis()));
		showInView(view, R.id.dateDMY, date);
		showInView(view, R.id.dateWeekday, weekday);
    }

    private void showDatePicker() {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(getDateCalendar());
        datePickerFragment.setTargetFragment(this, 0);
        datePickerFragment.show(getFragmentManager(), "datePicker");
    }

    private void nextDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, 1);
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, 1);
        update();
    }

    private void nextMonth() {
        getDateCalendar().add(Calendar.MONTH, 1);
        getTimeCalendar().add(Calendar.MONTH, 1);
        update();
    }

    private void prevDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, -1);
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, -1);
        update();
    }

    private void prevMonth() {
        getDateCalendar().add(Calendar.MONTH, -1);
        getTimeCalendar().add(Calendar.MONTH, -1);
        update();
    }
	
	protected abstract int getLayout();
	
	protected abstract void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception;

}
