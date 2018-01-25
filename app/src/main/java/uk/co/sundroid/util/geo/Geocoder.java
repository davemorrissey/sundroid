package uk.co.sundroid.util.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.domain.TimeZoneDetail;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.LogWrapper;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.location.LatitudeLongitude;
import uk.co.sundroid.util.time.TimeZoneResolver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.support.v4.content.ContextCompat;


public class Geocoder {
	
	private static final String TAG = Geocoder.class.getName();

	public static List<LocationDetails> search(String search, Context context) {
		
		List<LocationDetails> results = new ArrayList<>();
		try {
			android.location.Geocoder myLocation = new android.location.Geocoder(context, Locale.getDefault());
			List<Address> myList = myLocation.getFromLocationName(search, 1);
			for (Address address : myList) {
				if ((address.getLocality() != null || address.getFeatureName() != null) && address.getCountryName() != null && address.hasLatitude() && address.hasLongitude()) {
					LocationDetails locationDetails = new LocationDetails();
					String name = address.getLocality() != null ? address.getLocality() : address.getFeatureName();
					if (StringUtils.isNotEmpty(address.getFeatureName()) && !address.getFeatureName().equals(name)) {
						name = address.getFeatureName() + ", " + name;
					}
					locationDetails.setCountry(address.getCountryCode());
					locationDetails.setCountryName(address.getCountryName());
					locationDetails.setState(address.getAdminArea());
					locationDetails.setName(name);
					locationDetails.setLocation(new LatitudeLongitude(address.getLatitude(), address.getLongitude()));
					
					setTimeZone(locationDetails, context);
					
					results.add(locationDetails);
				}
				
			}
		} catch (Exception e) {
			LogWrapper.e(TAG, "Search failed: " + e.toString(), e);
			throw new RuntimeException("Search failed");
		}
		
		return results;
		
	}
	
	public static LocationDetails getLocationDetails(LatitudeLongitude location, Context context) {
		
		LocationDetails locationDetails = new LocationDetails();
		locationDetails.setLocation(location);
		
		if (SharedPrefsHelper.getReverseGeocode(context) && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			try {
				android.location.Geocoder myLocation = new android.location.Geocoder(context, Locale.getDefault());
				List<Address> myList = myLocation.getFromLocation(location.getLatitude().getDoubleValue(), location.getLongitude().getDoubleValue(), 1);
				for (Address address : myList) {
					if (StringUtils.isNotEmpty(address.getCountryCode())) {
						locationDetails.setCountry(address.getCountryCode());
						locationDetails.setName(address.getLocality());
						locationDetails.setState(address.getAdminArea());
						if (StringUtils.isEmpty(address.getLocality())) {
							if (StringUtils.isNotEmpty(address.getSubAdminArea())) {
								locationDetails.setName(address.getSubAdminArea());
							}
						}
						break;
					}
				}
				LogWrapper.d(TAG, "Country code: " + locationDetails.getCountry());
			} catch (Exception e) {
				LogWrapper.e(TAG, "Geocode failed: " + e.toString(), e);
			}
			
		}
		
		setTimeZone(locationDetails, context);
		
		return locationDetails;
		
	}
	
	private static void setTimeZone(LocationDetails locationDetails, Context context) {
		
		ArrayList<TimeZoneDetail> possibleTimeZones = TimeZoneResolver.getPossibleTimeZones(locationDetails.getLocation(), locationDetails.getCountry(), locationDetails.getState());
		locationDetails.setPossibleTimeZones(possibleTimeZones);
		if (possibleTimeZones.size() == 1) {
			locationDetails.setTimeZone(possibleTimeZones.get(0));
		}
		
		TimeZoneDetail defaultZone = SharedPrefsHelper.getDefaultZone(context);
		boolean defaultZoneOverride = SharedPrefsHelper.getDefaultZoneOverride(context);

		if (locationDetails.getTimeZone() == null || (defaultZone != null && defaultZoneOverride)) {
			locationDetails.setTimeZone(defaultZone);
		}
		
	}

}
