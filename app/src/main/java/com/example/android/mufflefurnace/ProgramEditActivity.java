package com.example.android.mufflefurnace;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;

import static com.example.android.mufflefurnace.Data.ProgramContract.BASE_CONTENT_URI;
import static com.example.android.mufflefurnace.Data.ProgramContract.PATH_POINTS;
import static com.example.android.mufflefurnace.Data.ProgramDbHelper.LOG_TAG;
import static com.example.android.mufflefurnace.R.id.action_edit_program_name;

public class ProgramEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Content URI for the existing program (null if it's a new pet)
     */


    private static final int EXISTING_PROGRAM_ID_LOADER = 1;
    private static final int POINTS_LOADER = 2;

    PointCursorAdapter mPointCursorAdapter;
    private Uri mCurrentProgramUri;
    private String mCurrentProgramName;
    private int mCurrentProgramId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_edit);


        Intent intent = getIntent();
        mCurrentProgramUri = intent.getData();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_point);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProgramEditActivity.this, AddPointActivity.class);
                intent.setData(mCurrentProgramUri);
                startActivity(intent);
            }
        });

       //get current  mCurrentProgramId;



        //find the ListView which will be populated with the program data
        ListView pointListView = (ListView) findViewById(R.id.list_view_points);

        //find and set empty view on the ListView, so that is only show when list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        pointListView.setEmptyView(emptyView);

        mPointCursorAdapter = new PointCursorAdapter(this, null);
        pointListView.setAdapter(mPointCursorAdapter);

        //Setup item click listener
        pointListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@Link EditorActivity}
                Intent intent = new Intent(ProgramEditActivity.this, AddPointActivity.class);

                //Form the content URI that represents the specific pet that was clicked on,
                //by appending the "id" (passed as input to this method) onto the
                // {@link ProgramEntry#CONTENT_URI}
                // for example, the URI would be "content://com.example.android.programs/program/2"
                //if the pet with ID 2 was clicked on
                Uri currentPointUri = ContentUris.withAppendedId(ProgramContract.ProgramEntry.CONTENT_URI_POINTS, id);

                //Set the URI on the data field of the intent
                intent.setData(currentPointUri);

                // Launch the activity to display the data
                startActivity(intent);
            }
        });


        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_ID_LOADER, null, this);
    }



    private void initPointLoader() {
        getSupportLoaderManager().initLoader(POINTS_LOADER, null, this);
    }


    //Show toast
    void displayToast(String text) {

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
        Log.i(LOG_TAG, "toast displayed");
        // toast.setGravity(Gravity.BOTTOM,0,0);

    }

    private void deleteProgram (){
        Uri pointsUri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_POINTS);

        String selection = ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID +"=?";
        String [] selectionArgs = {Integer.toString(mCurrentProgramId)};

        int deletePoints = getContentResolver().delete(pointsUri,selection,selectionArgs);

        if (deletePoints == -1) {
            //If the  new content URI is null, then there was a
            // n error with insertion
            displayToast("Error with delete program");
            Log.i(LOG_TAG, "Error with delete points in program");
        } else {

            int deleteProgram = getContentResolver().delete(mCurrentProgramUri,null,null);

            if (deleteProgram == 0) {
                //If the  new content URI is null, then there was an error with insertion
                displayToast("Error with delete program");
                Log.i(LOG_TAG, "Error with delete program");
            } else {
                String deleteProgramMessage = "Program deleted successful";
                displayToast(deleteProgramMessage);
                Log.i(LOG_TAG, "Deleted program row is " + Integer.toString(deleteProgram));
                Log.i(LOG_TAG, "Deleted points row is " + Integer.toString(deletePoints));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_program_edit, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case action_edit_program_name:

                Intent intent = new Intent(ProgramEditActivity.this, AddProgramActivity.class);
                intent.setData(mCurrentProgramUri);
                startActivity(intent);
                // insertPet();
                return true;


            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete:
                // Do nothing for now
                deleteProgram();
                Intent intentDelete = new Intent(ProgramEditActivity.this, ProgramsActivity.class);
                startActivity(intentDelete);
                return true;
            case android.R.id.home:
                // Navigate back to parent activity (ProgramViewActivity)
                Intent intent1 = new Intent(ProgramEditActivity.this, ProgramViewActivity.class);
                intent1.setData(mCurrentProgramUri);
                NavUtils.navigateUpTo(this, intent1);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id ==  EXISTING_PROGRAM_ID_LOADER)
        {
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


        if (id ==  POINTS_LOADER)
        {
            String[] projectionForPoint = {
                    ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID,
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_TIME,
                    ProgramContract.ProgramEntry.COLUMN_TEMPERATURE
            };

            String  mCurrentProgramIdString = Integer.toString(mCurrentProgramId);

            // Select Where ProgramId = currentProgramID
            String select = "(" + ProgramContract.ProgramEntry.COLUMN_PROGRAM_ID + "=" + mCurrentProgramIdString +"  )";


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
                int currentProgramIdIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry._ID);
                int currentProgramNameIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);

                mCurrentProgramName = cursor.getString(currentProgramNameIndex);
                mCurrentProgramId = cursor.getInt(currentProgramIdIndex);

                setTitle(mCurrentProgramName);
            }
            initPointLoader();
                break;

            case POINTS_LOADER:
               mPointCursorAdapter.swapCursor(cursor);
        }


    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPointCursorAdapter.swapCursor(null);
    }
}
