package uk.co.sundroid.activity.data.fragments.dialogs.date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.getInstance;

public class TimePickerFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private int hour;
    private int minute;
    private TimePicker timePicker;

    @FunctionalInterface
    public interface OnTimeSelectedListener {
        void onTimeSet(int hour, int minute);
    }

    public static TimePickerFragment newInstance(Calendar calendar) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("h", calendar.get(HOUR_OF_DAY));
        args.putInt("m", calendar.get(MINUTE));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar today = getInstance();
        this.hour = getArguments().getInt("h", today.get(HOUR_OF_DAY));
        this.minute = getArguments().getInt("m", today.get(MINUTE));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true); // TODO may not be required
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), layout.dialog_timepicker, null);
        timePicker = view.findViewById(id.timePicker);
        timePicker.setIs24HourView(SharedPrefsHelper.INSTANCE.getClockType24(getActivity().getApplicationContext()));
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        builder.setView(view);
        builder.setTitle("Set time");

        builder.setPositiveButton("Set", this);
        builder.setNeutralButton("Now", this);
        builder.setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof OnTimeSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                Calendar today = Calendar.getInstance();
                ((OnTimeSelectedListener)target).onTimeSet(today.get(HOUR_OF_DAY), today.get(MINUTE));
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                ((OnTimeSelectedListener)target).onTimeSet(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            }
        }
        dismiss();
    }

}