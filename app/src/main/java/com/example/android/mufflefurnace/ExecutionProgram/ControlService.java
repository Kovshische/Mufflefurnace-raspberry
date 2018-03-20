package com.example.android.mufflefurnace.ExecutionProgram;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ProgramViewActivity;
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
    public static final String VENT_STATUS = "ventStatus";
    public static final int PROGRAM_END = 1;
    public static final String INTENT_DATA_POINTS_ARRAY_LIST = "intentDataPointsArrayList";
    public static final String INTENT_VENT_ARRAY_LIST = "intentVentArrayList";
    public static final String START_TIME = "setStartTime";
    //GPIO
    private final static String GPIO_PIN_HEATING_POWER = "BCM21";
    static long startDate;
    static long currentDate;
    static Integer timeFromStartSec;
    static boolean powerInstance;
    static int programStatus;
    private final String LOG_TAG = ControlService.class.getSimpleName();
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;
    ArrayList<DataPoint> dataPointArrayList;
    ArrayList<DataPoint> ventArrayList;
    int targetTemp;
    int sensorTemp;
    Intent myIntent;
    private HeatingPowerWrapper heatingPowerWrapper;
    private Integer ventStatus = ProgramContract.ProgramEntry.VENT_CLOSE;

    private PointManager pointManager;
    private VentPointManager ventPointManager;

    private String startTimeString = "";
    private Boolean waitToStart = false;
    private long setStartTime;

    private int aProgramId;



    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            calculateTimeToStart();
            calculateTimeFromStart();
            //CalculateTemp should be before get program status;
            calculateTemp();
            calculateVentStatus();

            getSensorTemp();
            getProgramStatus();
            //control power
            controlPower(sensorTemp, targetTemp);
            getPowerInstance();
            sendProgramParam();
            saveToTheDB();

            handler.postDelayed(this, 100); // 0.1 second
        }
    };

    //Get time in seconds - return time in format HH:MM:SS
    public static String mTimeToString(int time) {

        int hours;
        String timeString;

        if (time >=0 ){
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
        } else {
            time = (time - 1) * (-1);
            if (time < 24 * 60 * 60) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                timeString = sdf.format(time * 1000);
                timeString = "-" +timeString;
            } else {

                hours = time / (60 * 60);

                SimpleDateFormat sdf = new SimpleDateFormat(":mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                timeString = sdf.format(time * 1000);

                timeString = Integer.toString(hours) + timeString;
                timeString = "-" +timeString;
            }
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

        Calendar calendar = (Calendar) myIntent.getSerializableExtra(ProgramViewActivity.INTENT_CALENDAR);
        setStartTime = calendar.getTimeInMillis();

        aProgramId = myIntent.getIntExtra(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID, 0);
        dataPointArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra(INTENT_DATA_POINTS_ARRAY_LIST);
        pointManager = new PointManager(dataPointArrayList);
        ventArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra(INTENT_VENT_ARRAY_LIST);
        ventPointManager = new VentPointManager(ventArrayList);

//        ventArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra(INTENT_VENT_ARRAY_LIST);
//        ventPointManager = new VentPointManager(ventArrayList);
//        Log.i(LOG_TAG, "VentArrayList");

        startDate = Calendar.getInstance().getTimeInMillis();
        if (setStartTime > startDate ){
            startDate = setStartTime;
        }


//        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0.1 second


    }

    @Override
    public void onDestroy() {
        heatingPowerWrapper.turnOff();
        heatingPowerWrapper.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
    }

    private void calculateTemp() {
//        dataPointArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra(INTENT_DATA_POINTS_ARRAY_LIST);
//        pointManager = new PointManager(dataPointArrayList);
        try {
            targetTemp = pointManager.getTemperature(timeFromStartSec);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.toString());
            //when program end
            targetTemp = 0;
            heatingPowerWrapper.turnOff();
        }


    }

    private void calculateVentStatus(){


        try {
            ventStatus = ventPointManager.getVentStatus(timeFromStartSec);
        } catch (IllegalArgumentException e){
            Log.d(LOG_TAG, e.toString());
        }

    }

    private int calculateTimeFromStart() {

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
//            Log.i(LOG_TAG, "SensorTemp: " + sensorTemp + " °C");
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
        intent.putExtra(VENT_STATUS, ventStatus);
        intent.putExtra(START_TIME, startTimeString);


        sendBroadcast(intent);
    }

    private void controlPower(double sensorTemp, int targetTemp) {
        if (sensorTemp < targetTemp) {
            heatingPowerWrapper.turnOn();
        } else {
            heatingPowerWrapper.turnOff();
        }
    }

    private void controlVent(int ventStatus){

    }

    private void getProgramStatus() {
        programStatus = pointManager.getProgramStatus();
    }

    private void calculateTimeToStart() {

        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (setStartTime > currentTime){
            waitToStart = true;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MMM HH:mm");
            startTimeString = simpleDateFormat.format(setStartTime);
        } else {
            waitToStart = false;
            startTimeString = "";
        }

//        Log.d(LOG_TAG,"setStartTime " + setStartTime);
//        Log.d(LOG_TAG,"currentTime " + currentTime);
//        Log.d(LOG_TAG, "startTimeString" + startTimeString);
    }
    private void saveToTheDB (){
        ContentValues valuesArchivePoint = new ContentValues();
        valuesArchivePoint.put(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID, aProgramId);
        valuesArchivePoint.put(ProgramContract.ProgramEntry.COLUMN_A_TIME, timeFromStartSec);
        valuesArchivePoint.put(ProgramContract.ProgramEntry.COLUMN_A_TARGET_TEMPERATURE, targetTemp);
        valuesArchivePoint.put(ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE, sensorTemp);

        Uri newUri = getContentResolver().insert(ProgramContract.ProgramEntry.CONTENT_URI_A_POINTS, valuesArchivePoint);
        if (newUri == null){
            Log.i(LOG_TAG, "Error with saving point to archive");
        }
    }
}
