package uk.co.sundroid.domain

import java.io.Serializable
import java.util.ArrayList

import uk.co.sundroid.util.geometry.Accuracy.*
import uk.co.sundroid.util.location.LatitudeLongitude

class LocationDetails(val location: LatitudeLongitude) : Serializable {

    var id: Int = 0

    var name: String? = null

    var country: String? = null

    var countryName: String? = null

    var state: String? = null

    var timeZone: TimeZoneDetail? = null

    var possibleTimeZones: ArrayList<TimeZoneDetail>? = null

    val displayName: String
        get() = name ?: location?.getPunctuatedValue(MINUTES) ?: "Unknown"

    override fun toString(): String {
        return "location=$location, name=$name, country=$country, state=$state, timeZone=$timeZone"
    }

    companion object {
        private const val serialVersionUID = -4706308086519494893L
    }

}
