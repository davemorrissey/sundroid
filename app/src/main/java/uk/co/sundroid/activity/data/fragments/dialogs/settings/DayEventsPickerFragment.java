package uk.co.sundroid.activity.data.fragments.dialogs.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;

public class DayEventsPickerFragment extends DialogFragment {

    private boolean[] currentEvents;
    private OnViewPrefsChangedListener onViewPrefsChangedListener;

    public static DayEventsPickerFragment newInstance(boolean[] currentEvents) {
        DayEventsPickerFragment fragment = new DayEventsPickerFragment();
        Bundle args = new Bundle();
        args.putBooleanArray("currentEvents", currentEvents);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentEvents = getArguments().getBooleanArray("currentEvents");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onViewPrefsChangedListener = (OnViewPrefsChangedListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMultiChoiceItems(new CharSequence[] { "Sun", "Moon", "Planets" }, currentEvents, (d, id, value) -> {
            if (currentEvents != null) {
                currentEvents[id] = value;
            }
        });
        builder.setTitle("Select events");
        builder.setPositiveButton("OK", (d, i) -> {
            Context context = getActivity().getApplicationContext();
            SharedPrefsHelper.INSTANCE.setShowElement(context, "evtByTimeSun", currentEvents[0]);
            SharedPrefsHelper.INSTANCE.setShowElement(context, "evtByTimeMoon", currentEvents[1]);
            SharedPrefsHelper.INSTANCE.setShowElement(context, "evtByTimePlanets", currentEvents[2]);
            onViewPrefsChangedListener.onViewPrefsUpdated();
            dismiss();
        });
        builder.setNegativeButton("Cancel", (d, i) -> dismiss());
        return builder.create();
    }

}