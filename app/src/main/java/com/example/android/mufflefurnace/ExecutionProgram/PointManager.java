package com.example.android.mufflefurnace.ExecutionProgram;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by admin on 11/28/2017.
 */

public class PointManager {
    private final String LOG_TAG = PointManager.class.getSimpleName();
    private ArrayList<DataPoint> dataPointArrayList;
    private double x;
    private double k;
    private double b;

    private int temperature;
    private Time time;
    private int timeInSeconds;
    private Time currentTime;
    private int currentTimeSeconds;

    private boolean isContainTime = false;

    private int startTime;
    private int finishTime;
    private int startTemperature;
    private int finishTemperature;
    private int programStatus;
    private int endTimeSeconds;
//    private Integer ventVentStatus = ProgramContract.ProgramEntry.VENT_CLOSE;

    public static final int PROGRAM_END = 1;
    public static final int PROGRAM_EXECUTING = 2;

    public PointManager(ArrayList<DataPoint> dataPointArrayList) {
        this.dataPointArrayList = dataPointArrayList;
    }

    // currentTime should be in seconds
    //dataPointArrayList - time should ne in hours (doble)
    public final int getTemperature(int currentTimeSeconds) {

        //check that graph contain at least 2 points.
        int i = dataPointArrayList.size();
        if (i < 2) {
            throw new IllegalArgumentException("Program should contain more than 1 point");
        }

        //check that time < max time
        else if (currentTimeSeconds > (int) 3600 * dataPointArrayList.get(i -1).getX()) {
            Log.i(LOG_TAG, "program end");
            programStatus = PROGRAM_END;
            throw new IllegalArgumentException("Time is out of range, your time = " + currentTimeSeconds + " max time = " + 3600 * dataPointArrayList.get(i - 1).getX());
            //return 0;
        }

        else if((currentTimeSeconds <= (int) 3600 * dataPointArrayList.get(i - 1).getX())) {
            programStatus = PROGRAM_EXECUTING;
            int ii = 0;
            while (isContainTime == false & ii < dataPointArrayList.size() - 1) {
                double startTimeDouble = dataPointArrayList.get(ii).getX();
                //time in seconds
                startTime = (int) (startTimeDouble * 3600);
                //Log.i("Start time", "" + startTime);

                double finishTimeDouble = dataPointArrayList.get(ii + 1).getX();
                //time in seconds
                finishTime = (int) (finishTimeDouble * 3600);
                //Log.i("Finish time", "" + finishTime);


                if (startTime <= currentTimeSeconds & finishTime >= currentTimeSeconds) {
                    isContainTime = true;

                    startTemperature = (int) dataPointArrayList.get(ii).getY();
                    //Log.i("Start temperature", "" + startTemperature);
                    finishTemperature = (int) dataPointArrayList.get((ii + 1)).getY();
                    //Log.i("Finish temperature", "" + finishTemperature);

                    temperature = startTemperature + (currentTimeSeconds - startTime) * (finishTemperature - startTemperature) / (finishTime - startTime);

//                    return temperature;
                }
                ii = ii + 1;
            }
        } else {
            throw new IllegalArgumentException("Time is out of range, your time = " + currentTimeSeconds + "max time =" + 3600 * dataPointArrayList.get(i - 1).getX());
        }

        return temperature;
    }
    public int getProgramStatus(){
        return programStatus;
    }

    public int getEndTimeSeconds (){

        int i = dataPointArrayList.size();
        endTimeSeconds = (int) (3600 * dataPointArrayList.get(i -1).getX());
        return endTimeSeconds;

    }
}
