package com.example.android.mufflefurnace.ExecutionProgram;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.android.mufflefurnace.AppProperties;
import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ProgramViewActivity;
import com.example.android.mufflefurnace.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.android.mufflefurnace.Data.ProgramContract.BASE_CONTENT_URI;
import static com.example.android.mufflefurnace.Data.ProgramContract.PATH_A_POINTS;
import static com.example.android.mufflefurnace.Data.ProgramContract.PATH_A_PROGRAMS;

public class ExecutingProgramActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;
    private static final int A_PROGRAMS_LOADER = 3;
    private final String LOG_TAG = ExecutingProgramActivity.class.getSimpleName();
    ArrayList<DataPoint> dataPointArrayList;
    ArrayList<DataPoint> dataPointArchiveArrayList;
    ArrayList<DataPoint> ventOpenPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventClosePointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventArrayList = new ArrayList<DataPoint>();
    LineGraphSeries<DataPoint> archiveSeries;
    EditText enterTimeEditText;
    Button enterButton;
    TextView expectedTemperatureTextView;
    //alert
    AlertDialog.Builder alert;
    Context context;
    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;
    private GraphView graph;
    //For check how pointManager works
    private int currentTime;
    private int expectedTempera;
    private int programStatus;
    private Intent controlServiceIntent;
    private Calendar calendar;
    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;
    private Uri aProgramUri;
    private Integer aProgramId;
    private int endTimeSeconds = 0;
    private int i = 0;
    private int a = 0;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executing_program);

        context = ExecutingProgramActivity.this;

        dataPointArrayList = new ArrayList<DataPoint>();
        dataPointArchiveArrayList = new ArrayList<DataPoint>();


