package uk.co.sundroid.activity.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import uk.co.sundroid.AbstractActivity;
import uk.co.sundroid.R;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.domain.TimeZoneDetail;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.LogWrapper;
import uk.co.sundroid.util.view.MergeAdapter;
import uk.co.sundroid.util.time.TimeZoneResolver;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class TimeZonePickerActivity extends AbstractActivity implements OnItemClickListener {
	
	private static final String TAG = TimeZonePickerActivity.class.getSimpleName();

	public static final int REQUEST_TIMEZONE = 1111;
	public static final int RESULT_TIMEZONE_SELECTED = 2221;
	public static final int RESULT_CANCELLED = 2222;


	public static final String INTENT_MODE = "mode";
	
	public static final int MODE_SELECT = 1;
	public static final int MODE_CHANGE = 2;
	
	private LocationDetails location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        location = SharedPrefsHelper.getSelectedLocation(this);
        int mode = getIntent().getIntExtra(INTENT_MODE, MODE_SELECT);

        setContentView(R.layout.zone);
        if (mode == MODE_SELECT) {
            setActionBarTitle(location.getDisplayName(), "Select time zone");
        } else {
            setActionBarTitle(location.getDisplayName(), "Change time zone");
            setDisplayHomeAsUpEnabled();
        }

    	ListView list = findViewById(R.id.timeZoneList);
    	MergeAdapter adapter = new MergeAdapter();
    	if (location.getPossibleTimeZones() != null && location.getPossibleTimeZones().size() > 0 && location.getPossibleTimeZones().size() < 20) {
			adapter.addView(View.inflate(this, R.layout.zone_best_header, null));
			Set<TimeZoneDetail> sortedTimeZones = new TreeSet<>(location.getPossibleTimeZones());
    		adapter.addAdapter(new TimeZoneAdapter(new ArrayList<>(sortedTimeZones)));
			adapter.addView(View.inflate(this, R.layout.zone_all_header, null));
    	}
    	
    	Set<TimeZoneDetail> sortedTimeZones = new TreeSet<>(TimeZoneResolver.Companion.getInstance().getAllTimeZones());
    	adapter.addAdapter(new TimeZoneAdapter(new ArrayList<>(sortedTimeZones)));
    	
    	list.setAdapter(adapter);
    	list.setOnItemClickListener(this);
    }

    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LogWrapper.d(TAG, "onItemClick(" + position + ", " + id + ")");
		
		// Update the passed location and update the current location.
		TimeZoneDetail timeZone = (TimeZoneDetail)parent.getItemAtPosition(position);
		location.setTimeZone(timeZone);
		SharedPrefsHelper.saveSelectedLocation(this, location);
		setResult(RESULT_TIMEZONE_SELECTED);
		finish();
	}

    @Override
    protected void onNavBackSelected() {
        setResult(RESULT_CANCELLED);
        finish();
    }

    private class TimeZoneAdapter extends ArrayAdapter<TimeZoneDetail> {
    	
    	private TimeZoneAdapter(ArrayList<TimeZoneDetail> list) {
    		super(TimeZonePickerActivity.this, R.layout.zone_row, list);
    	}
    	
    	@NonNull
		@Override
    	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.zone_row, parent, false);
			}
			TextView offset = row.findViewById(R.id.timeZoneRowOffset);
			offset.setText(getItem(position).getOffset(System.currentTimeMillis()));
			TextView cities = row.findViewById(R.id.timeZoneRowCities);
			cities.setText(getItem(position).getCities());
			return(row);
		}
    	
    }

}
