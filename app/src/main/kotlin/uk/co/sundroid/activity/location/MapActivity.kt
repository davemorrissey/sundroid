package uk.co.sundroid.activity.location

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.R
import uk.co.sundroid.R.drawable
import uk.co.sundroid.R.id
import uk.co.sundroid.NavItem
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.Prefs

import uk.co.sundroid.NavItem.NavItemLocation.*
import uk.co.sundroid.domain.MapType

class MapActivity : AbstractLocationActivity(), OnMapClickListener, OnInfoWindowClickListener {

    private val handler = Handler()

    private var map: GoogleMap? = null
    private var mapCentre: LatLng? = null
    private var mapZoom: Float = 0.toFloat()
    private var mapMarker: Marker? = null
    private var mapLocation: LatitudeLongitude? = null
    private var mapLocationDetails: LocationDetails? = null

    override val layout: Int
        get() = R.layout.loc_map

    override val viewTitle: String
        get() = "Map"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        setNavItems(listOf(NavItem("Page settings", drawable.icn_bar_viewsettings, HEADER, 0)))
        setUpMapIfNeeded()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert).apply {
                    setTitle("Location required")
                    setMessage("Location permission is required to look up names and time zones. Proceed?")
                    setPositiveButton(android.R.string.yes) { _, _ -> requestLocationPermission() }
                    setNegativeButton(android.R.string.no, null)
                    setIcon(android.R.drawable.ic_dialog_info)
                }.show()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Show alert only if this is the first time the user has denied permission,
                // later calls to this method happen without interaction if they selected
                // "always deny".
                if (!Prefs.mapLocationPermissionDenied(this)) {
                    Prefs.setMapLocationPermissionDenied(this, true)
                    AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert).apply {
                        setTitle("Location denied")
                        setMessage("Location name and time zone lookup will be unavailable. To fix this, you can grant this app location permission from Android settings.")
                        setPositiveButton(android.R.string.ok, null)
                        setIcon(android.R.drawable.ic_dialog_info)
                    }.show()
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Unset denied flag so next time permission is denied, the alert is displayed again
                Prefs.setMapLocationPermissionDenied(this, false)
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

    override fun onRestoreInstanceState(state: Bundle) {
        restoreInstanceState(state)
        super.onRestoreInstanceState(state)
    }

    override fun onStop() {
        this.map = null
        this.mapMarker = null
        this.mapCentre = null
        this.mapLocation = null
        this.mapLocationDetails = null
        super.onStop()
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

    override fun onResume() {
        super.onResume()
        setUpMapIfNeeded()
    }

    private fun setUpMapIfNeeded() {
        if (map == null) {
            (supportFragmentManager.findFragmentById(id.map) as SupportMapFragment).getMapAsync { googleMap ->
                map = googleMap
                setUpMap(googleMap)
            }
        }
    }

    private fun setUpMap(map: GoogleMap) {

        val location = Prefs.selectedLocation(this)

        // Hide the zoom controls as the button panel will cover it.
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false

        map.setInfoWindowAdapter(CustomInfoWindowAdapter())

        map.setOnInfoWindowClickListener(this)
        map.setOnMapClickListener(this)
        map.clear()

        map.mapType = Prefs.locMapType(applicationContext).googleId

        if (mapCentre != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapCentre, mapZoom)
            map.moveCamera(cameraUpdate)
        } else if (location != null) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(location.location.latitude.doubleValue, location.location.longitude.doubleValue), 6f)
            map.moveCamera(cameraUpdate)
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
        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        map?.animateCamera(cameraUpdate)
    }

    override fun onInfoWindowClick(marker: Marker) {
        val mapLocationDetails = this.mapLocationDetails ?: return
        Prefs.saveSelectedLocation(this, mapLocationDetails)
        if (mapLocationDetails.timeZone == null) {
            val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
            intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_SELECT)
            startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
        } else {
            finish()
        }
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
        Thread { updateLocationDetails(Geocoder.getLocationDetails(location, applicationContext)) }.start()
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

        private val window: View = View.inflate(this@MapActivity, R.layout.loc_map_infowindow, null)
        private val contents: View = View.inflate(this@MapActivity, R.layout.loc_map_infowindow, null)

        override fun getInfoWindow(marker: Marker): View {
            render(window)
            return window
        }

        override fun getInfoContents(marker: Marker): View {
            render(contents)
            return contents
        }

        private fun render(view: View) {
            val title = view.findViewById<TextView>(id.title)
            val button = view.findViewById<View>(id.button)
            title.text = "Loading..."
            button.visibility = View.INVISIBLE
            mapLocationDetails?.let {
                title.text = it.displayName
                button.visibility = View.VISIBLE
            }
        }
    }

    override fun onNavItemSelected(itemPosition: Int) {
        AlertDialog.Builder(this).apply {
            setTitle("Map type")
            setItems(MapType.displayNames().toTypedArray()) { _, index -> Prefs.setLocMapType(applicationContext, MapType.values()[index]) }
            setNegativeButton("Cancel", null)
        }.show()
    }

    companion object {
        private const val REQUEST_LOCATION = 1234
    }

}