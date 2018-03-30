package com.example.android.mufflefurnace;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by admin on 3/19/2018.
 */

public class ArchiveProgramViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ArchiveProgramViewActivity.class.getSimpleName();

    private Uri currentProgramUri;
    private GraphView graph;
    private SharedPreferences sharedPreferences;
    private boolean ifVentEnabled;
    private static final int A_TARGET_POINT_LOADER = 1;
    private static final int A_POINT_LOADER = 2;
    private int currentProgramId;
    ArchivePointCursorAdapter mPointCursorAdapter;

    private TextView graphInfoTextView;

    ArrayList<DataPoint> dataPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventOpenPointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> ventClosePointArrayList = new ArrayList<DataPoint>();
    ArrayList<DataPoint> aDataPointArrayList = new ArrayList<DataPoint>();

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    UsbDevice device;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_program_view);

        graphInfoTextView = (TextView) findViewById(R.id.archive_program_view_Info_graph_text);


        Intent intent = getIntent();
        currentProgramUri = intent.getData();
        currentProgramId = parsIdFromUri(currentProgramUri);

        ListView pointListView = (ListView) findViewById(R.id.list_view_a_points);
        pointListView.setFocusable(false);
        pointListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mPointCursorAdapter = new ArchivePointCursorAdapter(this, null);
        pointListView.setAdapter(mPointCursorAdapter);

        graph = (GraphView) findViewById(R.id.archive_graph_view);
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
/*
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
*/
        //set Vent visibility
        TextView ventTextView = (TextView) findViewById(R.id.archive_program_view_vent);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ifVentEnabled = sharedPreferences.getBoolean(getString(R.string.settings_vent_options_key), false);
        if (ifVentEnabled == false) {
            ventTextView.setVisibility(View.GONE);
        }

        getSupportLoaderManager().initLoader(A_TARGET_POINT_LOADER, null, this);



    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == A_TARGET_POINT_LOADER) {

            String[] projectionForPoint = {
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID,
                    ProgramContract.ProgramEntry.COLUMN_TIME,
                    ProgramContract.ProgramEntry.COLUMN_TEMPERATURE,
                    ProgramContract.ProgramEntry.COLUMN_VENT,
            };

            String mCurrentProgramIdString = Integer.toString(currentProgramId);

            // Select Where ProgramId = currentProgramID
            String select = "(" + ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID + "=" + mCurrentProgramIdString + "  )";


            return new CursorLoader(
                    this,
                    ProgramContract.ProgramEntry.CONTENT_URI_A_T_POINTS,
                    projectionForPoint,
                    select,
                    null,
                    ProgramContract.ProgramEntry.COLUMN_TIME
            );
        } else if(id == A_POINT_LOADER){

            String[] projectionForPoint = {
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID,
                    ProgramContract.ProgramEntry.COLUMN_A_TIME,
                    ProgramContract.ProgramEntry.COLUMN_A_TARGET_TEMPERATURE,
                    ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE,
                    ProgramContract.ProgramEntry.COLUMN_A_VENT,
                    ProgramContract.ProgramEntry.COLUMN_A_DOOR,
                    ProgramContract.ProgramEntry.COLUMN_A_POWER
            };

            String mCurrentProgramIdString = Integer.toString(currentProgramId);

            String select = "(" + ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID + "=" + mCurrentProgramIdString + "  )";

            return new CursorLoader(
                    this,
                    ProgramContract.ProgramEntry.CONTENT_URI_A_POINTS,
                    projectionForPoint,
                    select,
                    null,
                    ProgramContract.ProgramEntry.COLUMN_A_TIME
            );
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case A_TARGET_POINT_LOADER:


                if (cursor == null || cursor.getCount() < 1) {
                    graphInfoTextView.setVisibility(View.VISIBLE);
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

                initPointLoader();

                break;

            case A_POINT_LOADER:
                mPointCursorAdapter.swapCursor(cursor);

                Integer aPointsAmount= cursor.getCount();
                Integer sensorTemp = null;
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }

                int aTimeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_TIME);
                int aSensorTempColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE);
 //               int aVentColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_VENT);

                //add every second if overall time < 60 minutes
                if (aPointsAmount <= 360){
                    while (cursor.moveToNext()){
                        int time = cursor.getInt(aTimeColumnIndex);
                        double aTimeDouble = (double) time / (60 *60);

                        if (!cursor.isNull(aSensorTempColumnIndex)){
                            sensorTemp = cursor.getInt(aSensorTempColumnIndex);
                            aDataPointArrayList.add(new DataPoint(aTimeDouble, sensorTemp));
                        }
                    }
                    //add every minute if overall time < 60 minutes
                } else if (aPointsAmount <= 3600) {

                    while (cursor.move(10)) {
                        int time = cursor.getInt(aTimeColumnIndex);
                        double aTimeDouble = (double) time / (60 * 60);

                        if (!cursor.isNull(aSensorTempColumnIndex)) {
                            sensorTemp = cursor.getInt(aSensorTempColumnIndex);
                            aDataPointArrayList.add(new DataPoint(aTimeDouble, sensorTemp));
                        }
                    }
                } else {
                    while (cursor.move(60)) {
                        int time = cursor.getInt(aTimeColumnIndex);
                        double aTimeDouble = (double) time / (60 * 60);

                        if (!cursor.isNull(aSensorTempColumnIndex)) {
                            sensorTemp = cursor.getInt(aSensorTempColumnIndex);
                            aDataPointArrayList.add(new DataPoint(aTimeDouble, sensorTemp));
                        }
                    }
                }

                DataPoint[] aDataPoint = aDataPointArrayList.toArray(new DataPoint[]{});
                LineGraphSeries<DataPoint> seriesAPoints = new LineGraphSeries<>(aDataPoint);
                seriesAPoints.setColor(Color.RED);
                graph.addSeries(seriesAPoints);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private int parsIdFromUri(Uri uri) {
        String aProgramIdString = uri.getEncodedPath();
        String[] aProgramURIParts = aProgramIdString.split("/");
        aProgramIdString = aProgramURIParts[2];
        Log.d(LOG_TAG, "archive program id " + aProgramIdString);
        int id = Integer.parseInt(aProgramIdString);
        return id;
    }

    private void initPointLoader() {
        getSupportLoaderManager().initLoader(A_POINT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_archive_program_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_send:

                Log.d(LOG_TAG, "Send options is chosen");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_EMAIL, "addresses");
                intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
//                intent.putExtra(Intent.EXTRA_STREAM, attachment);

                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
                    Log.d(LOG_TAG, "intentEmail");
            } else {
                    Log.d(LOG_TAG, "intentEmail not supported");
                }


