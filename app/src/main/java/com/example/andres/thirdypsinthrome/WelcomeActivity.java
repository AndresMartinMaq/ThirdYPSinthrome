package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If it is the first time opening the app, go through the welcome and setup. Otherwise, go to main activity.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_time_opened", true)){
            setContentView(R.layout.activity_welcome);
            //TODO, after initial setup, set "first_time_opened" to false.
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

}
