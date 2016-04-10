package com.example.andres.thirdypsinthrome;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class NotesActivityFragment extends Fragment implements DatePicker.OnDateChangedListener{

    public NotesActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_notes, container, false);

        //Set onDateChanged listener:
        int year = MyUtils.getTodayField(Calendar.YEAR);
        int month = MyUtils.getTodayField(Calendar.MONTH);
        int day = MyUtils.getTodayField(Calendar.DAY_OF_MONTH);
        ((DatePicker) view.findViewById(R.id.datePicker)).init(year, month, day, this);
        //Buttons listener
        final Button cancelBttn = (Button)view.findViewById(R.id.bttn_cancel);
        final Button editOrSaveBttn = (Button)view.findViewById(R.id.bttn_edit_or_save);
        final EditText txtArea = (EditText) view.findViewById(R.id.noteEditTxt);
        EditingButtonsListener listener = new EditingButtonsListener(editOrSaveBttn,cancelBttn,txtArea);
        editOrSaveBttn.setOnClickListener(listener);
        cancelBttn.setOnClickListener(listener);
        //Initial state
        cancelBttn.setVisibility(View.GONE);
        txtArea.setFocusable(false);  txtArea.setClickable(false);

        return view;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        //TODO
    }

    public void saveNote(){

    }

    private class EditingButtonsListener implements View.OnClickListener {

        boolean editing;
        Button cancelBttn;
        Button editBttn;
        EditText txtArea;

        public EditingButtonsListener(Button editBttn, Button cancelBttn, EditText txtArea){
            this.cancelBttn = cancelBttn;
            this.editBttn = editBttn;
            this.txtArea = txtArea;
            editing = false;
        }
        @Override
        public void onClick(View v) {
            if (v.equals(editBttn)){
                if (!editing) {
                    txtArea.setFocusable(true);  txtArea.setClickable(true);
                    txtArea.setFocusableInTouchMode(true);
                    editBttn.setText("Save");
                    cancelBttn.setVisibility(View.VISIBLE);
                    cancelBttn.setClickable(true);
                    editing = true;
                } else {
                    txtArea.setFocusable(false);  txtArea.setClickable(false);
                    editBttn.setText("Edit");
                    cancelBttn.setVisibility(View.GONE);
                    cancelBttn.setClickable(false);
                    editing = false;
                    saveNote();
                }
            } else {
                txtArea.setFocusable(false);  txtArea.setClickable(false);
                editBttn.setText("Edit");
                cancelBttn.setVisibility(View.GONE);
                editing = false;
            }
        }
    }
}
