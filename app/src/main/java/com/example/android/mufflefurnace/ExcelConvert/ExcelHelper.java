package com.example.android.mufflefurnace.ExcelConvert;

import android.net.Uri;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 4/5/2018.
 */

public class ExcelHelper {

    private Uri currentAProgramUri;

    public ExcelHelper(Uri uri) {
        currentAProgramUri = uri;
    }

public void createExcelFile ()throws Exception{
    HSSFWorkbook workbook = new HSSFWorkbook();

    HSSFSheet sheet1 = workbook.createSheet("sheet name 1");
//    writeToSheet(testData(), sheet1);

    HSSFRow row = sheet1.createRow(0);
    HSSFCell nameCell = row.createCell(0);
    nameCell.setCellType(Cell.CELL_TYPE_STRING);
    nameCell.setCellValue("test");



    File file = new File("testSheet.xls");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    workbook.write(fileOutputStream);
    fileOutputStream.close();

}


public File getTestFile() throws Exception{
    HSSFWorkbook workbook = new HSSFWorkbook();

    HSSFSheet sheet1 = workbook.createSheet("sheet name 1");
//    writeToSheet(testData(), sheet1);

    HSSFRow row = sheet1.createRow(0);
    HSSFCell nameCell = row.createCell(0);
    nameCell.setCellType(Cell.CELL_TYPE_STRING);
    nameCell.setCellValue("test");



    File file = new File("testSheet.xls");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    workbook.write(fileOutputStream);
    fileOutputStream.close();

    return file;
}
    private List<List<String>> testData() {
        List<List<String>> data = new ArrayList<List<String>>();
        data.add(Arrays.asList(new String[] {"column 1", "column 2", "column 3"}));
        data.add(Arrays.asList(new String[] {"value 1 1", "value 1 2", "value 1 3"}));
        data.add(Arrays.asList(new String[] {"value 2 1", "value 2 2", "value 2 3"}));
        data.add(Arrays.asList(new String[] {"value 3 1", "value 3 2", "value 3 3"}));
        return data;
    }
}
