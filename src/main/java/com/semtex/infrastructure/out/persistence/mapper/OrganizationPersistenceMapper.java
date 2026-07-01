package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.Organization;
import com.semtex.infrastructure.out.persistence.entity.OrganizationJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio Organization y su entidad JPA. */
@Mapper
public interface OrganizationPersistenceMapper {

    OrganizationJpaEntity toEntity(Organization domain);

    Organization toDomain(OrganizationJpaEntity entity);
}
