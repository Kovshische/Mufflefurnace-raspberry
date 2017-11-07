package com.example.android.mufflefurnace.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by admin on 7/21/2017.
 */

public class ProgramProvider  extends ContentProvider{

    public final String LOG_TAG = ProgramContract.class.getSimpleName();

    private static final int PROGRAMS = 100;
    private static final int PROGRAM_ID = 101;

    private static final int POINTS = 200;
    private static final int POINT_ID = 201;

    private static final int A_PROGRAMS = 300;
    private static final int A_PROGRAM_ID = 301;

    private static final int A_POINTS = 400;
    private static final int A_POINT_ID = 401;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_PROGRAMS, PROGRAMS);
        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_PROGRAMS + "/#", PROGRAM_ID);

        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_POINTS, POINTS);
        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_POINTS + "/#", POINT_ID);

        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_A_PROGRAMS, A_PROGRAMS);
        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_A_PROGRAMS + "/#", A_PROGRAM_ID);

        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_A_POINTS, A_POINTS);
        mUriMatcher.addURI(ProgramContract.CONTENT_AUTHORITY, ProgramContract.PATH_A_POINTS + "/#", A_POINT_ID);

    }

    private ProgramDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProgramDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri,  String[] projection,  String selection,  String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //This cursor will hold the result of the query

        Cursor cursor;
        // figure out if the URI mather can match the URI to a specific code

        int match = mUriMatcher.match(uri);
        switch (match) {
            case PROGRAMS:
                // For the PROGRAMS code, query the program table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_PROGRAMS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                        );
                break;

            case PROGRAM_ID:
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new  String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_PROGRAMS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case POINTS:
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_POINTS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case POINT_ID:
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new  String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_POINTS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case A_PROGRAMS:
                // For the PROGRAMS code, query the program table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case A_PROGRAM_ID:
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new  String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case A_POINTS:
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_A_POINTS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case A_POINT_ID:
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new  String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProgramContract.ProgramEntry.TABLE_A_POINTS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;


            default:
                throw new IllegalArgumentException("Cannot query, unknown URI" + uri);
        }
                //Set notification  URI on the Cursor
                // so we know what content URI the Cursor was created for.
                // If the data at this URI change, then we know we need to update the Cursor.
                cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public String getType( Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match){
            case  PROGRAMS:
                return ProgramContract.ProgramEntry.CONTENT_LIST_PROGRAMS_TYPE;
            case PROGRAM_ID:
                return ProgramContract.ProgramEntry.CONTENT_ITEM_PROGRAM_TYPE;
            case POINTS:
                return ProgramContract.ProgramEntry.CONTENT_LIST_POINTS_TYPE;
            case POINT_ID:
                return ProgramContract.ProgramEntry.CONTENT_ITEM_POINT_TYPE;
            case  A_PROGRAMS:
                return ProgramContract.ProgramEntry.CONTENT_LIST_A_PROGRAMS_TYPE;
            case A_PROGRAM_ID:
                return ProgramContract.ProgramEntry.CONTENT_ITEM_A_PROGRAM_TYPE;
            case A_POINTS:
                return ProgramContract.ProgramEntry.CONTENT_LIST_A_POINTS_TYPE;
            case A_POINT_ID:
                return ProgramContract.ProgramEntry.CONTENT_ITEM_A_POINT_TYPE;
            default:
                throw  new  IllegalArgumentException("Unknown" + uri + "witch match");
        }

    }


    @Override
    public Uri insert( Uri uri, ContentValues values) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case PROGRAMS:
                return insertProgram(uri, values);
            case POINTS:
                return insertPoint(uri, values);
            case A_PROGRAMS:
                return insertArchiveProgram(uri, values);
            case A_POINTS:
                return insertArchivePoint(uri, values);
            default:
                throw new IllegalArgumentException("Insention is not supported for " + uri);
        }
    }

    private Uri insertProgram(Uri uri, ContentValues contentValues){
        //sanity checks
        String name = contentValues.getAsString(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);
        if (name == null){
            throw new IllegalArgumentException("Program requires a name");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ProgramContract.ProgramEntry.TABLE_PROGRAMS, null, contentValues);

        if (id == -1){
            Log.e (LOG_TAG, "Failed to insert row for" + uri);
            return null;
        }
        //Notify all listeners that the data has changed for the pet content URI
        // uri: content://com/example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertPoint(Uri uri, ContentValues contentValues){
        //Check that the time is not null
        Integer time = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TIME);
        if (time == null && time < 0){
            throw new IllegalArgumentException("Point requires a valid time");
        }

        //Check that the temperature is not null
        Integer temperature = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
        if (temperature == null && temperature < 0){
            throw new IllegalArgumentException("Point requires a valid temperature");
        }

        //Check that the PROGRAM_ID is not null
        Integer programId = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID);
        if (programId == null ){
            throw new IllegalArgumentException("Program Id require a ID (integer)");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new point with the given values
        long id = database.insert(ProgramContract.ProgramEntry.TABLE_POINTS,null,contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the programs content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertArchiveProgram(Uri uri, ContentValues contentValues){
        //sanity checks
        Integer program_id = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID);
        if (program_id == null){
            throw new IllegalArgumentException("Program requires a name");
        }

        // Should add datetime. !!!!!

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS, null, contentValues);

        if (id == -1){
            Log.e (LOG_TAG, "Failed to insert row for" + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the pet content URI
        // uri: content://com/example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertArchivePoint(Uri uri, ContentValues contentValues){
        //Check that the time is not null
        Integer time = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TIME);
        if (time == null && time < 0){
            throw new IllegalArgumentException("Point requires a valid time");
        }

        //Check that the temperature is not null
        Integer temperature = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
        if (temperature == null && temperature < 0){
            throw new IllegalArgumentException("Point requires a valid temperature");
        }

        //Check that the PROGRAM_ID is not null
        Integer archiveProgramId = contentValues.getAsInteger(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID);
        if (archiveProgramId == null ){
            throw new IllegalArgumentException("ArchiveProgram Id require a ID (integer)");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new point with the given values
        long id = database.insert(ProgramContract.ProgramEntry.TABLE_A_POINTS, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the programs content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Track the number of rows that were deleted
        int rowDeleted;

        final int match = mUriMatcher.match(uri);
        switch (match){
            case PROGRAMS:
                // Delete all rows that match the selection and selection args
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_PROGRAMS, selection, selectionArgs);
                break;
            case PROGRAM_ID:
                // Delete single row given by the ID in the URI
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_PROGRAMS, selection, selectionArgs);
                break;
            case POINTS:
                // Delete all rows that match the selection and selection args
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_POINTS, selection, selectionArgs);
                break;
            case POINT_ID:
                // Delete single row given by the ID in the URI
                selection = ProgramContract.ProgramEntry._ID +"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_POINTS, selection,selectionArgs);
                break;
            case A_PROGRAMS:
                // Delete all rows that match the selection and selection args
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS, selection, selectionArgs);
                break;
            case A_PROGRAM_ID:
                // Delete single row given by the ID in the URI
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS, selection, selectionArgs);
                break;
            case A_POINTS:
                // Delete all rows that match the selection and selection args
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_A_POINTS, selection, selectionArgs);
                break;
            case A_POINT_ID:
                // Delete single row given by the ID in the URI
                selection = ProgramContract.ProgramEntry._ID +"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(ProgramContract.ProgramEntry.TABLE_A_POINTS, selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delettion is not supported fo " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return  rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);
        switch (match){
            case PROGRAMS:
                return updateProgram (uri, values,selection, selectionArgs);
            case PROGRAM_ID:
                // For the Program_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProgram(uri,values,selection,selectionArgs);
            case POINTS:
                return updatePoint (uri, values, selection, selectionArgs);
            case POINT_ID:
                selection = ProgramContract.ProgramEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return  updatePoint (uri, values, selection, selectionArgs);
            case A_PROGRAMS:
                return updateArchiveProgram (uri, values,selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    private int updateProgram(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present
        // check that the name value is not null.
        if(values.containsKey(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME)){
            String name = values.getAsString(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);
            if (name == null){
                throw new IllegalArgumentException("Program requires a name");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0){
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProgramContract.ProgramEntry.TABLE_PROGRAMS, values, selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    private int updatePoint (Uri uri, ContentValues values, String selection, String[] selectionArg){
        // If the {@link ProgramEntry#COLUMN_TIME} Key is present
        //Check that the time value is not null, <0.
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_TIME)){
            Integer time = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TIME);
            if (time == null && time < 0){
                throw new IllegalArgumentException("Point requires a valid time");
            }
        }

        // If the {@link ProgramEntry#COLUMN_TEMPERATURE} Key is present
        //Check that the temperature value is not null, <0.
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE)){
            Integer temperature = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
            if (temperature == null && temperature < 0){
                throw new IllegalArgumentException("Point required a valid Temperature");
            }
        }
        // If the {@link ProgramEntry#COLUMN_PROGRAM_ID} Key is present
        //Check that the program ID is not null
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID)){
            Integer programId = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID);
            if (programId == null && programId <= 0 ){
                throw new IllegalArgumentException("Point required a valid program Id");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProgramContract.ProgramEntry.TABLE_POINTS,values, selection, selectionArg);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private int updateArchiveProgram(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present
        // check that the name value is not null.
        if(values.containsKey(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID)){
            Integer programId = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID);
            if (programId == null){
                throw new IllegalArgumentException("Archive Program requires a program Id");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0){
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProgramContract.ProgramEntry.TABLE_A_PROGRAMS, values, selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    private int updateArchivePoint (Uri uri, ContentValues values, String selection, String[] selectionArg){
        // If the {@link ProgramEntry#COLUMN_TIME} Key is present
        //Check that the time value is not null, <0.
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_TIME)){
            Integer time = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TIME);
            if (time == null && time < 0){
                throw new IllegalArgumentException("Archive Point requires a valid time");
            }
        }

        // If the {@link ProgramEntry#COLUMN_TEMPERATURE} Key is present
        //Check that the temperature value is not null, <0.
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE)){
            Integer temperature = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
            if (temperature == null && temperature < 0){
                throw new IllegalArgumentException("Archive point required a valid temperature");
            }
        }
        // If the {@link ProgramEntry#COLUMN_PROGRAM_ID} Key is present
        //Check that the program ID is not null
        if (values.containsKey(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID)){
            Integer archiveProgramId = values.getAsInteger(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID);
            if (archiveProgramId == null && archiveProgramId <=0 ){
                throw new IllegalArgumentException("Archive Point required a valid Archive Program ID ");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProgramContract.ProgramEntry.TABLE_A_POINTS,values, selection, selectionArg);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
