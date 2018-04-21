package com.example.android.mufflefurnace.ExcelConvert;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.android.mufflefurnace.Data.ProgramContract;
import com.example.android.mufflefurnace.Data.ProgramDbHelper;
import com.example.android.mufflefurnace.Data.ProgramProvider;
import com.example.android.mufflefurnace.ProgramCursorAdapter;
import com.github.mjdev.libaums.fs.UsbFile;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 4/5/2018.
 */

public class ExcelHelper implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ExcelHelper.class.getSimpleName();
    private Uri currentAProgramUri;
    private Integer currentProgramId;
    private UsbFile usbFile;
    private File file;
    UsbDevice device;
    Context context;
    LoaderManager loaderManager;

    private static final int A_PROGRAM_LOADER = 1;

    private String aProgramName;
    private String aProgramStartedAt;
    private String fileName;

    private ProgramDbHelper programDbHelper;

    public ExcelHelper(Context context, Uri uri, Integer id) {
        this.context = context;
        currentAProgramUri = uri;
        currentProgramId = id;
//        getProgramName();

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


    public File createExcelFile() throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet1 = workbook.createSheet("sheet_test");
//    writeToSheet(testData(), sheet1);

        HSSFRow row = sheet1.createRow(1);
        HSSFCell nameCell = row.createCell(1);
        nameCell.setCellType(Cell.CELL_TYPE_STRING);
        nameCell.setCellValue("test");


//        File file = new File(fileName);

        File file1 = new File(context.getFilesDir(), fileName);


        FileOutputStream fileOutputStream = new FileOutputStream(file1);
        workbook.write(fileOutputStream);
        fileOutputStream.close();

        return file1;
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
