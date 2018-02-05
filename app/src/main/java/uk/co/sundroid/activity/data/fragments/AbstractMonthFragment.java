package uk.co.sundroid.activity.data.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.view.ButtonDragGestureDetector;
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener;
import uk.co.sundroid.util.log.LogWrapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class AbstractMonthFragment<T> extends AbstractDataFragment implements MonthPickerFragment.OnMonthSelectedListener {

	private static final String TAG = AbstractMonthFragment.class.getSimpleName();

    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.US);

    private GestureDetector monthDetector;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		if (container == null) {
			return null;
		}
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
    public void onMonthSet(int year, int month) {
        getDateCalendar().set(Calendar.YEAR, year);
        getDateCalendar().set(Calendar.MONTH, month);
        getTimeCalendar().set(Calendar.YEAR, year);
        getTimeCalendar().set(Calendar.MONTH, month);
        update();
    }

    private void safeInit(View view) {
        LocationDetails location = getLocation();
        Calendar calendar = getDateCalendar();
        try {
            if (location != null && calendar != null && view != null && !isDetached()) {
                LogWrapper.d(TAG, hashCode() + " running safeUpdate");
                initGestures(view);
                updateMonth(location, calendar, view);
            }
        } catch (Exception e) {
            LogWrapper.e(TAG, hashCode() + " Failed to update data view", e);
        }
    }

	private void safeUpdate(View view) {
		LocationDetails location = getLocation();
		Calendar calendar = getDateCalendar();
		try {
			if (location != null && calendar != null && view != null && !isDetached()) {
                LogWrapper.d(TAG, hashCode() + " running safeUpdate");
                initGestures(view);
                updateMonth(location, calendar, view);
				update(location, calendar, view);
			}
		} catch (Exception e) {
			LogWrapper.e(TAG, hashCode() + " Failed to update data view", e);
		}
	}

    private void initGestures(View view) {
        if (monthDetector == null) {
            ButtonDragGestureDetectorListener monthListener = new ButtonDragGestureDetectorListener() {
                @Override public void onButtonDragUp() { prevYear(); }
                @Override public void onButtonDragDown() { nextYear(); }
                @Override public void onButtonDragLeft() { prevMonth(); }
                @Override public void onButtonDragRight() { nextMonth(); }
            };
            monthDetector = new GestureDetector(getApplicationContext(), new ButtonDragGestureDetector(monthListener, getApplicationContext()));
        }

        view.findViewById(id.monthPrev).setOnClickListener(v -> prevMonth());
        view.findViewById(id.monthNext).setOnClickListener(v -> nextMonth());
        view.findViewById(id.zoneButton).setOnClickListener(v -> startTimeZone());
        view.findViewById(id.monthButton).setOnClickListener(v -> showMonthPicker());
        view.findViewById(id.monthButton).setOnTouchListener((v, event) -> monthDetector != null && monthDetector.onTouchEvent(event));
    }

    private void updateMonth(LocationDetails location, Calendar calendar, View view) {
    	if (SharedPrefsHelper.getShowTimeZone(getApplicationContext())) {
    		showInView(view, id.zoneButton);
			TimeZone zone = location.getTimeZone().getZone();
			boolean zoneDST = zone.inDaylightTime(new Date(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)));
			String zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG);
			textInView(view, id.zoneName, zoneName);

			String zoneCities = location.getTimeZone().getOffset(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)); // Get day's main offset.
			if (StringUtils.isNotEmpty(location.getTimeZone().getCities())) {
				zoneCities += " " + location.getTimeZone().getCities();
			}
			textInView(view, id.zoneCities, zoneCities);
    	} else {
    		removeInView(view, id.zoneButton);
    	}

		monthFormat.setTimeZone(calendar.getTimeZone());
		String month = monthFormat.format(new Date(calendar.getTimeInMillis()));
		showInView(view, id.month, month);
    }

    private void showMonthPicker() {
        MonthPickerFragment monthPickerFragment = MonthPickerFragment.newInstance(getDateCalendar());
        monthPickerFragment.setTargetFragment(this, 0);
        monthPickerFragment.show(getFragmentManager(), "monthPicker");
    }

    private void nextMonth() {
        getDateCalendar().add(Calendar.MONTH, 1);
        getTimeCalendar().add(Calendar.MONTH, 1);
        update();
    }

    private void prevMonth() {
        getDateCalendar().add(Calendar.MONTH, -1);
        getTimeCalendar().add(Calendar.MONTH, -1);
        update();
    }

    private void nextYear() {
        getDateCalendar().add(Calendar.YEAR, 1);
        getTimeCalendar().add(Calendar.YEAR, 1);
        update();
    }

    private void prevYear() {
        getDateCalendar().add(Calendar.YEAR, -1);
        getTimeCalendar().add(Calendar.YEAR, -1);
        update();
    }

    protected abstract int getLayout();
	
	protected abstract void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception;

    private Handler handler = new Handler();

    protected void offThreadUpdate(final LocationDetails location, final Calendar calendar, final View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (!isSafe()) {
                    return;
                }
                final T data = calculate(location, calendar);
                handler.post(() -> {
                    if (!isSafe()) {
                        return;
                    }
                    post(view, data);
                });
            }
        };
        thread.start();

    }

    protected T calculate(LocationDetails location, Calendar calendar) {
        return null;
    }

    protected void post(View view, T data) {

    }



}
