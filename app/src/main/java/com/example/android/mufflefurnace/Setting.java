package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by admin on 3/3/2017.
 */
public class Setting {
    float heatingSpeed = 0;
    float coolingSpeed = 0;

    //File name
    public static final String APP_SETTINGS = "app_settings";
    //Setting name
    public static final String HEATING_SPEED = "heating_speed";
    public static final String COOLING_SPEED = "cooling_speed";
    // instance of the class
    private SharedPreferences sSettings;





    public Setting(SharedPreferences setting){
        sSettings = setting;
    }


    // get setting (if there is no setting - get 0
    public float getHeatingSpeed(){
        if (sSettings.contains(HEATING_SPEED)) {
            heatingSpeed = sSettings.getFloat(HEATING_SPEED, 0);
        }
        return heatingSpeed;
    }

    public float getCoolingSpeed(){
        if (sSettings.contains(COOLING_SPEED)) {
            coolingSpeed = sSettings.getFloat(COOLING_SPEED, 0);
        }
        return coolingSpeed;
    }

    //Edit setting
    public void editHeatingSpeed(float heatingSpeed){
        //sSettings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sSettings.edit();
        editor.putFloat(HEATING_SPEED, heatingSpeed);
        editor.apply();
    }



    public void editCoolingSpeed(float heatingSpeed){
        SharedPreferences.Editor editor = sSettings.edit();
        editor.putFloat(COOLING_SPEED, heatingSpeed);
        editor.apply();
    }

}
