package com.semtex.infrastructure.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.semtex.infrastructure.out.persistence.TenantFilters;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA de {@code documents} (solo metadatos del archivo).
 */
@Entity
@Table(name = "documents")
@Filter(name = TenantFilters.TENANT_FILTER)
public class DocumentJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected DocumentJpaEntity() {}

    public DocumentJpaEntity(UUID id, String name, String storagePath, String mimeType,
                             Long fileSizeBytes, UUID organizationId, UUID uploadedBy,
                             LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.organizationId = organizationId;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public UUID getId()                 { return id; }
    public String getName()             { return name; }
    public String getStoragePath()      { return storagePath; }
    public String getMimeType()         { return mimeType; }
    public Long getFileSizeBytes()      { return fileSizeBytes; }
    public UUID getOrganizationId()     { return organizationId; }
    public UUID getUploadedBy()         { return uploadedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
