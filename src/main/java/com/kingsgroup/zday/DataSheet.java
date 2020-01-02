package com.kingsgroup.zday;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class DataSheet {
    private Head[] header;
    private List<Map<String, String>> rows;

    void writeToJson(String filename) throws Exception {
        Head[] header = getHeader();
        Map<String, Object> sheetJson = new LinkedHashMap<>();
        Map<String, String> jsonHeader = new LinkedHashMap<>();
        for (int i = 0; i < header.length; i ++) {
            jsonHeader.put(header[i].getShortName(), header[i].getName());
        }

        sheetJson.put("header", jsonHeader);
        sheetJson.put("rows", rows);

        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(sheetJson);
        BufferedWriter output = new BufferedWriter(new FileWriter(filename));
        output.write(json);
        output.close();
        //System.out.println(json);
    }

    static DataSheet readFromExcel(String filename) throws Exception {
        try (FileInputStream fis = new FileInputStream(filename)) {
            Workbook workbook = new XSSFWorkbook(fis);

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return null;
            }

            DataSheet dataSheet = new DataSheet();
            // 读取 header
            Row rowHeader = rowIterator.next();
            Head[] header = new Head[rowHeader.getLastCellNum()];

            Iterator<Cell> cellIterator = rowHeader.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                header[cell.getColumnIndex()] = new Head(cell.toString());
            }

            dataSheet.header = header;

            List<Map<String, String>> dataRows = new ArrayList<>();
            //
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> datum = new LinkedHashMap<>();
                boolean hasData = false;
                cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (header.length > cell.getColumnIndex()) {
                        CellType type = cell.getCellType();
                        String value = getCellString(type, cell);
                        if (value != null) {
                            hasData = true;
                            datum.put(header[cell.getColumnIndex()].getShortName(), value);
                        }
                    }
                }

                if (hasData) {
                    dataRows.add(datum);
                }
            }

            dataSheet.rows = dataRows;

            return dataSheet;
        }
    }


    private Head[] getHeader() {
        return header;
    }

    private List<Map<String, String>> getRows() {
        return rows;
    }

    private static boolean numericEqual(double d1, double d2) {
        double diff = d1 - d2;
        double zero = .000001;

        return diff > -zero && diff < zero;
    }

    private static boolean attemptToInt(double num, int[] refInt) {

        double round = Math.round(num);
        if (numericEqual(num, round)) {
            refInt[0] = (int) round;

            return true;
        }

        return false;
    }

    private static String getCellString(CellType type, Cell cell) throws Exception {
        switch (type) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double cellValue = cell.getNumericCellValue();
                int[] refInt = new int[1];
                return attemptToInt(cellValue, refInt) ? String.valueOf(refInt[0]) : String.valueOf(cellValue);
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "true" : "false";
            case FORMULA:
                return getCellString(cell.getCachedFormulaResultType(), cell);
            case BLANK:
                return null;
            default:
                throw new Exception("invalid cell type " + type);
        }
    }
}
