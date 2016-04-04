package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
        View view = getView();
        TextView datetxtv = (TextView) view.findViewById(R.id.txtv_main_date);
        TextView amounttxtv = (TextView) view.findViewById(R.id.txtv_main_quantity);
        ImageView tick = (ImageView) view.findViewById(R.id.main_tick);

        if(today == null){
            Log.d("4Abril", "today was null");
            datetxtv.setText(R.string.msg_main_nodosage);
            amounttxtv.setText("");
            amounttxtv.setTextSize(0f);
            ((TextView) view.findViewById(R.id.txtv_main_units)).setText("");
            tick.setVisibility(View.INVISIBLE);
            return;
        }
        datetxtv.setText(getString(R.string.main_today_txt)+" "+MyUtils.dateLongToStr(today.date));
        Log.d("April", "Day mg in DayHolder is: "+today.mg);
        amounttxtv.setText(String.valueOf(today.mg));
        amounttxtv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 72f);
        //Set the icon.
        if (today.taken){
            tick.setVisibility(View.VISIBLE);
        } else {
            tick.setVisibility(View.INVISIBLE);
        }
    }
}
