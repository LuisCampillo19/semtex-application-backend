package com.semtex.infrastructure.out.parsing;

import com.semtex.domain.model.ParsedSpreadsheet;
import com.semtex.domain.port.out.SpreadsheetParserPort;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de parseo Excel/CSV con Apache POI + commons-csv.
 *
 * Cada fila se convierte en {@code Map<columna, valor>} que luego se persiste como JSONB.
 * Los valores numéricos/booleanos se infieren para que las consultas JSONB por número funcionen.
 */
@Component
public class PoiCsvSpreadsheetParser implements SpreadsheetParserPort {

    @Override
    public ParsedSpreadsheet parse(byte[] content, String contentType, String filename) {
        if (isCsv(contentType, filename)) {
            return parseCsv(content);
        }
        return parseExcel(content);
    }

    private boolean isCsv(String contentType, String filename) {
        boolean byMime = contentType != null && contentType.toLowerCase().contains("csv");
        boolean byExt = filename != null && filename.toLowerCase().endsWith(".csv");
        return byMime || byExt;
    }

    // ---------------- Excel ----------------

    private ParsedSpreadsheet parseExcel(byte[] content) {
        Map<String, List<Map<String, Object>>> rowsBySheet = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            DataFormatter formatter = new DataFormatter();

            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName();
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    warnings.add("Hoja '" + sheetName + "' vacía — ignorada.");
                    continue;
                }
                List<String> headers = extractHeaders(headerRow, formatter);
                List<Map<String, Object>> rows = new ArrayList<>();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || isRowBlank(row, formatter)) continue;
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int col = 0; col < headers.size(); col++) {
                        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        rowMap.put(headers.get(col), extractCellValue(cell, formatter));
                    }
                    rows.add(rowMap);
                }
                rowsBySheet.put(sheetName, rows);
                totalRows += rows.size();
            }
        } catch (IOException e) {
            throw new SpreadsheetParseException("No se pudo leer el archivo Excel.", e);
        }
        return new ParsedSpreadsheet(rowsBySheet, totalRows, warnings);
    }

    private List<String> extractHeaders(Row headerRow, DataFormatter formatter) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim();
            headers.add(header.isEmpty() ? "col_" + cell.getColumnIndex() : header);
        }
        return headers;
    }

    private Object extractCellValue(Cell cell, DataFormatter formatter) {
        return switch (cell.getCellType()) {
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> formatter.formatCellValue(cell);
            case BLANK -> null;
            default -> cell.getStringCellValue().trim();
        };
    }

    private boolean isRowBlank(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) return false;
        }
        return true;
    }

    // ---------------- CSV ----------------

    private ParsedSpreadsheet parseCsv(byte[] content) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            for (CSVRecord record : parser) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (String header : headers) {
                    String raw = record.isMapped(header) ? record.get(header) : "";
                    rowMap.put(header, coerce(raw));
                }
                rows.add(rowMap);
            }
        } catch (IOException e) {
            throw new SpreadsheetParseException("No se pudo leer el archivo CSV.", e);
        }
        return new ParsedSpreadsheet(Map.of("default", rows), rows.size(), warnings);
    }

    private Object coerce(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException ignored) {
            // no es número
        }
        if ("true".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(value)) return Boolean.FALSE;
        return value;
    }

    /** Error de parseo. Extiende IllegalArgumentException → el GlobalExceptionHandler lo mapea a HTTP 400. */
    public static class SpreadsheetParseException extends IllegalArgumentException {
        public SpreadsheetParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
