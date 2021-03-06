package com.example.android.mufflefurnace;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;

import static com.example.android.mufflefurnace.Data.ProgramContract.CONTENT_AUTHORITY;

public class AddPointActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public final String LOG_TAG = AddPointActivity.class.getSimpleName();

    private static final String TIME_SEPARATOR = ":";
    //For URI matcher
    private static final int PROGRAM = 100;
    private static final int POINT = 101;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int EXISTING_POINT_LOADER = 2;
    private static int LENGTH_SEPARATOR = 1;

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
    private Uri mCurrentUri;
    private Uri mCurrentProgramUri;
    private Uri mCurrentPointUri;
    private int mCurrentProgramID;
    private EditText timeTextView;
    private EditText temperatureTextView;
    private Spinner ventSpinner;
    private View deleteItem;
    private String addPointMessage;
    private String editPointMessage;
    private boolean ifInsertPointSuccess;
    private View ventOptionView;
    private SharedPreferences sharedPreferences;
    private Integer vent;
    private boolean ifVentEnabled;

    String[]ventOptions = {"None","Open","Close"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_point);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ifVentEnabled = sharedPreferences.getBoolean(getString(R.string.settings_vent_options_key),false);

//        setTitle(R.string.add_point_add_point);

        timeTextView = (EditText) findViewById(R.id.add_point_time);
        temperatureTextView = (EditText) findViewById(R.id.add_point_temperature);
        ventOptionView =(View)findViewById(R.id.vent_option);

        //set vent visibility
        setVentOptionVisibility();

        //Add spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ventOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ventSpinner = (Spinner) findViewById(R.id.add_point_vent_spinner);
        ventSpinner.setAdapter(spinnerAdapter);

        ventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    vent = null;
                    Log.i(LOG_TAG, "ventilation in position " + vent );
                }
               if (i==1){
                   vent = ProgramContract.ProgramEntry.VENT_OPEN;
                   Log.i(LOG_TAG, "ventilation in position " + vent );
               }
               if (i == 2){
                   vent = ProgramContract.ProgramEntry.VENT_CLOSE;
                   Log.i(LOG_TAG, "ventilation in position " + vent );
               }

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0){
            }
        });

        //Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentUri = intent.getData();


        final Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void setVentOptionVisibility (){

        if (ifVentEnabled == false){
            ventOptionView.setVisibility(View.INVISIBLE);
        }
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
        Integer temperatureInteger;
        Integer timeInteger;


        String temperatureString = temperatureTextView.getText().toString().trim();
        if (temperatureString.equals("")){
            temperatureInteger = null;
//            Log.i(LOG_TAG,"temperature Integer = " + temperatureInteger);
        } else {
            temperatureInteger = Integer.parseInt(temperatureString);
        }


        String timeString = timeTextView.getText().toString().trim();
        if (timeString.equals("")){
            timeInteger = null;
//            Log.i(LOG_TAG,"temperature Integer = " + timeInteger);
        } else {
            timeInteger = timeToInteger(timeString);
        }


        //Check to max temperature

        String maxTemperature = sharedPreferences.getString(
                getString(R.string.settings_max_temperature_key),
                getString(R.string.settings_max_temperature_default)
        );

        if (timeInteger == null){
            displayToast("Please, add time" );
            ifInsertPointSuccess = false;
        } else if(ifVentEnabled == false && temperatureInteger == null){
            displayToast("Please, add temperature" );
            ifInsertPointSuccess = false;
        }else if(ifVentEnabled == true && temperatureInteger == null && vent == null ){
            displayToast("Please, add temperature or vent option");
            ifInsertPointSuccess = false;
        }
        else if (temperatureInteger != null && temperatureInteger > Integer.parseInt(maxTemperature)) {
            displayToast("temperature can not be more than " + maxTemperature);
            ifInsertPointSuccess = false;
        } else {

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE, temperatureInteger);
            values.put(ProgramContract.ProgramEntry.COLUMN_TIME, timeInteger);
            values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID, mCurrentProgramID);
            if (vent != null ){
//                Log.i(LOG_TAG, "Vent is open or closed");
                values.put(ProgramContract.ProgramEntry.COLUMN_VENT, vent);
            }


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

            ifInsertPointSuccess = true;
        }
    }

    private void updatePoint() {
        //get data from the fields
        // Use trim to eliminate leading or trailing white space
        Integer temperatureInteger;
        String temperatureString = temperatureTextView.getText().toString().trim();

        if (temperatureString.equals("")){
            temperatureInteger = null;
            Log.i(LOG_TAG,"temperature Integer = " + temperatureInteger);
        } else {
             temperatureInteger = Integer.parseInt(temperatureString);
        }


        String timeString = timeTextView.getText().toString().trim();
        Integer timeInteger = timeToInteger(timeString);


        //Check to max temperature

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String maxTemperature = sharedPreferences.getString(
                getString(R.string.settings_max_temperature_key),
                getString(R.string.settings_max_temperature_default)
        );

      //  if (temperatureInteger.)
        if (timeInteger == null){
            displayToast("Please, add time" );
            ifInsertPointSuccess = false;
        } else if(ifVentEnabled == false && temperatureInteger == null){
            displayToast("Please, add temperature" );
            ifInsertPointSuccess = false;
        }else if(ifVentEnabled == true && temperatureInteger == null && vent == null ){
            displayToast("Please, add temperature or vent option");
            ifInsertPointSuccess = false;
        }
        else if (temperatureInteger != null && temperatureInteger > Integer.parseInt(maxTemperature)) {
            displayToast("temperature can not be more than " + maxTemperature);
            ifInsertPointSuccess = false;
        } else {

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE, temperatureInteger);
            values.put(ProgramContract.ProgramEntry.COLUMN_TIME, timeInteger);
//          values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID, mCurrentProgramID);
            values.put(ProgramContract.ProgramEntry.COLUMN_VENT, vent);


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

            ifInsertPointSuccess = true;
        }
    }

    public void deletePoint() {
        int delete = getContentResolver().delete(mCurrentPointUri, null, null);

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

    //Get time in minutes.
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
                    if (ifInsertPointSuccess == true) {
                        finish();
                        return true;
                    }
                }
                if (matchAddEditPoint == POINT) {
                    updatePoint();
                    if (ifInsertPointSuccess == true) {
                        finish();
                        return true;
                    }
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
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID,
                    ProgramContract.ProgramEntry.COLUMN_VENT
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
                    int ventColumnIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_VENT);

                    Integer temperature = null;
                    if (!cursor.isNull(temperatureColumnIndex)) {
                        temperature = cursor.getInt(temperatureColumnIndex);
                    }
                    String temperatureString;
                    if (temperature != null){
                        temperatureString = Integer.toString(temperature);
                    } else {
                        temperatureString = "";
                    }

                    Integer vent = null;
                    if (!cursor.isNull(ventColumnIndex)){
                        vent = cursor.getInt(ventColumnIndex);
                    }


                    int time = cursor.getInt(timeColumnIndex);
                    mCurrentProgramID = cursor.getInt(programIdIndex);

                    timeTextView.setText(PointCursorAdapter.mTimeToString(time));

                    temperatureTextView.setText(temperatureString);
                    if (vent == null){
                        ventSpinner.setSelection(0);
                    } else if (vent == ProgramContract.ProgramEntry.VENT_OPEN){
                        ventSpinner.setSelection(1);
                    } else if (vent == ProgramContract.ProgramEntry.VENT_CLOSE){
                        ventSpinner.setSelection(2);
                    }

                    mCurrentProgramUri = ContentUris.withAppendedId(ProgramContract.ProgramEntry.CONTENT_URI_PROGRAMS, mCurrentProgramID);

                }
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}


