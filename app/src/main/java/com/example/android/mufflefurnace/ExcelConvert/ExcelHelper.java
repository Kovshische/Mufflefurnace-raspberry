package com.example.android.mufflefurnace.ExcelConvert;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.android.mufflefurnace.ArchivePointCursorAdapter;
import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.Data.ProgramDbHelper;
import com.example.android.mufflefurnace.Data.ProgramProvider;
import com.example.android.mufflefurnace.PointCursorAdapter;
import com.example.android.mufflefurnace.ProgramCursorAdapter;
import com.example.android.mufflefurnace.R;
import com.github.mjdev.libaums.fs.UsbFile;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 4/5/2018.
 */

public class ExcelHelper implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String PROGRAM_NAME = "Program:";
    private static final String STARTED_AT = "Started at:";

    private static final String TIME = "Time";
    private static final String TARGET_T = "Target T";
    private static final String SENSOR_T = "Sensor T";
    private static final String POWER = "Power";
    private static final String VENT = "Vent";
    private static final String DOOR = "Door";


    private static final String LOG_TAG = ExcelHelper.class.getSimpleName();
    private Uri currentAProgramUri;
    private Integer currentAProgramId;
    private UsbFile usbFile;
    private File file;
    UsbDevice device;
    Context context;
    LoaderManager loaderManager;

    private static final int A_PROGRAM_LOADER = 1;

    private String aProgramName;
    private String aProgramNameWithTime;
    private String aProgramStartedAt;
    private String fileName;

    private ProgramDbHelper programDbHelper;
    SQLiteDatabase db;


    private SharedPreferences sharedPreferences;
    private CellStyle csTemperature;

    public ExcelHelper(Context context, Uri uri, Integer id) {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        this.context = context;
        currentAProgramUri = uri;
        currentAProgramId = id;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

//        getProgramName();

        String[] projectionForAProgram = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME,
                ProgramContract.ProgramEntry.COLUMN_STARTED_AT
        };

        String[] selectionArgs = {
                String.valueOf(currentAProgramId)
        };
        Log.d(LOG_TAG, "Current program id " + String.valueOf(currentAProgramId));

        programDbHelper = new ProgramDbHelper(context);
        db = programDbHelper.getReadableDatabase();

