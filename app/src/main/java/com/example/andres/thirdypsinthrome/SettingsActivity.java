package com.example.andres.thirdypsinthrome;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    //TODO consider enhancing ui using spinners and such personalised Preference xml units.
    //Note: With the default values, automode will be available.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_prefs);

        //For all preferences, attach an OnPreferenceChangeListener so the UI summary can be updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mininr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_maxinr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_name_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_time_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mg_per_tablet_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_alarmtone_key)));
        //These are  here so they doesn't get triggered onCreation like the others.
        findPreference(getString(R.string.pref_alarm_timing_key)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_alarm_enabled_key)).setOnPreferenceChangeListener(this);

        //For when this is the initial setup.
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_time_opened", true)) {
        } else {
            //Remove continue button.
            Preference continueButton = findPreference("prefs_continue_bttn");
            //TODO minor, consider changing all this stuff to instead add the continue button if first time, using newPreference.setLayoutResource.
            getPreferenceScreen().removePreference(continueButton);
        }
    }

    //To be called by the continue button in the initial setting up of these settings.
    public void openMain(View v) {
        //Set "first time opened" to false.
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("first_time_opened", false).apply();
        //Register the user's data in the database and open the main activity.
        try {
            DBHelper.getInstance(getApplicationContext()).registerUser(getApplicationContext());

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);//Makes sure back button won't ever lead to this again.
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attaches a listener so the summary is always updated when the preference changes.
     * Summary refers to the value displayed on the UI as being the value of the setting.
     * Also fires the listener once, to initialize the summary.
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }

            if (preference.getKey().equals(getString(R.string.pref_alarm_timing_key))){
                //Set the alarms
                setAlarms(stringValue);
            }
        }else
        if (preference.getKey().equals(getString(R.string.pref_alarmtone_key))){
            //For alarm tone, get the name (otherwise it displays the uri).
            Uri ringtoneUri = Uri.parse(stringValue);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            String name = ringtone.getTitle(this);
            preference.setSummary(name);
        }else
        if (preference.getKey().equals(getString(R.string.pref_alarm_enabled_key))) {
            boolean firstTimeOpened = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_time_opened", true);
            if (stringValue.equals("false") && !firstTimeOpened) {
                //If trying to disable alarms, show a dialog to inform that already set alarms will need to be deleted manually.
                new AlertDialog.Builder(this).setTitle(getString(R.string.notice))
                        .setMessage(getString(R.string.dg_past_alarms_not_deleted))
                        .setPositiveButton(getString(R.string.dg_past_alarms_bttn1), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                //Start the Clock Activity.
                                if (Build.VERSION.SDK_INT >= 19) {
                                    startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS));
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.later), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            //Notice the lack of setSummary() here, due to it being a checkbox.
        }else
        if(preference.getKey().equals(getString(R.string.pref_med_name_key))){
            //For the medicine, check and set whether automatic dosage generation will be possible with it.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String medName = stringValue.toLowerCase();
            prefs.edit().putString(preference.getKey(), medName).commit(); //Also, make sure only lowercase letters are stored.

            float inrMin = Float.parseFloat(prefs.getString(getString(R.string.pref_mininr_key), ""));
            float inrMax = Float.parseFloat(prefs.getString(getString(R.string.pref_maxinr_key), ""));

            boolean autoMode = DsgAdjustHolder.isAutoModePossible(this, medName, inrMin, inrMax);
            prefs.edit().putBoolean(getString(R.string.automode_prefkey), autoMode).commit();

            preference.setSummary(medName);
        } else {
            // For other preferences, set the summary to the value's string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    //Setting an alarm for the medicine taking time
    private void setAlarms(String timingStr) {
        //Get prefernces used
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Uri ringtoneUri = Uri.parse(prefs.getString(getString(R.string.pref_alarmtone_key), getString(R.string.pref_alarmtone_default)));
        String intakeTimeStr = prefs.getString(getString(R.string.pref_med_time_key), getString(R.string.pref_med_time_default));

        //Set a calendar to the medicine intake time.
        String[] hAndMins = intakeTimeStr.split(":");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(MyUtils.getTodayLong() * 1000l);
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hAndMins[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(hAndMins[1]));

        //Days the alarm will be set for.
        Integer[] week = {Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY, Calendar.SUNDAY};
        ArrayList<Integer> daysOfWeek = new ArrayList<Integer>(Arrays.asList(week));

        //Set alarms X minutes prior to this time (may set 1, 2 or more alarms depending on the timingStr set chosen by user).
        String[] strArr = timingStr.split(",");
        for (String x : strArr){

            c.add(Calendar.MINUTE, -Integer.parseInt(x) );

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, hour );
            i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarm_message));
            if (Build.VERSION.SDK_INT >= 19) {
                i.putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri.toString());
                i.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, daysOfWeek);
            }
            //Only open the clock activity if this is the last alarm to be set.
            if (x.equals(strArr[strArr.length-1])){
                i.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
                startActivity(i);
            } else {
                i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                startActivity(i);
                try {Thread.sleep(1000);} catch (InterruptedException e) {}
            }
            //Undo the change to the calendar, in case another alarm will be set.
            c.add(Calendar.MINUTE, +Integer.parseInt(x) );
        }
    }
}
