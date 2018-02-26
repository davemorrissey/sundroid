package uk.co.sundroid.activity.data.fragments;

import android.content.Intent;
import uk.co.sundroid.AbstractFragment;
import uk.co.sundroid.activity.data.DataActivity;
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener;
import uk.co.sundroid.activity.location.TimeZonePickerActivity;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.log.LogWrapper;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;

import java.util.Calendar;

/**
 * Parent class for fragments that show data.
 */
public abstract class AbstractDataFragment extends AbstractFragment implements OnViewPrefsChangedListener {

    private static final String TAG = AbstractDataFragment.class.getSimpleName();

    public abstract void initialise() throws Exception;

	public abstract void update() throws Exception;

    @Override
    public void onViewPrefsUpdated() {
        try {
            initialise();
            update();
        } catch (Exception e) {
            LogWrapper.e(TAG, "Initialise for settings change failed", e);
        }
    }

    protected void startTimeZone() {
        Intent intent = new Intent(getActivity(), TimeZonePickerActivity.class);
        intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_CHANGE);
        getActivity().startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
    }

    protected boolean isSafe() {
        return getActivity() != null && !isDetached() && getApplicationContext() != null;
    }

    protected LocationDetails getLocation() {
        return SharedPrefsHelper.INSTANCE.getSelectedLocation(getApplicationContext());
    }

    protected Calendar getDateCalendar() {
        if (getActivity() instanceof DataActivity) {
            return ((DataActivity)getActivity()).getDateCalendar();
        }
        return null;
    }

    protected Calendar getTimeCalendar() {
        if (getActivity() instanceof DataActivity) {
            return ((DataActivity)getActivity()).getTimeCalendar();
        }
        return null;
    }

}
