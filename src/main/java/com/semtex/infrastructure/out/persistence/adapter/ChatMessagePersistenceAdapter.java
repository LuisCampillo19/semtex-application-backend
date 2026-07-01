package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.ChatMessage;
import com.semtex.domain.port.out.ChatMessageRepositoryPort;
import com.semtex.infrastructure.out.persistence.entity.ChatMessageJpaEntity;
import com.semtex.infrastructure.out.persistence.mapper.ChatMessagePersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.ChatMessageJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Adaptador JPA del puerto de mensajes de chat. */
@Component
public class ChatMessagePersistenceAdapter implements ChatMessageRepositoryPort {

    private static final int MAX_LIMIT = 500;
    private static final int DEFAULT_LIMIT = 100;

    private final ChatMessageJpaRepository jpa;
    private final ChatMessagePersistenceMapper mapper;

    public ChatMessagePersistenceAdapter(ChatMessageJpaRepository jpa, ChatMessagePersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        return mapper.toDomain(jpa.save(mapper.toEntity(message)));
    }

    @Override
    public List<ChatMessage> findHistory(UUID organizationId, UUID userId, UUID documentId, int limit) {
        PageRequest page = PageRequest.of(0, normalize(limit));
        List<ChatMessageJpaEntity> entities = documentId == null
                ? jpa.findByOrganizationIdAndUserIdOrderByCreatedAtAsc(organizationId, userId, page)
                : jpa.findByOrganizationIdAndUserIdAndDocumentIdOrderByCreatedAtAsc(
                        organizationId, userId, documentId, page);
        return entities.stream().map(mapper::toDomain).toList();
    }

    private int normalize(int requested) {
        if (requested <= 0) return DEFAULT_LIMIT;
        return Math.min(requested, MAX_LIMIT);
    }
}
