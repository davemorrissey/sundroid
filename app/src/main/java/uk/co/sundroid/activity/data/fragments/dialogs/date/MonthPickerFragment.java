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

import static java.util.Calendar.*;

public class MonthPickerFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private int year;
    private int month;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;

    @FunctionalInterface
    public interface OnMonthSelectedListener {
        void onMonthSet(int year, int month);
    }

    public static MonthPickerFragment newInstance(Calendar calendar) {
        MonthPickerFragment fragment = new MonthPickerFragment();
        Bundle args = new Bundle();
        args.putInt("y", calendar.get(YEAR));
        args.putInt("m", calendar.get(MONTH));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar today = getInstance();
        this.year = getArguments().getInt("y", today.get(YEAR));
        this.month = getArguments().getInt("m", today.get(MONTH));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true); // TODO may not be required
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View content = View.inflate(getActivity(), layout.dialog_monthpicker, null);
        monthPicker = content.findViewById(id.monthPicker);
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setWrapSelectorWheel(true);
        monthPicker.setDisplayedValues(new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" });
        monthPicker.setValue(month);
        yearPicker = content.findViewById(id.yearPicker);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(2100);
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(year);

        builder.setView(content);
        builder.setTitle("Set month");
        builder.setPositiveButton("Set", this);
        builder.setNeutralButton("This month", this);
        builder.setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof OnMonthSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                Calendar today = getInstance();
                ((OnMonthSelectedListener)target).onMonthSet(today.get(YEAR), today.get(MONTH));
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                ((OnMonthSelectedListener)target).onMonthSet(yearPicker.getValue(), monthPicker.getValue());
            }
        }
        dismiss();
    }

}