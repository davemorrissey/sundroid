package uk.co.sundroid.domain

import com.google.android.gms.maps.GoogleMap

enum class MapType(val displayName: String, val googleId: Int) {

    NORMAL ("Normal", GoogleMap.MAP_TYPE_NORMAL),
    SATELLITE ("Satellite", GoogleMap.MAP_TYPE_SATELLITE),
    TERRAIN ("Terrain", GoogleMap.MAP_TYPE_TERRAIN),
    HYBRID ("Hybrid", GoogleMap.MAP_TYPE_HYBRID);

    companion object {
        fun displayNames(): List<String> {
            return MapType.values().map({ it.displayName })
        }
    }

}