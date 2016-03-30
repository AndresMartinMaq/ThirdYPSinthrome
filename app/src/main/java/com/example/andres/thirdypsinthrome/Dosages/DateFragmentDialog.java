package com.example.andres.thirdypsinthrome.Dosages;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.example.andres.thirdypsinthrome.R;

import java.util.Calendar;

public class DateFragmentDialog extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.DateSpinnerTheme, this, year, month, day);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setCalendarViewShown(false);//These 2 don't actually work by themselves, as the app theme overrides them.
        dialog.getDatePicker().setSpinnersShown(true);

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }
}