//        archiveSeries = new LineGraphSeries<>();
//        archiveSeries.setColor(R.color.colorAccent);


        //Examine the intent that was used to launch this activity
        //in order to figure out if we're creating a new program or editing existing one.
        Intent intent = getIntent();
        mCurrentProgramUri = intent.getData();
        calendar = (Calendar) intent.getSerializableExtra(ProgramViewActivity.INTENT_CALENDAR);

        graph = (GraphView) findViewById(R.id.executing_program_graph_view);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return super.formatLabel(value, isValueX);
                } else {
                    return super.formatLabel(value, isValueX) + "°C ";
                }
            }
        });

        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);


        /*
        //Check that PointManager works
        enterTimeEditText = (EditText) findViewById(R.id.enteredTime);
        enterButton = (Button) findViewById(R.id.enterButton);
        expectedTemperatureTextView = (TextView) findViewById(R.id.temperatureOnTime);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PointManager pointManager = new PointManager(dataPointArrayList);
                currentTime = Integer.parseInt(enterTimeEditText.getText().toString().trim());
                expectedTempera = pointManager.getTemperature(currentTime);
                expectedTemperatureTextView.setText(Integer.toString(expectedTempera));

            }
        });
      */

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ifVentEnabled = sharedPreferences.getBoolean(getString(R.string.settings_vent_options_key), false);

        LinearLayout ventLinearLayout = (LinearLayout) findViewById(R.id.executing_program_vent_linear_layout);
        if (ifVentEnabled == false) {
            ventLinearLayout.setVisibility(View.GONE);
        }
    }

    private  void updateUI(Intent intent) {

        String time = intent.getStringExtra(ControlService.TIME);
        int timeInt = intent.getIntExtra(ControlService.TIME_SEC, 0);
        String targetTemp = intent.getStringExtra(ControlService.TARGET_TEMP);
        int sensorTemp = intent.getIntExtra(ControlService.SENSOR_TEMP, 0);
        String sensorTempString = Integer.toString(sensorTemp);
        Boolean powerInstance = intent.getBooleanExtra(ControlService.POWER_INSTANCE, false);
        programStatus = intent.getIntExtra(ControlService.PROGRAM_STATUS, 0);
        Integer ventStatus = intent.getIntExtra(ControlService.VENT_STATUS, ProgramContract.ProgramEntry.VENT_CLOSE);
        String startTime = intent.getStringExtra(ControlService.START_TIME);
        String error = intent.getStringExtra(ControlService.ERROR);

        Log.d(LOG_TAG, time);
        Log.d(LOG_TAG, targetTemp);
        Log.d(LOG_TAG, sensorTempString);
        Log.d(LOG_TAG, ventStatus.toString());

        TextView timeTextView = (TextView) findViewById(R.id.executing_program_time);
        timeTextView.setText(time);
        TextView targetTempTextView = (TextView) findViewById(R.id.executing_program_target_temp);
        targetTempTextView.setText(targetTemp);
        TextView sensorTempTextView = (TextView) findViewById(R.id.executing_program_sensor_temp);
        sensorTempTextView.setText(sensorTempString);
        RadioButton powerRadioButton = (RadioButton) findViewById(R.id.executing_program_power_indicate);
        powerRadioButton.setChecked(powerInstance);
        TextView ventStatusTextView = (TextView) findViewById(R.id.executing_program_vent_status);
        String ventStatusString = "";
        if (ventStatus == ProgramContract.ProgramEntry.VENT_CLOSE) {
            ventStatusString = getString(R.string.add_point_close);
        }
        if (ventStatus == ProgramContract.ProgramEntry.VENT_OPEN) {
            ventStatusString = getString(R.string.add_point_open);
        }
        ventStatusTextView.setText(ventStatusString);
        //set end program
        TextView programStatusTextView = (TextView) findViewById(R.id.executing_program_status);

        if (!startTime.equals("")) {
            Date currentTime = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MMM HH:mm:ss");
            String currentTimeString = simpleDateFormat.format(currentTime);

            startTime = "Start time: " + startTime + "\n" + "Current time: " + currentTimeString;

            programStatusTextView.setText(startTime);
            programStatusTextView.setVisibility(View.VISIBLE);
        } else {
            programStatusTextView.setVisibility(View.INVISIBLE);
        }
        if (programStatus == ControlService.PROGRAM_END) {
            programStatusTextView.setText(getString(R.string.executing_program_program_has_finished));
            programStatusTextView.setVisibility(View.VISIBLE);
        }

        TextView errorTextView = (TextView) findViewById(R.id.executing_program_error);
        if (error != null){
            errorTextView.setText(error);
            errorTextView.setVisibility(View.VISIBLE);
        } else
            errorTextView.setVisibility(View.GONE);

        //graph in real time
        double timeDouble = (double) timeInt / 3600;

        //Update archiveSeries 1 time per 10 seconds if end time > 3600 seconds, 1 time per 60 seconds if end time > 36000 seconds

        if (endTimeSeconds < 3600) {
            archiveSeries.appendData(new DataPoint(timeDouble, sensorTemp), false, 100000000);
        } else if (endTimeSeconds < 36000) {
            Log.d(LOG_TAG, "End time is " +endTimeSeconds + "; i = " + i);
            if (i == 0 || a < 2) {
                archiveSeries.appendData(new DataPoint(timeDouble, sensorTemp), false, 100000000);
                i++;
                a++;
            }
            else {
                if (i == 9) {
                    i = 0;
                } else  i++;
            }
        } else {
            Log.d(LOG_TAG, "End time is " + endTimeSeconds + "; i = " + i);
            if (i == 0 || a < 2) {
                archiveSeries.appendData(new DataPoint(timeDouble, sensorTemp), false, 100000000);
                i++;
                a++;
            } else {
                if (i == 59) {
                    i = 0;
                } else i++;
            }
        }
    }


    private void initPointLoader() {
        getSupportLoaderManager().initLoader(POINTS_LOADER, null, this);
    }

    private void initArchiveProgramLoader() {
        getSupportLoaderManager().initLoader(A_PROGRAMS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == EXISTING_PROGRAM_ID_LOADER) {
            Log.d(LOG_TAG, "EXISTING_PROGRAM_ID_LOADER on create");
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
            Log.d(LOG_TAG, "POINTS_LOADER onCreateLoader");
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
        }
        if (id == A_PROGRAMS_LOADER) {

            Log.d(LOG_TAG, "A_PROGRAMS_LOADER onCreate");
            String[] projectionForAProgram = {
                    ProgramContract.ProgramEntry._ID,
            };

            String sortOrder = ProgramContract.ProgramEntry.COLUMN_STARTED_AT + " DESC";

            return new CursorLoader(
                    this,
                    ProgramContract.ProgramEntry.CONTENT_URI_A_PROGRAMS,
                    projectionForAProgram,
                    null,
                    null,
                    sortOrder
            );
        }
        else return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case EXISTING_PROGRAM_ID_LOADER:
                Log.d(LOG_TAG, "EXISTING_PROGRAM_ID_LOADER onLoadFinished");
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
                addArchiveProgram();



                initPointLoader();
                break;


            case POINTS_LOADER:
                Log.d(LOG_TAG, "POINTS_LOADER onLoaderFinished");
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }

                int timeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME);
                int temperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);
                int ventColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_VENT);


                //Display graphView

                while (cursor.moveToNext()) {
                    ContentValues valuesArchiveTargetPoint = new ContentValues();
                    valuesArchiveTargetPoint.put(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID, aProgramId);

                    Integer temperature = null;
                    int time = cursor.getInt(timeColumnIndex);
                    valuesArchiveTargetPoint.put(ProgramContract.ProgramEntry.COLUMN_TIME, time);

                    double timeDouble = (double) time / 60;
                    //                       Log.i("array for graphView", time + "/" + temperature);

                    int pointsCounter = 0;
                    if (!cursor.isNull(temperatureColumnIndex)) {
                        temperature = cursor.getInt(temperatureColumnIndex);
                        //Add to archive target point
                        valuesArchiveTargetPoint.put(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE, temperature);

                        dataPointArrayList.add(new DataPoint(timeDouble, temperature));
                        Log.i("array for graphView", time + "/" + temperature);
                        pointsCounter = pointsCounter + 1;
                    }

                    if (ifVentEnabled == true) {
                        if (cursor.getInt(ventColumnIndex) == ProgramContract.ProgramEntry.VENT_OPEN) {
                            ventOpenPointArrayList.add(new DataPoint(timeDouble, 0));
                            ventArrayList.add(new DataPoint(timeDouble, ProgramContract.ProgramEntry.VENT_OPEN));
                            valuesArchiveTargetPoint.put(ProgramContract.ProgramEntry.COLUMN_VENT, ProgramContract.ProgramEntry.VENT_OPEN);
                        }
                        if (cursor.getInt(ventColumnIndex) == ProgramContract.ProgramEntry.VENT_CLOSE) {
                            ventClosePointArrayList.add(new DataPoint(timeDouble, 0));
                            ventArrayList.add(new DataPoint(timeDouble, ProgramContract.ProgramEntry.VENT_CLOSE));
                            valuesArchiveTargetPoint.put(ProgramContract.ProgramEntry.COLUMN_VENT, ProgramContract.ProgramEntry.VENT_CLOSE);
                        }
                    }


                    Uri newUri = getContentResolver().insert(ProgramContract.ProgramEntry.CONTENT_URI_A_T_POINTS, valuesArchiveTargetPoint);
                    if (newUri == null) {
                        Log.i(LOG_TAG, "Error with saving point to archive");
                    }
                }

                DataPoint[] dataPoint = dataPointArrayList.toArray(new DataPoint[]{});
                Log.i("length of datapoint", Integer.toString(dataPoint.length));
                DataPoint[] ventOpenPoint = ventOpenPointArrayList.toArray(new DataPoint[]{});
                DataPoint[] ventClosePoint = ventClosePointArrayList.toArray(new DataPoint[]{});


                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);

                int i = dataPointArrayList.size();
                endTimeSeconds = (int) (3600 * dataPointArrayList.get(i -1).getX());

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
                graph.addSeries(series);
                graph.addSeries(seriesOpenVent);
                graph.addSeries(seriesCloseVent);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMaxX(maxTime);

                //Add series for real time temperature;
                archiveSeries = new LineGraphSeries<>();
