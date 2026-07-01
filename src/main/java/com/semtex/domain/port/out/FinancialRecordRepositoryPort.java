package com.semtex.domain.port.out;

import com.semtex.domain.model.FinancialRecord;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: persistencia y consulta de registros financieros.
 *
 * Las lecturas pasan el {@code organizationId} explícito (repos tenant-aware) y además quedan
 * cubiertas por el filtro Hibernate; la consulta JSONB nativa exige el tenant porque el filtro
 * no aplica a SQL nativo.
 */
public interface FinancialRecordRepositoryPort {

    void saveAll(List<FinancialRecord> records);

    List<FinancialRecord> findByOrganization(UUID organizationId, int limit);

    List<FinancialRecord> findByDocument(UUID documentId, UUID organizationId, int limit);

    List<FinancialRecord> findByJsonField(UUID organizationId, String fieldName, String value, int limit);
}
