package com.example.android.mufflefurnace;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.ExecutionProgram.ExecutingProgramActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class ProgramViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;

    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;

    PointCursorAdapter mPointCursorAdapter;

    ArrayList<DataPoint> dataPointArrayList = new ArrayList<DataPoint>();
    private GraphView graph;


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
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProgramViewActivity.this, ExecutingProgramActivity.class);
                i.setData(mCurrentProgramUri);
                startActivity(i);
            }
        });

/*
        //Add graphView
        GraphView graph = (GraphView) findViewById(R.id.graph_view);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);



        GraphView graph = (GraphView) findViewById(R.id.graph_view);
*/


// try to fix accelerated mode


        graph = (GraphView) findViewById(R.id.graph_view);





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

                    double timeDouble = (double) time/60;

                    dataPointArrayList.add(new DataPoint(timeDouble,temperature));
                    Log.i("array for graphView", time + "/" + temperature);
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


                //graph.setTitle("Название графика");
                //graph.getGridLabelRenderer().setVerticalAxisTitle("°C");

                mPointCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPointCursorAdapter.swapCursor(null);
    }
}
