package uk.co.sundroid.activity.location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import uk.co.sundroid.R;
import uk.co.sundroid.R.attr;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.dao.DatabaseHelper;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.geometry.Accuracy;

import java.util.List;

public class SavedLocationsActivity extends AbstractLocationActivity implements OnClickListener, DialogInterface.OnClickListener {

	private DatabaseHelper db;

    private static final int DIALOG_DELETE = 204;
    private static final int TAG_ACTION = attr.sundroid_custom_1;
    private static final int TAG_LOCATION_ID = attr.sundroid_custom_2;
	private static final String ACTION_DELETE = "delete";
    private static final String ACTION_VIEW = "view";

    private long contextSavedLocationId = 0;

	@Override
	protected int getLayout() {
		return R.layout.loc_saved;
	}

	@Override
	protected String getViewTitle() {
		return "Saved locations";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new DatabaseHelper(this);
        populateSavedLocations();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
		}
	}

    @Override
    public void onClick(View view) {
        String savedLocationId = (String)view.getTag(TAG_LOCATION_ID);
        String action = (String)view.getTag(TAG_ACTION);
        if (savedLocationId != null && action != null && db != null) {
            LocationDetails locationDetails = db.getSavedLocation(Integer.parseInt(savedLocationId));
            if (action.equals(ACTION_VIEW)) {
                SharedPrefsHelper.saveSelectedLocation(this, locationDetails);
                if (locationDetails.getTimeZone() == null) {
                    Intent intent = new Intent(getApplicationContext(), TimeZonePickerActivity.class);
                    intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT);
                    startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
                } else {
                    setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED);
                    finish();
                }
            } else if (action.equals(ACTION_DELETE)) {
                confirmDelete(Integer.parseInt(savedLocationId));
            }
            return;
        }
        super.onClick(view);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            delete();
        }
        dialogInterface.dismiss();
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_DELETE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete");
            builder.setMessage("Delete this saved location?");
            builder.setPositiveButton("OK", this);
            builder.setNegativeButton("Cancel", this);
            return builder.create();
        }
        return super.onCreateDialog(id);
    }

    private void populateSavedLocations() {

        List<LocationDetails> locations = db.getSavedLocations();
        LinearLayout list = findViewById(id.savedLocationsList);
        list.removeAllViews();

        if (locations.size() > 0) {
            show(id.savedLocationsList);
            remove(id.savedLocationsNone);
        } else {
            remove(id.savedLocationsList);
            show(id.savedLocationsNone);
        }

        boolean first = true;
        for (final LocationDetails location : locations) {
            if (!first) {
                getLayoutInflater().inflate(layout.divider, list);
            }
            View row = View.inflate(this, layout.loc_saved_row, null);
            TextView name = row.findViewById(R.id.savedLocName);
            name.setText(location.getName());
            TextView coords = row.findViewById(R.id.savedLocCoords);
            coords.setText(location.getLocation().getPunctuatedValue(Accuracy.MINUTES));

            row.findViewById(id.savedLocText).setTag(TAG_LOCATION_ID, Integer.toString(location.getId()));
            row.findViewById(id.savedLocText).setTag(TAG_ACTION, ACTION_VIEW);
            row.findViewById(id.savedLocText).setOnClickListener(this);
            row.findViewById(id.savedLocDelete).setTag(TAG_LOCATION_ID, Integer.toString(location.getId()));
            row.findViewById(id.savedLocDelete).setTag(TAG_ACTION, ACTION_DELETE);
            row.findViewById(id.savedLocDelete).setOnClickListener(this);
            list.addView(row);

            first = false;
        }

    }

    private void confirmDelete(int savedLocationId) {
        contextSavedLocationId = savedLocationId;
        showDialog(DIALOG_DELETE);
    }

    private void delete() {
        if (contextSavedLocationId > 0 && db != null) {
            db.deleteSavedLocation(contextSavedLocationId);
            populateSavedLocations();
        }
    }

}
