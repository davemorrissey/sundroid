package uk.co.sundroid;

import uk.co.sundroid.domain.LocationDetails;
import android.os.Looper;

public interface LocaterListener {
	
	void locationError();
	
	void locationTimeout();
	
	void locationReceived(LocationDetails locationDetails);
	
	Object getSystemService(String id);
	
	Looper getMainLooper();

}
