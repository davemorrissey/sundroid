package uk.co.sundroid.activity.location

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.loc_options.*
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.Locater
import uk.co.sundroid.activity.LocaterListener
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.Page
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.view.SimpleAlertFragment
import uk.co.sundroid.util.view.SimpleProgressFragment

class LocationSelectFragment : AbstractFragment(), LocaterListener {

    private var locater: Locater? = null

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager
                .getInstance(requireContext())
                .registerReceiver(cancelReceiver, IntentFilter(SimpleProgressFragment.CANCELLED))
    }

    override fun onDestroy() {
        LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(cancelReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Change location")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return when (container) {
            null -> null
            else -> inflater.inflate(R.layout.loc_options, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locOptionMyLocation.setOnClickListener { startLocater() }
        locOptionMap.setOnClickListener { setPage(Page.LOCATION_MAP) }
        locOptionSearch.setOnClickListener { setPage(Page.LOCATION_SEARCH) }
        locOptionSavedPlaces.setOnClickListener { setPage(Page.LOCATION_LIST) }
    }

    private fun startLocater() {
        locater?.cancel()
        locater = Locater(this, requireContext()).apply {
            if (this.start()) {
                SimpleProgressFragment.show(requireFragmentManager(), "Finding your location...")
            } else {
                locationError()
            }
        }
    }

    private fun start(activity: Class<out Activity>) {
        requireFragmentManager().popBackStack() // FIXME needed while other screens are activities
        val intent = Intent(requireContext(), activity)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            locater?.cancel()
        }
    }

    override fun getMainLooper(): Looper {
        return requireActivity().mainLooper
    }

    override fun getSystemService(id: String): Any? {
        return requireActivity().getSystemService(id)
    }

    override fun locationError() {
        SimpleAlertFragment.show(
                requireFragmentManager(),
                "Location lookup failed",
                "Location services are disabled. Enable wireless networks or GPS in your location settings."
        )
    }

    override fun locationTimeout() {
        handler.post {
            if (!isDetached) {
                SimpleAlertFragment.show(
                        requireFragmentManager(),
                        "Location lookup timeout",
                        "Couldn't find your location. Make sure you have a good signal or a clear view of the sky."
                )
            }
        }
    }

    override fun locationReceived(locationDetails: LocationDetails) {
        SimpleProgressFragment.close(requireFragmentManager())
        onLocationSelected(locationDetails)
    }

    companion object {
        const val REQUEST_LOCATION = 1110
    }

}
