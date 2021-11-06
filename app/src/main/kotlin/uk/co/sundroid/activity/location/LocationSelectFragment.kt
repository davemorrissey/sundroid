package uk.co.sundroid.activity.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.activity.*
import uk.co.sundroid.activity.LocaterStatus.*
import uk.co.sundroid.databinding.LocOptionsBinding
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.view.SimpleAlertFragment
import uk.co.sundroid.util.view.SimpleProgressFragment

class LocationSelectFragment : AbstractFragment(), LocaterListener {

    private lateinit var b: LocOptionsBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = LocOptionsBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.locOptionMyLocation.setOnClickListener { if (checkPermission()) { startLocater() } }
        b.locOptionMap.setOnClickListener { setPage(Page.LOCATION_MAP) }
        b.locOptionSearch.setOnClickListener { setPage(Page.LOCATION_SEARCH) }
        b.locOptionSavedPlaces.setOnClickListener { setPage(Page.LOCATION_LIST) }
    }

    private fun startLocater() {
        locater?.cancel()
        locater = Locater(this, requireContext()).apply {
            val result = this.start()
            if (result == STARTED) {
                SimpleProgressFragment.show(this@LocationSelectFragment, "Finding your location...")
            } else {
                locationError(result)
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PERMISSION_DENIED) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Permission denied")
                    setMessage("Sundroid cannot get your location. To fix this, you can grant this app location permission from Android settings.")
                    setPositiveButton(android.R.string.ok, null)
                    setNeutralButton("Permissions") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }.show()
            } else if (grantResults[0] == PERMISSION_GRANTED) {
                startLocater()
            }
        }
    }

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            locater?.cancel()
        }
    }

    override fun getMainLooper(): Looper? {
        return activity?.mainLooper
    }

    override fun getSystemService(id: String): Any? {
        return activity?.getSystemService(id)
    }

    override fun locationError(status: LocaterStatus) {
        SimpleProgressFragment.close(this)
        when (status) {
            TIMEOUT -> {
                showAlert("Location timeout", "Couldn't find your location. Make sure you have a good signal or a clear view of the sky.")
            }
            DENIED -> {
                showAlert("Permission denied", "Sundroid cannot get your location. To fix this, you can grant this app location permission from Android settings.")
            }
            else -> {
                showAlert("Location lookup failed", "Location services are disabled. Enable location in your settings.")
            }
        }
    }

    override fun locationReceived(locationDetails: LocationDetails) {
        SimpleProgressFragment.close(this)
        onLocationSelected(locationDetails)
    }

    private fun showAlert(title: String, message: String) {
        handler.post {
            if (!isDetached && host != null) {
                SimpleAlertFragment.show(parentFragmentManager, title, message)
            }
        }
    }

    companion object {
        const val REQUEST_LOCATION = 1110
    }

}
