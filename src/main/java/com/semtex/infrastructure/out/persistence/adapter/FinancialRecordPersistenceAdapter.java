package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.FinancialRecord;
import com.semtex.domain.port.out.FinancialRecordRepositoryPort;
import com.semtex.infrastructure.out.persistence.mapper.FinancialRecordPersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.FinancialRecordJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Adaptador JPA del puerto de registros financieros. */
@Component
public class FinancialRecordPersistenceAdapter implements FinancialRecordRepositoryPort {

    private static final int MAX_LIMIT = 1_000;
    private static final int DEFAULT_LIMIT = 100;

    private final FinancialRecordJpaRepository jpa;
    private final FinancialRecordPersistenceMapper mapper;

    public FinancialRecordPersistenceAdapter(FinancialRecordJpaRepository jpa,
                                             FinancialRecordPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<FinancialRecord> records) {
        jpa.saveAll(records.stream().map(mapper::toEntity).toList());
    }

    @Override
    public List<FinancialRecord> findByOrganization(UUID organizationId, int limit) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(0, normalize(limit)))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<FinancialRecord> findByDocument(UUID documentId, UUID organizationId, int limit) {
        return jpa.findByDocumentIdAndOrganizationIdOrderByRowIndexAsc(
                        documentId, organizationId, PageRequest.of(0, normalize(limit)))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<FinancialRecord> findByJsonField(UUID organizationId, String fieldName, String value, int limit) {
        return jpa.findByOrganizationIdAndJsonField(organizationId, fieldName, value, normalize(limit))
                .stream().map(mapper::toDomain).toList();
    }

    private int normalize(int requested) {
        if (requested <= 0) return DEFAULT_LIMIT;
        return Math.min(requested, MAX_LIMIT);
    }
}
