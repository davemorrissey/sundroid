package uk.co.sundroid.activity.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.zone.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.util.view.MergeAdapter


class TimeZonePickerFragment : AbstractFragment() {

    override fun onResume() {
        super.onResume()
        val location = Prefs.selectedLocation(requireContext())
        (activity as MainActivity).apply {
            setToolbarTitle("Select time zone")
            setToolbarSubtitle(location?.displayName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return when (container) {
            null -> null
            else -> inflater.inflate(R.layout.zone, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val location = Prefs.selectedLocation(requireContext())
        if (location == null) {
            returnToData()
            return
        }

        val adapter = MergeAdapter()

        val possible = location.possibleTimeZones
        if (possible != null && possible.size > 0 && possible.size < 20) {
            adapter.addView(View.inflate(requireContext(), R.layout.zone_best_header, null))
            adapter.addAdapter(TimeZoneAdapter(possible.sorted()))
            adapter.addView(View.inflate(requireContext(), R.layout.zone_all_header, null))
        }

        adapter.addAdapter(TimeZoneAdapter(TimeZoneResolver.getAllTimeZones().sorted()))

        timeZoneList.adapter = adapter
        timeZoneList.setOnItemClickListener { parent, _, position, id ->
            d(TAG, "onItemClick($position, $id)")
            val timeZone = parent.getItemAtPosition(position) as TimeZoneDetail
            Prefs.saveSelectedLocationTimeZone(requireContext(), timeZone)
            returnToData()
        }
    }


    private inner class TimeZoneAdapter constructor(list: List<TimeZoneDetail>) : ArrayAdapter<TimeZoneDetail>(requireContext(), R.layout.zone_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: layoutInflater.inflate(R.layout.zone_row, parent, false)
            text(row, R.id.timeZoneRowOffset, getItem(position)!!.getOffset(System.currentTimeMillis()))
            text(row, R.id.timeZoneRowCities, getItem(position)!!.cities ?: "")
            return row
        }
    }

    companion object {
        private val TAG = TimeZonePickerFragment::class.java.simpleName

        const val REQUEST_TIMEZONE = 1111
    }

}
