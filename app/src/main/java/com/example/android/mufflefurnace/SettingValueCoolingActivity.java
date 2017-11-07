package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingValueCoolingActivity extends AppCompatActivity {
    public static SharedPreferences sharedPreferences;
    float coolingSpeed = 0;


    //Save settings



    public static String toString(float settingValue) {
        String stringSettingValue = "" + settingValue + "";
        return stringSettingValue;
    }



    @Override
    protected void onResume() {
        super.onResume();

        EditText settingValue = (EditText) findViewById(R.id.setting_value);

        sharedPreferences = getSharedPreferences(Setting.APP_SETTINGS, Context.MODE_PRIVATE);
        final Setting settings = new Setting(sharedPreferences);

        coolingSpeed = settings.getCoolingSpeed();
        settingValue.setText(toString(coolingSpeed));

      /*  if (sharedPreferences.contains(Setting.COOLING_SPEED)) {
            coolingSpeed = sharedPreferences.getFloat(Setting.COOLING_SPEED, 0);
            String a= toString(coolingSpeed);
            settingValue.setText(a);
        }
*/
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_value);

        sharedPreferences = getSharedPreferences(Setting.APP_SETTINGS, Context.MODE_PRIVATE);
        final Setting settings = new Setting(sharedPreferences);


        Button save = (Button) findViewById(R.id.save_value);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Setting mSettings = settings;
                EditText settingValue = (EditText) findViewById(R.id.setting_value);

                String a = "0";
                 a = settingValue.getText().toString();
                coolingSpeed = Float.parseFloat(a);

                mSettings.editCoolingSpeed(coolingSpeed);



                // go to the settings
                Intent i = new Intent(SettingValueCoolingActivity.this, SettingsActivity.class);
                startActivity(i);

            }
        });
        TextView settingDescription = (TextView) findViewById(R.id.setting_value_description);
        settingDescription.setText(R.string.settings_max_cooling_rate_text);

    }
}
