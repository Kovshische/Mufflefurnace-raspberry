package com.example.android.mufflefurnace;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ExecutionProgram.ExecutingProgramActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;

import static com.example.android.mufflefurnace.Data.ProgramDbHelper.LOG_TAG;


public class ProgramViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    //AppCompatActivity

    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;
    public static final String INTENT_CALENDAR = "Intent calendar";
    PointCursorAdapter mPointCursorAdapter;
    ArrayList<DataPoint> dataPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventOpenPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventClosePointArrayList = new ArrayList<DataPoint>();
    AlertDialog.Builder alertStartNowOrSetTime;
    Button datePickerButton, timePickerButton;
    EditText dataEditText, timeEditText;
    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;
    private GraphView graph;
    private TextView programShouldContainTextView;
    private int pointsCounter = 0;
    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date_time;

    private Calendar intentCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_view);


        //find the ListView which will be populated with the program data
        ListView pointListView = (ListView) findViewById(R.id.list_view_points);

        mPointCursorAdapter = new PointCursorAdapter(this, null);
        pointListView.setAdapter(mPointCursorAdapter);

        //Examine the intent that was used to launch this activity
        //in order to figure out if we're creating a new pet or editing existing one.
        Intent intent = getIntent();
        mCurrentProgramUri = intent.getData();


        //Start button
        RelativeLayout start = (RelativeLayout) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProgram();
            }
        });


        graph = (GraphView) findViewById(R.id.graph_view);

        programShouldContainTextView = (TextView) findViewById(R.id.program_view_program_should_contain);


        //set Vent visibility
        TextView ventTextView = (TextView) findViewById(R.id.program_view_vent);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ifVentEnabled = sharedPreferences.getBoolean(getString(R.string.settings_vent_options_key), false);
        if (ifVentEnabled == false) {
            ventTextView.setVisibility(View.GONE);
        }


        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);


    }


    private void initPointLoader() {
        getSupportLoaderManager().initLoader(POINTS_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_program_view, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_edit_program:

                Intent intent = new Intent(ProgramViewActivity.this, ProgramEditActivity.class);
                intent.setData(mCurrentProgramUri);
                startActivity(intent);

                return true;


            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == EXISTING_PROGRAM_ID_LOADER) {
            if (mCurrentProgramUri == null) {
                return null;
            }
            String[] projection = {
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME
            };

            return new CursorLoader(this,
                    mCurrentProgramUri,
                    projection,
                    null,
                    null,
                    null
            );

        }

        if (id == POINTS_LOADER) {
            String[] projectionForPoint = {
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID,
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_TIME,
                    ProgramContract.ProgramEntry.COLUMN_TEMPERATURE,
                    ProgramContract.ProgramEntry.COLUMN_VENT
            };

            String mCurrentProgramIdString = Integer.toString(mCurrentProgramId);

            // Select Where ProgramId = currentProgramID
            String select = "(" + ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID + "=" + mCurrentProgramIdString + "  )";


            return new CursorLoader(
                    this,
                    ProgramContract.ProgramEntry.CONTENT_URI_POINTS,
                    projectionForPoint,
                    select,
                    null,
                    ProgramContract.ProgramEntry.COLUMN_TIME
            );
        } else return null;
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
                    int currentProgramIDIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);
                    int currentProgramIdIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID);

                    mCurrentProgramName = cursor.getString(currentProgramIDIndex);
                    mCurrentProgramId = cursor.getInt(currentProgramIdIndex);

                    setTitle(mCurrentProgramName);
                }
                initPointLoader();
                break;


            case POINTS_LOADER:
                if (cursor == null || cursor.getCount() < 1) {
                    programShouldContainTextView.setVisibility(View.VISIBLE);
                    return;
                }

                int timeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME);
                int temperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
                int ventColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_VENT);

                //Display graphView

                while (cursor.moveToNext()) {
                    int time = cursor.getInt(timeColumnIndex);
                    double timeDouble = (double) time / 60;
                    Integer temperature;

                    if (!cursor.isNull(temperatureColumnIndex)) {
                        temperature = cursor.getInt(temperatureColumnIndex);
                        dataPointArrayList.add(new DataPoint(timeDouble, temperature));
                        Log.i("array for graphView", time + "/" + temperature);
                        pointsCounter = pointsCounter + 1;
                    }

                    if (ifVentEnabled == true) {
                        if (cursor.getInt(ventColumnIndex) == ProgramContract.ProgramEntry.VENT_OPEN) {
                            ventOpenPointArrayList.add(new DataPoint(timeDouble, 0));
                        }
                        if (cursor.getInt(ventColumnIndex) == ProgramContract.ProgramEntry.VENT_CLOSE) {
                            ventClosePointArrayList.add(new DataPoint(timeDouble, 0));
                        }
                    }
                }

                //Display text if there are less than 2 points
                if (pointsCounter < +2) {
                    programShouldContainTextView.setVisibility(View.VISIBLE);
                } else {

                    DataPoint[] dataPoint = dataPointArrayList.toArray(new DataPoint[]{});
                    DataPoint[] ventOpenPoint = ventOpenPointArrayList.toArray(new DataPoint[]{});
                    DataPoint[] ventClosePoint = ventClosePointArrayList.toArray(new DataPoint[]{});
//                DataPoint[] dataPoint = (DataPoint[]) dataPointArrayList.toArray(new DataPoint[0]);
//                Log.i("length of datapoint", Integer.toString(dataPoint.length));

                    LineGraphSeries<DataPoint> seriesPoint = new LineGraphSeries<>(dataPoint);

                    PointsGraphSeries<DataPoint> seriesOpenVent = new PointsGraphSeries<>(ventOpenPoint);
                    seriesOpenVent.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            paint.setTextSize(20);
                            paint.setColor(Color.BLACK);
                            canvas.rotate(-90, x, y);
                            canvas.drawText("vent open", x + 10, y, paint);
                            canvas.rotate(90, x, y);

                        }
                    });

                    PointsGraphSeries<DataPoint> seriesCloseVent = new PointsGraphSeries<>(ventClosePoint);
                    seriesCloseVent.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            paint.setTextSize(20);
                            paint.setColor(Color.BLACK);
                            canvas.rotate(-90, x, y);
                            canvas.drawText("vent close", x + 10, y, paint);
                            canvas.rotate(90, x, y);

                        }
                    });


                    //Get mat time
                    int length = dataPoint.length;
                    DataPoint lastDataPoint = dataPoint[length - 1];
                    double maxTime = lastDataPoint.getX();


                    //Set max time
                    graph.addSeries(seriesPoint);
                    graph.addSeries(seriesOpenVent);
                    graph.addSeries(seriesCloseVent);
                    graph.getViewport().setXAxisBoundsManual(true);
                    graph.getViewport().setMaxX(maxTime);


                    //graph.setTitle("Название графика");
                    //graph.getGridLabelRenderer().setVerticalAxisTitle("°C");

                }
                mPointCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPointCursorAdapter.swapCursor(null);
    }


    void displayToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
        Log.i(LOG_TAG, "toast displayed");
        // toast.setGravity(Gravity.BOTTOM,0,0);
    }

    void startProgram() {
//        Intent i = new Intent(ProgramViewActivity.this, ExecutingProgramActivity.class);
//        i.setData(mCurrentProgramUri);
        if (pointsCounter >= 2) {

             popupStartNowSetTime();
//            startActivity(i);

        } else {
            displayToast(getString(R.string.program_view_program_should_contain));
        }
    }

    void popupStartNowSetTime() {
        alertStartNowOrSetTime = new AlertDialog.Builder(this);
        alertStartNowOrSetTime.setTitle(getString(R.string.program_view_when_start_program));
        //alert.setMessage(getString(R.string.execution_program_dangerous_text));
        alertStartNowOrSetTime.setPositiveButton(getString(R.string.program_view_start_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToExecutedProgramActivity();
            }
        });
