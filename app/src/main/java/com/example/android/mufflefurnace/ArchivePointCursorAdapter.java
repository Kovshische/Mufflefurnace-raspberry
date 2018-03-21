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

/**
 * Created by admin on 3/21/2018.
 */

public class ArchivePointCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = ArchivePointCursorAdapter.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;


    public ArchivePointCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_a_point, viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView timeTextView  = (TextView) view.findViewById(R.id.a_time);
        TextView targetTemTextView  = (TextView) view.findViewById(R.id.a_target_t);
        TextView sensorTemTextView  = (TextView) view.findViewById(R.id.a_sensor_t);
        TextView ventTextView = (TextView) view.findViewById(R.id.a_vent);
        TextView powerTextView  = (TextView) view.findViewById(R.id.a_power);
        TextView doorTextView  = (TextView) view.findViewById(R.id.a_door);

        Integer time = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_TIME));
        String timeString = PointCursorAdapter.mTimeToString(time);
        timeTextView.setText(timeString);

        Integer targetTemInteger = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_TARGET_TEMPERATURE));
        String targetTemString = Integer.toString(targetTemInteger);
        targetTemTextView.setText(targetTemString);

        Integer sensorTemInteger = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE));
        String sensorTemString = Integer.toString(sensorTemInteger);
        sensorTemTextView.setText(sensorTemString);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ifVentEnabled = sharedPreferences.getBoolean(context.getString(R.string.settings_vent_options_key),false);
        if (ifVentEnabled == true) {
            Integer ventInt = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_VENT));
            String ventString = PointCursorAdapter.ventToString(ventInt);
            ventTextView.setText(ventString);
        } else {
            ventTextView.setVisibility(View.GONE);
        }

        Integer powerInt = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_POWER));
        String powerString = PointCursorAdapter.powerToString(powerInt);
        powerTextView.setText(powerString);

        Integer doorInt = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_DOOR));
        String doorString = PointCursorAdapter.doorToString(doorInt);
        doorTextView.setText(doorString);

    }


}
