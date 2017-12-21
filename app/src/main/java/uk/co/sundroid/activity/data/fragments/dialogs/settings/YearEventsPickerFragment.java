package uk.co.sundroid.activity.data.fragments.dialogs.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener;
import uk.co.sundroid.util.SharedPrefsHelper;

public class YearEventsPickerFragment extends DialogFragment implements OnClickListener, OnMultiChoiceClickListener {

    private final boolean[] currentEvents = new boolean[8];
    private OnViewPrefsChangedListener onViewPrefsChangedListener;

    public static YearEventsPickerFragment newInstance() {
        return new YearEventsPickerFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onViewPrefsChangedListener = (OnViewPrefsChangedListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);// TODO Redundant?

        currentEvents[0] = SharedPrefsHelper.getShowElement(getActivity(), "yearNewMoon", true);
        currentEvents[1] = SharedPrefsHelper.getShowElement(getActivity(), "yearFullMoon", true);
        currentEvents[2] = SharedPrefsHelper.getShowElement(getActivity(), "yearQuarterMoon", true);
        currentEvents[3] = SharedPrefsHelper.getShowElement(getActivity(), "yearSolstice", true);
        currentEvents[4] = SharedPrefsHelper.getShowElement(getActivity(), "yearEquinox", true);
        currentEvents[5] = SharedPrefsHelper.getShowElement(getActivity(), "yearLunarEclipse", true);
        currentEvents[6] = SharedPrefsHelper.getShowElement(getActivity(), "yearSolarEclipse", true);
        currentEvents[7] = SharedPrefsHelper.getShowElement(getActivity(), "yearEarthApsis", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMultiChoiceItems(
                new CharSequence[]{ "New moons", "Full moons", "Quarter moons", "Solstices", "Equinoxes", "Lunar eclipses", "Solar eclipses", "Earth aphelion and perihelion" },
                currentEvents,
                this);
        builder.setTitle("Select events");
        builder.setPositiveButton("OK", this);
        builder.setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id, boolean value) {
        currentEvents[id] = value;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE && onViewPrefsChangedListener != null) {
            SharedPrefsHelper.setShowElement(getActivity(), "yearNewMoon", currentEvents[0]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearFullMoon", currentEvents[1]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearQuarterMoon", currentEvents[2]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearSolstice", currentEvents[3]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearEquinox", currentEvents[4]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearLunarEclipse", currentEvents[5]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearSolarEclipse", currentEvents[6]);
            SharedPrefsHelper.setShowElement(getActivity(), "yearEarthApsis", currentEvents[7]);
            onViewPrefsChangedListener.onViewPrefsUpdated();
        }
        dismiss();
    }

}