package com.example.android.mufflefurnace;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by admin on 7/21/2017.
 */

public class ProgramCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = ProgramCursorAdapter.class.getSimpleName();

    public ProgramCursorAdapter(Context context,Cursor cursor){
        super(context, cursor, 0);
    }




    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_program, parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView textViewName  = (TextView) view.findViewById(R.id.program_name);
        TextView textViewCreated = (TextView) view.findViewById(R.id.program_created);

        //Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME));
        //String created = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_CREATED_AT));
        String created  = cursor.getString(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_CREATED_AT));
        created = convertDate(created);


//        int i = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_CREATED_AT));
//        Log.d(LOG_TAG, Integer.toString(i));


        final int program_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));

        textViewName.setText(name);
        textViewCreated.setText(created);
       // textViewCreated.setText(created);


    }

    public static String convertDate (String dataSQL){
        String dateTime ="";
        Date date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = sdf.parse(dataSQL);
        } catch (ParseException e) {
            date = null;
            e.printStackTrace();
            Log.d(LOG_TAG, "incorrect data time format " + dataSQL);
        }

        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy   HH:mm");
        Calendar calendar = Calendar.getInstance();
        sdf2.setTimeZone(calendar.getTimeZone());
        dateTime = sdf2.format(date);

        return dateTime;
    }

}
