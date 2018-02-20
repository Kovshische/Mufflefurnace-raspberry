package com.example.android.mufflefurnace.ExecutionProgram;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

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
    long startDate;
    long currentDate;
    int timeFromStartSec;
    ArrayList<DataPoint> dataPointArrayList;
    int temp;

    Intent myIntent;


    @Override
    public void onCreate(){
        super.onCreate();

        intent = new Intent(ControlService.CONTROL_ACTION);
        startDate = Calendar.getInstance().getTimeInMillis();



    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        myIntent = intent;

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            calculateTimeFromSrart();
            calculateTemp();
           displayTempTime();
           handler.postDelayed(this, 1000); // 10 seconds
        }
    };

    private int calculateTemp() {

        dataPointArrayList = (ArrayList<DataPoint>) myIntent.getSerializableExtra("pointsArray");
        PointManager pointManager = new PointManager(dataPointArrayList);
        temp = pointManager.getTemperature(timeFromStartSec);

        return temp;
    }

    private int calculateTimeFromSrart() {
        currentDate = Calendar.getInstance().getTimeInMillis();
        long timeFromStart = currentDate - startDate;
        timeFromStart = timeFromStart/1000;
        Long l = timeFromStart;
        timeFromStartSec = Integer.valueOf(l.intValue());
        return timeFromStartSec;
    }

    private void displayTempTime() {
        Log.d(LOG_TAG, "entered DisplayInfo");


        intent.putExtra("time", mTimeToString(timeFromStartSec));
        intent.putExtra("temp",Integer.toString(temp));
        intent.putExtra("counter", String.valueOf(++counter));
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
}
