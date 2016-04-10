package com.example.andres.thirdypsinthrome.Dosages;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
//TODO small issue, the chosen inr values get set to the latest one on fragment recreation

public class EnterDoseFragment extends Fragment {

    private long selectedDate = -1; //For restoring activity instance state.

    public EnterDoseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public long getSelectedStartDate() {
        if (selectedDate == -1l){
            selectedDate = MyUtils.getTodayLong();
        }
        return selectedDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_dose, container, false);

        //Set the Start Date and INR.
        EditText inrTxtF = (EditText) view.findViewById(R.id.txtF_new_INR);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            selectedDate = savedInstanceState.getLong("selectedDate");
            updateUI(view, getSelectedStartDate());
            inrTxtF.setText(savedInstanceState.getString("newINRentered"));
        } else {
            //Default to today's date.
            selectedDate = MyUtils.getTodayLong();
            updateUI(view, selectedDate);
            inrTxtF.setHint("INR at Start Date");
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("selectedDate", selectedDate);
        //savedInstanceState.putDoubleArray("weekOfIntakeValues", getWeekIntakeValues());
        savedInstanceState.putString("newINRentered", ((EditText) getView().findViewById(R.id.txtF_new_INR)).getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    //The activity calls this method when the user selects a start date.
    public void onDateSelection(int year, int monthOfYear, int dayOfMonth){
        //Set the selected date field.
        long startDate = MyUtils.dateParamsToLong(year, monthOfYear, dayOfMonth);
        selectedDate = startDate;
        //Show in the UI.
        updateUI(getView(), startDate);
    }

    private void updateUI(View view, long selectedStartDate) {
        //Update button.
        ((Button) view.findViewById(R.id.bttn_edit_startdate)).setText(MyUtils.dateLongToStr(selectedDate));
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
                array[0] = -1; //Use this to check, as seen in areValuesMissing().
            }
        }
        return array;
    }

    public static boolean areValuesMissing(double[] intakes){
        return intakes[0] == -1;
    }

    //Get the inr value input
    public float getINRInput(){
        EditText inrTxtF = (EditText) getView().findViewById(R.id.txtF_new_INR);
        String inrStr = ((EditText) getView().findViewById(R.id.txtF_new_INR)).getText().toString();
        if (inrStr.equals("")){ return 0f; }

        float inrF = Float.parseFloat(inrStr);
        if (inrF < 0){ return 0f; }
        return inrF;
    }
}