//                Intent mailClient = new Intent(Intent.ACTION_VIEW);
//                mailClient.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivity");
//                startActivity(mailClient);


//                 Intent mailIntent = new Intent(ArchiveProgramViewActivity.this, MailActivity.class);
//                 startActivity(mailIntent);

                //Works with file manager 111
                Intent intentM = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                intentM.addCategory(Intent.CATEGORY_OPENABLE);
//                intentM.setType("*/*");
//                startActivityForResult(intentM, 42);

                Intent intentT = new Intent(Intent.ACTION_GET_CONTENT);
                intentT.setType("*/*");
                startActivityForResult(Intent.createChooser(intentT, "Open with ..."), 42);
                return true;


            case R.id.action_test:

                UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);

                mUsbManager.requestPermission(device, mPermissionIntent);

                UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this );

                for(UsbMassStorageDevice device: devices) {

                    // before interacting with a device you need to call init()!
                    try {
                        device.init();

                        // Only uses the first partition on the device
                        FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
                        Log.d(LOG_TAG, "Capacity: " + currentFs.getCapacity());
                        Log.d(LOG_TAG, "Occupied Space: " + currentFs.getOccupiedSpace());
                        Log.d(LOG_TAG, "Free Space: " + currentFs.getFreeSpace());
                        Log.d(LOG_TAG, "Chunk size: " + currentFs.getChunkSize());

                        UsbFile root = currentFs.getRootDirectory();

                        UsbFile[] files = root.listFiles();
                        for(UsbFile file: files) {
                            Log.d(LOG_TAG, file.getName());
                            if(file.isDirectory()) {
//                                Log.d(LOG_TAG, Long.toString(file.getLength()));
                            }
                        }

                        UsbFile newDir = root.createDirectory("foo");
                        UsbFile file = newDir.createFile("bar.txt");

// write to a file
                        OutputStream os = new UsbFileOutputStream(file);

                        os.write("hello".getBytes());
                        os.close();
/*
// read from a file
                        InputStream is = new UsbFileInputStream(file);
                        byte[] buffer = new byte[currentFs.getChunkSize()];
                        is.read(buffer);
*/
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "Can not get access to the usb");
                        e.printStackTrace();
                    }


                }

        }

        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        Log.d(LOG_TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
}
