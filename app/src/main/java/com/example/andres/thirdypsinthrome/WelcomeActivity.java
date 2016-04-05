package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If it is the first time opening the app, go through the welcome and setup. Otherwise, go to main activity.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_time_opened", true)){
            setContentView(R.layout.activity_welcome);
            setUpADGInfo();
            //after initial setup, "first_time_opened" will be set to false.
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    //To be called when the app first launches, to put in database the info regarding automatic dosage generation.
    //In the future, might be substituted by loading this from an external file.
    private void setUpADGInfo(){ //Could do this in another thread.

        for(String medName : DsgAdjustHolder.KNOWN_MEDS) {
            //Insert, in the db, the medicines for which we can do ADG.
            DBHelper.getInstance(this).addMedicine(medName, 4); //Note, the "4" here is irrelevant, as it will get replaced once the initial setup is done.
            //Insert, in the db, the corresponding Dosage Adjustment tables.
            DBHelper.getInstance(this).addDAdjustTabels( long medID, List<DsgAdjustHolder > tableQuoteUnquote);
        }
    }
}
