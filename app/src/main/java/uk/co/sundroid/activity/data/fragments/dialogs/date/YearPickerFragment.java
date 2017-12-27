package uk.co.sundroid.activity.data.fragments.dialogs.date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;

import java.util.Calendar;

import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

public class YearPickerFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private int year;
    private NumberPicker yearPicker;

    @FunctionalInterface
    public interface OnYearSelectedListener {
        void onYearSet(int year);
    }

    public static YearPickerFragment newInstance(Calendar calendar) {
        YearPickerFragment fragment = new YearPickerFragment();
        Bundle args = new Bundle();
        args.putInt("y", calendar.get(YEAR));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar today = getInstance();
        this.year = getArguments().getInt("y", today.get(YEAR));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true); // TODO May not be required
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View content = View.inflate(getActivity(), layout.dialog_yearpicker, null);
        yearPicker = content.findViewById(id.yearPicker);
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2020);
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(year);
        builder.setView(content);
        builder.setTitle("Set year");
        builder.setPositiveButton("Set", this);
        builder.setNeutralButton("This year", this);
        builder.setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof OnYearSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                Calendar today = getInstance();
                ((OnYearSelectedListener)target).onYearSet(today.get(YEAR));
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                ((OnYearSelectedListener)target).onYearSet(yearPicker.getValue());
            }
        }
        dismiss();
    }

}