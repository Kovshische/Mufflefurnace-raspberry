package com.example.android.mufflefurnace.ExecutionProgram;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

/**
 * Created by admin on 3/12/2018.
 */

public class VentPointManager {
    private final String LOG_TAG = PointManager.class.getSimpleName();
    private ArrayList<DataPoint> ventOpenArrayList;
    private ArrayList<DataPoint> ventCloseArrayList;
    private Integer ventStatus = ProgramContract.ProgramEntry.VENT_CLOSE;

    public VentPointManager(ArrayList<DataPoint> ventOpenArrayList, ArrayList<DataPoint> ventCloseArrayList) {
        this.ventOpenArrayList = ventOpenArrayList;
        this.ventCloseArrayList = ventCloseArrayList;
    }
     public int getVentStatus (int currentTimeSeconds){




        return ventStatus;
    }
}
