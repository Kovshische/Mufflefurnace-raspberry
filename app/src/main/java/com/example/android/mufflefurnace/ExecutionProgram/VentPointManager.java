package com.example.android.mufflefurnace.ExecutionProgram;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

/**
 * Created by admin on 3/12/2018.
 */

public class VentPointManager {
    private final String LOG_TAG = PointManager.class.getSimpleName();
    private ArrayList<DataPoint> ventArrayList;
    private ArrayList<DataPoint> ventOpenArrayList;
    private ArrayList<DataPoint> ventCloseArrayList;
    private Integer ventStatus = ProgramContract.ProgramEntry.VENT_CLOSE;
    private Boolean isContainTime = false;
    private int currentTimeSeconds;

    public VentPointManager(ArrayList<DataPoint> ventArrayList) {
        this.ventArrayList = ventArrayList;

    }
     public int getVentStatus (int currentTimeSeconds){
        this.currentTimeSeconds = currentTimeSeconds;
         int i = ventArrayList.size();
         if (i < 1){
             return ProgramContract.ProgramEntry.VENT_CLOSE;
         } else {
             int ii = 0;
             while ( ii < i){
                 double timeDouble =  ventArrayList.get(ii).getX();
                 //time in seconds
                 Integer time = (int) (timeDouble * 3600);

                 if (currentTimeSeconds >= time){
                     ventStatus = (int) ventArrayList.get(ii).getY();
                 }
                 ii = ii+1;
             }
         }

        return ventStatus;
    }
}
