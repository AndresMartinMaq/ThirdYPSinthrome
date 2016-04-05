package com.example.andres.thirdypsinthrome;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        //Attach listener for he today panel
        view.findViewById(R.id.today_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todayPressed();
            }
        });

        return view;
    }

    private void bindTodaySummary(){
        new Thread(new Runnable() {
            public void run() {
                long userID = MyUtils.getUserID(getContext());
                today = DBHelper.getInstance(getContext()).getToday(userID);
                updateUI();
            }
        }).start();
    }

    private void updateUI(){
        //Use 'today' field to update UI.
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

    //For when the user clicks the large main panel, to mark today as taken
    public void todayPressed(){
        //If today is not taken then show dialogue asking if you wish to mark today as taken.
        if (today == null || today.taken){ return; }

        final View thisView = getView();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_mark_as_taken_msg)
                .setTitle(R.string.dialog_mark_as_taken_title);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        //On confirmation, display the tick and modify the database.
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //This db access shouldn't be expensive enough to pose a problem for the thread.
                DBHelper.getInstance(getContext()).setDayAsTaken(today.id, MyUtils.getDevFromMedTakingTime(getContext()));
                thisView.findViewById(R.id.main_tick).setVisibility(View.VISIBLE);
                today.taken = true;
            }
        });
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();
    }
}