//        ProgramProvider programProvider = new ProgramProvider();

        Cursor cursor =  db.query(
                ProgramContract.ProgramEntry.TABLE_A_PROGRAMS,
                projectionForAProgram,
                ProgramContract.ProgramEntry._ID + "=?",
                selectionArgs,
                null,
                null,
                null);


        if (cursor == null || cursor.getCount() < 1) {
            Log.d(LOG_TAG, "cursor is NOT valid");
        } else if (cursor.moveToFirst()) {
            int currentAProgramNameIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME);
            int currentAProgramStartedIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_STARTED_AT);

            aProgramName = cursor.getString(currentAProgramNameIndex);
            aProgramStartedAt = cursor.getString(currentAProgramStartedIndex);
            aProgramStartedAt = ProgramCursorAdapter.convertDateForFileName(aProgramStartedAt);
            aProgramNameWithTime = aProgramName + "_" + aProgramStartedAt;

            fileName = aProgramNameWithTime + ".xls";
        }
    }


    public File createExcelFile() throws Exception {


        Workbook workbook = new XSSFWorkbook();

        Sheet sheet1 = workbook.createSheet("test");
//    writeToSheet(testData(), sheet1);

        //Set program Name, started at
        Row row1 = sheet1.createRow(1);
        Cell cell_1_A = row1.createCell(1);
//        nameCell.setCellType(Cell.CELL_TYPE_STRING);
        cell_1_A.setCellValue(PROGRAM_NAME);
        Cell cell_1_B = row1.createCell(2);
        cell_1_B.setCellValue(aProgramName);

        Row row2 = sheet1.createRow(2);
        Cell cell_2_A = row2.createCell(1);
        cell_2_A.setCellValue(STARTED_AT);
        Cell call_2_B = row2.createCell(2);
        call_2_B.setCellValue(aProgramStartedAt);

        Row row4 = sheet1.createRow(4);
        row4.createCell(1).setCellValue(TIME);
        row4.createCell(2).setCellValue(TARGET_T);
        row4.createCell(3).setCellValue(SENSOR_T);
        row4.createCell(4).setCellValue(POWER);


        int i = 5;
        boolean ifVentEnabled;
        ifVentEnabled = sharedPreferences.getBoolean(context.getString(R.string.settings_vent_options_key), false);
        Log.d(LOG_TAG, "vent enabled " + ifVentEnabled);
        if (ifVentEnabled == true){
            row4.createCell(i).setCellValue(VENT);
            i++;
        }
        boolean ifDoorEnabled;
        ifDoorEnabled = sharedPreferences.getBoolean(context.getString(R.string.settings_door_options_key), false);
        Log.d(LOG_TAG, "door enabled " + ifDoorEnabled);
        if (ifDoorEnabled == true){
            row4.createCell(i).setCellValue(DOOR);
        }



        //Add points
        String[] projectionForAPoint = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID,
                ProgramContract.ProgramEntry.COLUMN_A_TIME,
                ProgramContract.ProgramEntry.COLUMN_A_TARGET_TEMPERATURE,
                ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE,
                ProgramContract.ProgramEntry.COLUMN_A_VENT,
                ProgramContract.ProgramEntry.COLUMN_A_DOOR,
                ProgramContract.ProgramEntry.COLUMN_A_POWER
        };

        String[] selectionArgs = {
                String.valueOf(currentAProgramId)
        };

        Cursor cursor =  db.query(
                ProgramContract.ProgramEntry.TABLE_A_POINTS,
                projectionForAPoint,
                ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_ID + "=?",
                selectionArgs,
                null,
                null,
                ProgramContract.ProgramEntry.COLUMN_A_TIME);

        if (cursor == null || cursor.getCount() < 1) {
            Log.d(LOG_TAG, "cursor 2 is NOT valid");
        } else if (cursor.moveToFirst()) {
            int timeColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_TIME);
            int targetTemperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_TARGET_TEMPERATURE);
            int sensorTemperatureColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_SENSOR_TEMPERATURE);
            int powerColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_POWER);
            int ventColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_VENT);
            int doorColumnIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_A_DOOR);

            int rowCounter = 5;
            while (cursor.moveToNext()) {
                Row row = sheet1.createRow(rowCounter);

                //Time
                int time = cursor.getInt(timeColumnIndex);
                String timeString = ArchivePointCursorAdapter.mTimeToString(time);
                row.createCell(1).setCellValue(timeString);

                //Target T
                int targetT = cursor.getInt(targetTemperatureColumnIndex);
                String targetTString = Integer.toString(targetT);

//                csTemperature = workbook.createCellStyle();
//                csTemperature.setFillForegroundColor(HSSFColor.LIME.index);
//                csTemperature.setDataFormat(workbook.createDataFormat().getFormat("###"));

                Cell c = null;
                c = row.createCell(2);
                c.setCellValue(targetT);
//                c.setCellStyle(csTemperature);

                //Sensor T
                int sensorT = cursor.getInt(sensorTemperatureColumnIndex);
                String sensorTString = Integer.toString(sensorT);
                c = row.createCell(3);
                c.setCellValue(sensorT);


                //Power
                int power = cursor.getInt(powerColumnIndex);
                String powerString = PointCursorAdapter.powerToString(power);
                c = row.createCell(4);
                c.setCellValue(powerString);

                //vent
                int ii = 5;
                if (ifVentEnabled == true){
                    int vent = cursor.getInt(ventColumnIndex);
                    String ventString = PointCursorAdapter.ventToString(vent);
                    c = row.createCell(ii);
                    c.setCellValue(ventString);
                    ii ++;
                }

                //door
                if (ifDoorEnabled == true){
                    int door = cursor.getInt(doorColumnIndex);
                    String doorString = PointCursorAdapter.doorToString(door);
                    c = row.createCell(ii);
                    c.setCellValue(doorString);
                }

                rowCounter++;
            }
        }



            //Add graph
            // Create a drawing canvas on the worksheet
            Drawing drawing = sheet1.createDrawingPatriarch();
            // Define anchor points in the worksheet to position the chart
            ClientAnchor anchor = drawing.createAnchor(0,0,0,0,8,1,18,11);
             // Create the chart object based on the anchor point
            Chart chart = drawing.createChart(anchor);
             // Define legends for the line chart and set the position of the legend
            ChartLegend legend = chart.getOrCreateLegend();
            legend.setPosition(LegendPosition.BOTTOM);
             // Create data for the chart
            LineChartData data = chart.getChartDataFactory().createLineChartData();
            // Define chart AXIS
            ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
            ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
            // Define Data sources for the chart
            // Set the right cell range that contain values for the chart
            // Pass the worksheet and cell range address as inputs
            // Cell Range Address is defined as First row, last row, first column, last column
            ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sheet1, new CellRangeAddress(5,15,1,1));
            ChartDataSource<Number> ysTargetT = DataSources.fromNumericCellRange(sheet1, new CellRangeAddress(5,15,2,2));
            // Add chart data sources as data to the chart
            data.addSeries(xs,ysTargetT);
            // Plot the chart with the inputs from data and chart axis
            chart.plot(data,new ChartAxis[]{bottomAxis,leftAxis});







        File file = new File(context.getExternalFilesDir(null), fileName);

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.d(LOG_TAG, "Success");
        } catch (IOException e){
            Log.d(LOG_TAG, e.toString());
        } catch (Exception e){
            Log.d(LOG_TAG, e.toString());
        }


        return file;
    }

    public void writeExcelFile(FileOutputStream os) throws Exception {
        Workbook workbook = new HSSFWorkbook();

        Sheet sheet1 = workbook.createSheet("test");
//    writeToSheet(testData(), sheet1);

        Row row = sheet1.createRow(1);
        Cell nameCell = row.createCell(1);
//        nameCell.setCellType(Cell.CELL_TYPE_STRING);
        nameCell.setCellValue("test");


//        File file = new File(fileName);

        File file = new File(context.getExternalFilesDir(null), fileName);
        File file1 = new File(context.getFilesDir(), fileName);


        FileOutputStream fileOutputStream = os;
        workbook.write(fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();


    }

    private void getProgramName() {

//        loaderManager.initLoader(A_PROGRAM_LOADER, null,  this);

        String[] projectionForAProgram = {
                ProgramContract.ProgramEntry._ID,
                ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME,
                ProgramContract.ProgramEntry.COLUMN_STARTED_AT
        };

        programDbHelper = new ProgramDbHelper(context);
        SQLiteDatabase db = programDbHelper.getReadableDatabase();

        ProgramProvider programProvider = new ProgramProvider();
        Cursor cursor =  db.query(
                ProgramContract.ProgramEntry.TABLE_A_PROGRAMS,
                projectionForAProgram,
                null,
                null,
                null,
                 null,
                 null);


        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int currentAProgramNameIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME);
            int currentAProgramStartedIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_STARTED_AT);

            aProgramName = cursor.getString(currentAProgramNameIndex);
            aProgramStartedAt = cursor.getString(currentAProgramStartedIndex);
            aProgramStartedAt = ProgramCursorAdapter.convertDateForFileName(aProgramStartedAt);

            fileName = aProgramName + ".xls";
        }
    }


    public String getFileName() {
//        getProgramName();
        return fileName;
    }

    public void writeToUsbFlash (){

    }

    public void writeToTestFile(OutputStream outputStream) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet1 = workbook.createSheet("sheet_1");

        HSSFRow row = sheet1.createRow(1);

        HSSFCell nameCell = row.createCell(0);
        nameCell.setCellType(Cell.CELL_TYPE_STRING);
        nameCell.setCellValue("test");


        FileOutputStream fileOutputStream = new FileOutputStream(file);
        workbook.write(fileOutputStream);
