package uk.co.sundroid.activity.location

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView

import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.R
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.view.MergeAdapter
import uk.co.sundroid.util.time.TimeZoneResolver

class TimeZonePickerActivity : AbstractActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = Prefs.selectedLocation(this)
        if (location == null) {
            setResult(RESULT_CANCELLED)
            finish()
            return
        }

        val mode = intent.getIntExtra(INTENT_MODE, MODE_SELECT)

        setContentView(R.layout.zone)
        if (mode == MODE_SELECT) {
            setActionBarTitle(location.displayName, "Select time zone")
        } else {
            setActionBarTitle(location.displayName, "Change time zone")
            setDisplayHomeAsUpEnabled()
        }

        val list = findViewById<ListView>(R.id.timeZoneList)
        val adapter = MergeAdapter()

        val possible = location.possibleTimeZones
        if (possible != null && possible.size > 0 && possible.size < 20) {
            adapter.addView(View.inflate(this, R.layout.zone_best_header, null))
            adapter.addAdapter(TimeZoneAdapter(possible.sorted()))
            adapter.addView(View.inflate(this, R.layout.zone_all_header, null))
        }

        adapter.addAdapter(TimeZoneAdapter(TimeZoneResolver.getAllTimeZones().sorted()))

        list.adapter = adapter
        list.setOnItemClickListener { parent, _, position, id ->
            d(TAG, "onItemClick($position, $id)")
            val timeZone = parent.getItemAtPosition(position) as TimeZoneDetail
            Prefs.saveSelectedLocationTimeZone(this, timeZone)
            setResult(RESULT_TIMEZONE_SELECTED)
            finish()
        }
    }

    override fun onNavBackSelected() {
        setResult(RESULT_CANCELLED)
        finish()
    }

    private inner class TimeZoneAdapter constructor(list: List<TimeZoneDetail>) : ArrayAdapter<TimeZoneDetail>(this@TimeZonePickerActivity, R.layout.zone_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: layoutInflater.inflate(R.layout.zone_row, parent, false)
            textInView(row, R.id.timeZoneRowOffset, getItem(position).getOffset(System.currentTimeMillis()))
            textInView(row, R.id.timeZoneRowCities, getItem(position).cities)
            return row
        }
    }

    companion object {
        private val TAG = TimeZonePickerActivity::class.java.simpleName

        const val REQUEST_TIMEZONE = 1111
        const val RESULT_TIMEZONE_SELECTED = 2221
        const val RESULT_CANCELLED = 2222

        const val INTENT_MODE = "mode"

        const val MODE_SELECT = 1
        const val MODE_CHANGE = 2
    }

}
