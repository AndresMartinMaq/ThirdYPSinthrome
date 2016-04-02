package com.example.andres.thirdypsinthrome;

import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andres.thirdypsinthrome.Dosages.DateFragmentDialog;
import com.example.andres.thirdypsinthrome.Dosages.EnterDoseFragment;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class DosageActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosages);

        //Set Fragment up
        if (findViewById(R.id.dose_fragment_holder) != null) {

            //If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {return;}

            DosagesFragment firstFragment = new DosagesFragment();
            // Add the fragment to the 'dose_fragment_holder' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.dose_fragment_holder, firstFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dosages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonPressed(View v) {
        Log.d("DosageActivity", "ButtonPressed on DosageActivity called.");
        int id = v.getId();

        if (id == R.id.btt_enter_dosage) {
            // Create a new Fragment to be placed in the activity layout
            EnterDoseFragment enterDoseFragment = new EnterDoseFragment();

            // Replace whatever is in the fragment_container view with this fragment,
            // Could add the transaction to the back stack so the user can navigate back
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.dose_fragment_holder, enterDoseFragment);
            //transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void showDatePickerDialog(View v) {
        DateFragmentDialog newFragment = new DateFragmentDialog();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    //Used by DateFragmentDialog to comm with EnterDosage Fragment to update the latter's ui.
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        EnterDoseFragment enterDoseFragment =
                (EnterDoseFragment) getSupportFragmentManager().findFragmentById(R.id.dose_fragment_holder);
        enterDoseFragment.showDate(year, monthOfYear, dayOfMonth);
    }

    //To be called by the "Done" button after the user has entered all required info.
    public void onNewDosageEntered(View view) throws Exception {
        //Get Fragment
        EnterDoseFragment enterDoseFmt = (EnterDoseFragment) getSupportFragmentManager().findFragmentById(R.id.dose_fragment_holder);
        //TODO check all fields have been filled

        //Get entered values from SharedPreferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long userID = prefs.getLong(getString(R.string.userID_prefkey), -1);
        if (userID == -1){ throw new Exception("Could not find sharedPreference user ID");}
        double[] intakes = enterDoseFmt.getWeekIntakeValues();
        long startDate = enterDoseFmt.getSelectedStartDate();
        long endDate = MyUtils.addDays(startDate, intakes.length);
        //Add new Dosage
        DBHelper.dbHelperInst(this).addDosageManually(userID, startDate, endDate, intakes);

        //Go back to DosagesFragment, with updated UI.
        DosagesFragment dosagesFmt = new DosagesFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dose_fragment_holder, dosagesFmt).commit();

        //Display a confirmatory message
        Toast.makeText(this, "New Dosage recorded", Toast.LENGTH_LONG).show();
    }

    //---------------------------------------------------------------------------------
    public static class DosagesFragment extends Fragment {

        public DosagesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_dosages, container, false);

            View item1 = view.findViewById(R.id.dsg_item1);
            View item2 = view.findViewById(R.id.dsg_item2);
            View item3 = view.findViewById(R.id.dsg_item3);
            View item4 = view.findViewById(R.id.dsg_item4);
            View item5 = view.findViewById(R.id.dsg_item5);

            TextView date1 = (TextView) item1.findViewById(R.id.dsg_item_date);
            date1.setText("22 February");

            ImageView img1 = (ImageView) item1.findViewById(R.id.dsg_item_icon);
            img1.setImageResource(R.drawable.tick);

            //To set the scrollView to scroll to the middle of an item
            centerScrollViewOn(item3, (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView), view.findViewById(R.id.scrollingLinearLayout));

            return view;
        }

        //To set the scrollView to scroll to the middle of an item
        public void centerScrollViewOn(final View item, final HorizontalScrollView hsv, View insideLinearLayout){
            //This next line makes it scroll right just until the item is fully visible.
            hsv.requestChildFocus(insideLinearLayout, item);
            //This will center the item.
            hsv.post(new Runnable() {
                public void run() {
                    int extraScrollX = item.getWidth() / 2;
                    hsv.scrollTo(hsv.getScrollX() + (hsv.getWidth()/2)-extraScrollX, 0);
                }
            });
        }

    }
}