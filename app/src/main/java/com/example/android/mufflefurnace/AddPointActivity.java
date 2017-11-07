package com.example.android.mufflefurnace;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;

import static com.example.android.mufflefurnace.Data.ProgramContract.CONTENT_AUTHORITY;
import static com.example.android.mufflefurnace.Data.ProgramDbHelper.LOG_TAG;

public class AddPointActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentUri;
    private Uri mCurrentProgramUri;
    private Uri mCurrentPointUri;

    private int mCurrentProgramID;

    private EditText timeTextView;
    private EditText temperatureTextView;

    private View deleteItem;

    private static final String TIME_SEPARATOR = ":";
    private static int LENGTH_SEPARATOR = 1;


    private String addPointMessage;
    private String editPointMessage;

    //For URI matcher
    private static final int PROGRAM = 100;
    private static final int POINT = 101;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        mUriMatcher.addURI(CONTENT_AUTHORITY, ProgramContract.PATH_PROGRAMS + "/#", PROGRAM);
        mUriMatcher.addURI(CONTENT_AUTHORITY, ProgramContract.PATH_POINTS + "/#", POINT);
    }

    int matchAddEditPoint;

    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int EXISTING_POINT_LOADER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_point);


//        setTitle(R.string.add_point_add_point);

        timeTextView = (EditText) findViewById(R.id.add_point_time);
        temperatureTextView = (EditText) findViewById(R.id.add_point_temperature);

        //Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentUri = intent.getData();


        final Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override   public void onClick(View view) {
                deletePoint();
                Intent i = new Intent(AddPointActivity.this, ProgramEditActivity.class);
                i.setData(mCurrentProgramUri);
                startActivity(i);
            }
        });

        //Uri matcher (is it program Uri or point Uri)
        matchAddEditPoint = mUriMatcher.match(mCurrentUri);

        switch (matchAddEditPoint) {
            case PROGRAM:
                mCurrentProgramUri = mCurrentUri;
                setTitle(R.string.add_point_title_add_point);
                getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);
                deleteButton.setVisibility(View.GONE);
                break;
            case POINT:
                mCurrentPointUri = mCurrentUri;
                setTitle(R.string.add_point_title_edit_point);
                getSupportLoaderManager().initLoader(EXISTING_POINT_LOADER, null, this);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + mCurrentUri);
        }


        //       getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);

    }

    //Show toast
    void displayToast(String text) {

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
        Log.i(LOG_TAG, "toast displayed");
        // toast.setGravity(Gravity.BOTTOM,0,0);

    }

    private void insertPoint() {
        //get data from the fields
        // Use trim to eliminate leading or trailing white space
        String temperatureString = temperatureTextView.getText().toString().trim();
        int temperatureInteger = Integer.parseInt(temperatureString);

        String timeString = timeTextView.getText().toString().trim();
        int timeInteger = timeToInteger(timeString);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE, temperatureInteger);
        values.put(ProgramContract.ProgramEntry.COLUMN_TIME, timeInteger);
        values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID, mCurrentProgramID);


        Uri newUri = getContentResolver().insert(ProgramContract.ProgramEntry.CONTENT_URI_POINTS, values);
        if (newUri == null) {
            //If the  new content URI is null, then there was an error with insertion
            displayToast("Error with saving point");
            Log.i(LOG_TAG, "Error with saving point");
        } else {
            addPointMessage = "Point saved successful";
            displayToast(addPointMessage);
            Log.i(LOG_TAG, "New row is " + newUri.toString());
        }


    }

    private void updatePoint() {
        //get data from the fields
        // Use trim to eliminate leading or trailing white space
        String temperatureString = temperatureTextView.getText().toString().trim();
        int temperatureInteger = Integer.parseInt(temperatureString);

        String timeString = timeTextView.getText().toString().trim();
        int timeInteger = timeToInteger(timeString);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE, temperatureInteger);
        values.put(ProgramContract.ProgramEntry.COLUMN_TIME, timeInteger);
//        values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID, mCurrentProgramID);

        int update = getContentResolver().update(mCurrentPointUri, values, null, null);

