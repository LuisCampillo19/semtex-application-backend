package com.semtex.infrastructure.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.semtex.infrastructure.out.persistence.TenantFilters;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA de {@code chat_messages} (inmutable: nunca se hace UPDATE).
 */
@Entity
@Table(name = "chat_messages")
@Filter(name = TenantFilters.TENANT_FILTER)
public class ChatMessageJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "message_role")
    private MessageRoleJpa role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageRoleJpa { USER, AGENT }

    protected ChatMessageJpaEntity() {}

    public ChatMessageJpaEntity(UUID id, MessageRoleJpa role, String content,
                                UUID organizationId, UUID userId, UUID documentId,
                                Integer tokensUsed, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.organizationId = organizationId;
        this.userId = userId;
        this.documentId = documentId;
        this.tokensUsed = tokensUsed;
        this.createdAt = createdAt;
    }

    public UUID getId()                 { return id; }
    public MessageRoleJpa getRole()     { return role; }
    public String getContent()          { return content; }
    public UUID getOrganizationId()     { return organizationId; }
    public UUID getUserId()             { return userId; }
    public UUID getDocumentId()         { return documentId; }
    public Integer getTokensUsed()      { return tokensUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
