package uk.co.sundroid.activity.location;

import uk.co.sundroid.AbstractActivity;
import android.content.Intent;
import android.os.Bundle;

public abstract class AbstractLocationActivity extends AbstractActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(getLayout());
        setActionBarTitle(getViewTitle());
        setDisplayHomeAsUpEnabled();
    }
    
    protected abstract String getViewTitle();
    
    protected abstract int getLayout();

    @Override
    protected void onNavBackSelected() {
        setResult(LocationSelectActivity.RESULT_CANCELLED);
        finish();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == LocationSelectActivity.RESULT_LOCATION_SELECTED || resultCode == TimeZonePickerActivity.RESULT_TIMEZONE_SELECTED) {
			setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED);
			finish();
		}
	}
    
}
