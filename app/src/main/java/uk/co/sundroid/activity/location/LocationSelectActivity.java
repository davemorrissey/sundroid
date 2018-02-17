package uk.co.sundroid.activity.location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import uk.co.sundroid.activity.Locater;
import uk.co.sundroid.activity.LocaterListener;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.log.LogWrapper;

public class LocationSelectActivity extends AbstractLocationActivity implements LocaterListener, OnClickListener, DialogInterface.OnClickListener {
	
	private static final String TAG = LocationSelectActivity.class.getSimpleName();

	public static final int REQUEST_LOCATION = 1110;
	public static final int RESULT_LOCATION_SELECTED = 2220;
	public static final int RESULT_CANCELLED = 2223;

	public static final int DIALOG_LOCATING = 101;
	public static final int DIALOG_LOCATION_ERROR = 103;
	public static final int DIALOG_LOCATION_TIMEOUT = 105;
	private Locater locater;
	
	private Handler handler = new Handler();
	
	@Override
	protected String getViewTitle() {
		return "Change location";
	}

	@Override
	protected int getLayout() {
		return R.layout.loc_options;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		findViewById(id.locOptionMyLocation).setOnClickListener(this);
        findViewById(id.locOptionMap).setOnClickListener(this);
		findViewById(id.locOptionSearch).setOnClickListener(this);
		findViewById(id.locOptionSavedPlaces).setOnClickListener(this);
		findViewById(id.locOptionCoords).setOnClickListener(this);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    private void startMap() {
		Intent intent = new Intent(this, MapActivity.class);
		startActivityForResult(intent, REQUEST_LOCATION);
    }

	private void startSavedLocations() {
    	Intent intent = new Intent(this, SavedLocationsActivity.class);
    	startActivityForResult(intent, REQUEST_LOCATION);
    }
    
    private void startSearch() {
    	Intent intent = new Intent(this, SearchActivity.class);
    	startActivityForResult(intent, REQUEST_LOCATION);
    }
    
    private void startCoords() {
    	Intent intent = new Intent(this, CoordsActivity.class);
    	startActivityForResult(intent, REQUEST_LOCATION);
    }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DIALOG_LOCATING) {
			ProgressDialog progressDialog = (ProgressDialog)dialog;
			progressDialog.setMessage("Finding your location, please wait...");
			return;
		} else if (id == DIALOG_LOCATION_TIMEOUT) {
            AlertDialog alertDialog = (AlertDialog)dialog;
			alertDialog.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.");
			return;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_LOCATING) {
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Locating");
			progressDialog.setMessage("Finding your location, please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, id1) -> {
                if (locater != null) { locater.cancel(); }
                dismissDialog(DIALOG_LOCATING);
            });
			return progressDialog;
		} else if (id == DIALOG_LOCATION_TIMEOUT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Location lookup timeout");
			builder.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.");
			builder.setNeutralButton("OK", this);
			return builder.create();
		} else if (id == DIALOG_LOCATION_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Location lookup failed");
			builder.setMessage("Location services are disabled. Enable wireless networks or GPS in your location settings.");
			builder.setNeutralButton("OK", this);
			return builder.create();
		}
		return super.onCreateDialog(id);	
	}

	public void locationError() {
		dismissDialog(DIALOG_LOCATING);
		showDialog(DIALOG_LOCATION_ERROR);
	}
	
	public void locationTimeout() {
    	handler.post(
				() -> {
                    dismissDialog(DIALOG_LOCATING);
                    showDialog(DIALOG_LOCATION_TIMEOUT);
                });
	}

	public void locationReceived(LocationDetails locationDetails) {
		try {
			dismissDialog(DIALOG_LOCATING);
		} catch (Exception e) {
			// May not have been shown yet.
		}
		LogWrapper.d(TAG, "Location received: " + locationDetails);
		SharedPrefsHelper.INSTANCE.saveSelectedLocation(this, locationDetails);
		
		if (locationDetails.getTimeZone() == null) {
			Intent intent = new Intent(getApplicationContext(), TimeZonePickerActivity.class);
			intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT);
			startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
		} else {
			setResult(RESULT_LOCATION_SELECTED);
			finish();
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.locOptionMyLocation:
				if (locater != null) { locater.cancel(); }
				locater = new Locater(this, getApplicationContext());
				if (locater.start()) {
					showDialog(DIALOG_LOCATING);
				} else {
					showDialog(DIALOG_LOCATION_ERROR);
				}
				return;
            case id.locOptionMap:
                startMap();
                return;
			case R.id.locOptionSearch:
				startSearch();
				return;
			case R.id.locOptionSavedPlaces:
				startSavedLocations();
				return;
			case R.id.locOptionCoords:
				startCoords();
				return;
		}
		super.onClick(view);
	}
    
}
