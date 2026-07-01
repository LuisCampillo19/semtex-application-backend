package com.semtex.domain.port.out;

import com.semtex.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

/** Puerto de salida: persistencia y consulta de logs de auditoría (solo INSERT + lectura). */
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog log);

    List<AuditLog> findByOrganization(UUID organizationId, int limit);
}
