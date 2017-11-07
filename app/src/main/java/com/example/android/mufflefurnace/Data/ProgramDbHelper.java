package com.example.android.mufflefurnace.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.mufflefurnace.Data.ProgramContract.ProgramEntry;

/**
 * Created by admin on 7/17/2017.
 */

public class ProgramDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProgramDbHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "muffle_furnace";
    public static final int DATABASE_VERSION = 1;

    public ProgramDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PROGRAMS_TABLE = "CREATE TABLE " + ProgramEntry.TABLE_PROGRAMS + " ("
                + ProgramEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProgramEntry.COLUMN_PROGRAM_NAME + " TEXT NOT NULL, "
                + ProgramEntry.COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +");";
      //  + ProgramEntry.COLUMN_CREATED_AT + " DATETIME CURRENT_TIMESTAMP " +");";
        db.execSQL(SQL_CREATE_PROGRAMS_TABLE);

        String SQL_CREATE_POINTS_TABLE = "CREATE TABLE " + ProgramEntry.TABLE_POINTS + " ("
                + ProgramEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProgramEntry.COLUMN_PROGRAM_ID + " INTEGER NOT NULL, "
                + ProgramEntry.COLUMN_TEMPERATURE + " INTEGER NOT NULL, "
                + ProgramEntry.COLUMN_TIME + " INTEGER NOT NULL" +");";
        db.execSQL(SQL_CREATE_POINTS_TABLE);

        String SQL_CREATE_A_PROGRAMS_TABLE = "CREATE TABLE " + ProgramEntry.TABLE_A_PROGRAMS + " ("
                + ProgramEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProgramEntry.COLUMN_PROGRAM_ID + " INTEGER NOT NULL, "
                + ProgramEntry.COLUMN_STARTED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + ProgramEntry.COLUMN_FINISHED_AT + " DATETIME" +");";
        db.execSQL(SQL_CREATE_A_PROGRAMS_TABLE);

        String SQL_CREATE_A_POINTS_TABLE = "CREATE TABLE " + ProgramEntry.TABLE_A_POINTS + " ("
                + ProgramEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProgramEntry.COLUMN_A_PROGRAM_ID + " INTEGER NOT NULL, "
                + ProgramEntry.COLUMN_TEMPERATURE + " INTEGER NOT NULL, "
                + ProgramEntry.COLUMN_TIME + " INTEGER NOT NULL" +");";
        db.execSQL(SQL_CREATE_A_POINTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
