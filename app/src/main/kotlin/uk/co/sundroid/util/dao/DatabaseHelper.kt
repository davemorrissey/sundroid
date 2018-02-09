package uk.co.sundroid.util.dao

import java.util.ArrayList

import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.TimeZoneResolver
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 6) {

    val savedLocations: ArrayList<LocationDetails>
        get() {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM saved_locations ORDER BY name ASC", null)

            cursor.moveToFirst()
            val savedLocations = ArrayList<LocationDetails>()
            while (!cursor.isAfterLast) {
                val savedLocation = LocationDetails(LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)))
                savedLocation.id = cursor.getInt(0)
                savedLocation.name = cursor.getString(3)
                savedLocation.country = cursor.getString(4)
                savedLocation.timeZone = TimeZoneResolver.getTimeZone(cursor.getString(5))
                savedLocations.add(savedLocation)
                cursor.moveToNext()
            }
            cursor.close()
            return savedLocations
        }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE saved_locations (id INTEGER PRIMARY KEY AUTOINCREMENT, " + "lat REAL, lon REAL, name TEXT, country TEXT, zoneId TEXT, zoneOffset TEXT, tstamp INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun addSavedLocation(location: LocationDetails) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(LAT, location.location.latitude.doubleValue)
        cv.put(LON, location.location.longitude.doubleValue)
        cv.put(NAME, location.name)
        cv.put(COUNTRY, location.country)
        cv.put(TIMEZONE_ID, location.timeZone!!.id)
        cv.put(TIMESTAMP, System.currentTimeMillis())
        db.insert("saved_locations", NAME, cv)
    }

    fun getSavedLocation(id: Int): LocationDetails? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM saved_locations WHERE id = " + id, null)

        var savedLocation: LocationDetails? = null
        cursor.moveToFirst()
        if (!cursor.isAfterLast) {
            savedLocation = LocationDetails(LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)))
            savedLocation.id = cursor.getInt(0)
            savedLocation.name = cursor.getString(3)
            savedLocation.country = cursor.getString(4)
            savedLocation.timeZone = TimeZoneResolver.getTimeZone(cursor.getString(5))
        }
        cursor.close()
        return savedLocation
    }

    fun deleteSavedLocation(rowId: Long) {
        val db = readableDatabase
        db.delete("saved_locations", "id = ?", arrayOf(rowId.toString()))
    }

    companion object {
        private val DATABASE_NAME = "sundroid"
        private val LAT = "lat"
        private val LON = "lon"
        private val NAME = "name"
        private val COUNTRY = "country"
        private val TIMEZONE_ID = "zoneId"
        private val TIMESTAMP = "tstamp"
    }

}
