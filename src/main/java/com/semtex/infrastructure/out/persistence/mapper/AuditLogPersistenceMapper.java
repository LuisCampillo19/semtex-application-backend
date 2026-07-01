package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.AuditLog;
import com.semtex.infrastructure.out.persistence.entity.AuditLogJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio AuditLog y su entidad JPA (AuditAction ↔ AuditActionJpa). */
@Mapper
public interface AuditLogPersistenceMapper {

    AuditLogJpaEntity toEntity(AuditLog domain);

    AuditLog toDomain(AuditLogJpaEntity entity);
}
