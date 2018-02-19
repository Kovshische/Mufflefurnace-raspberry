package com.example.android.mufflefurnace.ExecutionProgram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ProgramViewActivity;
import com.example.android.mufflefurnace.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class ExecutingProgramActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG = PointManager.class.getSimpleName();


    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;

    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;

    ArrayList<DataPoint> dataPointArrayList = new ArrayList<DataPoint>();
    private GraphView graph;
    EditText enterTimeEditText;
    Button enterButton;
    TextView expectedTemperatureTextView;

    //For check how pointManager works
    private int currentTime;
    private int expectedTempera;

    private Intent controlServiceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executing_program);



        //Examine the intent that was used to launch this activity
        //in order to figure out if we're creating a new pet or editing existing one.
        Intent intent = getIntent();
        mCurrentProgramUri = intent.getData();

        graph = (GraphView) findViewById(R.id.executing_program_graph_view);
        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);

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

        //service test
         controlServiceIntent = new Intent(this, ControlService.class);


    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent) {
        String counter = intent.getStringExtra("counter");
        String time = intent.getStringExtra("time");
        Log.d(LOG_TAG, counter);
        Log.d(LOG_TAG, time);

        TextView timeTextView = (TextView) findViewById(R.id.executing_program_time);
        timeTextView.setText(time);
        TextView targetTempTextView = (TextView)findViewById(R.id.executing_program_target_temp);
        targetTempTextView.setText(counter);

    }


    private void initPointLoader() {
        getSupportLoaderManager().initLoader(POINTS_LOADER, null, this);
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

        if (id == POINTS_LOADER)
        {
            String[] projectionForPoint = {
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID,
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_TIME,
                    ProgramContract.ProgramEntry.COLUMN_TEMPERATURE
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
        else return null;
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
                    return;
                }

                int timeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TIME);
                int temperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_TEMPERATURE);

                //Display graphView

                while (cursor.moveToNext()){
                    int time = cursor.getInt(timeColumnIndex);
                    int temperature = cursor.getInt(temperatureColumnIndex);

                    //time in hours
                    double timeDouble = (double) time/60;

                    dataPointArrayList.add(new DataPoint(timeDouble,temperature));
                    Log.i("array for graphView min", time + "/" + temperature);
                  //  Log.i("array for graphView", timeDouble + "/" + temperature);
                }


                DataPoint[] dataPoint = dataPointArrayList.toArray(new DataPoint[]{});
//                DataPoint[] dataPoint = (DataPoint[]) dataPointArrayList.toArray(new DataPoint[0]);
                Log.i("length of datapoint", Integer.toString(dataPoint.length));

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);

                //Get mat time
                int length = dataPoint.length;
                DataPoint lastDataPoint = dataPoint[length-1];
                double maxTime = lastDataPoint.getX();


                //Set max time
                graph.addSeries(series);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMaxX(maxTime);

                graph.addSeries(series);



        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate back to parent activity (ProgramViewActivity)
                Intent intent1 = new Intent(ExecutingProgramActivity.this, ProgramViewActivity.class);
                intent1.setData(mCurrentProgramUri);
                NavUtils.navigateUpTo(this, intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(controlServiceIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(ControlService.CONTROL_ACTION));
    }
}
