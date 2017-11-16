package com.example.android.mufflefurnace;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


    }

    public static class MuffleFurnaceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        private void bindPreferenceSummaryToValue(Preference preference){
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(),"");
            onPreferenceChange(preference,preferenceString);
        }

        @Override
        public void onCreate(Bundle saveInstanceState){
            super.onCreate(saveInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference maxTemperature = findPreference(getString(R.string.settings_max_temperature_key));
            bindPreferenceSummaryToValue(maxTemperature);

            Preference maxHeartingRate = findPreference(getString(R.string.settings_max_heating_rate_key));
            bindPreferenceSummaryToValue(maxHeartingRate);

            Preference maxCoolingRate = findPreference(getString(R.string.settings_max_cooling_rate_key));
            bindPreferenceSummaryToValue(maxCoolingRate);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    }
}
