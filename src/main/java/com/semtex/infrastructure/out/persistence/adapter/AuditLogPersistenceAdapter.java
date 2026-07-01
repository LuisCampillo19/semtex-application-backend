package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.AuditLog;
import com.semtex.domain.port.out.AuditLogRepositoryPort;
import com.semtex.infrastructure.out.persistence.mapper.AuditLogPersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.AuditLogJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Adaptador JPA del puerto de auditoría. */
@Component
public class AuditLogPersistenceAdapter implements AuditLogRepositoryPort {

    private static final int MAX_LIMIT = 500;
    private static final int DEFAULT_LIMIT = 100;

    private final AuditLogJpaRepository jpa;
    private final AuditLogPersistenceMapper mapper;

    public AuditLogPersistenceAdapter(AuditLogJpaRepository jpa, AuditLogPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AuditLog save(AuditLog log) {
        return mapper.toDomain(jpa.save(mapper.toEntity(log)));
    }

    @Override
    public List<AuditLog> findByOrganization(UUID organizationId, int limit) {
        int normalized = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(0, normalized))
                .stream().map(mapper::toDomain).toList();
    }
}
