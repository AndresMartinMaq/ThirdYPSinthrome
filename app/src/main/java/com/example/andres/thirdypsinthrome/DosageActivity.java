package com.example.andres.thirdypsinthrome;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;
import com.example.andres.thirdypsinthrome.Dosages.ADGManager;
import com.example.andres.thirdypsinthrome.Dosages.DateFragmentDialog;
import com.example.andres.thirdypsinthrome.Dosages.DosagePlansFragment;
import com.example.andres.thirdypsinthrome.Dosages.EnterDoseFragment;
import com.example.andres.thirdypsinthrome.LoadersAndAdapters.DosageLoader;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class DosageActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    public float mgSumAsked = -1; //Used only when attempting ADG and no previous dosage is available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosages);
        getSupportActionBar().setElevation(0f);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Set Fragment up
        if (findViewById(R.id.dose_fragment_holder) != null) {

            //If we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {return;}

            DosagesFragment firstFragment = new DosagesFragment();
            // Add the fragment to the 'dose_fragment_holder' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.dose_fragment_holder, firstFragment, DosagesFragment.TAG).commit();
        }

    }

    public void buttonPressed(View v) {
        int id = v.getId();

        if (id == R.id.btt_enter_dosage) {
            // Create a new Fragment to be placed in the activity layout
            EnterDoseFragment enterDoseFragment = new EnterDoseFragment();

            // Replace whatever is in the fragment_container view with this fragment,
            // Could add the transaction to the back stack so the user can navigate back
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.dose_fragment_holder, enterDoseFragment, EnterDoseFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        if (id == R.id.btt_enter_INR) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean(getString(R.string.automode_prefkey), false)) {
                showGenerateDoseDialog();
            }
            //If ADG is not available, show error dialog.
            else {
                new AlertDialog.Builder(this).setTitle(R.string.dg_noDAG_title)
                        .setMessage(R.string.dg_noDAG_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {dialog.dismiss();}
                    })
                        .show();
            }
        }
        if (id == R.id.btt_dosage_callendar) {
            getSupportActionBar().setTitle("Dosage Plans");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.dose_fragment_holder, new DosagePlansFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void showDatePickerDialog(View v) {
        DateFragmentDialog newFragment = new DateFragmentDialog();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    //Show a dialog asking for the INR to generate the new dosage.
    private void showGenerateDoseDialog(){
        final View dialogView = getLayoutInflater().inflate(R.layout.fragment_gen_dose, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dg_newINR_title);
        builder.setView(dialogView);
        // Set up the buttons with listeners
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inrInput = ((EditText) dialogView.findViewById(R.id.dg_INR_editText)).getText().toString();
                float inr = Float.parseFloat(inrInput);
                boolean today = ((RadioButton) dialogView.findViewById(R.id.rbttn_today)).isChecked();
                long startDate = MyUtils.getTodayLong();
                if (!today){//If 'tomorrow' was checked.
                    startDate = MyUtils.addDays(startDate, 1);
                }
                attemptADG(inr, startDate);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }

    //Checks the conditions are correct for ADG and launches ADG if so.
    private boolean attemptADG(float inr, long startDate){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Check if Automode is available for medicine and if a current or just finished dosage plan is available (as they contain info necessary for ADG).
        //If Automode is available, but neither a current or just finished plan is, ask for additional information.
        if (prefs.getBoolean(getString(R.string.automode_prefkey), false)) {
            DosagesFragment doseFragment = (DosagesFragment) getSupportFragmentManager().findFragmentById(R.id.dose_fragment_holder);
            DosageHolder lastDosage = doseFragment.dosage;
            if (lastDosage == null){
                long userID =  prefs.getLong(getString(R.string.userID_prefkey), 0);
                long yesterday = MyUtils.addDays(MyUtils.getTodayLong(), -1);
                DosageHolder justFinishedPlan = DBHelper.getInstance(this).getDosagePlanEndingOn(userID, yesterday);
                lastDosage = justFinishedPlan;
            }

            if (lastDosage == null && mgSumAsked < 1) {
                //Ask for additional info.
                showAskForMgSum(inr, startDate);
            } else {
                String medName = prefs.getString(getString(R.string.pref_med_name_key), "");
                float minINR = Float.parseFloat(prefs.getString(getString(R.string.pref_mininr_key), "0"));
                float maxINR = Float.parseFloat(prefs.getString(getString(R.string.pref_maxinr_key), "0"));

                try {
                    //Generate the Dosage
                    if (lastDosage != null) {
                        ADGManager.generateDosage(this, medName, lastDosage, startDate, minINR, maxINR, inr);
                    } else {
                        ADGManager.generateDosage(this, medName, mgSumAsked, startDate, minINR, maxINR, inr);
                    }
                    //Go back to main activity to refresh.
                    //Also, Pop backstack so unupdated activities and fragments die.
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); //finish the current activity.
                } catch (Exception e) {
                    //Display error dialog if generation fails.
                    new AlertDialog.Builder(this).setTitle("Error")
                            .setMessage(getString(R.string.dg_DAG_errormsg)+" "+e.getMessage())
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    //Needed to ask for mgSum when no previous dosage can be used as reference for ADG.
    //Should exclusively be called from attemptADG
    private void showAskForMgSum(final float inr, final long startDate){
        final View dialogView = getLayoutInflater().inflate(R.layout.fragment_gen_dose, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dg_addinfo_required_title);
        builder.setView(dialogView);
        //Personalise it for the purpose
        ((LinearLayout)dialogView.findViewById(R.id.panel2)).removeAllViews();
        final EditText editText = (EditText) dialogView.findViewById(R.id.dg_INR_editText);
        editText.setHint(R.string.dg_addinfo_required_hint);
        ((TextView)dialogView.findViewById(R.id.dg_msg_txtv)).setText(R.string.dg_addinfo_required_msg);
        // Set up the buttons with listeners
        builder.setPositiveButton(getString(R.string.dg_addinfo_required_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mgInput = editText.getText().toString();
                try {
                    mgSumAsked = Float.parseFloat(mgInput);
                } catch (Exception e) {
                }
                attemptADG(inr, startDate);
            }
        });
        builder.setNegativeButton(getString(R.string.dg_addinfo_required_manu), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EnterDoseFragment enterDoseFragment = new EnterDoseFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.dose_fragment_holder, enterDoseFragment, EnterDoseFragment.TAG);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setSingleLine(true);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(true);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(12);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setSingleLine(true);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(true);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(12);
    }

    //Used by DateFragmentDialog to comm with EnterDosage Fragment to update the latter's ui.
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        EnterDoseFragment enterDoseFragment =
                (EnterDoseFragment) getSupportFragmentManager().findFragmentById(R.id.dose_fragment_holder);
        enterDoseFragment.onDateSelection(year, monthOfYear, dayOfMonth);
    }

    //To be called by the "Done" button after the user has entered all required info.
    public void onNewDosageEntered(View view) throws Exception {
        //Get Fragment
        EnterDoseFragment enterDoseFmt = (EnterDoseFragment) getSupportFragmentManager().findFragmentById(R.id.dose_fragment_holder);
        //Get entered values
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long userID = prefs.getLong(getString(R.string.userID_prefkey), -1);
        if (userID == -1){ throw new Exception("Could not find sharedPreference user ID");}
        double[] intakes = enterDoseFmt.getWeekIntakeValues();
        long startDate = enterDoseFmt.getSelectedStartDate();
        long endDate = MyUtils.addDays(startDate, intakes.length - 1);
        float newINR = enterDoseFmt.getINRInput();

        //Check all fields have been filled
        if (newINR == 0f || EnterDoseFragment.areValuesMissing(intakes)){
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        //Check startDate is not in the past
        if (!(MyUtils.getTodayLong() <= startDate)){
            new AlertDialog.Builder(this).setMessage(R.string.dialog_startDate_in_past)
                    .setTitle(R.string.dialog_startDate_in_past_title)
                    .setPositiveButton(R.string.ok, null)
                    .create().show();
            return;
        }
        //Check dates are not occupied by another dosage plan
        if (!DBHelper.getInstance(this).isDatesAvailable(userID, startDate, endDate)){
            new AlertDialog.Builder(this).setMessage(getString(R.string.dialog_dates_unavailable_msg))
                    .setTitle(getString(R.string.dialog_dates_unavailable_title))
                    .setPositiveButton(R.string.ok, null)
                    .create().show();
            return;
        }

        //Add new Dosage
        DBHelper.getInstance(this).addDosageManually(userID, startDate, endDate, intakes, newINR);

        //Go back to DosagesFragment, with updated UI.
        DosagesFragment dosagesFmt = (DosagesFragment) getSupportFragmentManager().findFragmentByTag(DosagesFragment.TAG);//Could also just create a new one here and not restart the loader, this seems more elegant.
        getSupportLoaderManager().restartLoader(DosageActivity.DosagesFragment.LOADER_ID, null, dosagesFmt);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dose_fragment_holder, dosagesFmt, DosagesFragment.TAG).commit();

        //Display a confirmatory message
        Toast.makeText(this, getString(R.string.toast_confirmation), Toast.LENGTH_LONG).show();
    }

    //To delete current plan
    public void showDeletePlanDialog(final DosageHolder dosageHolder){
        if (dosageHolder == null){
            Toast.makeText(this, R.string.toast_no_plan_to_abandon, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this).setMessage(getString(R.string.db_abandon_plan_msg))
                .setTitle(getString(R.string.dg_abandon_plan_title))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete dosage plan.
                        DBHelper.getInstance(DosageActivity.this).deleteCurrentDosagePlan(dosageHolder.id);
                        Toast.makeText(DosageActivity.this, R.string.dg_abandon_plan_toast, Toast.LENGTH_SHORT).show();
                        //Return to an updated main activity.
                        Intent intent = new Intent(DosageActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    //---------------------------------------------------------------------------------
    public static class DosagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<DosageHolder>{

        public static final int LOADER_ID = 3;
        public static String TAG = "DosagesFragment";
        DosageHolder dosage;

        public DosagesFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_dosages, container, false);

            //Setting UI to display the most relevant Dosage (making use of a Loader to get the data) will happen
            //due to the loader being initiated in onActivityCreated and, subsequently, onLoadFinished calling the updateUI method.

            /*View item4 = view.findViewById(R.id.dsg_item4);
            //To set the scrollView to scroll to the middle of an item
            if (item4 != null) {
                centerScrollViewOn(item4, (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView), view.findViewById(R.id.scrollingLinearLayout));
            }*/

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
                    hsv.scrollTo(hsv.getScrollX() + (hsv.getWidth() / 2) - extraScrollX, 0);
                }
            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_dosages, menu);
            super.onCreateOptionsMenu(menu, menuInflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    ((DosageActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_all_dosage_plans);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.dose_fragment_holder, new DosagePlansFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.action_delete:
                    try {
                        ((DosageActivity) getActivity()).showDeletePlanDialog(dosage);
                    } catch (ClassCastException e){}
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        public void updateUI(DosageHolder dosage){
            LinearLayout parentLayout = null;
            this.dosage = dosage;
            View view;
            try {
                view = getView();
                parentLayout = (LinearLayout) view.findViewById(R.id.scrollingLinearLayout);
            } catch (NullPointerException e){
                //This happens when going back from this activity.
                return;
            }
            View item1 = parentLayout.findViewById(R.id.dsg_item1);
            View item2 = parentLayout.findViewById(R.id.dsg_item2);
            View item3 = parentLayout.findViewById(R.id.dsg_item3);
            View item4 = parentLayout.findViewById(R.id.dsg_item4);
            View item5 = parentLayout.findViewById(R.id.dsg_item5);
            View item6 = parentLayout.findViewById(R.id.dsg_item6);
            View item7 = parentLayout.findViewById(R.id.dsg_item7);
            View[] itemViews = {item1,item2,item3,item4,item5,item6,item7};

            //If no dosage should be displayed, instead show a message to inform the user.
            if (dosage == null){
                Log.d("DosagesFmt", "No current dosage found to display");
                //Remove dosage views' contents
                for (int i = 0; i < itemViews.length; i++){
                    View item = itemViews[i];

                    ((TextView) item.findViewById(R.id.dsg_item_date)).setText("");
                    ImageView imgView = (ImageView) item.findViewById(R.id.dsg_item_icon);
                    ((TextView) item.findViewById(R.id.dsg_item_mgs)).setText("");
                    ((TextView) item.findViewById(R.id.dsg_item_units)).setText("");
                }
                //Update message below
                ((TextView)view.findViewById(R.id.dosage_txt1)).setText("");
                ((TextView)view.findViewById(R.id.dosage_txt2)).setText("");
                ((TextView)view.findViewById(R.id.dosage_txt3)).setText(R.string.msg_no_current_dosage);

                view.findViewById(R.id.btt_dosage_callendar).setEnabled(false);
                return;
            } else {
                view.findViewById(R.id.btt_dosage_callendar).setEnabled(true);
            }

            //Iterate the views in the linear layout, modifying their values
            int i = 0;
            for (DayHolder day : dosage.days) {
                if (i > itemViews.length -1){break;}
                View item = itemViews[i];
                item.setVisibility(View.VISIBLE);
                //Set date
                TextView dateView = (TextView) item.findViewById(R.id.dsg_item_date);
                dateView.setText(MyUtils.dateLongToStr(day.date));
                //Set Icon
                ImageView imgView = (ImageView) item.findViewById(R.id.dsg_item_icon);
                if (day.taken) {
                    imgView.setImageResource(R.drawable.tick);
                } else {
                    imgView.setImageResource(R.drawable.circle);
                }
                //Set milligrams
                TextView mgView = (TextView) item.findViewById(R.id.dsg_item_mgs);
                mgView.setText(String.valueOf(day.mg));
                i++;
            }
            //Hide any views that have not been filled (due to a dosage detailing fewer than 7 days).
            for (int i2 = i; i2 < itemViews.length; i2++){
                //this is zero indexed and when a view is removed, the rest fall into place
                //(e.g.: removing view "2" will cause view "3" to become 2 and "4" to become 3, etc.
            //    parentLayout.getChildAt(i2).setVisibility(View.INVISIBLE);
                parentLayout.removeViewAt(i);
            }

            //Set the message that states the endDate of the dosage.
            String endDate = MyUtils.dateLongToStr(MyUtils.addDays(dosage.endDate, -1));
            ((TextView) view.findViewById(R.id.dosage_txt2)).setText(endDate);

            //Center the view on today, for aesthetics.
            //centerScrollViewOn(item1, (HorizontalScrollView) parentLayout.getParent(), parentLayout);
        }

        //----Loader methods-----
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(LOADER_ID, null, this); //This will call onCreateLoader appropriately.
            super.onActivityCreated(savedInstanceState);
        }
        @Override //Method called when a new loader needs to be created.
        public Loader<DosageHolder> onCreateLoader(int id, Bundle args) {
            //Simply creates the Loader.
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            long userID = prefs.getLong(getString(R.string.userID_prefkey), -1);
            return new DosageLoader(getContext(), userID);
        }

        @Override
        public void onLoadFinished(Loader<DosageHolder> loader, DosageHolder data) {
            //Update Views
            updateUI(data);
        }

        @Override
        public void onLoaderReset(Loader<DosageHolder> loader) {
            updateUI(dosage);
        }
    }
}