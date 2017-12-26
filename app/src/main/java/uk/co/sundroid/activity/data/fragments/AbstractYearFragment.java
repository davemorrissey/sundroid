package uk.co.sundroid.activity.data.fragments;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import uk.co.sundroid.R;
import uk.co.sundroid.R.drawable;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.data.fragments.dialogs.date.YearPickerFragment;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.StringUtilsKt;
import uk.co.sundroid.util.view.ButtonDragGestureDetector;
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener;
import uk.co.sundroid.util.LogWrapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractYearFragment extends AbstractDataFragment implements OnClickListener, OnTouchListener, YearPickerFragment.OnYearSelectedListener {
	
	private static final String TAG = AbstractYearFragment.class.getSimpleName();

    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    private GestureDetector yearDetector;

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
        LogWrapper.d(TAG, hashCode() + " initialise");
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
    public void onYearSet(int year) {
        getDateCalendar().set(Calendar.YEAR, year);
        getTimeCalendar().set(Calendar.YEAR, year);
        update();
    }

    @Override
    public void onClick(View button) {
        switch (button.getId()) {
            case id.yearPrev:
                prevYear();
                return;
            case id.yearNext:
                nextYear();
                return;
            case id.yearButton:
                showYearPicker();
                return;
            case id.zoneButton:
                startTimeZone();
                return;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == id.yearButton && yearDetector != null) {
            return yearDetector.onTouchEvent(event);
        }
        return false;
    }

    private void safeInit(View view) {
        LocationDetails location = getLocation();
        Calendar calendar = getDateCalendar();
        try {
            if (location != null && calendar != null && view != null && !isDetached()) {
                initGestures(view);
                updateYear(location, calendar, view);
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
                updateYear(location, calendar, view);
				update(location, calendar, view);
			}
		} catch (Exception e) {
			LogWrapper.e(TAG, "Failed to update data view", e);
		}
	}

    private void initGestures(View view) {
        if (yearDetector == null) {
            ButtonDragGestureDetectorListener yearListener = new ButtonDragGestureDetectorListener() {
                @Override public void onButtonDragUp() { prevYear(); }
                @Override public void onButtonDragDown() { nextYear(); }
                @Override public void onButtonDragLeft() { prevYear(); }
                @Override public void onButtonDragRight() { nextYear(); }
            };
            yearDetector = new GestureDetector(getApplicationContext(), new ButtonDragGestureDetector(yearListener, getApplicationContext()));
        }
        view.findViewById(id.yearPrev).setOnClickListener(this);
        view.findViewById(id.yearNext).setOnClickListener(this);
        view.findViewById(id.yearButton).setOnClickListener(this);
        view.findViewById(id.yearButton).setOnTouchListener(this);
        view.findViewById(id.zoneButton).setOnClickListener(this);
    }

    private void updateYear(LocationDetails location, Calendar calendar, View view) {
    	if (SharedPrefsHelper.getShowTimeZone(getApplicationContext())) {
    		showInView(view, R.id.zoneButton);
			TimeZone zone = location.getTimeZone().getZone();
			boolean zoneDST = zone.inDaylightTime(new Date(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)));
			String zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG);
			textInView(view, R.id.zoneName, zoneName);

			String zoneCities = location.getTimeZone().getOffset(calendar.getTimeInMillis() + (12 * 60 * 60 * 1000)); // Get day's main offset.
			if (StringUtilsKt.isNotEmpty(location.getTimeZone().getCities())) {
				zoneCities += " " + location.getTimeZone().getCities();
			}
			textInView(view, R.id.zoneCities, zoneCities);
    	} else {
    		removeInView(view, R.id.zoneButton);
    	}

		yearFormat.setTimeZone(calendar.getTimeZone());
		String year = yearFormat.format(new Date(calendar.getTimeInMillis()));
		showInView(view, R.id.year, year);

        if (calendar.get(Calendar.YEAR) <= 2000) {
            view.findViewById(id.yearPrev).setEnabled(false);
            imageInView(view, id.yearPrev, drawable.navigation_previous_item_disabled);
        } else {
            view.findViewById(id.yearPrev).setEnabled(true);
            imageInView(view, id.yearPrev, drawable.navigation_previous_item);
        }
        if (calendar.get(Calendar.YEAR) >= 2020) {
            view.findViewById(id.yearNext).setEnabled(false);
            imageInView(view, id.yearNext, drawable.navigation_next_item_disabled);
        } else {
            view.findViewById(id.yearNext).setEnabled(true);
            imageInView(view, id.yearNext, drawable.navigation_next_item);
        }
    }

    private void showYearPicker() {
        YearPickerFragment yearPickerFragment = YearPickerFragment.newInstance(getDateCalendar());
        yearPickerFragment.show(getFragmentManager(), "yearPicker");
    }

    private void nextYear() {
        if (getDateCalendar().get(Calendar.YEAR) < 2020) {
            getDateCalendar().add(Calendar.YEAR, 1);
            getTimeCalendar().add(Calendar.YEAR, 1);
            update();
        }
    }

    private void prevYear() {
        if (getDateCalendar().get(Calendar.YEAR) > 2000) {
            getDateCalendar().add(Calendar.YEAR, -1);
            getTimeCalendar().add(Calendar.YEAR, -1);
            update();
        }
    }

	protected abstract int getLayout();
	
	protected abstract void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception;

}
