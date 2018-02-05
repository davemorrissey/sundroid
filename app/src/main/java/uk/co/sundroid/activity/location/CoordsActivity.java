package uk.co.sundroid.activity.location;

import uk.co.sundroid.util.geo.Geocoder;
import uk.co.sundroid.R;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.location.LatitudeLongitude;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.log.LogWrapper;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class CoordsActivity extends AbstractLocationActivity implements OnClickListener {
	
	private static final String TAG = CoordsActivity.class.getSimpleName();
	
	public static final int DIALOG_LOOKINGUP = 101;
	
    @Override
	protected int getLayout() {
		return R.layout.loc_coords;
	}

	@Override
	protected String getViewTitle() {
		return "Enter coordinates";
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	LogWrapper.d(TAG, "onCreate()");
		
    	View coordsSubmit = findViewById(R.id.coordsSubmit);
    	coordsSubmit.setOnClickListener(this);
    	
    	EditText coordsField = findViewById(R.id.coordsField);
    	coordsField.setOnEditorActionListener(new CoordsActionListener());
    	coordsField.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable field) {
				try {
					parseLocation();
					show(R.id.coordsValid);
					hide(R.id.coordsInvalid);
				} catch (Exception e) {
					show(R.id.coordsInvalid);
					hide(R.id.coordsValid);
				}
			}
			public void beforeTextChanged(CharSequence string, int start, int count, int after) { }
			public void onTextChanged(CharSequence string, int start, int before, int count) { }
    	});
    }
    
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.coordsSubmit:
				startSubmit();
				return;
		}
        super.onClick(button);
	}
	
	private void startSubmit() {
		try {
			LatitudeLongitude location = parseLocation();
			startLookup(location);
		} catch (Exception e) {
			LogWrapper.e(TAG, "Parse error", e);
			Toast.makeText(getApplicationContext(), "Invalid coordinates. Please try again.", Toast.LENGTH_LONG).show();
		}
	}
	
	private LatitudeLongitude parseLocation() throws Exception {
		
		EditText coordsField = findViewById(R.id.coordsField);
		String coordsValue = coordsField.getText().toString().toUpperCase();
		
		LatitudeLongitude location;
		
		if (coordsValue.matches("[NS][0-9]+(\\.[0-9]+)? [WE][0-9]+(\\.[0-9]+)?")) {
			
			String lat = coordsValue.split(" ")[0];
			String lon = coordsValue.split(" ")[1];
			
			double latDbl = Double.parseDouble(lat.substring(1));
			double lonDbl = Double.parseDouble(lon.substring(1));
			if (lat.startsWith("S")) { latDbl = -latDbl; }
			if (lon.startsWith("W")) { lonDbl = -lonDbl; }
			
			location = new LatitudeLongitude(latDbl, lonDbl);
			
		} else if (coordsValue.matches("-?[0-9]+(\\.[0-9]+)? -?[0-9]+(\\.[0-9]+)?")) {
			
			String lat = coordsValue.split(" ")[0];
			String lon = coordsValue.split(" ")[1];
			double latDbl = Double.parseDouble(lat);
			double lonDbl = Double.parseDouble(lon);
			location = new LatitudeLongitude(latDbl, lonDbl);
			
		} else {
			
			location = new LatitudeLongitude(coordsValue);
			
		}
		
		return location;
		
	}

	private void startLookup(final LatitudeLongitude location) {
		LogWrapper.d(TAG, "startLookup()");
		
		showDialog(DIALOG_LOOKINGUP);
		final Activity activity = this;
		// FIXME AsyncTask
    	Thread thread = new Thread() {
    		@Override
    		public void run() {
    	    	final LocationDetails locationDetails = Geocoder.getLocationDetails(location, getApplicationContext());
    	    	dismissDialog(DIALOG_LOOKINGUP);
    			SharedPrefsHelper.saveSelectedLocation(activity, locationDetails);
    			if (locationDetails.getTimeZone() == null) {
    				Intent intent = new Intent(getApplicationContext(), TimeZonePickerActivity.class);
    				intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT);
    				startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
    			} else {
    				setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED);
    				finish();
    			}
    		}
    	};
    	thread.start();
    }
	
	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_LOOKINGUP) {
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Looking up location...");
			progressDialog.setMessage("Looking up location, please wait. This can be disabled in settings.");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			return progressDialog;
		}
		return super.onCreateDialog(id);	
	}
	
	private class CoordsActionListener implements OnEditorActionListener {
		public boolean onEditorAction(TextView view, int id, KeyEvent arg2) {
			if (id == EditorInfo.IME_ACTION_GO) {
				startSubmit();
				return true;
			}
			return false;
		}
	}
    
}