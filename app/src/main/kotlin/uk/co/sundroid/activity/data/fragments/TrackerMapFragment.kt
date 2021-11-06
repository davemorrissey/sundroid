package uk.co.sundroid.activity.data.fragments

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.Prefs

class TrackerMapFragment(location: LocationDetails, mapCenterListener: MapCenterListener) : SupportMapFragment() {

    private var location: LocationDetails? = location
    private var mapCenterListener: MapCenterListener? = mapCenterListener

    @FunctionalInterface
    interface MapCenterListener {
        fun setLocationPoint(point: Point)
    }

    init {
        retainInstance = true
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View {
        val view = super.onCreateView(layoutInflater, viewGroup, bundle)!!

        getMapAsync { map ->
            if (map != null) {
                map.setOnCameraMoveListener {
                    if (location != null && mapCenterListener != null) {
                        val point = map.projection.toScreenLocation(convertToGoogle(location!!))
                        mapCenterListener?.setLocationPoint(point)
                    }
                }
                val uiSettings = map.uiSettings
                uiSettings.isZoomControlsEnabled = true
                uiSettings.isCompassEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isRotateGesturesEnabled = false
                uiSettings.isTiltGesturesEnabled = false

                val mapType = Prefs.sunTrackerMapType(requireContext())
                map.mapType = mapType.googleId

                if (location != null) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(convertToGoogle(location!!), 6f)
                    map.moveCamera(cameraUpdate)
                }
            }
        }

        return view
    }

    private fun convertToGoogle(locationDetails: LocationDetails): LatLng {
        return LatLng(locationDetails.location.latitude.doubleValue, locationDetails.location.longitude.doubleValue)
    }

}
