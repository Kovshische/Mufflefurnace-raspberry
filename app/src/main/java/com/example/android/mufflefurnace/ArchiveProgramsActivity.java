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
import android.widget.RelativeLayout;

import com.example.android.mufflefurnace.Data.ProgramContract;

/**
 * Created by admin on 3/19/2018.
 */

public class ArchiveProgramsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = AppCompatActivity.class.getSimpleName();
    private static final int PROGRAM_LOADER = 0;
    ArchiveProgramCursorAdapter archiveProgramCursorAdapter;

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

        archiveProgramCursorAdapter = new ArchiveProgramCursorAdapter(this, null);
        programListView.setAdapter(archiveProgramCursorAdapter);


        programListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@Link EditorActivity}
                Intent intent = new Intent(ArchiveProgramsActivity.this, ArchiveProgramViewActivity.class);

                //Form the content URI that represents the specific pet that was clicked on,
                //by appending the "id" (passed as input to this method) onto the
                // {@link ProgramEntry#CONTENT_URI}
                // for example, the URI would be "content://com.example.android.programs/program/2"
                //if the pet with ID 2 was clicked on
                Uri currentAProgramUri = ContentUris.withAppendedId(ProgramContract.ProgramEntry.CONTENT_URI_A_PROGRAMS, id);

                //Set the URI on the data field of the intent
                intent.setData(currentAProgramUri);

                // Launch the activity to display the data
                startActivity(intent);
            }
        });

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

        String sortOrder = ProgramContract.ProgramEntry.COLUMN_STARTED_AT + " DESC";

        return new CursorLoader(this, //parent activity content
                ProgramContract.ProgramEntry.CONTENT_URI_A_PROGRAMS,   //Provider content URI to query
                projection,             //Columns to include in the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection arguments
                sortOrder);                   //Default sort order


    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        archiveProgramCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        archiveProgramCursorAdapter.swapCursor(null);
    }


}
