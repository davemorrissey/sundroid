package uk.co.sundroid.util.location

import java.util.ArrayList
import java.util.Locale

import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.*
import uk.co.sundroid.util.time.TimeZoneResolver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat


object Geocoder {

    private val TAG = Geocoder::class.java.name

    fun search(search: String, context: Context): List<LocationDetails> {

        val results = ArrayList<LocationDetails>()
        try {
            val myLocation = android.location.Geocoder(context, Locale.getDefault())
            val myList = myLocation.getFromLocationName(search, 1)
            for (address in myList) {
                if ((address.locality.isNotEmpty() || address.featureName.isNotEmpty()) && address.countryName.isNotEmpty() && address.hasLatitude() && address.hasLongitude()) {
                    val locationDetails = LocationDetails(LatitudeLongitude(address.latitude, address.longitude))
                    var name = if (address.locality != null) address.locality else address.featureName
                    if (isNotEmpty(address.featureName) && address.featureName != name) {
                        name = address.featureName + ", " + name
                    }
                    locationDetails.country = address.countryCode
                    locationDetails.countryName = address.countryName
                    locationDetails.state = address.adminArea
                    locationDetails.name = name

                    setTimeZone(locationDetails, context)

                    results.add(locationDetails)
                }

            }
        } catch (e: Exception) {
            e(TAG, "Search failed: $e", e)
            throw RuntimeException("Search failed")
        }

        return results
    }

    fun getLocationDetails(location: LatitudeLongitude, context: Context): LocationDetails {

        val locationDetails = LocationDetails(location)

        if (SharedPrefsHelper.getReverseGeocode(context) && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val myLocation = android.location.Geocoder(context, Locale.getDefault())
                val myList = myLocation.getFromLocation(location.latitude.doubleValue, location.longitude.doubleValue, 1)
                for (address in myList) {
                    if (isNotEmpty(address.countryCode)) {
                        locationDetails.country = address.countryCode
                        locationDetails.name = address.locality
                        locationDetails.state = address.adminArea
                        if (isEmpty(address.locality)) {
                            if (isNotEmpty(address.subAdminArea)) {
                                locationDetails.name = address.subAdminArea
                            }
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                e(TAG, "Geocode failed: " + e.toString(), e)
            }

        }

        setTimeZone(locationDetails, context)

        return locationDetails

    }

    private fun setTimeZone(locationDetails: LocationDetails, context: Context) {

        val possibleTimeZones = TimeZoneResolver.getPossibleTimeZones(locationDetails.location, locationDetails.country, locationDetails.state)
        locationDetails.possibleTimeZones = possibleTimeZones
        if (possibleTimeZones.size == 1) {
            locationDetails.timeZone = possibleTimeZones[0]
        }

        val defaultZone = SharedPrefsHelper.getDefaultZone(context)
        val defaultZoneOverride = SharedPrefsHelper.getDefaultZoneOverride(context)

        if (locationDetails.timeZone == null || defaultZone != null && defaultZoneOverride) {
            locationDetails.timeZone = defaultZone
        }

    }

}
