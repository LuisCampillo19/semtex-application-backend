package com.semtex.infrastructure.out.persistence.entity;

import com.semtex.infrastructure.out.persistence.TenantFilters;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA de {@code organizations}. Vive SOLO en infraestructura;
 * el dominio usa {@link com.semtex.domain.model.Organization}.
 *
 * El {@code @FilterDef} global de tenant se declara en el {@code package-info} de
 * {@code com.semtex.shared.persistence}; aquí la organización se filtra a sí misma por su
 * {@code id} (su id ES el tenant).
 */
@Entity
@Table(name = "organizations")
@Filter(name = TenantFilters.TENANT_FILTER, condition = TenantFilters.BY_ID)
public class OrganizationJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected OrganizationJpaEntity() {}

    public OrganizationJpaEntity(UUID id, String name, String slug, boolean active,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId()                 { return id; }
    public String getName()             { return name; }
    public String getSlug()             { return slug; }
    public boolean isActive()           { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
