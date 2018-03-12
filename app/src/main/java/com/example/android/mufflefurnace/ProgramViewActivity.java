package com.example.android.mufflefurnace;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ExecutionProgram.ExecutingProgramActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

import static com.example.android.mufflefurnace.Data.ProgramDbHelper.LOG_TAG;

public class ProgramViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;

    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;

    PointCursorAdapter mPointCursorAdapter;

    ArrayList<DataPoint> dataPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventOpenPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventClosePointArrayList = new ArrayList<DataPoint>();
    private GraphView graph;
    private TextView programShouldContainTextView;
    private int pointsCounter = 0;

    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;


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
                Intent i = new Intent(ProgramViewActivity.this, ExecutingProgramActivity.class);
                i.setData(mCurrentProgramUri);
                if (pointsCounter >= 2) {
                    startActivity(i);
                } else {
                    displayToast(getString(R.string.program_view_program_should_contain));
                }
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
}
