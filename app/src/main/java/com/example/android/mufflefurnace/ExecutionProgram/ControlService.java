package com.example.android.mufflefurnace.ExecutionProgram;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by admin on 2/16/2018.
 */

public class ControlService extends Service {

    private final String LOG_TAG = PointManager.class.getSimpleName();
    public static final String CONTROL_ACTION = "Control action";
    private final Handler handler = new Handler();
    Intent intent;
    int counter =0;
    static long startDate;
    static long currentDate;
    static int timeFromStartSec;
    static boolean powerInstance;
    ArrayList<DataPoint> dataPointArrayList;
    int targetTemp;
    float sensorTemp;

    Intent myIntent;

    //GPIO
    private final static  String GPIO_PIN_HEATING_POWER = "BCM21";
    private HeatingPowerWrapper heatingPowerWrapper;

    @Override
    public void onCreate(){
        super.onCreate();
        intent = new Intent(ControlService.CONTROL_ACTION);
        heatingPowerWrapper = new HeatingPowerWrapper(GPIO_PIN_HEATING_POWER);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        startDate = Calendar.getInstance().getTimeInMillis();
        myIntent = intent;
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    @Override
    public void onDestroy(){
        heatingPowerWrapper.turnOff();
        heatingPowerWrapper.onDestroy();
    }



    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            calculateTimeFromSrart();
            calculateTemp();
            getSensorTemp();
            //control power
            controlPower(Math.round(sensorTemp), targetTemp);
            getPowerInstance();
            displayTempTime();

           handler.postDelayed(this, 1000); // 0.1 second
        }
    };

    private void calculateTemp() {

        dataPointArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra("pointsArray");
        PointManager pointManager = new PointManager(dataPointArrayList);
        try {
            targetTemp = pointManager.getTemperature(timeFromStartSec);
        } catch (IllegalArgumentException e){
            Log.d(LOG_TAG, e.toString());
            //when program end
            targetTemp = 0;
            heatingPowerWrapper.turnOff();
        }
    }

    private int calculateTimeFromSrart() {
        currentDate = Calendar.getInstance().getTimeInMillis();
        long timeFromStart = currentDate - startDate;
        timeFromStart = timeFromStart/1000;
        Long l = timeFromStart;
        timeFromStartSec = Integer.valueOf(l.intValue());
        return timeFromStartSec;
    }
    private void getSensorTemp () {
        try {
            Max6675 max6675 = new Max6675();
            sensorTemp = max6675.getTemp();
            Log.i(LOG_TAG, "SensorTemp: " + sensorTemp + " °C");
           max6675.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getPowerInstance () {
            powerInstance = heatingPowerWrapper.getPowerInstance();
     }

    private void displayTempTime() {
        Log.d(LOG_TAG, "entered DisplayInfo");

        intent.putExtra("time", mTimeToString(timeFromStartSec));
        intent.putExtra("targetTemp",Integer.toString(targetTemp));
        intent.putExtra("sensorTemp", Float.toString(sensorTemp));
        intent.putExtra("powerInstance", powerInstance);

        sendBroadcast(intent);
    }


    //Get time in seconds - return time in format HH:MM:SS
    public static String mTimeToString (int time){

        int hours;
        String timeString;


        if (time < 24*60*60){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time*1000);
        }

        else {

            hours = time/(60*60);

            SimpleDateFormat sdf = new SimpleDateFormat(":mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time*1000);

            timeString = Integer.toString(hours) + timeString;
        }
        return timeString;
    }

    private void controlPower ( int sensorTemp, int targetTemp){
        if (sensorTemp < targetTemp){
            heatingPowerWrapper.turnOn();
        }
        else {
            heatingPowerWrapper.turnOff();
        }
    }
}
