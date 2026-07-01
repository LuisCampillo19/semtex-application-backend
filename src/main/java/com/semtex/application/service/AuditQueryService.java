package com.semtex.application.service;

import com.semtex.domain.model.AuditLog;
import com.semtex.domain.port.in.QueryAuditLogUseCase;
import com.semtex.domain.port.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Caso de uso: lectura del historial de auditoría. */
@Service
@Transactional(readOnly = true)
public class AuditQueryService implements QueryAuditLogUseCase {

    private final AuditLogRepositoryPort repository;

    public AuditQueryService(AuditLogRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<AuditLog> listByOrganization(UUID organizationId, int limit) {
        return repository.findByOrganization(organizationId, limit);
    }
}
