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
 * Entidad JPA de {@code users}.
 *
 * El rol se mapea con {@code @JdbcTypeCode(NAMED_ENUM)} para que Hibernate 6 lo
 * convierta correctamente al ENUM nativo {@code user_role} de PostgreSQL (sin casts frágiles).
 * El enum JPA local está aislado del enum de dominio {@code identity.domain.model.Role}.
 */
@Entity
@Table(name = "users")
@Filter(name = TenantFilters.TENANT_FILTER)
public class UserJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "user_role")
    private RoleJpa role;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum RoleJpa { ADMIN, OPERATOR, AUDITOR }

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String email, RoleJpa role, UUID organizationId,
                         boolean active, LocalDateTime lastLoginAt,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.organizationId = organizationId;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId()                   { return id; }
    public String getEmail()              { return email; }
    public RoleJpa getRole()              { return role; }
    public UUID getOrganizationId()       { return organizationId; }
    public boolean isActive()             { return active; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
}
