package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class MainActivityFragment extends Fragment {

    DayHolder today;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_main, container, false);

        //Make the central panel display today's main information.
        bindTodaySummary();

        return view;
    }

    private void bindTodaySummary(){
        new Thread(new Runnable() {
            public void run() {
                long userID = MyUtils.getUserID(getContext());
                today = DBHelper.dbHelperInst(getContext()).getToday(userID);
                updateUI();
            }
        }).start();
    }

    private void updateUI(){
        //TODO use 'today' field to update UI.
    }
}
