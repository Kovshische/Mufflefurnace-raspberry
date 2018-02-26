package com.example.android.mufflefurnace.ExecutionProgram;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by admin on 2/26/2018.
 */

public class HeatingPowerWrapper {

    private @Nullable
    Gpio gpio;

    private final static String LOG_TAG = HeatingPowerWrapper.class.getSimpleName();

    public HeatingPowerWrapper (String gpioPin){
        try {
            PeripheralManagerService service = new PeripheralManagerService();
            gpio = service.openGpio(gpioPin);
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnOn (){
        if (gpio == null){
            return;
        }
        try {
            gpio.setValue(true);
            Log.d(LOG_TAG, "turn on");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnOff (){
        if (gpio == null){
            return;
        }
        try {
            gpio.setValue(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        try {
            gpio.close();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            gpio = null;
        }
    }

}
