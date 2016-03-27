package com.example.andres.thirdypsinthrome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    //TODO consider enhancing ui using spinners and such personalised Preference xml units.
    //TODO put titles for INR and Medicine and such.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_prefs);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mininr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_maxinr_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_name_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_med_time_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_mg_per_tablet_key)));

        //For when this is the initial setup.
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("TESTT", "1");
        if (prefs.getBoolean("first_time_opened", true)) {
            Log.d("TESTT", "2");
            //"continue" button.
            Preference button = findPreference("prefs_continue_bttn");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Record initial setup has been done.
                    //prefs.edit().putBoolean("my_first_time", false).commit();
                    Log.d("TESTT", "Would have sset myfirsttime to false");
                    //Launch Main activity.
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                    return true;
                }
            });
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
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}
