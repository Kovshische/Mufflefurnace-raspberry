package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingValueHeatingActivity extends AppCompatActivity {
    public float heatingSpeed = 0;
    String a = "0";

    //Save settings

    //File name
    public static final String APP_SETTINGS = "app_settings";
    //Setting name
    public static final String HEATING_SPEED = "xx";
    // instance of the class
    public static SharedPreferences mSettings;





    public static String toString(float settingValue) {
        String stringSettingValue = "" + settingValue + "";
        return stringSettingValue;
    }


    @Override
    protected void onResume() {
        super.onResume();

        EditText settingValue = (EditText) findViewById(R.id.setting_value);

        if (mSettings.contains(HEATING_SPEED)) {
            heatingSpeed = mSettings.getFloat(HEATING_SPEED, 1);
            a= toString(heatingSpeed);
            settingValue.setText(a);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_value);

        //create a file where will be saved settings
       mSettings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);

        //Save data

        Button save = (Button) findViewById(R.id.save_value);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText settingValue = (EditText) findViewById(R.id.setting_value);

                EditText mHeatingValue = settingValue;


                String a = mHeatingValue.getText().toString();
                //String to float
                heatingSpeed = Float.parseFloat(a);


                SharedPreferences.Editor editor = mSettings.edit();

                editor.putFloat(HEATING_SPEED, heatingSpeed);
                editor.apply();

                Log.i("Heating activity",a);


                //Go to the settings page

                Intent i = new Intent(SettingValueHeatingActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });


        //Display heating speed
        TextView settingValue = (TextView) findViewById(R.id.setting_value);

        if(mSettings.contains(HEATING_SPEED)){
            heatingSpeed = mSettings.getFloat(HEATING_SPEED,0);
        }



        a = toString(heatingSpeed);
        //String a = toString(heatingSpeed);
        settingValue.setText(a);




        TextView settingDescription = (TextView) findViewById(R.id.setting_value_description);
        settingDescription.setText(R.string.settings_max_heating_rate_text);
    }
}
