package uk.co.sundroid.activity.data.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;

public class TrackerMapFragment extends MapFragment {

    @FunctionalInterface
    public interface MapCenterListener {

        void setLocationPoint(Point point);

    }

    private LocationDetails location;
    private MapCenterListener mapCenterListener;

    public TrackerMapFragment() {

    }

    public TrackerMapFragment(LocationDetails location, MapCenterListener mapCenterListener) {
        setRetainInstance(true);
        this.location = location;
        this.mapCenterListener = mapCenterListener;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);

        getMapAsync(map -> {
            if (map != null) {
                map.setOnCameraMoveListener(() -> {
                    if (location != null && mapCenterListener != null) {
                        Point point = map.getProjection().toScreenLocation(convertToGoogle(location));
                        mapCenterListener.setLocationPoint(point);
                    }
                });
                UiSettings uiSettings = map.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setTiltGesturesEnabled(false);

                String mapMode = SharedPrefsHelper.INSTANCE.getSunTrackerMapMode(getActivity().getApplicationContext());
                switch (mapMode) {
                    case "normal":
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case "satellite":
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case "terrain":
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case "hybrid":
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }

                if (location != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(convertToGoogle(location), 6);
                    map.moveCamera(cameraUpdate);
                }
            }
        });

        return view;
    }

    private LatLng convertToGoogle(LocationDetails locationDetails) {
        return new LatLng(locationDetails.getLocation().getLatitude().getDoubleValue(), locationDetails.getLocation().getLongitude().getDoubleValue());
    }

}
