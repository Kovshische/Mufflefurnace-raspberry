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

    private int ii = 0;
    private int startTime;
    private int finishTime;
    private int startTemperature;
    private int finishTemperature;

    public PointManager (ArrayList<DataPoint> dataPointArrayList){
        this.dataPointArrayList = dataPointArrayList;
    }

    // currentTime should be in seconds
    public int getTemperature (int currentTimeSeconds){

        //check that graph contain at least 2 points.
        int i = dataPointArrayList.size();
        if (i < 2){
            throw new IllegalArgumentException("Program should contain more than 1 point");
        }

        //check that time < max time
        if ( currentTimeSeconds > (int) 60*dataPointArrayList.get(i).getX()){
            throw new IllegalArgumentException("Time is out of range");
        }


        while (isContainTime == false & ii < dataPointArrayList.size()){
            startTime = (int) dataPointArrayList.get(ii).getX();
            //time in seconds
            startTime = startTime*60;

            finishTime = (int) dataPointArrayList.get((ii+1)).getX();
            //time in seconds
            finishTime = finishTime*60;



            if (startTime <= currentTimeSeconds & finishTime >= currentTimeSeconds){
                isContainTime = true;

                startTemperature = (int) dataPointArrayList.get(ii).getY();
                finishTemperature = (int) dataPointArrayList.get((ii+1)).getY();

                temperature = startTemperature + (currentTimeSeconds - startTime) * (finishTemperature - startTemperature)/(finishTime - startTime);

                return temperature;
            }
            ii = ii +1;
        }

        return temperature;
    }
}
