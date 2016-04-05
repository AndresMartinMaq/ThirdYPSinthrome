package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    //TODO consider enhancing ui using spinners and such personalised Preference xml units.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_prefs);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        //With the default values, automode will be available.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mininr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_maxinr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_name_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_time_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mg_per_tablet_key)));

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
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
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

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

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
        }else if(preference.getKey().equals(getString(R.string.pref_med_name_key))){
            //For the medicine, check and set whether automatic dosage generation will be possible with it.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String medName = prefs.getString(preference.getKey(), "");
            float inrMin = Float.parseFloat(prefs.getString(getString(R.string.pref_mininr_key), ""));
            float inrMax = Float.parseFloat(prefs.getString(getString(R.string.pref_maxinr_key), ""));

            boolean autoMode = DsgAdjustHolder.isAutoModePossible(medName, inrMin, inrMax);
            prefs.edit().putBoolean(getString(R.string.automode_prefkey), autoMode).apply();

            preference.setSummary(stringValue);
        } else {
            // For other preferences, set the summary to the value's string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}
