package uk.co.sundroid.activity.location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.geo.Geocoder;
import uk.co.sundroid.R;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.LogWrapper;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AbstractLocationActivity implements OnClickListener, OnItemClickListener {
	
	private static final String TAG = SearchActivity.class.getSimpleName();
	
	public static final int DIALOG_SEARCHING = 101;
	public static final int DIALOG_SEARCH_ERROR = 103;
	public static final int DIALOG_SEARCH_NONE = 105;
	
	private SearchResultAdapter listAdapter;
	
	private Handler handler = new Handler();
	
	@Override
	protected String getViewTitle() {
		return "Search";
	}

	@Override
	protected int getLayout() {
		return R.layout.loc_search;
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
		listAdapter = new SearchResultAdapter(new ArrayList<>());
		
		ListView list = findViewById(R.id.searchList);
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(this);
		
    	View submit = findViewById(R.id.searchSubmit);
    	submit.setOnClickListener(this);

    	EditText searchField = findViewById(R.id.searchField);
    	searchField.setOnEditorActionListener(new SearchActionListener());
    }
    
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.searchSubmit:
				startSearch();
				return;
		}
        super.onClick(button);
	}
	
	private void startSearch() {
		EditText searchField = findViewById(R.id.searchField);
		final String searchValue = searchField.getText().toString();
		
		if (StringUtils.isEmpty(searchValue)) {
			Toast.makeText(getApplicationContext(), "Please enter a search term", Toast.LENGTH_LONG).show();
			return;
		}
		
		showDialog(DIALOG_SEARCHING);
		
    	Thread thread = new Thread() {
    		@Override
    		public void run() {
    			try {
	    	    	final List<LocationDetails> results = Geocoder.search(searchValue, getApplicationContext());
	    	    	if (results.isEmpty()) {
	    	        	handler.post(() -> {
                                    dismissDialog(DIALOG_SEARCHING);
                                    showDialog(DIALOG_SEARCH_NONE);
                                });
	    	    	} else {
	    	        	handler.post(() -> {
                                    remove(R.id.searchNotes, R.id.searchNotes2);
                                    dismissDialog(DIALOG_SEARCHING);
                                       listAdapter.clear();
                                       for (LocationDetails result : results) {
                                           listAdapter.add(result);
                                       }
                                });

	    	    	}
    			} catch (Exception e) {
    				LogWrapper.e(TAG, "Search failed", e);
    	        	handler.post(() -> {
                                dismissDialog(DIALOG_SEARCHING);
                                showDialog(DIALOG_SEARCH_ERROR);
                            });
    			}
    		}
    	};
    	thread.start();
    	
    }

	private class SearchResultAdapter extends ArrayAdapter<LocationDetails> {
		
		private SearchResultAdapter(ArrayList<LocationDetails> list) {
			super(SearchActivity.this, R.layout.loc_search_row, list);
		}
		
		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.loc_search_row, parent, false);
			}
			LocationDetails item = getItem(position);
			if (item != null) {
				String extra = item.getCountryName();
				if (StringUtils.isNotEmpty(item.getState())) {
					extra = item.getState() + ", " + extra;
				}
				textInView(row, R.id.searchLocName, item.getName());
				textInView(row, R.id.searchLocExtra, extra);
			}
			return(row);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LocationDetails locationDetails = (LocationDetails)parent.getItemAtPosition(position);
		SharedPrefsHelper.saveSelectedLocation(this, locationDetails);
		if (locationDetails.getTimeZone() == null) {
			Intent intent = new Intent(getApplicationContext(), TimeZonePickerActivity.class);
			intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT);
			startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
		} else {
			setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED);
			finish();
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_SEARCHING) {
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Searching...");
			progressDialog.setMessage("Searching, please wait.");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			return progressDialog;
		} else if (id == DIALOG_SEARCH_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Search failed");
			builder.setMessage("There was a problem searching for matching locations. Please check your network signal and reboot your phone to make sure Google services are up to date.");
			builder.setNeutralButton("OK", (d, i) -> { });
			return builder.create();
		} else if (id == DIALOG_SEARCH_NONE) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No matches");
			builder.setMessage("There were no locations matching your search. Please try another search term.");
			builder.setNeutralButton("OK", (d, i) -> { });
			return builder.create();
		}
		return super.onCreateDialog(id);	
	}
	
	private class SearchActionListener implements OnEditorActionListener {
		public boolean onEditorAction(TextView view, int id, KeyEvent arg2) {
			if (id == EditorInfo.IME_ACTION_SEARCH) {
				startSearch();
				return true;
			}
			return false;
		}
	}
  
}