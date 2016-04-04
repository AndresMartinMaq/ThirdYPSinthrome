package com.example.andres.thirdypsinthrome.Dosages;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextDirectionHeuristic;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;

import java.text.ParseException;
//TODO small issue, the chosen inr values get set to the latest one on fragment recreation

public class EnterDoseFragment extends Fragment {

    private String selectedDateStr; //For restoring activity instance state.

    public EnterDoseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public long getSelectedStartDate() {
        if (selectedDateStr == null){
            selectedDateStr = MyUtils.getTodayStr();
        }
        try {
            return MyUtils.dateStrToEpochLong(selectedDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;//This in theory will never happen.
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_dose, container, false);

        //Set the Start Date and INR.
        EditText inrTxtF = (EditText) view.findViewById(R.id.txtF_new_INR);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            selectedDateStr = savedInstanceState.getString("selectedDate");
            updateUI(view, getSelectedStartDate());
            inrTxtF.setText(savedInstanceState.getString("newINRentered"));
        } else {
            //Default to today's date.
            selectedDateStr = MyUtils.getTodayStr();
            updateUI(view, MyUtils.getTodayLong());
            inrTxtF.setHint("INR at Start Date");
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("selectedDate", selectedDateStr);
        //savedInstanceState.putDoubleArray("weekOfIntakeValues", getWeekIntakeValues());
        savedInstanceState.putString("newINRentered", ((EditText) getView().findViewById(R.id.txtF_new_INR)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    //Checks whether all required fields have been filled.
    public boolean isAllFieldsFilled(){
        //TODO
        return true;
    }

    //The activity calls this method when the user selects a start date.
    public void onDateSelection(int year, int monthOfYear, int dayOfMonth){
        //Set the selected date field.
        long startDate = MyUtils.dateParamsToLong(year, monthOfYear, dayOfMonth);
        selectedDateStr = MyUtils.dateLongToStr(startDate);
        //Show in the UI.
        updateUI(getView(), startDate);
    }

    private void updateUI(View view, long selectedStartDate) {
        //Update button.
        ((Button) view.findViewById(R.id.bttn_edit_startdate)).setText(selectedDateStr);
        //Update list of days.
        int[] txtFieldIDs = {R.id.enter_dose_item1,
                R.id.enter_dose_item2, R.id.enter_dose_item3,
                R.id.enter_dose_item4, R.id.enter_dose_item5,
                R.id.enter_dose_item6, R.id.enter_dose_item7,};
        for (int i = 0; i < txtFieldIDs.length; i++) {
            View item = view.findViewById(txtFieldIDs[i]);
            String dateStr = MyUtils.dateLongToStr(MyUtils.addDays(selectedStartDate, i));
            ((TextView) item.findViewById(R.id.date)).setText(dateStr);
        }
    }

    //Get the values in textFields and return them as an array of doubles.
    public double[] getWeekIntakeValues(){
        int size = 7;
        double[] array = new double[size];
        int[] txtFieldIDs = {R.id.enter_dose_item1,
                R.id.enter_dose_item2, R.id.enter_dose_item3,
                R.id.enter_dose_item4, R.id.enter_dose_item5,
                R.id.enter_dose_item6, R.id.enter_dose_item7,};
        View view = getView();
        for (int i = 0; i < size ; i++) {
            String str = ((EditText)view.findViewById(txtFieldIDs[i]).findViewById(R.id.entered_day_intake)).getText().toString();
            try {
                double number = Double.parseDouble(str);
                if (number < 0) {throw new NumberFormatException("All intake values must be filled and non-negative."); }
                array[i] = number;
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return array;
    }
}


