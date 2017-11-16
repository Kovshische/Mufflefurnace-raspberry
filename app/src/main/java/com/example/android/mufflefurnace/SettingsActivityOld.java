package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivityOld extends AppCompatActivity {
    String a = "0";
    float heatingSpeed = 0;
    float coolingSpeed = 0;
    int maxTemperature = 1200;

    private SharedPreferences mSettings;

    public static SharedPreferences sharedPreferences;

    public static String toString(float settingValue) {
        String stringSettingValue = "" + settingValue + "";
        return stringSettingValue;
    }




    @Override
    protected void onResume() {
        super.onResume();

        TextView settingMaxTemperature = (TextView) findViewById(R.id.settings_max_temperature_value);
        TextView settingHearingValue = (TextView) findViewById(R.id.settings_hearing_value);
        TextView settingCoolingValue = (TextView) findViewById(R.id.settings_cooling_value);

        sharedPreferences = getSharedPreferences(Setting.APP_SETTINGS, Context.MODE_PRIVATE);
        final Setting settings = new Setting(sharedPreferences);

        if (mSettings.contains(SettingValueHeatingActivity.HEATING_SPEED)) {
            heatingSpeed = mSettings.getFloat(SettingValueHeatingActivity.HEATING_SPEED, 1);
            a = SettingValueHeatingActivity.toString(heatingSpeed);
            settingHearingValue.setText(a);
        }

       // if (mSettings.contains(SettingV))



        coolingSpeed = settings.getCoolingSpeed();
        settingCoolingValue.setText(toString(coolingSpeed));


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_old);

        //startActivityForResult( new Intent( android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0 );

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
                Intent i = new Intent(SettingsActivityOld.this, SettingValueHeatingActivity.class);
                startActivity(i);
            }

        });

        LinearLayout cooling = (LinearLayout) findViewById(R.id.settings_cooling);
        cooling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivityOld.this, SettingValueCoolingActivity.class);
                startActivity(i);
            }

        });

        TextView hearingTextView = (TextView) findViewById(R.id.settings_cooling_value);
        hearingTextView.setText("2");

        // hearingTextView.setText(hearingSpeed.toString());

    }
}
