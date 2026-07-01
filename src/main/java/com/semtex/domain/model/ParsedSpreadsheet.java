package com.semtex.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Resultado de parsear un Excel/CSV: filas por hoja.
 * Para CSV solo hay una hoja con nombre "default".
 *
 * Modelo de dominio puro: cada fila es un {@code Map<columna, valor>} que se persistirá como JSONB.
 */
public record ParsedSpreadsheet(
        Map<String, List<Map<String, Object>>> rowsBySheet,
        int totalRows,
        List<String> warnings
) {
    public boolean isEmpty() {
        return totalRows == 0;
    }
}
