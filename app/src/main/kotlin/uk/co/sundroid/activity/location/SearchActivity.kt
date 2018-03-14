package uk.co.sundroid.activity.location

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemClickListener

import uk.co.sundroid.util.*
import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.Prefs

import java.util.ArrayList

import kotlinx.android.synthetic.main.loc_search.*
import uk.co.sundroid.R.string.*
import uk.co.sundroid.util.async.Async

class SearchActivity : AbstractLocationActivity(), OnItemClickListener {

    private var listAdapter: SearchResultAdapter? = null

    override val viewTitle: String
        get() = "Search"

    override val layout: Int
        get() = R.layout.loc_search

    override fun onResume() {
        super.onResume()
        listAdapter = SearchResultAdapter(ArrayList())
        searchList.adapter = listAdapter
        searchList.setOnItemClickListener { parent, _, position, _ -> run {
            val locationDetails = parent.getItemAtPosition(position) as LocationDetails
            Prefs.saveSelectedLocation(this, locationDetails)
            if (locationDetails.timeZone == null) {
                val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
                intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
                startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
            } else {
                setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
                finish()
            }
        }}
        searchSubmit.setOnClickListener { startSearch() }
        searchField.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) startSearch(); true }
    }

    private fun startSearch() {
        val searchValue = searchField.text.toString()
        if (searchValue.isEmpty()) {
            longToast("Please enter a search term")
            return
        }

        showDialog(DIALOG_SEARCHING)

        Async(
                inBackground = { Geocoder.search(searchValue, this@SearchActivity) },
                onFail = { alert(loc_search_error_title, loc_search_error_msg) },
                onDone = { results -> run {
                    dismissDialog(DIALOG_SEARCHING)
                    if (results.isEmpty()) {
                        alert(loc_search_none_title, loc_search_none_msg)
                    } else {
                        remove(searchNotes, searchNotes2)
                        listAdapter?.clear()
                        listAdapter?.addAll(results)
                    }
                }}
        ).execute()
    }

    private inner class SearchResultAdapter(list: ArrayList<LocationDetails>) : ArrayAdapter<LocationDetails>(this@SearchActivity, R.layout.loc_search_row, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: layoutInflater.inflate(R.layout.loc_search_row, parent, false)
            val item = getItem(position)
            if (item != null) {
                var extra = item.countryName
                if (isNotEmpty(item.state)) {
                    extra = "${item.state}, $extra"
                }
                text(row, R.id.searchLocName, item.name)
                text(row, R.id.searchLocExtra, extra)
            }
            return row
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val locationDetails = parent.getItemAtPosition(position) as LocationDetails
        Prefs.saveSelectedLocation(this, locationDetails)
        if (locationDetails.timeZone == null) {
            val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
            intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
            startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
        } else {
            setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
            finish()
        }
    }

    public override fun onCreateDialog(id: Int): Dialog {
        if (id == DIALOG_SEARCHING) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Searching...")
            progressDialog.setMessage("Searching, please wait.")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            return progressDialog
        }
        return super.onCreateDialog(id)
    }

    companion object {
        const val DIALOG_SEARCHING = 101
    }

}