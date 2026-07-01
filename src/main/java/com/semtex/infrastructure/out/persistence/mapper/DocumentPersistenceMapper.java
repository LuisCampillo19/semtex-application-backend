package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.Document;
import com.semtex.infrastructure.out.persistence.entity.DocumentJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio Document y su entidad JPA. */
@Mapper
public interface DocumentPersistenceMapper {

    DocumentJpaEntity toEntity(Document domain);

    Document toDomain(DocumentJpaEntity entity);
}
