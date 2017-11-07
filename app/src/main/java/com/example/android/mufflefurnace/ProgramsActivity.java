package com.example.android.mufflefurnace;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.mufflefurnace.Data.ProgramContract;

public class ProgramsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = ProgramsActivity.class.getSimpleName();
    private static final int PROGRAM_LOADER = 0;
    ProgramCursorAdapter mProgramCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programs);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProgramsActivity.this, AddProgramActivity.class);
                startActivity(intent);
            }
        });

        //find the ListView which will be populated with the pet data
        ListView programListView = (ListView) findViewById(R.id.list_view_programs);


        //find and set empty view on the ListView, so that is only show when list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        programListView.setEmptyView(emptyView);

        mProgramCursorAdapter = new ProgramCursorAdapter(this, null);
        programListView.setAdapter(mProgramCursorAdapter);


        //Setup item click listener
        programListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@Link EditorActivity}
                Intent intent = new Intent(ProgramsActivity.this, ProgramViewActivity.class);

                //Form the content URI that represents the specific pet that was clicked on,
                //by appending the "id" (passed as input to this method) onto the
                // {@link ProgramEntry#CONTENT_URI}
                // for example, the URI would be "content://com.example.android.programs/program/2"
                //if the pet with ID 2 was clicked on
                Uri currentProgramUri = ContentUris.withAppendedId(ProgramContract.ProgramEntry.CONTENT_URI_PROGRAMS, id);

                //Set the URI on the data field of the intent
                intent.setData(currentProgramUri);

                // Launch the activity to display the data
                startActivity(intent);
            }
        });

        //Kick off loader
        getSupportLoaderManager().initLoader(PROGRAM_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // define the projection that specifies the column from the table we care about.
        String[] projection = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME,
                ProgramContract.ProgramEntry.COLUMN_CREATED_AT};
        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this, //parent activity content
                ProgramContract.ProgramEntry.CONTENT_URI_PROGRAMS,   //Provider content URI to query
                projection,             //Columns to include in the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection arguments
                null);                   //Default sort order


    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        // Update with this new cursor containing updated program data
        mProgramCursorAdapter.swapCursor(data);


    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mProgramCursorAdapter.swapCursor(null);
    }

}
