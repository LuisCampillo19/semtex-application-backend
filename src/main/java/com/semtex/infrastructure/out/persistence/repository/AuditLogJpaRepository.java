package com.semtex.infrastructure.out.persistence.repository;

import com.semtex.infrastructure.out.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {
    List<AuditLogJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
}
