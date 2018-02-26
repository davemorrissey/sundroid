package uk.co.sundroid.activity.location

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView.OnEditorActionListener

import uk.co.sundroid.util.*
import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*

import java.util.ArrayList

class SearchActivity : AbstractLocationActivity(), OnClickListener, OnItemClickListener, OnEditorActionListener {

    private var listAdapter: SearchResultAdapter? = null

    private val handler = Handler()

    override val viewTitle: String
        get() = "Search"

    override val layout: Int
        get() = R.layout.loc_search

    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAdapter = SearchResultAdapter(ArrayList())

        val list = findViewById<ListView>(R.id.searchList)
        list.adapter = listAdapter
        list.onItemClickListener = this

        val submit = findViewById<View>(R.id.searchSubmit)
        submit.setOnClickListener({ _ -> startSearch() })

        val searchField = findViewById<EditText>(R.id.searchField)
        searchField.setOnEditorActionListener(this)
    }

    private fun startSearch() {
        val searchField = findViewById<EditText>(R.id.searchField)
        val searchValue = searchField.text.toString()

        if (isEmpty(searchValue)) {
            Toast.makeText(applicationContext, "Please enter a search term", Toast.LENGTH_LONG).show()
            return
        }

        showDialog(DIALOG_SEARCHING)

        val thread = object : Thread() {
            override fun run() {
                try {
                    val results = Geocoder.search(searchValue, applicationContext)
                    if (results.isEmpty()) {
                        handler.post {
                            dismissDialog(DIALOG_SEARCHING)
                            showDialog(DIALOG_SEARCH_NONE)
                        }
                    } else {
                        handler.post {
                            remove(R.id.searchNotes, R.id.searchNotes2)
                            dismissDialog(DIALOG_SEARCHING)
                            listAdapter?.clear()
                            for (result in results) {
                                listAdapter?.add(result)
                            }
                        }

                    }
                } catch (e: Exception) {
                    e(TAG, "Search failed", e)
                    handler.post {
                        dismissDialog(DIALOG_SEARCHING)
                        showDialog(DIALOG_SEARCH_ERROR)
                    }
                }

            }
        }
        thread.start()

    }

    private inner class SearchResultAdapter constructor(list: ArrayList<LocationDetails>) : ArrayAdapter<LocationDetails>(this@SearchActivity, R.layout.loc_search_row, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: layoutInflater.inflate(R.layout.loc_search_row, parent, false)
            val item = getItem(position)
            if (item != null) {
                var extra = item.countryName
                if (isNotEmpty(item.state)) {
                    extra = item.state + ", " + extra
                }
                textInView(row, R.id.searchLocName, item.name)
                textInView(row, R.id.searchLocExtra, extra)
            }
            return row
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val locationDetails = parent.getItemAtPosition(position) as LocationDetails
        SharedPrefsHelper.saveSelectedLocation(this, locationDetails)
        if (locationDetails.timeZone == null) {
            val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
            intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
            startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
        } else {
            setResult(LocationSelectActivity.RESULT_LOCATION_SELECTED)
            finish()
        }
    }

    override fun onEditorAction(view: TextView, id: Int, arg2: KeyEvent): Boolean {
        if (id == EditorInfo.IME_ACTION_SEARCH) {
            startSearch()
            return true
        }
        return false
    }

    public override fun onCreateDialog(id: Int): Dialog {
        if (id == DIALOG_SEARCHING) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Searching...")
            progressDialog.setMessage("Searching, please wait.")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            return progressDialog
        } else if (id == DIALOG_SEARCH_ERROR) {
            return AlertDialog.Builder(this).apply {
                setTitle("Search failed")
                setMessage("There was a problem searching for matching locations. Please check your network signal and reboot your phone to make sure Google services are up to date.")
                setNeutralButton("OK", null)
            }.create()
        } else if (id == DIALOG_SEARCH_NONE) {
            return AlertDialog.Builder(this).apply {
                setTitle("No matches")
                setMessage("There were no locations matching your search. Please try another search term.")
                setNeutralButton("OK", null)
            }.create()
        }
        return super.onCreateDialog(id)
    }

    companion object {
        private val TAG = SearchActivity::class.java.simpleName
        const val DIALOG_SEARCHING = 101
        const val DIALOG_SEARCH_ERROR = 103
        const val DIALOG_SEARCH_NONE = 105
    }

}