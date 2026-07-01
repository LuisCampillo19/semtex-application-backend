package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.User;
import com.semtex.infrastructure.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio User y su entidad JPA (Role ↔ RoleJpa por nombre). */
@Mapper
public interface UserPersistenceMapper {

    UserJpaEntity toEntity(User domain);

    User toDomain(UserJpaEntity entity);
}
