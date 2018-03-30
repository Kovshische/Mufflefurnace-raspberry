package com.example.android.mufflefurnace;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;

/**
 * Created by admin on 3/19/2018.
 */

public class ArchiveProgramCursorAdapter extends CursorAdapter {


    public ArchiveProgramCursorAdapter(Context context,Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_program, parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView textViewName  = (TextView) view.findViewById(R.id.program_name);
        TextView textViewStarted = (TextView) view.findViewById(R.id.program_created);
        TextView textViewCreatedAtLabel = (TextView) view.findViewById(R.id.created_at_label);

        textViewCreatedAtLabel.setText("Launched at: ");

        //Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME));
        //String created = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_CREATED_AT));
        String created  = cursor.getString(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_STARTED_AT));


        final int program_id = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));

        textViewName.setText(name);
        textViewStarted.setText(created);
        // textViewCreated.setText(created);


    }
}
