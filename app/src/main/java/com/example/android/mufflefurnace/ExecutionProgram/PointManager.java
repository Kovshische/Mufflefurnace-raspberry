package com.example.android.mufflefurnace.ExecutionProgram;

import com.jjoe64.graphview.series.DataPoint;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by admin on 11/28/2017.
 */

public class PointManager {
    private ArrayList<DataPoint> dataPointArrayList;

    private final String LOG_TAG = PointManager.class.getSimpleName();

    private double x;
    private double k;
    private double b;

    private int temperature;
    private Time time;
    private int timeInSeconds;
    private  Time currentTime;
    private  int currentTimeSeconds;

    private boolean isContainTime = false;

    private int startTime;
    private int finishTime;
    private int startTemperature;
    private int finishTemperature;

    public PointManager (ArrayList<DataPoint> dataPointArrayList){
        this.dataPointArrayList = dataPointArrayList;
    }

    // currentTime should be in seconds
    //dataPointArrayList - time should ne in hours (doble)
    public int getTemperature (int currentTimeSeconds){

        //check that graph contain at least 2 points.
        int i = dataPointArrayList.size();
        if (i < 2){
            throw new IllegalArgumentException("Program should contain more than 1 point");
        }

        //check that time < max time
        if ( currentTimeSeconds > (int) 3600*dataPointArrayList.get(i-1).getX()){
            throw new IllegalArgumentException("Time is out of range, your time = " + currentTimeSeconds + "max time =" + 3600*dataPointArrayList.get(i-1).getX() );
        }


        int ii = 0;
        while (isContainTime == false & ii < dataPointArrayList.size()-1){
            double startTimeDouble = dataPointArrayList.get(ii).getX();
            //time in seconds
            startTime = (int) (startTimeDouble*3600);
            //Log.i("Start time", "" + startTime);

            double finishTimeDouble = dataPointArrayList.get(ii+1).getX();
            //time in seconds
            finishTime = (int) (finishTimeDouble*3600);
            //Log.i("Finish time", "" + finishTime);



            if (startTime <= currentTimeSeconds & finishTime >= currentTimeSeconds){
                isContainTime = true;

                startTemperature = (int) dataPointArrayList.get(ii).getY();
                //Log.i("Start temperature", "" + startTemperature);
                finishTemperature = (int) dataPointArrayList.get((ii+1)).getY();
                //Log.i("Finish temperature", "" + finishTemperature);

                temperature = startTemperature + (currentTimeSeconds - startTime) * (finishTemperature - startTemperature)/(finishTime - startTime);

                return temperature;
            }
            ii = ii+1;
        }

        return temperature;
    }
}
