package uk.co.sundroid.util.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.TimeZoneResolver
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 7), AutoCloseable {

    val savedLocations: ArrayList<LocationDetails>
        get() {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM saved_locations ORDER BY name ASC", null)

            cursor.moveToFirst()
            val savedLocations = ArrayList<LocationDetails>()
            while (!cursor.isAfterLast) {
                val savedLocation = LocationDetails(LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)))
                savedLocation.id = cursor.getLong(0)
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
        db.execSQL("CREATE TABLE saved_locations (id INTEGER PRIMARY KEY AUTOINCREMENT, lat REAL, lon REAL, name TEXT, country TEXT, zoneId TEXT, zoneOffset TEXT, tstamp INTEGER);")
        db.execSQL("CREATE TABLE widget_locations (id INTEGER, lat REAL, lon REAL, name TEXT, country TEXT, zoneId TEXT, zoneOffset TEXT, tstamp INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE widget_locations (id INTEGER, lat REAL, lon REAL, name TEXT, country TEXT, zoneId TEXT, zoneOffset TEXT, tstamp INTEGER);")
        }
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
        val id = db.insert("saved_locations", NAME, cv)
        location.id = id
    }

    fun deleteSavedLocation(rowId: Long) {
        val db = readableDatabase
        db.delete("saved_locations", "id = ?", arrayOf(rowId.toString()))
    }

    fun setWidgetLocation(widgetId: Int, location: LocationDetails) {
        deleteWidgetLocation(widgetId)
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(ID, widgetId)
        cv.put(LAT, location.location.latitude.doubleValue)
        cv.put(LON, location.location.longitude.doubleValue)
        cv.put(NAME, location.name)
        cv.put(COUNTRY, location.country)
        cv.put(TIMEZONE_ID, location.timeZone!!.id)
        cv.put(TIMESTAMP, System.currentTimeMillis())
        db.insert("widget_locations", NAME, cv)
    }

    fun getWidgetLocation(widgetId: Int): LocationDetails? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM widget_locations WHERE id = $widgetId ORDER BY tstamp DESC", null)
        cursor.moveToFirst()
        var widgetLocation: LocationDetails? = null
        if (!cursor.isAfterLast) {
            widgetLocation = LocationDetails(LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)))
            widgetLocation.id = cursor.getInt(0).toLong()
            widgetLocation.name = cursor.getString(3)
            widgetLocation.country = cursor.getString(4)
            widgetLocation.timeZone = TimeZoneResolver.getTimeZone(cursor.getString(5))
        }
        cursor.close()
        return widgetLocation
    }

    fun deleteWidgetLocation(widgetId: Int) {
        val db = readableDatabase
        val args = arrayOf(widgetId.toString())
        db.delete("widget_locations", "id = ?", args)
    }

    companion object {
        private const val DATABASE_NAME = "sundroid"
        private const val ID = "id"
        private const val LAT = "lat"
        private const val LON = "lon"
        private const val NAME = "name"
        private const val COUNTRY = "country"
        private const val TIMEZONE_ID = "zoneId"
        private const val TIMESTAMP = "tstamp"
    }

}
