package com.semtex.infrastructure.out.persistence.repository;

import com.semtex.infrastructure.out.persistence.entity.FinancialRecordJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FinancialRecordJpaRepository extends JpaRepository<FinancialRecordJpaEntity, UUID> {

    List<FinancialRecordJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    List<FinancialRecordJpaEntity> findByDocumentIdAndOrganizationIdOrderByRowIndexAsc(
            UUID documentId, UUID organizationId, Pageable pageable);

    /**
     * Búsqueda exacta por un campo dentro del JSONB. Usa {@code ->>} (texto), por lo que matchea
     * tanto valores string como numéricos (almacenados como número y comparados como texto),
     * corrigiendo el bug del match solo-string. El {@code organization_id} es obligatorio porque
     * el filtro Hibernate NO aplica a queries nativas.
     */
    @Query(value = """
            SELECT * FROM financial_records
            WHERE organization_id = :orgId
              AND row_data ->> :fieldName = :value
            ORDER BY row_index ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<FinancialRecordJpaEntity> findByOrganizationIdAndJsonField(
            @Param("orgId") UUID organizationId,
            @Param("fieldName") String fieldName,
            @Param("value") String value,
            @Param("limit") int limit);
}
