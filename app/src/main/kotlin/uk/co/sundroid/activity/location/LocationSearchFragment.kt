package uk.co.sundroid.activity.location

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.R.string.*
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.LocSearchBinding
import uk.co.sundroid.databinding.LocSearchRowBinding
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.async.Async
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.location.Geocoder
import java.util.*
import uk.co.sundroid.util.view.SimpleAlertFragment.Companion.show as showAlert
import uk.co.sundroid.util.view.SimpleProgressFragment.Companion.close as closeProgress
import uk.co.sundroid.util.view.SimpleProgressFragment.Companion.show as showProgress


class LocationSearchFragment : AbstractFragment() {

    private lateinit var b: LocSearchBinding

    private var listAdapter: SearchResultAdapter? = null

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Search")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = LocSearchBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = SearchResultAdapter(ArrayList())
        b.searchList.adapter = listAdapter
        b.searchList.setOnItemClickListener { parent, _, position, _ -> run {
            onLocationSelected(parent.getItemAtPosition(position) as LocationDetails)
        }}
        b.searchSubmit.setOnClickListener { v ->
            val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            startSearch()
        }
        b.searchField.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) startSearch(); true }
    }

    private fun startSearch() {
        val searchValue = b.searchField.text.toString()
        if (searchValue.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_LONG).show()
            return
        }

        b.searchListWrapper.visibility = GONE
        showProgress(this, "Searching...")

        Async(
                inBackground = { Geocoder.search(searchValue, requireContext()) },
                onFail = { showAlert(requireContext(), parentFragmentManager, loc_search_error_title, loc_search_error_msg) },
                onDone = { results -> run {
                    closeProgress(this)
                    if (results.isEmpty()) {
                        showAlert(requireContext(), parentFragmentManager, loc_search_none_title, loc_search_none_msg)
                    } else {
                        b.searchListWrapper.visibility = VISIBLE
                        b.searchNotes.visibility = GONE
                        b.searchNotes2.visibility = GONE
                        listAdapter?.clear()
                        listAdapter?.addAll(results)
                    }
                }}
        ).execute()
    }

    private inner class SearchResultAdapter(list: ArrayList<LocationDetails>) : ArrayAdapter<LocationDetails>(requireContext(), R.layout.loc_search_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView?.let { LocSearchRowBinding.bind(convertView) } ?: run { LocSearchRowBinding.inflate(layoutInflater) }
            val item = getItem(position)
            if (item != null) {
                var extra = item.countryName
                if (isNotEmpty(item.state)) {
                    extra = "${item.state}, $extra"
                }
                row.searchLocName.text = item.name ?: ""
                row.searchLocExtra.text = extra ?: ""
            }
            return row.root
        }
    }

}