//        alertStartNowOrSetTime.setPositiveButton()
        alertStartNowOrSetTime.setNegativeButton(getString(R.string.program_view_set_time), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                datePicker();
            }
        });
        alertStartNowOrSetTime.setCancelable(true);
        alertStartNowOrSetTime.show();
    }

    public Dialog setTimeDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_set_time, null));

        View setTimeView = View.inflate(this, R.layout.dialog_set_time,null);
/*
        setTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(ProgramViewActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                     //   dataEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });
*/
        builder.setPositiveButton(getString(R.string.program_view_set_time_popup_set), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        return builder.create();
    }



    void popupSetTime() {
        setTimeDialog().show();
    }


    private void datePicker(){

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {


                        intentCalendar.set(Calendar.YEAR, year);
                        intentCalendar.set(Calendar.MONTH, monthOfYear);
                        intentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        intentCalendar.set(Calendar.SECOND, 0);
                        intentCalendar.set(Calendar.MILLISECOND, 0);
                        //*************Call Time Picker Here ********************
                       timePicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() -1000);
        datePickerDialog.show();
    }

    private void timePicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


                        Calendar datetime = Calendar.getInstance();
//                        Calendar c = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minute);

                        intentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        intentCalendar.set(Calendar.MINUTE, minute);

                        if (datetime.getTimeInMillis() >= c.getTimeInMillis()) {

                            date_time = intentCalendar.getTime().toString();
                            Toast.makeText(getApplicationContext(), date_time, Toast.LENGTH_LONG).show();

                            goToExecutedProgramActivity();

                        } else {
                            //it's before current'
                            Toast.makeText(getApplicationContext(), "Invalid Time", Toast.LENGTH_LONG).show();
                        }

                    }


                }, mHour, mMinute, false);

        timePickerDialog.show();
    }
    private void goToExecutedProgramActivity(){
        Intent intent = new Intent(ProgramViewActivity.this, ExecutingProgramActivity.class);
        intent.setData(mCurrentProgramUri);
        intent.putExtra(INTENT_CALENDAR, intentCalendar);
        startActivity(intent);
    }

}
