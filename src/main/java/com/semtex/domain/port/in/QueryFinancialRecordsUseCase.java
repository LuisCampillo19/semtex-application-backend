package com.semtex.domain.port.in;

import com.semtex.domain.model.FinancialRecord;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (driving): consulta de registros financieros del tenant.
 */
public interface QueryFinancialRecordsUseCase {

    List<FinancialRecord> query(QueryCommand command);

    /**
     * @param organizationId tenant (resuelto del token)
     * @param documentId     opcional: filas de un documento
     * @param fieldName      opcional (con value): búsqueda exacta por campo JSONB
     * @param value          opcional (con fieldName)
     * @param limit          tope solicitado (el adaptador acota a un máximo interno)
     */
    record QueryCommand(UUID organizationId, UUID documentId, String fieldName, String value, int limit) {}
}
