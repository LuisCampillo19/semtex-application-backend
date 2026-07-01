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
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA de {@code audit_logs} (INSERT-only). {@code metadata} es JSONB.
 */
@Entity
@Table(name = "audit_logs")
@Filter(name = TenantFilters.TENANT_FILTER)
public class AuditLogJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "audit_action")
    private AuditActionJpa action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AuditActionJpa {
        DOCUMENT_UPLOADED, FINANCIAL_QUERY, EMAIL_SENT, EMAIL_FAILED,
        USER_LOGIN, USER_CREATED, USER_DEACTIVATED, ROLE_CHANGED
    }

    protected AuditLogJpaEntity() {}

    public AuditLogJpaEntity(UUID id, AuditActionJpa action, String description,
                             Map<String, Object> metadata, UUID organizationId,
                             UUID performedBy, LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.description = description;
        this.metadata = metadata;
        this.organizationId = organizationId;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    public UUID getId()                      { return id; }
    public AuditActionJpa getAction()        { return action; }
    public String getDescription()           { return description; }
    public Map<String, Object> getMetadata() { return metadata; }
    public UUID getOrganizationId()          { return organizationId; }
    public UUID getPerformedBy()             { return performedBy; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
}
