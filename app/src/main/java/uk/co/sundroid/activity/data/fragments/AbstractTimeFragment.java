package uk.co.sundroid.activity.data.fragments;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment;
import uk.co.sundroid.activity.data.fragments.dialogs.date.TimePickerFragment;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.view.ButtonDragGestureDetector;
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener;
import uk.co.sundroid.util.LogWrapper;
import uk.co.sundroid.util.time.TimeUtils;
import uk.co.sundroid.util.time.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class AbstractTimeFragment extends AbstractDataFragment implements OnClickListener, OnTouchListener, OnSeekBarChangeListener, DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {
	
	private static final String TAG = AbstractTimeFragment.class.getSimpleName();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    private SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.US);

    private GestureDetector dateDetector;
    private GestureDetector timeDetector;

    protected abstract int getLayout();

    protected abstract void initialise(final LocationDetails location, final Calendar dateCalendar, final Calendar timeCalendar, final View view) throws Exception;

    protected abstract void update(final LocationDetails location, final Calendar dateCalendar, final Calendar timeCalendar, final View view, boolean timeOnly) throws Exception;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(getLayout(), container, false);
        safeInitialise(view);
		safeUpdate(view, false);
		return view;
	}

    @Override
    public void initialise() {
        if (getView() != null) {
            safeInitialise(getView());
        }
    }

    @Override
    public void update() {
        if (getView() != null) {
            safeUpdate(getView(), false);
        }
    }

    @Override
    public void onDateSet(int year, int month, int date) {
        getDateCalendar().set(year, month, date);
        getTimeCalendar().set(year, month, date);
        update();
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        getTimeCalendar().set(Calendar.HOUR_OF_DAY, hour);
        getTimeCalendar().set(Calendar.MINUTE, minute);
        update();
    }

    @Override
    public void onClick(View button) {
        switch (button.getId()) {
            case id.timeButton:
                showTimePicker();
                return;
            case id.dateButton:
                showDatePicker();
                return;
            case id.zoneButton:
                startTimeZone();
                return;
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == id.dateButton) {
            return dateDetector.onTouchEvent(event);
        } else if (view.getId() == id.timeButton) {
            return timeDetector.onTouchEvent(event);
        }
        return false;
    }

    private void safeInitialise(View view) {
        LocationDetails location = getLocation();
        Calendar dateCalendar = getDateCalendar();
        Calendar timeCalendar = getTimeCalendar();
        try {
            if (location != null && dateCalendar != null && timeCalendar != null && view != null && !isDetached()) {
                initGestures(view);
                updateDateAndTime(view, dateCalendar, timeCalendar);
                initialise(location, dateCalendar, timeCalendar, view);
            }
        } catch (Exception e) {
            LogWrapper.e(TAG, "Failed to init data view", e);
        }
    }

	private void safeUpdate(View view, boolean timeOnly) {
		LocationDetails location = getLocation();
        Calendar dateCalendar = getDateCalendar();
        Calendar timeCalendar = getTimeCalendar();
		try {
			if (location != null && dateCalendar != null && timeCalendar != null && view != null && !isDetached()) {
                updateDateAndTime(view, dateCalendar, timeCalendar);
				update(location, dateCalendar, timeCalendar, view, timeOnly);
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
        view.findViewById(id.dateButton).setOnClickListener(this);
        view.findViewById(id.dateButton).setOnTouchListener(this);
        view.findViewById(id.zoneButton).setOnClickListener(this);

        if (timeDetector == null) {
            ButtonDragGestureDetectorListener timeListener = new ButtonDragGestureDetectorListener() {
                @Override public void onButtonDragUp() { prevHour(); }
                @Override public void onButtonDragDown() { nextHour(); }
                @Override public void onButtonDragLeft() { prevMinute(); }
                @Override public void onButtonDragRight() { nextMinute(); }
            };
            timeDetector = new GestureDetector(getApplicationContext(), new ButtonDragGestureDetector(timeListener, getApplicationContext()));
        }
        view.findViewById(id.timeButton).setOnClickListener(this);
        view.findViewById(id.timeButton).setOnTouchListener(this);
        ((SeekBar)view.findViewById(R.id.timeSeeker)).setOnSeekBarChangeListener(this);
    }

    private void updateDateAndTime(View view, Calendar dateCalendar, Calendar timeCalendar) {
        LocationDetails location = getLocation();
        if (location == null || view == null) {
            return;
        }


    	if (SharedPrefsHelper.getShowTimeZone(getApplicationContext())) {
    		showInView(view, id.zoneButton);
			TimeZone zone = location.getTimeZone().getZone();
			boolean zoneDST = zone.inDaylightTime(new Date(dateCalendar.getTimeInMillis() + (12 * 60 * 60 * 1000)));
			String zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG);
			textInView(view, id.zoneName, zoneName);

			String zoneCities = location.getTimeZone().getOffset(dateCalendar.getTimeInMillis() + (12 * 60 * 60 * 1000)); // Get day's main offset.
			if (StringUtils.isNotEmpty(location.getTimeZone().getCities())) {
				zoneCities += " " + location.getTimeZone().getCities();
			}
			textInView(view, id.zoneCities, zoneCities);
    	} else {
    		removeInView(view, id.zoneButton);
    	}

        Time time = TimeUtils.formatTime(getApplicationContext(), timeCalendar, false, false);
        showInView(view, id.timeHM, time.getTime() + time.getMarker());

        dateFormat.setTimeZone(dateCalendar.getTimeZone());
		weekdayFormat.setTimeZone(dateCalendar.getTimeZone());
		String date = dateFormat.format(new Date(dateCalendar.getTimeInMillis()));
		String weekday = weekdayFormat.format(new Date(dateCalendar.getTimeInMillis()));
		showInView(view, id.dateDMY, date);
		showInView(view, id.dateWeekday, weekday);

        int minutes = (timeCalendar.get(Calendar.HOUR_OF_DAY) * 60) + timeCalendar.get(Calendar.MINUTE);
        ((SeekBar)view.findViewById(R.id.timeSeeker)).setProgress(minutes);
    }

    private void showDatePicker() {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(getDateCalendar());
        datePickerFragment.setTargetFragment(this, 0);
        datePickerFragment.show(getFragmentManager(), "datePicker");
    }

    private void showTimePicker() {
        TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(getTimeCalendar());
        timePickerFragment.setTargetFragment(this, 0);
        timePickerFragment.show(getFragmentManager(), "timePicker");
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && getTimeCalendar() != null) {
            int hours = progress/60;
            int minutes = progress - (hours * 60);
            getTimeCalendar().set(Calendar.HOUR_OF_DAY, hours);
            getTimeCalendar().set(Calendar.MINUTE, minutes);
            safeUpdate(getView(), false);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // No action.
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // No action.
    }

    private void nextDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, 1);
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, 1);
        safeUpdate(getView(), false);
    }

    private void nextMonth() {
        getDateCalendar().add(Calendar.MONTH, 1);
        getTimeCalendar().add(Calendar.MONTH, 1);
        safeUpdate(getView(), false);
    }

    private void prevDate() {
        getDateCalendar().add(Calendar.DAY_OF_MONTH, -1);
        getTimeCalendar().add(Calendar.DAY_OF_MONTH, -1);
        safeUpdate(getView(), false);
    }

    private void prevMonth() {
        getDateCalendar().add(Calendar.MONTH, -1);
        getTimeCalendar().add(Calendar.MONTH, -1);
        safeUpdate(getView(), false);
    }

    private void nextMinute() {
        if (getView() != null) {
            int dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR);
            getTimeCalendar().add(Calendar.MINUTE, 1);
            int minutes = (getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60) + getTimeCalendar().get(Calendar.MINUTE);
            ((SeekBar)getView().findViewById(id.timeSeeker)).setProgress(minutes);
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, 1);
                safeUpdate(getView(), false);
            } else {
                safeUpdate(getView(), true);
            }
        }
    }

    private void prevMinute() {
        if (getView() != null) {
            int dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR);
            getTimeCalendar().add(Calendar.MINUTE, -1);
            int minutes = (getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60) + getTimeCalendar().get(Calendar.MINUTE);
            ((SeekBar)getView().findViewById(id.timeSeeker)).setProgress(minutes);
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, -1);
                safeUpdate(getView(), false);
            } else {
                safeUpdate(getView(), true);
            }
        }
    }

    private void nextHour() {
        if (getView() != null) {
            int dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR);
            getTimeCalendar().add(Calendar.HOUR_OF_DAY, 1);
            int minutes = (getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60) + getTimeCalendar().get(Calendar.MINUTE);
            ((SeekBar)getView().findViewById(id.timeSeeker)).setProgress(minutes);
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, 1);
                safeUpdate(getView(), false);
            } else {
                safeUpdate(getView(), true);
            }
        }
    }

    private void prevHour() {
        if (getView() != null) {
            int dayOfYear = getTimeCalendar().get(Calendar.DAY_OF_YEAR);
            getTimeCalendar().add(Calendar.HOUR_OF_DAY, -1);
            int minutes = (getTimeCalendar().get(Calendar.HOUR_OF_DAY) * 60) + getTimeCalendar().get(Calendar.MINUTE);
            ((SeekBar)getView().findViewById(id.timeSeeker)).setProgress(minutes);
            if (getTimeCalendar().get(Calendar.DAY_OF_YEAR) != dayOfYear) {
                getDateCalendar().add(Calendar.DAY_OF_MONTH, -1);
                safeUpdate(getView(), false);
            } else {
                safeUpdate(getView(), true);
            }
        }
    }
    
}
