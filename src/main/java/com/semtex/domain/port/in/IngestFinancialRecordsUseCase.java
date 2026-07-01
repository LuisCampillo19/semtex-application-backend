package com.semtex.domain.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de entrada (driving): API publicada del contexto financial para que el contexto
 * document inyecte las filas extraídas de un Excel/CSV, sin acoplar ambos dominios.
 */
public interface IngestFinancialRecordsUseCase {

    void ingest(IngestCommand command);

    record IngestCommand(UUID documentId, UUID organizationId, List<SheetData> sheets) {}

    /** Una hoja con sus filas (cada fila = columna→valor) en tipos primitivos. */
    record SheetData(String sheetName, List<Map<String, Object>> rows) {}
}
