package com.example.andres.thirdypsinthrome.Dosages;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.andres.thirdypsinthrome.DosageActivity;
import com.example.andres.thirdypsinthrome.MainActivity;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.text.ParseException;

import static com.example.andres.thirdypsinthrome.MyUtils.*;

public class EnterDoseFragment extends Fragment {

    private String selectedDateStr; //For restoring activity instance state.

    public EnterDoseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public long getSelectedStartDate() throws ParseException {
        return MyUtils.dateStrToEpochLong(selectedDateStr);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_dose, container, false);

        //Set the Start Date and INR.
        Button dateBttn = (Button) view.findViewById(R.id.bttn_edit_startdate);
        EditText inrTxtF = (EditText) view.findViewById(R.id.txtF_new_INR);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            selectedDateStr = savedInstanceState.getString("selectedDate");
            dateBttn.setText(selectedDateStr);
            inrTxtF.setText(savedInstanceState.getString("newINRentered"));
        } else {
            //Default to today's date.
            selectedDateStr = MyUtils.getToday();
            dateBttn.setText(selectedDateStr);
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

    public void showDate( int year, int monthOfYear, int dayOfMonth){
        Button button = (Button) getView().findViewById(R.id.bttn_edit_startdate);
        selectedDateStr = MyUtils.formatDate(year, monthOfYear, dayOfMonth);
        button.setText(selectedDateStr);
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


