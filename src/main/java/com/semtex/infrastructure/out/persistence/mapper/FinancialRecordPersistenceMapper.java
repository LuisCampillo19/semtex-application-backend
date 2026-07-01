package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.FinancialRecord;
import com.semtex.infrastructure.out.persistence.entity.FinancialRecordJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio FinancialRecord y su entidad JPA. */
@Mapper
public interface FinancialRecordPersistenceMapper {

    FinancialRecordJpaEntity toEntity(FinancialRecord domain);

    FinancialRecord toDomain(FinancialRecordJpaEntity entity);
}
