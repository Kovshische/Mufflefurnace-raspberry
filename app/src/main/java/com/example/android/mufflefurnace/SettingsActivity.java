package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    String a = "0";
    float heatingSpeed = 0;
    float coolingSpeed = 0;

    private SharedPreferences mSettings;

    public static SharedPreferences sharedPreferences;

    public static String toString(float settingValue) {
        String stringSettingValue = "" + settingValue + "";
        return stringSettingValue;
    }


    @Override
    protected void onResume() {
        super.onResume();
        TextView settingHearingValue = (TextView) findViewById(R.id.settings_hearing_value);


        sharedPreferences = getSharedPreferences(Setting.APP_SETTINGS, Context.MODE_PRIVATE);
        final Setting settings = new Setting(sharedPreferences);

        if (mSettings.contains(SettingValueHeatingActivity.HEATING_SPEED)) {
            heatingSpeed = mSettings.getFloat(SettingValueHeatingActivity.HEATING_SPEED, 1);
            a = SettingValueHeatingActivity.toString(heatingSpeed);
            settingHearingValue.setText(a);
        }

        TextView settingCoolingValue = (TextView) findViewById(R.id.settings_cooling_value);

        coolingSpeed = settings.getCoolingSpeed();
        settingCoolingValue.setText(toString(coolingSpeed));


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        TextView settingValue = (TextView) findViewById(R.id.settings_hearing_value);

        TextView settingCoolingValue = (TextView) findViewById(R.id.settings_cooling_value);


        mSettings = getSharedPreferences(SettingValueHeatingActivity.APP_SETTINGS, Context.MODE_PRIVATE);

        if (mSettings.contains(SettingValueHeatingActivity.HEATING_SPEED)) {
            heatingSpeed = mSettings.getFloat(SettingValueHeatingActivity.HEATING_SPEED, 0);
            a = SettingValueHeatingActivity.toString(heatingSpeed);
            settingValue.setText(a);
        }

        sharedPreferences = getSharedPreferences(Setting.APP_SETTINGS, Context.MODE_PRIVATE);
        final Setting settings = new Setting(sharedPreferences);

        coolingSpeed = settings.getCoolingSpeed();
        settingCoolingValue.setText(toString(coolingSpeed));




        LinearLayout hearing = (LinearLayout) findViewById(R.id.settings_hearing);
        hearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, SettingValueHeatingActivity.class);
                startActivity(i);
            }

        });

        LinearLayout cooling = (LinearLayout) findViewById(R.id.settings_cooling);
        cooling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, SettingValueCoolingActivity.class);
                startActivity(i);
            }

        });

        TextView hearingTextView = (TextView) findViewById(R.id.settings_cooling_value);
        hearingTextView.setText("2");

        // hearingTextView.setText(hearingSpeed.toString());

    }
}
