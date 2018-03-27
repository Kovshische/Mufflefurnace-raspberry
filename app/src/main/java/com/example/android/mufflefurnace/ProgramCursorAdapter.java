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


        int i = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_CREATED_AT));
        Log.d(LOG_TAG, Integer.toString(i));


        final int program_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));

        textViewName.setText(name);
        textViewCreated.setText(created);
       // textViewCreated.setText(created);


    }



}
