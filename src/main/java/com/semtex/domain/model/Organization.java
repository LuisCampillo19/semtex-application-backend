package com.semtex.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio: Organization — raíz del aislamiento multi-inquilino.
 *
 * REGLA HEXAGONAL: Java puro, sin anotaciones de Spring/JPA/Jackson.
 */
public class Organization {

    private final UUID id;
    private String name;
    private final String slug;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Factory de creación (id y timestamps generados). */
    public static Organization create(String name, String slug) {
        LocalDateTime now = LocalDateTime.now();
        return new Organization(UUID.randomUUID(), name, slug, true, now, now);
    }

    /** Constructor canónico (también usado por el mapper de persistencia). */
    public Organization(UUID id, String name, String slug, boolean active,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la organización no puede estar vacío.");
        }
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId()                 { return id; }
    public String getName()             { return name; }
    public String getSlug()             { return slug; }
    public boolean isActive()           { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
