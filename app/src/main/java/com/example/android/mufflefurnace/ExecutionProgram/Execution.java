package com.example.android.mufflefurnace.ExecutionProgram;

import android.content.ContentResolver;
import android.net.Uri;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by admin on 9/7/2017.
 */

public class Execution {
    private Uri mCurrentProgramUri;
    String mCurrentProgramName;
    int mCurrentProgramId;


    ContentResolver contentResolver;


    public Execution (Uri currentProgramUri){
        mCurrentProgramUri = currentProgramUri;

    }

    public DataPoint[] getPointsFromDB (Uri mCurrentProgramUri){


        if (mCurrentProgramUri == null) {
            return null;
        }
        String[] projection = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME
        };


/*
        Cursor cursor = getContentResolver().query (
                mCurrentProgramUri,
                projection,
                null,
                null,
                null
        );


        if (cursor == null || cursor.getCount() < 1) {
            return null;
        }
        if (cursor.moveToFirst()) {
            int currentProgramIDIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);
            int currentProgramIdIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID);

            mCurrentProgramName = cursor.getString(currentProgramIDIndex);
            mCurrentProgramId = cursor.getInt(currentProgramIdIndex);


        }

*/
        return null;
    }


}
