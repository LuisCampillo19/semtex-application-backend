package com.semtex.infrastructure.out.persistence.mapper;

import com.semtex.domain.model.ChatMessage;
import com.semtex.infrastructure.out.persistence.entity.ChatMessageJpaEntity;
import org.mapstruct.Mapper;

/** Mapper MapStruct entre el dominio ChatMessage y su entidad JPA (MessageRole ↔ MessageRoleJpa). */
@Mapper
public interface ChatMessagePersistenceMapper {

    ChatMessageJpaEntity toEntity(ChatMessage domain);

    ChatMessage toDomain(ChatMessageJpaEntity entity);
}
