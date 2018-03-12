package com.example.android.mufflefurnace.ExecutionProgram;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.mufflefurnace.Data.ProgramContract;
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

    public static final String BROADCAST_ACTION = "Control action";
    //for intent:
    public static final String TIME = "time";
    public static final String TIME_SEC = "timeSec";
    public static final String TARGET_TEMP = "targetTemp";
    public static final String SENSOR_TEMP = "sensorTemp";
    public static final String POWER_INSTANCE = "powerInstance";
    public static final String PROGRAM_STATUS = "programStatus";
    public static final int PROGRAM_END = 1;
    //GPIO
    private final static String GPIO_PIN_HEATING_POWER = "BCM21";
    static long startDate;
    static long currentDate;
    static int timeFromStartSec;
    static boolean powerInstance;
    static int programStatus;
    private final String LOG_TAG = PointManager.class.getSimpleName();
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;
    ArrayList<DataPoint> dataPointArrayList;
    int targetTemp;
    int sensorTemp;
    Intent myIntent;
    private HeatingPowerWrapper heatingPowerWrapper;
    private Integer ventPosition = ProgramContract.ProgramEntry.VENT_CLOSE;

    private PointManager pointManager;
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            calculateTimeFromSrart();
            //CalculateTemp should be before get program status;
            calculateTemp();
            getSensorTemp();
            getProgramStatus();
            //control power
            controlPower(sensorTemp, targetTemp);
            getPowerInstance();
            sendProgramParam();

            handler.postDelayed(this, 1000); // 0.1 second
        }
    };

    //Get time in seconds - return time in format HH:MM:SS
    public static String mTimeToString(int time) {

        int hours;
        String timeString;


        if (time < 24 * 60 * 60) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time * 1000);
        } else {

            hours = time / (60 * 60);

            SimpleDateFormat sdf = new SimpleDateFormat(":mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time * 1000);

            timeString = Integer.toString(hours) + timeString;
        }
        return timeString;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(ControlService.BROADCAST_ACTION);
        heatingPowerWrapper = new HeatingPowerWrapper(GPIO_PIN_HEATING_POWER);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        myIntent = intent;

        startDate = Calendar.getInstance().getTimeInMillis();
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

    }

    @Override
    public void onDestroy() {
        heatingPowerWrapper.turnOff();
        heatingPowerWrapper.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    private void calculateTemp() {
        dataPointArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra("pointsArray");
        pointManager = new PointManager(dataPointArrayList);
        try {
            targetTemp = pointManager.getTemperature(timeFromStartSec);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.toString());
            //when program end
            targetTemp = 0;
            heatingPowerWrapper.turnOff();
        }
    }

    private int calculateTimeFromSrart() {
        currentDate = Calendar.getInstance().getTimeInMillis();
        long timeFromStart = currentDate - startDate;
        timeFromStart = timeFromStart / 1000;
        Long l = timeFromStart;
        timeFromStartSec = Integer.valueOf(l.intValue());
        return timeFromStartSec;
    }

    private void getSensorTemp() {
        try {
            Max6675 max6675 = new Max6675();
            sensorTemp = Math.round(max6675.getTemp());
            Log.i(LOG_TAG, "SensorTemp: " + sensorTemp + " °C");
            max6675.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getPowerInstance() {
        powerInstance = heatingPowerWrapper.getPowerInstance();
    }

    private void sendProgramParam() {
        Log.d(LOG_TAG, "entered DisplayInfo");

        intent.putExtra(TIME, mTimeToString(timeFromStartSec));
        intent.putExtra(TIME_SEC, timeFromStartSec);
        intent.putExtra(TARGET_TEMP, Integer.toString(targetTemp));
        intent.putExtra(SENSOR_TEMP, sensorTemp);
        intent.putExtra(POWER_INSTANCE, powerInstance);
        intent.putExtra(PROGRAM_STATUS, programStatus);


        sendBroadcast(intent);
    }

    private void controlPower(double sensorTemp, int targetTemp) {
        if (sensorTemp < targetTemp) {
            heatingPowerWrapper.turnOn();
        } else {
            heatingPowerWrapper.turnOff();
        }
    }

    private void getProgramStatus() {
        programStatus = pointManager.getProgramStatus();
    }
}
