package com.example.andres.thirdypsinthrome.Dosages;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.example.andres.thirdypsinthrome.MyUtils;

import java.util.Calendar;

public class DateFragmentDialog extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    DatePickerDialog.OnDateSetListener mCallback;
    private DatePickerDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setCalendarViewShown(false);//These 2 don't actually work by themselves, as the app theme overrides them.
        dialog.getDatePicker().setSpinnersShown(true);
        //Set bounds to the choice
        dialog.getDatePicker().setMinDate(MyUtils.getTodayLong()*1000l);
        dialog.getDatePicker().setMaxDate(MyUtils.addDays(MyUtils.getTodayLong(), 30)*1000l);

        return dialog;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (DatePickerDialog.OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DatePickerDialog.OnDateSetListener");
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        //Send this info th activity
        mCallback.onDateSet(view, year, month, day);
    }
}
