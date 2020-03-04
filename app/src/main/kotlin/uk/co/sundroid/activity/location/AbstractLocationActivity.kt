package uk.co.sundroid.activity.location

import uk.co.sundroid.AbstractActivity
import android.content.Intent
import android.os.Bundle

abstract class AbstractLocationActivity : AbstractActivity() {

    protected abstract val viewTitle: String

    protected abstract val layout: Int

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        setActionBarTitle(viewTitle)
        setDisplayHomeAsUpEnabled()
    }

    override fun onNavBackSelected() {
        setResult(LocationSelectActivity.RESULT_CANCELLED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == LocationSelectActivity.RESULT_LOCATION_SELECTED || resultCode == TimeZonePickerActivity.RESULT_TIMEZONE_SELECTED) {
            setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
            finish()
        }
    }

}
