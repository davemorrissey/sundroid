package uk.co.sundroid.dao;

import java.util.ArrayList;

import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import uk.co.sundroid.util.time.TimeZoneResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "sundroid";
	
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String NAME = "name";
	private static final String COUNTRY = "country";
	private static final String TIMEZONE_ID = "zoneId";
	private static final String TIMESTAMP = "tstamp";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 6);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE saved_locations (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"lat REAL, lon REAL, name TEXT, country TEXT, zoneId TEXT, zoneOffset TEXT, tstamp INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	public void addSavedLocation(LocationDetails location) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(LAT, location.getLocation().getLatitude().getDoubleValue());
		cv.put(LON, location.getLocation().getLongitude().getDoubleValue());
		cv.put(NAME, location.getName());
		cv.put(COUNTRY, location.getCountry());
		cv.put(TIMEZONE_ID, location.getTimeZone().getId());
		cv.put(TIMESTAMP, System.currentTimeMillis());
		db.insert("saved_locations", NAME, cv);
	}

	public ArrayList<LocationDetails> getSavedLocations() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM saved_locations ORDER BY name ASC", null);
		
		cursor.moveToFirst();
		ArrayList<LocationDetails> savedLocations = new ArrayList<>();
		while (!cursor.isAfterLast()) {
			LocationDetails savedLocation = new LocationDetails();
			savedLocation.setId(cursor.getInt(0));
			savedLocation.setLocation(new LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)));
			savedLocation.setName(cursor.getString(3));
			savedLocation.setCountry(cursor.getString(4));
			savedLocation.setTimeZone(TimeZoneResolver.getTimeZone(cursor.getString(5), true));
			savedLocations.add(savedLocation);
			cursor.moveToNext();
		}
		cursor.close();
		return savedLocations;
	}

    public LocationDetails getSavedLocation(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, lat, lon, name, country, zoneId FROM saved_locations WHERE id = " + id, null);

        LocationDetails savedLocation = null;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            savedLocation = new LocationDetails();
            savedLocation.setId(cursor.getInt(0));
            savedLocation.setLocation(new LatitudeLongitude(cursor.getDouble(1), cursor.getDouble(2)));
            savedLocation.setName(cursor.getString(3));
            savedLocation.setCountry(cursor.getString(4));
            savedLocation.setTimeZone(TimeZoneResolver.getTimeZone(cursor.getString(5), true));
        }
        cursor.close();
        return savedLocation;
    }
	
	public void deleteSavedLocation(long rowId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] args = { String.valueOf(rowId) };
		db.delete("saved_locations", "id = ?", args);
	}
	
}