//        int update1 = getContentResolver().update(mCurrentPointUri, values,);

        if (update == 0) {
            //If the  new content URI is null, then there was an error with insertion
            displayToast("Error with update program");
            Log.i(LOG_TAG, "Error with update program");
        } else {
            editPointMessage = "Program updated successful";
            displayToast(editPointMessage);
            Log.i(LOG_TAG, "Updated row is " + Integer.toString(update));
        }
    }

    public void deletePoint(){
        int delete = getContentResolver().delete(mCurrentPointUri,null,null);

        if (delete == 0) {
            //If the  new content URI is null, then there was an error with insertion
            displayToast("Error with delete point");
            Log.i(LOG_TAG, "Error with delete point");
        } else {
            editPointMessage = "Point deleted successful";
            displayToast(editPointMessage);
            Log.i(LOG_TAG, "Deleted row is " + Integer.toString(delete));
        }
    }

    private int timeToInteger(String timeString) {
        int timeInSeconds;
        int hours;
        int minutes;
        String minutesString;

        if (timeString.contains(TIME_SEPARATOR)) {
            int i = timeString.indexOf(TIME_SEPARATOR);
            Integer lengthTime = timeString.length();

            hours = Integer.parseInt(timeString.substring(0, i));

            minutesString = timeString.substring(i + LENGTH_SEPARATOR, lengthTime);

            if (minutesString.contains(TIME_SEPARATOR)) {
                int ii = minutesString.indexOf(TIME_SEPARATOR);
                minutes = Integer.parseInt(minutesString.substring(0, ii));
            } else {
                if (minutesString.equals("")) {
                    minutes = 0;
                } else {
                    minutes = Integer.parseInt(minutesString);
                }
            }

        } else {
            minutes = 0;
            if (timeString.equals("")) {
                hours = 0;
            } else {
                hours = Integer.parseInt(timeString);
            }
        }


        timeInSeconds = (hours * 60) + minutes;

        return timeInSeconds;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_point_add, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                if (matchAddEditPoint == PROGRAM) {
                        // Save point to the data base
                        insertPoint();
                        finish();
                        return true;
                }
                    if (matchAddEditPoint == POINT){
                        updatePoint();
                        finish();
                        return true;
                }
                return true;



            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (ProgramViewActivity)
                Intent intent1 = new Intent(AddPointActivity.this, ProgramEditActivity.class);
                intent1.setData(mCurrentProgramUri);
                NavUtils.navigateUpTo(this, intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        private int getmCurrentProgramID (){

            //Examine the intent that was used to launch this activity
            Intent intent = getIntent();
            mCurrentProgramUri = intent.getData();

            String [] projection = {
                    ProgramContract.ProgramEntry._ID
            };


            Cursor cursor = getContentResolver().query(
                    mCurrentProgramUri,
                    projection,
                    null,
                    null,
                    null
            );

            mCurrentProgramID = cursor.getInt(cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID));

            return mCurrentProgramID;

        }

    */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EXISTING_PROGRAM_ID_LOADER) {
            if (mCurrentProgramUri == null) {
                return null;
            }
            String[] projection = {
                    ProgramContract.ProgramEntry._ID
            };

            return new CursorLoader(this,
                    mCurrentProgramUri,
                    projection,
                    null,
                    null,
                    null
            );
        }
        if (id == EXISTING_POINT_LOADER) {
            if (mCurrentPointUri == null) {
                return null;
            }
            String[] projection = {
                    ProgramContract.ProgramEntry.COLUMN_TIME,
                    ProgramContract.ProgramEntry.COLUMN_TEMPERATURE,
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID
            };

            return new CursorLoader(this,
                    mCurrentPointUri,
                    projection,
                    null,
                    null,
                    null
            );
        } else {
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case EXISTING_PROGRAM_ID_LOADER:

                // Bail early if the cursor is null or there is less than 1 row in the cursor
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }
                if (cursor.moveToFirst()) {
                    int currentProgramIDIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry._ID);
                    mCurrentProgramID = cursor.getInt(currentProgramIDIndex);
                    // for testing
//                    setTitle(Integer.toString(mCurrentProgramID));

                }
                break;
            case EXISTING_POINT_LOADER:
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }
                if (cursor.moveToFirst()) {
                    int temperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
                    int timeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME);
                    int programIdIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID);

                    int temperature = cursor.getInt(temperatureColumnIndex);
                    int time = cursor.getInt(timeColumnIndex);
                    mCurrentProgramID = cursor.getInt(programIdIndex);

                    timeTextView.setText(PointCursorAdapter.mTimeToString(time));
                    temperatureTextView.setText(Integer.toString(temperature));

                    mCurrentProgramUri = ContentUris.withAppendedId(ProgramContract.ProgramEntry.CONTENT_URI_PROGRAMS, mCurrentProgramID);

                }
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}


