package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses CSV files.
 *
 * <p>The CSV file is assumed to have a first line containing the column headings.
 * The CSV file must be in UTF-8.
 * Does not handle quotes in fields (e.g. as generated by Excel).
 * Field names are case-sensitive.
 *
 * <p>Create an object and set attributes such as the field-separator, list of acceptable columns, etc.
 * Then either call parseAndCallHandler or parseToListOfMaps.
 * <pre>
 *    CsvLineHandler myHandler = new CsvLineHandler() {
 *        void processCsvLine(Map<String,String> line) { .. }
 *    };
 *    CsvParser csvParser = new CsvParser();
 *    csvParser.setDesiredFields("abc","def"); // field set in file must be this set
 *    csvParser.setNonEmptyFields("abc");      // all of these fields must have non-empty values
 *    csvParser.parseAndCallHandler(myHandler, aFile);
 *    csvParser.parseAndCallHandler(myHandler, aReader);
 *    List<Map<String,String>> contents = csvParser.parseToListOfMaps(aFile);
 * </pre>
 *
 * <p>Glossary:
 * <ul>
 * <li>Field: name of column
 * <li>Column index: e.g. 0 is the left-most column
 * <li>Line: a row of data or header
 * </ul>
 *
 */
public class CsvParser {

    public interface CsvLineHandler {
        void processCsvLine(Map<String, String> line);
    }

    public class MalformedCsvException extends Exception {  // checked ex. because it's always possible CSV invalid, must handle it
        public MalformedCsvException(String msg) { super(msg); }
    }

    protected class ArrayOfMapsLineHandler implements CsvLineHandler {
        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        public void processCsvLine(Map<String, String> line) { result.add(line); }
    }

    protected String fieldSeparatorRegexp = ",";
    protected String[] desiredFields = null;
    protected String[] nonEmptyFields = null;

    public void setFieldSeparatorRegexp(String x) { fieldSeparatorRegexp = x; }
    public void setDesiredFields(String... x) { desiredFields = x; }
    public void setNonEmptyFields(String... x) { nonEmptyFields = x; }

    public void parseAndCallHandler(CsvLineHandler lineHandler, BufferedReader r) throws MalformedCsvException {
        try {
            String headerLine = r.readLine();
            if (headerLine == null) throw new MalformedCsvException("File was empty (header line is mandatory)");
            String[] fieldForColIdx = headerLine.split(fieldSeparatorRegexp);
            if (desiredFields != null) {
                for (String desiredField : desiredFields)
                    if ( ! Arrays.asList(fieldForColIdx).contains(desiredField))
                        throw new MalformedCsvException("Column '" + desiredField + "' is missing");
                for (String foundField : fieldForColIdx)
                    if ( ! Arrays.asList(desiredFields).contains(foundField))
                        throw new MalformedCsvException("Column '" + foundField + "' unexpected");
            }

            int lineNumber = 2;
            while (true) {
                try {
                    String line = r.readLine();
                    if (line == null) break; // end of file
                    String[] valueForColIdx = line.split(fieldSeparatorRegexp);
                    if (valueForColIdx.length == 0) continue; // ignore blank lines e.g. at end of file
                    if (valueForColIdx.length != fieldForColIdx.length) throw new MalformedCsvException("Expected " +
                        fieldForColIdx.length + " fields but found " + valueForColIdx.length + " fields");
                    Map<String, String> valueForField = new HashMap<String, String>();
                    for (int c = 0; c < valueForColIdx.length; c++) {
                        String field = fieldForColIdx[c];
                        String val = valueForColIdx[c];
                        if (nonEmptyFields != null && Arrays.asList(nonEmptyFields).contains(field))
                            if (val.length() == 0) throw new MalformedCsvException("Column " + c + ", field '" + field + "': value may not be empty");
                        valueForField.put(field, val);
                    }
                    lineHandler.processCsvLine(valueForField);

                    lineNumber++;
                }
                catch (MalformedCsvException e) { throw new MalformedCsvException("Line " + lineNumber + ": " + e.getMessage()); }
            }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void parseAndCallHandler(CsvLineHandler lineHandler, File f) throws MalformedCsvException {
        try {
            FileReader r = new FileReader(f);
            try {
                BufferedReader br = new BufferedReader(r);
                parseAndCallHandler(lineHandler, br);
            }
            finally { r.close(); }
        }
        catch (FileNotFoundException e) { throw new MalformedCsvException("File '"+f+"' doesn't exist"); }
        catch (IOException e) { throw new RuntimeException(f + ": " + e.getMessage(), e); }
        catch (MalformedCsvException e) { throw new MalformedCsvException(f + ": " + e.getMessage()); }
    }

    public List<Map<String, String>> parseToListOfMaps(BufferedReader r) throws MalformedCsvException {
        ArrayOfMapsLineHandler lineHandler = new ArrayOfMapsLineHandler();
        parseAndCallHandler(lineHandler, r);
        return lineHandler.result;
    }

    public List<Map<String, String>> parseToListOfMaps(File f) throws MalformedCsvException {
        ArrayOfMapsLineHandler lineHandler = new ArrayOfMapsLineHandler();
        parseAndCallHandler(lineHandler, f);
        return lineHandler.result;
    }
}
