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
import kotlinx.android.synthetic.main.loc_search.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.R.string.*
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.async.Async
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.location.Geocoder
import java.util.*
import uk.co.sundroid.util.view.SimpleAlertFragment.Companion.show as showAlert
import uk.co.sundroid.util.view.SimpleProgressFragment.Companion.close as closeProgress
import uk.co.sundroid.util.view.SimpleProgressFragment.Companion.show as showProgress


class LocationSearchFragment : AbstractFragment() {

    private var listAdapter: SearchResultAdapter? = null

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Search")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return when (container) {
            null -> null
            else -> inflater.inflate(R.layout.loc_search, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = SearchResultAdapter(ArrayList())
        searchList.adapter = listAdapter
        searchList.setOnItemClickListener { parent, _, position, _ -> run {
            onLocationSelected(parent.getItemAtPosition(position) as LocationDetails)
        }}
        searchSubmit.setOnClickListener { v ->
            val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            startSearch()
        }
        searchField.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) startSearch(); true }
    }

    private fun startSearch() {
        val searchValue = searchField.text.toString()
        if (searchValue.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_LONG).show()
            return
        }

        searchListWrapper.visibility = GONE
        showProgress(requireFragmentManager(), "Searching...")

        Async(
                inBackground = { Geocoder.search(searchValue, requireContext()) },
                onFail = { showAlert(requireContext(), requireFragmentManager(), loc_search_error_title, loc_search_error_msg) },
                onDone = { results -> run {
                    closeProgress(requireFragmentManager())
                    if (results.isEmpty()) {
                        showAlert(requireContext(), requireFragmentManager(), loc_search_none_title, loc_search_none_msg)
                    } else {
                        searchListWrapper.visibility = VISIBLE
                        searchNotes.visibility = GONE
                        searchNotes2.visibility = GONE
                        listAdapter?.clear()
                        listAdapter?.addAll(results)
                    }
                }}
        ).execute()
    }

    private inner class SearchResultAdapter(list: ArrayList<LocationDetails>) : ArrayAdapter<LocationDetails>(requireContext(), R.layout.loc_search_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: layoutInflater.inflate(R.layout.loc_search_row, parent, false)
            val item = getItem(position)
            if (item != null) {
                var extra = item.countryName
                if (isNotEmpty(item.state)) {
                    extra = "${item.state}, $extra"
                }
                text(row, R.id.searchLocName, item.name ?: "")
                text(row, R.id.searchLocExtra, extra ?: "")
            }
            return row
        }
    }

}