package uk.co.sundroid.activity.data.fragments.dialogs.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import uk.co.sundroid.R;
import uk.co.sundroid.R.id;
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.SharedPrefsHelper;

import java.util.Arrays;
import java.util.List;

public class TrackerSettingsFragment extends DialogFragment {

    private OnViewPrefsChangedListener onViewPrefsChangedListener;

    public static TrackerSettingsFragment newInstance() {
        return new TrackerSettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onViewPrefsChangedListener = (OnViewPrefsChangedListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        Context context = getActivity().getApplicationContext();
        View view = View.inflate(context, R.layout.dialog_trackersettings, null);

        Spinner body = view.findViewById(id.trackerSettingBody);
        Body currentBody = SharedPrefsHelper.getSunTrackerBody(context);
        if (currentBody != null) {
            body.setSelection(currentBody.ordinal());
        } else {
            body.setSelection(Body.values().length);
        }

        CheckBox map = view.findViewById(id.trackerSettingMap);
        map.setChecked(SharedPrefsHelper.getSunTrackerMode(context).equals("map"));
        map.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                view.findViewById(id.trackerSettingCompassWrapper).setVisibility(View.GONE);
                view.findViewById(id.trackerSettingMapModeWrapper).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(id.trackerSettingMapModeWrapper).setVisibility(View.GONE);
                view.findViewById(id.trackerSettingCompassWrapper).setVisibility(View.VISIBLE);
            }
        });

        List<String> availableMapModes = Arrays.asList("Map", "Satellite", "Terrain", "Hybrid");
        Spinner mapMode = view.findViewById(id.trackerSettingMapMode);
        ArrayAdapter<String> mapModeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, availableMapModes);
        mapModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapMode.setAdapter(mapModeAdapter);

        String currentMapMode = SharedPrefsHelper.getSunTrackerMapMode(context);
        switch (currentMapMode) {
            case "normal":
                mapMode.setSelection(0);
                break;
            case "satellite":
                mapMode.setSelection(1);
                break;
            case "terrain":
                mapMode.setSelection(2);
                break;
            case "hybrid":
                mapMode.setSelection(3);
                break;
            default:
                mapMode.setSelection(0);
                break;
        }

        CheckBox compass = view.findViewById(id.trackerSettingCompass);
        compass.setChecked(SharedPrefsHelper.getSunTrackerCompass(context));

        CheckBox linearElevation = view.findViewById(id.trackerSettingLinearElevation);
        linearElevation.setChecked(SharedPrefsHelper.getSunTrackerLinearElevation(context));

        CheckBox hourMarkers = view.findViewById(id.trackerSettingHourMarkers);
        hourMarkers.setChecked(SharedPrefsHelper.getSunTrackerHourMarkers(context));

        CheckBox text = view.findViewById(id.trackerSettingText);
        text.setChecked(SharedPrefsHelper.getSunTrackerText(context));

        if (SharedPrefsHelper.getSunTrackerMode(context).equals("map")) {
            view.findViewById(id.trackerSettingCompassWrapper).setVisibility(View.GONE);
        } else {
            view.findViewById(id.trackerSettingMapModeWrapper).setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Settings");
        builder.setView(view);
        builder.setNegativeButton("Cancel", (d, i) -> dismiss());
        builder.setPositiveButton("OK", (d, i) -> {
            if (body.getSelectedItemPosition() >= Body.values().length) {
                SharedPrefsHelper.setSunTrackerBody(context, null);
            } else {
                SharedPrefsHelper.setSunTrackerBody(context, Body.values()[body.getSelectedItemPosition()]);
            }

            if (map.isChecked()) {
                SharedPrefsHelper.setSunTrackerMode(context, "map");
                if (mapMode.getSelectedItemPosition() == 0) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "normal");
                } else if (mapMode.getSelectedItemPosition() == 1) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "satellite");
                } else if (mapMode.getSelectedItemPosition() == 2) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "terrain");
                } else if (mapMode.getSelectedItemPosition() == 3) {
                    SharedPrefsHelper.setSunTrackerMapMode(context, "hybrid");
                }
            } else {
                SharedPrefsHelper.setSunTrackerMode(context, "radar");
            }
            SharedPrefsHelper.setSunTrackerCompass(context, compass.isChecked());
            SharedPrefsHelper.setSunTrackerLinearElevation(context, linearElevation.isChecked());
            SharedPrefsHelper.setSunTrackerHourMarkers(context, hourMarkers.isChecked());
            SharedPrefsHelper.setSunTrackerText(context, text.isChecked());
            onViewPrefsChangedListener.onViewPrefsUpdated();
            dismiss();
        });
        return builder.create();
    }

}
