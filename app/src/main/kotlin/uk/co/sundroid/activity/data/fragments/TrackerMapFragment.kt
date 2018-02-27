package uk.co.sundroid.activity.data.fragments

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.prefs.SharedPrefsHelper

class TrackerMapFragment : MapFragment {

    private var location: LocationDetails? = null
    private var mapCenterListener: MapCenterListener? = null

    @FunctionalInterface
    interface MapCenterListener {

        fun setLocationPoint(point: Point)

    }


    constructor() {

    }

    constructor(location: LocationDetails, mapCenterListener: MapCenterListener) {
        retainInstance = true
        this.location = location
        this.mapCenterListener = mapCenterListener
    }

    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View? {
        val view = super.onCreateView(layoutInflater, viewGroup, bundle)

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

                val mapType = SharedPrefsHelper.getSunTrackerMapType(activity.applicationContext)
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
