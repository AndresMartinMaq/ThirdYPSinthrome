package com.example.andres.thirdypsinthrome.Dosages;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_dose, container, false);

        //Set the Start Date.
        Button dateBttn = (Button) view.findViewById(R.id.bttn_edit_startdate);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            selectedDateStr = savedInstanceState.getString("selectedDate");
            dateBttn.setText(selectedDateStr);
        } else {
            //Default to today's date.
            dateBttn.setText(MyUtils.getToday());
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("selectedDate", selectedDateStr);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void showDate( int year, int monthOfYear, int dayOfMonth){
        Button button = (Button) getView().findViewById(R.id.bttn_edit_startdate);
        selectedDateStr = MyUtils.formatDate(year, monthOfYear, dayOfMonth);
        button.setText(selectedDateStr);
    }
}


