package com.semtex.application.service;

import com.semtex.domain.model.AuditLog;
import com.semtex.domain.port.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste un log de auditoría en una transacción independiente ({@code REQUIRES_NEW}), de modo que
 * el registro sobreviva aunque la operación auditada falle y haga rollback (p. ej. EMAIL_FAILED).
 */
@Service
public class AuditRecorder {

    private final AuditLogRepositoryPort repository;

    public AuditRecorder(AuditLogRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditLog log) {
        repository.save(log);
    }
}