//                    archiveSeries.setColor(R.color.colorAccent);
                archiveSeries.setColor(Color.RED);
                graph.addSeries(archiveSeries);


                //Create control service
                controlServiceIntent = new Intent(ExecutingProgramActivity.this, ControlService.class);

                registerReceiver(broadcastReceiver, new IntentFilter(ControlService.BROADCAST_ACTION));
                controlServiceIntent.putExtra(ControlService.INTENT_DATA_POINTS_ARRAY_LIST, dataPointArrayList);
                controlServiceIntent.putExtra(ControlService.INTENT_VENT_ARRAY_LIST, ventArrayList);
                controlServiceIntent.putExtra(ProgramViewActivity.INTENT_CALENDAR, calendar);
                controlServiceIntent.putExtra(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID, aProgramId);

                Log.d(LOG_TAG, "init service");
                startService(controlServiceIntent);
                initArchiveProgramLoader();
                break;

            case A_PROGRAMS_LOADER:
                Log.d(LOG_TAG, "A_PROGRAMS_LOADER onLoaderFinished");
                deleteArchiveProgram(cursor);
                break;
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu


        switch (item.getItemId()) {
            case android.R.id.home:
                /*
                // Navigate back to parent activity (ProgramViewActivity)
                Intent intent1 = new Intent(ExecutingProgramActivity.this, ProgramViewActivity.class);
                intent1.setData(mCurrentProgramUri);
                NavUtils.navigateUpTo(this, intent1);
                return true;
*/
                if (programStatus == ControlService.PROGRAM_END) {
                    Intent intent1 = new Intent(ExecutingProgramActivity.this, ProgramViewActivity.class);
                    intent1.setData(mCurrentProgramUri);
                    NavUtils.navigateUpTo(this, intent1);
                    return true;
                } else {
                    alert = new AlertDialog.Builder(context);
                    alert.setTitle(getString(R.string.execution_program_attention));
                    alert.setMessage(getString(R.string.execution_program_dangerous_text));
                    alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1 = new Intent(ExecutingProgramActivity.this, ProgramViewActivity.class);
                            intent1.setData(mCurrentProgramUri);
                            NavUtils.navigateUpTo(ExecutingProgramActivity.this, intent1);
                        }
                    });
                    alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alert.setCancelable(true);
                    alert.show();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // startService(controlServiceIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(ControlService.BROADCAST_ACTION));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        stopService(controlServiceIntent);
        Log.i(LOG_TAG, "stop service");
    }

    private void addArchiveProgram() {
        ContentValues values = new ContentValues();
        values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID, mCurrentProgramId);
        values.put(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME, mCurrentProgramName);

        aProgramUri = getContentResolver().insert(ProgramContract.ProgramEntry.CONTENT_URI_A_PROGRAMS, values);
        Log.d(LOG_TAG, aProgramUri.getEncodedPath());


        String aProgramIdString = aProgramUri.getEncodedPath();
        String[] aProgramURIParts = aProgramIdString.split("/");
        aProgramIdString = aProgramURIParts[2];
        Log.d(LOG_TAG, "archive program id string:" + aProgramIdString);
        aProgramId = Integer.parseInt(aProgramIdString);
        Log.d(LOG_TAG, "archive program id " + aProgramId);
    }

    private void deleteArchiveProgram(Cursor cursor) {
        if (cursor.getCount() > AppProperties.MAX_ARCHIVE_PROGRAMS_AMOUNT) {

            cursor.move(AppProperties.MAX_ARCHIVE_PROGRAMS_AMOUNT);

            while (cursor.moveToNext()) {
                int archiveProgramIdColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID);
                int archiveProgramId = cursor.getInt(archiveProgramIdColumnIndex);

                // delete points
                String selection = ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID + "=?";
                String[] selectionArgs = {Integer.toString(archiveProgramId)};

                Uri pointsUri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_A_POINTS);
                int deletePoints = getContentResolver().delete(pointsUri, selection, selectionArgs);

                if (deletePoints == -1) {
                    //If the  new content URI is null, then there was a
                    // n error with insertion
                    Log.i(LOG_TAG, "Error with delete archive points in archive program");
                }

                //delete program
                String selectionProgram = ProgramContract.ProgramEntry._ID + "=?";
                String[] selectionProgramArg = {Integer.toString(archiveProgramId)};

                Uri programUri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_A_PROGRAMS);
                int deleteProgram = getContentResolver().delete(programUri, selectionProgram, selectionProgramArg);
                if (deleteProgram == -1) {
                    //If the  new content URI is null, then there was a
                    // n error with insertion
                    Log.i(LOG_TAG, "Error with delete archive program");
                }

            }

        }

    }

}
