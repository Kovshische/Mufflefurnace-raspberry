package com.example.android.mufflefurnace;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by admin on 7/21/2017.
 */

public class PointCursorAdapter extends CursorAdapter {

    private static final int day = 24*60;

    public PointCursorAdapter(Context context,Cursor cursor){
        super(context, cursor, 0);
    }




    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_point, parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView textViewTime  = (TextView) view.findViewById(R.id.time);
        TextView textViewTemperature = (TextView) view.findViewById(R.id.temperature);

        //Extract properties from cursor
        int time = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME));
        int temperature  = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE));

        String timeString = mTimeToString(time);
        String temperatureString = Integer.toString(temperature);

        final int program_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));


        textViewTime.setText(timeString);
        textViewTemperature.setText(temperatureString);
        // textViewCreated.setText(temperature);


    }

    public static String mTimeToString (int time){

        int hours;

        String timeString;


        if (time < 24*60){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time*60*1000);
        }

        else {

            hours = time/60;

            SimpleDateFormat sdf = new SimpleDateFormat(":mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            timeString = sdf.format(time*60*1000);

            timeString = Integer.toString(hours) + timeString;
        }



        return timeString;
    }


}
