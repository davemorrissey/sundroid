package uk.co.sundroid;

import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.location.Geocoder;
import uk.co.sundroid.util.location.LatitudeLongitude;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.log.LogWrapper;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class Locater implements LocationListener {
	
	private static final String TAG = Locater.class.getName();
	private LocaterListener listener;
	private Context context;
	private LocationManager locationManager;
	private String provider;
	private boolean finished = false;
	private TimeoutThread timeoutThread;
	
	public enum LocationType {
		UNKNOWN, // Something is providing location but don't know what
		GPS,
		NETWORK,
		UNAVAILABLE;
	}
	
	public Locater(LocaterListener listener, Context context) {
		this.listener = listener;
		this.context = context;
	}
	
	public LocationType start() {
		return start(true);
	}

	public LocationType start(boolean allowLastKnown) {
		
        if (timeoutThread != null) {
        	timeoutThread.stop();
        	timeoutThread = null;
        }
		
		locationManager = (LocationManager)listener.getSystemService(Context.LOCATION_SERVICE);
		
		if (locationManager == null) {
			LogWrapper.i(TAG, "No location manager service");
			 return LocationType.UNAVAILABLE;
		}
		
		if (SharedPrefsHelper.INSTANCE.getLastKnownLocation(context) && allowLastKnown) {
			LocationType lastKnownType = startLastKnown();
			if (lastKnownType != LocationType.UNAVAILABLE) {
				return lastKnownType;
			}
		}
		
		boolean network = false;
		try {
			network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception e) {
			// Presumably no network provider
		}
		boolean gps = false;
		try {
			gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception e) {
			// Presumably no GPS provider
		}
		
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
       	criteria.setAccuracy(Criteria.ACCURACY_FINE);

        provider = locationManager.getBestProvider(criteria, true);
        
        if (provider == null) {
           	LogWrapper.i(TAG, "No provider matching criteria");
            return LocationType.UNAVAILABLE;
        }
        LogWrapper.d(TAG, "Best provider: " + provider);
       
        locationManager.requestLocationUpdates(
        		provider,
        		1000 * 60 * 10, 1000,
        		this,
        		listener.getMainLooper());
        
        // Best provider will always attempt to lookup using GPS, which can take ages.
        // If both devices are available, specifically listen for network location as well.
        // Whichever returns first wins.
        if (!provider.equals(LocationManager.NETWORK_PROVIDER) && network) {
        	try {
        		LogWrapper.d(TAG, "Attempting network lookup in addition to GPS");
	            locationManager.requestLocationUpdates(
	            		LocationManager.NETWORK_PROVIDER,
	            		1000 * 60 * 10, 1000,
	            		this,
	            		listener.getMainLooper());
        	} catch (Exception e) {
        		LogWrapper.d(TAG, "Couldn't register for network lookup");
        	}
        }
        
        LogWrapper.d(TAG, "GPS: " + gps + ", NETWORK: " + network + ", provider: " + provider);

        timeoutThread = new TimeoutThread();
        timeoutThread.start();
        
        if (gps && network) {
        	return LocationType.UNKNOWN;
        } else if (gps) {
        	return LocationType.GPS;
        } else if (network) {
        	return LocationType.NETWORK;
        } else {
        	// ??
        	return LocationType.UNKNOWN;
        }
        
	}
	
	public LocationType startLastKnown() {
		try {
			final Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (gpsLocation != null) {
				LogWrapper.d(TAG, "GPS last known location received: " + gpsLocation.getProvider() + ", " + gpsLocation.getLatitude() + " " + gpsLocation.getLongitude());
				Thread thread = new Thread() {
					@Override public void run() {
						onLocationChanged(gpsLocation);
					}
				};
				thread.start();
				return LocationType.GPS;
			}
		} catch (Exception e) { }
		try {
			final Location netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (netLocation != null) {
				LogWrapper.d(TAG, "Network last known location received: " + netLocation.getProvider() + ", " + netLocation.getLatitude() + " " + netLocation.getLongitude());
				Thread thread = new Thread() {
					@Override public void run() {
						onLocationChanged(netLocation);
					}
				};
				thread.start();
				return LocationType.NETWORK;
			}
		} catch (Exception e) { }
		return LocationType.UNAVAILABLE;
	}
	
	
	public void cancel() {
		if (!finished) {
			if (timeoutThread != null) {
				timeoutThread.stop();
				timeoutThread = null;
			}
			finished = true;
			locationManager.removeUpdates(this);
			LogWrapper.d(TAG, "Cancelled");
		}
	}


	public void onLocationChanged(Location coords) {
		if (coords != null) {
			cancel();
			LogWrapper.d(TAG, "Location received: " + coords.getProvider() + ", " + coords.getLatitude() + " " + coords.getLongitude());
			LatitudeLongitude location = new LatitudeLongitude(coords.getLatitude(), coords.getLongitude());
			LocationDetails locationDetails = Geocoder.INSTANCE.getLocationDetails(location, context);
			listener.locationReceived(locationDetails);
		}
	}


	public void onProviderDisabled(String provider) {
		if (!finished) {
			LogWrapper.d(TAG, "Provider disabled: " + provider);
			if (provider.equals(this.provider)) {
				listener.locationError();
			}
			cancel();
		}
	}
	
	public void onTimeout() {
		if (!finished) {
			cancel();
			listener.locationTimeout();
		}
		
	}


	public void onProviderEnabled(String provider) {
		if (!finished) {
			LogWrapper.d(TAG, "Provider enabled: " + provider);
		}
	}


	public void onStatusChanged(String provider, int status, Bundle extra) {
		if (!finished) {
			LogWrapper.d(TAG, "Provider state change: " + provider + ", " + status);
			if (provider.equals(this.provider) && status == LocationProvider.OUT_OF_SERVICE) {
				listener.locationError();
				cancel();
			}
		}
	}
	
	public class TimeoutThread implements Runnable {

		private volatile Thread thread;
		
		private final String TAG = TimeoutThread.class.getName();

		public void start() {
			thread = new Thread(this, "LocationTimeout");
			thread.start();
			LogWrapper.d(TAG, "Started.");
		}
		
		public void stop() {
			Thread copy = thread;
			thread = null;
			copy.interrupt();
			LogWrapper.d(TAG, "Stopped.");
		}

		public void run() {
			Thread thisThread = Thread.currentThread();
			try {
				Thread.sleep(1000 * SharedPrefsHelper.INSTANCE.getLocationTimeout(context));
				if (thread == thisThread) {
					onTimeout();
				}
			} catch (InterruptedException e) {
				LogWrapper.d(TAG, "Interrupted.");
				Thread.currentThread().interrupt();
			}
		}
		
	}
	
	
}
