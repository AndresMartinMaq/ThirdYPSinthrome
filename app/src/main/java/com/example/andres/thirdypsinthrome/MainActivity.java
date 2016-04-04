package com.example.andres.thirdypsinthrome;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);//This makes the action bar not have a shadow.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonPressed(View v) {
        int id = v.getId();

        if (id == R.id.bttn_my_dosage) {
            startActivity(new Intent(this, DosageActivity.class));
        }else
        if (id == R.id.bttn_my_treatment) {
            startActivity(new Intent(this, SettingsActivity.class));
        }else
        if (id == R.id.bttn_hist_and_stats) {
            //startActivity(new Intent(this, SettingsActivity.class));
            Log.d("","History and Stats clicked, functionality not yet implemented.");
        }else
        if (id == R.id.bttn_cit_science) {
            //startActivity(new Intent(this, SettingsActivity.class));
            Log.d("","Citizen Science clicked, functionality not yet implemented.");
        }
    }

    public void testMethod(View view){
        Log.d("Tag", "Click detected");
    }
}
