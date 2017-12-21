package uk.co.sundroid.activity.data.fragments.dialogs.date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import uk.co.sundroid.R.id;
import uk.co.sundroid.R.layout;

import java.util.Calendar;
import java.util.TimeZone;

import static java.util.Calendar.*;

public class DatePickerFragment extends DialogFragment implements DialogInterface.OnClickListener, OnDateChangedListener, OnSeekBarChangeListener {

    private Calendar calendar;
    private OnDateSelectedListener onDateSelectedListener;
    private DatePicker datePicker;
    private SeekBar dateSeeker;

    @FunctionalInterface
    public interface OnDateSelectedListener {
        void onDateSet(int year, int month, int date);
    }

    public static DatePickerFragment newInstance(Calendar calendar) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putIntArray("ymd", new int[] { calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH) });
        args.putString("tz", calendar.getTimeZone().getID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int[] ymd = getArguments().getIntArray("ymd");
        String tz = getArguments().getString("tz");
        calendar = Calendar.getInstance();
        if (ymd != null && tz != null) {
            calendar.setTimeZone(TimeZone.getTimeZone(tz));
            calendar.set(ymd[0], ymd[1], ymd[2]);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.onDateSelectedListener = (OnDateSelectedListener) getParentFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true); // TODO May not be required
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), layout.dialog_datepicker, null);
        datePicker = view.findViewById(id.datePicker);
        datePicker.init(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), this);
        builder.setView(view);
        builder.setTitle("Set date");

        dateSeeker = view.findViewById(id.dateSeeker);
        dateSeeker.setOnSeekBarChangeListener(this);
        dateSeeker.setMax(365);
        dateSeeker.setProgress(calendar.get(DAY_OF_YEAR));

        builder.setPositiveButton("Set", this);
        builder.setNeutralButton("Today", this);
        builder.setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
        if (dateSeeker != null && calendar != null) {
            Calendar cal = Calendar.getInstance(calendar.getTimeZone());
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            dateSeeker.setProgress(calendar.get(DAY_OF_YEAR));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int dayOfYear, boolean fromUser) {
        if (fromUser && datePicker != null && calendar != null) {
            dayOfYear = Math.max(1, dayOfYear);
            Calendar cal = Calendar.getInstance(calendar.getTimeZone());
            cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
            datePicker.updateDate(datePicker.getYear(), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (onDateSelectedListener == null) {
            return;
        }
        if (button == DialogInterface.BUTTON_NEUTRAL) {
            Calendar today = Calendar.getInstance();
            onDateSelectedListener.onDateSet(today.get(YEAR), today.get(MONTH), today.get(DAY_OF_MONTH));
        } else if (button == DialogInterface.BUTTON_POSITIVE) {
            onDateSelectedListener.onDateSet(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        }
        dismiss();
    }

}