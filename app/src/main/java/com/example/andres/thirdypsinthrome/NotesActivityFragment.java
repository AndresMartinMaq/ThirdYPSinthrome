package com.example.andres.thirdypsinthrome;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesActivityFragment extends Fragment implements DatePicker.OnDateChangedListener{

    private static final int DAYS_TO_LOAD_LIMIT = 300;
    private Map<Long, DayHolder> days;                //Addressed by date.
    private EditText txtArea;
    private DayHolder selectedDay;

    public NotesActivityFragment() {
        days = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_notes, container, false);

        //Set onDateChanged listener:
        int year = MyUtils.getTodayField(Calendar.YEAR);
        int month = MyUtils.getTodayField(Calendar.MONTH);
        int day = MyUtils.getTodayField(Calendar.DAY_OF_MONTH);
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        datePicker.init(year, month, day, this);
        //Buttons listener
        final Button cancelBttn = (Button)view.findViewById(R.id.bttn_cancel);
        final Button editOrSaveBttn = (Button)view.findViewById(R.id.bttn_edit_or_save);
        txtArea = (EditText) view.findViewById(R.id.noteEditTxt);
        EditingButtonsListener listener = new EditingButtonsListener(editOrSaveBttn,cancelBttn,txtArea);
        editOrSaveBttn.setOnClickListener(listener);
        cancelBttn.setOnClickListener(listener);

        //Initial state
        cancelBttn.setVisibility(View.GONE);
        txtArea.setFocusable(false);  txtArea.setClickable(false);
        long today = MyUtils.getTodayLong();
        datePicker.setMaxDate(MyUtils.addDays(today, 1) * 1000l);
        datePicker.setMinDate(MyUtils.addDays(today, -DAYS_TO_LOAD_LIMIT) * 1000l);

        //Get notes from db in case the user will browse them.
        fetchNotes();

        return view;
    }

    private void fetchNotes(){
        new Thread(new Runnable() {
            public void run() {
                long userID = MyUtils.getUserID(getContext());
                List<DayHolder> list = DBHelper.getInstance(getContext()).getNotes(userID, DAYS_TO_LOAD_LIMIT);
                for (DayHolder day : list) {
                    days.put(day.date, day);
                }
                selectedDay = days.get(MyUtils.getTodayLong());
                updateNoteUI();
            }
        }).start();
    }

    private void updateNoteUI(){
        if (selectedDay != null){
            txtArea.setText(selectedDay.notes);
        } else {
            txtArea.setText("");
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        //Set text area to show the note of the chosen date (if any).
        long dateSeconds = MyUtils.dateParamsToLong(year, monthOfYear, dayOfMonth);
        selectedDay = days.get(dateSeconds);
        updateNoteUI();
    }

    public void saveNote(){
        String note = txtArea.getText().toString();
        if (selectedDay != null) {
            selectedDay.notes = note;
            DBHelper.getInstance(getContext()).addNote(selectedDay.id, note);
        } else {
            //Show dialog explaining the day should be in a dosage to have it get a note.
            new AlertDialog.Builder(getContext()).setTitle("")
                    .setMessage(getString(R.string.dg_citsci_unrecorded_day_msg))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
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
