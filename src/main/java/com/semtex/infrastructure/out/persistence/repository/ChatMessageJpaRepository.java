package com.semtex.infrastructure.out.persistence.repository;

import com.semtex.infrastructure.out.persistence.entity.ChatMessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, UUID> {

    List<ChatMessageJpaEntity> findByOrganizationIdAndUserIdOrderByCreatedAtAsc(
            UUID organizationId, UUID userId, Pageable pageable);

    List<ChatMessageJpaEntity> findByOrganizationIdAndUserIdAndDocumentIdOrderByCreatedAtAsc(
            UUID organizationId, UUID userId, UUID documentId, Pageable pageable);
}
