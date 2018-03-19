package com.example.android.mufflefurnace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;

/**
 * Created by admin on 3/19/2018.
 */

public class ArchiveProgramViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private final String LOG_TAG = ArchiveProgramViewActivity.class.getSimpleName();

    private Uri currentProgramUri;
    private GraphView graph;
    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;
    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private int currentProgramId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_program_view);

        Intent intent = getIntent();
        currentProgramUri = intent.getData();

        graph = (GraphView) findViewById(R.id.graph_view);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX){
                if (isValueX){
                    return super.formatLabel(value, isValueX);
                } else {
                    return super.formatLabel(value, isValueX) + "°C ";
                }
            }
        });

        //set Vent visibility
        TextView ventTextView = (TextView) findViewById(R.id.program_view_vent);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ifVentEnabled = sharedPreferences.getBoolean(getString(R.string.settings_vent_options_key), false);
        if (ifVentEnabled == false) {
            ventTextView.setVisibility(View.GONE);
        }

        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projectionForPoint = {
                ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID,
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_TIME,
                ProgramContract.ProgramEntry.COLUMN_TEMPERATURE,
                ProgramContract.ProgramEntry.COLUMN_VENT
        };

        String mCurrentProgramIdString = Integer.toString(currentProgramId);

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void getProgramId (){
        String aProgramIdString = currentProgramUri.getEncodedPath();
        String[] aProgramURIParts = aProgramIdString.split("/");
        aProgramIdString = aProgramURIParts[2];
        Log.d(LOG_TAG, "archive program id " + aProgramIdString);
        currentProgramId = Integer.getInteger(aProgramIdString);
    }
}
