package uk.co.sundroid.activity.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory.newLatLng
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R.drawable
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.LocMapBinding
import uk.co.sundroid.databinding.LocMapInfowindowBinding
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.Prefs

class LocationMapFragment : AbstractFragment(), OnMapClickListener, OnInfoWindowClickListener {

    private val handler = Handler()

    private var map: GoogleMap? = null
    private var mapCentre: LatLng? = null
    private var mapZoom: Float = 0.toFloat()
    private var mapMarker: Marker? = null
    private var mapLocation: LatitudeLongitude? = null
    private var mapLocationDetails: LocationDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Permission required")
                    setMessage("Sundroid needs permission to look up location names and time zones. Proceed?")
                    setPositiveButton(android.R.string.yes) { _, _ -> requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION) }
                    setNegativeButton(android.R.string.no, null)
                    setIcon(android.R.drawable.ic_dialog_info)
                }.show()
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Map")
            setToolbarSubtitle(null)
            setViewConfigurationCallback({ openSettingsDialog() })
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).setViewConfigurationCallback(null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return LocMapBinding.inflate(inflater).root
    }

    override fun onAttachFragment(child: Fragment) {
        super.onAttachFragment(child)
        (child as? SupportMapFragment)?.getMapAsync {
            map = it
            setUpMap(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PERMISSION_DENIED) {
                // Show alert only if this is the first time the user has denied permission,
                // later calls to this method happen without interaction if they selected
                // "always deny".
                if (!Prefs.mapLocationPermissionDenied(requireContext())) {
                    Prefs.setMapLocationPermissionDenied(requireContext(), true)
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Permission denied")
                        setMessage("Location name and time zone lookup will be unavailable. To fix this, you can grant this app location permission from Android settings.")
                        setPositiveButton(android.R.string.ok, null)
                        setNeutralButton("Permissions") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }.show()
                }
            } else if (grantResults[0] == PERMISSION_GRANTED) {
                // Unset denied flag so next time permission is denied, the alert is displayed again
                Prefs.setMapLocationPermissionDenied(requireContext(), false)
            }
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        map?.let {
            state.putDouble("mapCentreLat", it.cameraPosition.target.latitude)
            state.putDouble("mapCentreLon", it.cameraPosition.target.longitude)
            state.putFloat("mapZoom", it.cameraPosition.zoom)
            state.putSerializable("mapLocation", mapLocation)
            state.putSerializable("mapLocationDetails", mapLocationDetails)
        }
        super.onSaveInstanceState(state)
    }

    private fun restoreInstanceState(state: Bundle?) {
        if (state != null) {
            try {
                mapCentre = LatLng(state.getDouble("mapCentreLat"), state.getDouble("mapCentreLon"))
                mapZoom = state.getFloat("mapZoom")
                mapLocation = state.getSerializable("mapLocation") as LatitudeLongitude
                mapLocationDetails = state.getSerializable("mapLocationDetails") as LocationDetails
            } catch (e: Exception) {
                // Default map
            }
        }
    }

    private fun setUpMap(map: GoogleMap) {
        val location = Prefs.selectedLocation(requireContext())

        // Hide the zoom controls as the button panel will cover it.
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = false
            isMyLocationButtonEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }
        map.apply {
            mapType = Prefs.locMapType(requireContext()).googleId
            setInfoWindowAdapter(CustomInfoWindowAdapter())
            setOnInfoWindowClickListener(this@LocationMapFragment)
            setOnMapClickListener(this@LocationMapFragment)
            clear()

            mapCentre?.let {
                moveCamera(newLatLngZoom(it, mapZoom))
            } ?: location?.let {
                moveCamera(newLatLngZoom(location.location.toLatLng(), 6f))
            }
        }

        mapLocation?.let {
            addMarker(it)
        }
    }

    override fun onMapClick(latLng: LatLng) {
        val location = LatitudeLongitude(latLng.latitude, latLng.longitude)
        mapLocation = location
        mapLocationDetails = null
        addMarker(location)
        startPointLookup(location)
        map?.animateCamera(newLatLng(latLng))
    }

    override fun onInfoWindowClick(marker: Marker) {
        val mapLocationDetails = this.mapLocationDetails ?: return
        onLocationSelected(mapLocationDetails)
    }

    private fun addMarker(mapLocation: LatitudeLongitude) {
        map?.clear()
        mapMarker = map?.addMarker(MarkerOptions()
                .position(LatLng(mapLocation.latitude.doubleValue, mapLocation.longitude.doubleValue))
                .title("Fetching location")
                .icon(BitmapDescriptorFactory.fromResource(drawable.pixel)))
        mapMarker?.showInfoWindow()
    }

    private fun startPointLookup(location: LatitudeLongitude) {
        Thread { updateLocationDetails(Geocoder.getLocationDetails(location, requireContext())) }.start()
    }

    private fun updateLocationDetails(locationDetails: LocationDetails?) {
        // Details may be null after rotate.
        if (locationDetails == null) {
            return
        }
        // Data may be received after a new point has been tapped. Discard if so.
        if (mapLocation?.getAbbreviatedValue() != locationDetails.location.getAbbreviatedValue()) {
            return
        }
        mapLocationDetails = locationDetails
        if (mapMarker != null) {
            handler.post { mapMarker?.showInfoWindow() }
        }
    }

    internal inner class CustomInfoWindowAdapter : InfoWindowAdapter {

        private val window = LocMapInfowindowBinding.inflate(layoutInflater)
        private val contents = LocMapInfowindowBinding.inflate(layoutInflater)

        override fun getInfoWindow(marker: Marker): View {
            render(window)
            return window.root
        }

        override fun getInfoContents(marker: Marker): View {
            render(contents)
            return contents.root
        }

        private fun render(b: LocMapInfowindowBinding) {
            b.title.text = "Loading..."
            b.button.visibility = View.INVISIBLE
            mapLocationDetails?.let {
                b.title.text = it.displayName
                b.button.visibility = View.VISIBLE
            }
        }
    }

    private fun openSettingsDialog() {
        val names = MapType.displayNames()
        val selectedIndex = names.indexOf(Prefs.locMapType(requireContext()).displayName)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Map view")
        builder.setSingleChoiceItems(names.toTypedArray(), selectedIndex, null)
        builder.setNegativeButton("Cancel") { _, _ -> run { } }
        builder.setPositiveButton("OK") { dialog, _ ->
            if (dialog is AlertDialog) {
                val selectedItem = dialog.listView.checkedItemPosition
                val mapType = MapType.values()[selectedItem]
                Prefs.setLocMapType(requireContext(), MapType.values()[selectedItem])
                map?.mapType = mapType.googleId
            }
        }
        builder.show()
    }

    companion object {
        private const val REQUEST_LOCATION = 1234
    }

}