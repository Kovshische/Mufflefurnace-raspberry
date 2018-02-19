package com.example.android.mufflefurnace.ExecutionProgram;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

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

    @Override
    public void onCreate(){
        super.onCreate();

        intent = new Intent(ControlService.CONTROL_ACTION);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
        startDate = Calendar.getInstance().getTimeInMillis();
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            calculateTimeFromSrart();
           displayTempTime();
           handler.postDelayed(this, 1000); // 10 seconds
        }
    };

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


        intent.putExtra("time", String.valueOf(timeFromStartSec));
        intent.putExtra("counter", String.valueOf(++counter));
        sendBroadcast(intent);
    }
}
