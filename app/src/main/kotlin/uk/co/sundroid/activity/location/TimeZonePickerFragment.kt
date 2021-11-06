package uk.co.sundroid.activity.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.ZoneAllHeaderBinding
import uk.co.sundroid.databinding.ZoneBestHeaderBinding
import uk.co.sundroid.databinding.ZoneBinding
import uk.co.sundroid.databinding.ZoneRowBinding
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.util.view.MergeAdapter


class TimeZonePickerFragment : AbstractFragment() {

    private lateinit var b: ZoneBinding

    override fun onResume() {
        super.onResume()
        val location = Prefs.selectedLocation(requireContext())
        (activity as MainActivity).apply {
            setToolbarTitle("Select time zone")
            setToolbarSubtitle(location?.displayName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = ZoneBinding.inflate(inflater)
        return b.root
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
            adapter.addView(ZoneBestHeaderBinding.inflate(layoutInflater).root)
            adapter.addAdapter(TimeZoneAdapter(possible.sorted()))
            adapter.addView(ZoneAllHeaderBinding.inflate(layoutInflater).root)
        }

        adapter.addAdapter(TimeZoneAdapter(TimeZoneResolver.getAllTimeZones().sorted()))

        b.timeZoneList.adapter = adapter
        b.timeZoneList.setOnItemClickListener { parent, _, position, _ ->
            val timeZone = parent.getItemAtPosition(position) as TimeZoneDetail
            Prefs.saveSelectedLocationTimeZone(requireContext(), timeZone)
            returnToData()
        }
    }

    private inner class TimeZoneAdapter constructor(list: List<TimeZoneDetail>) : ArrayAdapter<TimeZoneDetail>(requireContext(), R.layout.zone_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView?.let { ZoneRowBinding.bind(convertView) } ?: run { ZoneRowBinding.inflate(layoutInflater) }
            row.timeZoneRowOffset.text = getItem(position)!!.getOffset(System.currentTimeMillis())
            row.timeZoneRowCities.text = getItem(position)!!.cities ?: ""
            return row.root
        }
    }

}
