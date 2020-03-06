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

}
