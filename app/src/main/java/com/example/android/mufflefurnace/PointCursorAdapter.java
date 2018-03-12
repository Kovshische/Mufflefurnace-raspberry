package com.example.android.mufflefurnace;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
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
    private static final String LOG_TAG = PointCursorAdapter.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;


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
        TextView textViewVent = (TextView)view.findViewById(R.id.vent);

        //Extract properties from cursor
        Integer time = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME));

        Integer temperature = null;
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE))){
            temperature  = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE));
        }

        Integer vent = null;
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_VENT))){
            vent = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_VENT));
        }

        String timeString = mTimeToString(time);
        String temperatureString;
        if (temperature != null){
             temperatureString = Integer.toString(temperature);
        } else {
            temperatureString = "";
        }
        String ventString;
        if (vent == null){
            ventString ="";
        }else if (vent == ProgramContract.ProgramEntry.VENT_OPEN){
            ventString = "Open";
        } else if (vent == ProgramContract.ProgramEntry.VENT_CLOSE){
            ventString ="Close";
        } else {
            ventString ="";
        }

        final int program_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));


        textViewTime.setText(timeString);
        textViewTemperature.setText(temperatureString);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ifVentEnabled = sharedPreferences.getBoolean(context.getString(R.string.settings_vent_options_key),false);
        if (ifVentEnabled == true){
            textViewVent.setText(ventString);
        } else {
            textViewVent.setVisibility(View.GONE);
        }

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
