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
import uk.co.sundroid.Locater;
import uk.co.sundroid.Locater.LocationType;
import uk.co.sundroid.LocaterListener;
import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.LogWrapper;

public class LocationSelectActivity extends AbstractLocationActivity implements LocaterListener, OnClickListener, DialogInterface.OnClickListener {
	
	private static final String TAG = LocationSelectActivity.class.getSimpleName();

	public static final int REQUEST_LOCATION = 1110;
	public static final int RESULT_LOCATION_SELECTED = 2220;
	public static final int RESULT_CANCELLED = 2223;

	public static final int DIALOG_LOCATING = 101;
	public static final int DIALOG_LOCATION_ERROR = 103;
	public static final int DIALOG_LOCATION_TIMEOUT = 105;
	private LocationType locationType;
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
			if (locationType == LocationType.NETWORK) {
				progressDialog.setMessage("GPS is unavailable, using wireless networks.");
			} else if (locationType == LocationType.GPS) {
				progressDialog.setMessage("Wireless networks location is unavailable, using GPS. This may take a few moments, and won't work indoors.");
			} else {
				progressDialog.setMessage("Finding location from wireless networks or GPS.");
			}
			return;
		} else if (id == DIALOG_LOCATION_TIMEOUT) {
            AlertDialog alertDialog = (AlertDialog)dialog;
			if (locationType == LocationType.NETWORK) {
				alertDialog.setMessage("Couldn't find network location. Enable GPS or wifi, or make sure you have a good signal.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			} else if (locationType == LocationType.GPS) {
				alertDialog.setMessage("Couldn't find GPS location. Enable network location, or make sure you have a clear view of the sky.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			} else {
				alertDialog.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			}
			return;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_LOCATING) {
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Locating...");
			if (locationType == LocationType.NETWORK) {
				progressDialog.setMessage("GPS is unavailable, using wireless networks.");
			} else if (locationType == LocationType.GPS) {
				progressDialog.setMessage("Wireless networks location is unavailable, using GPS. This may take a few moments, and won't work indoors.");
			} else {
				progressDialog.setMessage("Finding location from wireless networks or GPS.");
			}
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
			if (locationType == LocationType.NETWORK) {
				builder.setMessage("Couldn't find network location. Enable GPS or wifi, or make sure you have a good signal.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			} else if (locationType == LocationType.GPS) {
				builder.setMessage("Couldn't find GPS location. Enable network location, or make sure you have a clear view of the sky.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			} else {
				builder.setMessage("Couldn't find your location. Make sure you have a good signal or a clear view of the sky.\n\nIf this problem continues, please try rebooting your phone to make sure your Google services are up to date.");
			}
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
		SharedPrefsHelper.saveSelectedLocation(this, locationDetails);
		
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
				locationType = locater.start();
				if (locationType != LocationType.UNAVAILABLE) {
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
