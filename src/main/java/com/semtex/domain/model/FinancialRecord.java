package com.semtex.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Modelo de dominio: FinancialRecord — una fila de un Excel/CSV procesado.
 *
 * El {@code Map<String,Object>} es el equivalente Java del JSONB de PostgreSQL:
 * flexible, sin esquema fijo, con acceso tipado por clave.
 *
 * REGLA HEXAGONAL: Java puro, sin anotaciones externas.
 */
public class FinancialRecord {

    private final UUID id;
    private final UUID documentId;
    private final UUID organizationId;
    private final String sheetName;
    private final Integer rowIndex;
    private final Map<String, Object> rowData;
    private final LocalDateTime createdAt;

    /** Factory de creación (id y timestamp generados). */
    public static FinancialRecord create(UUID documentId, UUID organizationId, String sheetName,
                                         Integer rowIndex, Map<String, Object> rowData) {
        return new FinancialRecord(UUID.randomUUID(), documentId, organizationId, sheetName, rowIndex,
                rowData, LocalDateTime.now());
    }

    /** Constructor canónico (también usado por el mapper de persistencia). */
    public FinancialRecord(UUID id, UUID documentId, UUID organizationId, String sheetName,
                           Integer rowIndex, Map<String, Object> rowData, LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.organizationId = organizationId;
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.rowData = rowData;
        this.createdAt = createdAt;
    }

    // ---- Lógica de dominio: acceso seguro a los datos del Excel ----

    public Object getField(String columnName) {
        return rowData.get(columnName);
    }

    public boolean hasField(String columnName) {
        return rowData.containsKey(columnName);
    }

    public Double getNumericField(String columnName) {
        Object value = rowData.get(columnName);
        if (value instanceof Number num) return num.doubleValue();
        if (value instanceof String str) {
            try { return Double.parseDouble(str.replace(",", ".")); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    public UUID getId()                     { return id; }
    public UUID getDocumentId()             { return documentId; }
    public UUID getOrganizationId()         { return organizationId; }
    public String getSheetName()            { return sheetName; }
    public Integer getRowIndex()            { return rowIndex; }
    public Map<String, Object> getRowData() { return Collections.unmodifiableMap(rowData); }
    public LocalDateTime getCreatedAt()     { return createdAt; }
}
