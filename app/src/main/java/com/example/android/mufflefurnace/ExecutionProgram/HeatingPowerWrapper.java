package com.example.android.mufflefurnace.ExecutionProgram;

import android.support.annotation.Nullable;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by admin on 2/26/2018.
 */

public class HeatingPowerWrapper {

    private @Nullable
    Gpio gpio;

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