//    file.getAbsoluteFile();
        fileOutputStream.close();


//    OutputStream outputStream = new UsbFileOutputStream(usbFile);
//    outputStream.write(fileOutputStream);


    }

/*
public void createFile (){

    UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    registerReceiver(mUsbReceiver, filter);

    mUsbManager.requestPermission(device, mPermissionIntent);

    UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this );

    for(UsbMassStorageDevice device: devices)

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

        UsbFile newDir = root.createDirectory("HotSpace");
        UsbFile file = newDir.createFile("bar.txt");
        UsbFile fileExcel = newDir.createFile(aProgramName + aProgramStartedAt + ".xls");

// write to a file
        OutputStream os = new UsbFileOutputStream(file);

        os.write("hello".getBytes());
        os.close();

    } catch (IOException e) {
        Log.d(LOG_TAG, "Can not get access to the usb");
        e.printStackTrace();
    }
}
*/

    private List<List<String>> testData() {
        List<List<String>> data = new ArrayList<List<String>>();
        data.add(Arrays.asList(new String[]{"column 1", "column 2", "column 3"}));
        data.add(Arrays.asList(new String[]{"value 1 1", "value 1 2", "value 1 3"}));
        data.add(Arrays.asList(new String[]{"value 2 1", "value 2 2", "value 2 3"}));
        data.add(Arrays.asList(new String[]{"value 3 1", "value 3 2", "value 3 3"}));
        return data;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == A_PROGRAM_LOADER) {
            Log.d(LOG_TAG, "A_PROGRAMS_LOADER onCreate");
            String[] projectionForAProgram = {
                    ProgramContract.ProgramEntry._ID,
                    ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME,
                    ProgramContract.ProgramEntry.COLUMN_STARTED_AT
            };
            return new CursorLoader(context,
                    currentAProgramUri,
                    projectionForAProgram,
                    null,
                    null,
                    null
            );
        } else {
            return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case A_PROGRAM_LOADER:
                if (cursor == null || cursor.getCount() < 1) {
                    return;
                }
                if (cursor.moveToFirst()) {
                    int currentAProgramNameIndex = cursor.getColumnIndex(ProgramContract.ProgramEntry.COLUMN_A_PROGRAM_NAME);
                    int currentAProgramStartedIndex = cursor.getColumnIndexOrThrow(ProgramContract.ProgramEntry.COLUMN_STARTED_AT);

                    aProgramName = cursor.getString(currentAProgramNameIndex);
                    aProgramStartedAt = cursor.getString(currentAProgramStartedIndex);
                    aProgramStartedAt = ProgramCursorAdapter.convertDate(aProgramStartedAt);

                    fileName = aProgramName + ".xls";
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
