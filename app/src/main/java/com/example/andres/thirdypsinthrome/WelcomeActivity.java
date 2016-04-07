package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.List;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If it is the first time opening the app, go through the welcome and setup. Otherwise, go to main activity.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_time_opened", true)){
            setContentView(R.layout.activity_welcome);
            //after initial setup, "first_time_opened" will be set to false.
            if (!(prefs.getBoolean("DAGSetupDone", false))) {
                setUpADGInfo(prefs);
            }
        } else {
            //Start main activity, making sure this activity is finished so user cannot back-button to it.
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //To be called when the app first launches, puts in database the info regarding automatic dosage generation.
    //In the future, might be substituted by loading this from an external file.
    private void setUpADGInfo(SharedPreferences prefs){
        //Done in another thread, since could be expensive.
        new Thread(new Runnable() {
            public void run() {
                for(String medName : DsgAdjustHolder.KNOWN_MEDS) {
                    //Insert, in the db, the medicines for which we can do ADG.
                    long medID = DBHelper.getInstance(getApplicationContext()).addMedicine(medName, 4); //Note, the "4" here is irrelevant, as it will get replaced once the initial setup is done.
                    //Insert, in the db, their corresponding Dosage Adjustment tables.
                    List<DsgAdjustHolder> tables = DsgAdjustHolder.getDATables(medName);
                    DBHelper.getInstance(getApplicationContext()).addDAdjustTables(medID, tables);
                }
            }
        }).start();
        prefs.edit().putBoolean("DAGSetupDone", true).apply();
    }
}
