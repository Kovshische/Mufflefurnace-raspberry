package com.example.android.mufflefurnace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.mufflefurnace.Data.ProgramContract;


public class AddProgramActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String LOG_TAG = AddProgramActivity.class.getSimpleName();

    private String addProgramMessage;
    private String editProgramNameMessage;


    private Uri mCurrentProgramUri;

    private static final int EXISTING_PROGRAM_LOADER = 1;

    private EditText mProgramNameTextView;

    //   private ProgramDbHelper mDbHelper = new ProgramDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_program);

        Intent intent = getIntent();
        mCurrentProgramUri = intent.getData();

        if (mCurrentProgramUri == null) {
            setTitle(R.string.add_program_title_add_program);
        } else {
            setTitle(R.string.add_program_title_edit_program);
        }

        // Find all relevant views that we will need to read user input from
        mProgramNameTextView = (EditText) findViewById(R.id.edit_program_name);

        getSupportLoaderManager().initLoader(EXISTING_PROGRAM_LOADER, null, this);

    }

    //Show toast
    void displayToast(String text) {

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
        Log.i(LOG_TAG, "toast displayed");
        // toast.setGravity(Gravity.BOTTOM,0,0);
    }

    private void insertProgram() {
        String nameString = mProgramNameTextView.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME, nameString);

        Uri newUri = getContentResolver().insert(ProgramContract.ProgramEntry.CONTENT_URI_PROGRAMS, values);

        if (newUri == null) {
            //If the  new content URI is null, then there was an error with insertion
            displayToast("Error with saving program");
            Log.i(LOG_TAG, "Error with saving program");
        } else {
            addProgramMessage = "Program saved successful";
            displayToast(addProgramMessage);
            Log.i(LOG_TAG, "New row is " + newUri.toString());
        }
    }



    private void updateProgram() {
        String nameString = mProgramNameTextView.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME, nameString);

        int update = getContentResolver().update(mCurrentProgramUri, values, ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME, null);

        if (update == 0) {
            //If the  new content URI is null, then there was an error with insertion
            displayToast("Error with update program");
            Log.i(LOG_TAG, "Error with update program");
        } else {
            editProgramNameMessage = "Program updated successful";
            displayToast(editProgramNameMessage);
            Log.i(LOG_TAG, "Updated row is " + Integer.toString(update));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (mCurrentProgramUri == null) {
                    // Save pet to the data base
                    insertProgram();
                    finish();
                    return true;
                } else {
                    updateProgram();
                    finish();
                    return true;
                }


                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (mCurrentProgramUri == null) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                } else {
                    // Navigate back to parent activity (ProgramEditActivity)
                    Intent intent1 = new Intent(AddProgramActivity.this, ProgramEditActivity.class);
                    intent1.setData(mCurrentProgramUri);
                    NavUtils.navigateUpTo(this, intent1);
                    return true;
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mCurrentProgramUri == null) {
            return null;
        }

        String[] projection = {
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_PROGRAM_NAME);

            String name = cursor.getString(nameColumnIndex);

            mProgramNameTextView.setText(name);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProgramNameTextView.setText("");

    }
}
