package com.semtex.application.service;

import com.semtex.domain.model.FinancialRecord;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase;
import com.semtex.domain.port.out.FinancialRecordRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: consulta de registros financieros. Elige la ruta según los filtros recibidos.
 */
@Service
@Transactional(readOnly = true)
public class FinancialRecordQueryService implements QueryFinancialRecordsUseCase {

    private final FinancialRecordRepositoryPort repository;

    public FinancialRecordQueryService(FinancialRecordRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<FinancialRecord> query(QueryCommand command) {
        if (command.fieldName() != null && command.value() != null) {
            return repository.findByJsonField(command.organizationId(),
                    command.fieldName(), command.value(), command.limit());
        }
        if (command.documentId() != null) {
            return repository.findByDocument(command.documentId(), command.organizationId(), command.limit());
        }
        return repository.findByOrganization(command.organizationId(), command.limit());
    }
}
