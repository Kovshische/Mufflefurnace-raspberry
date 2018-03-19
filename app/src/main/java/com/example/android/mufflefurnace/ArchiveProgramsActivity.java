package com.example.android.mufflefurnace;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.android.mufflefurnace.Data.ProgramContract;

/**
 * Created by admin on 3/19/2018.
 */

public class ArchiveProgramsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = AppCompatActivity.class.getSimpleName();
    private static final int PROGRAM_LOADER = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programs);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        RelativeLayout emptyView =(RelativeLayout) findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);

        //find the ListView which will be populated with the pet data
        ListView programListView = (ListView) findViewById(R.id.list_view_programs);

        getSupportLoaderManager().initLoader(PROGRAM_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // define the projection that specifies the column from the table we care about.
        String[] projection = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME,
                ProgramContract.ProgramEntry.COLUMN_STARTED_AT};
        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this, //parent activity content
                ProgramContract.ProgramEntry.CONTENT_URI_A_PROGRAMS,   //Provider content URI to query
                projection,             //Columns to include in the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection arguments
                null);                   //Default sort order


    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
