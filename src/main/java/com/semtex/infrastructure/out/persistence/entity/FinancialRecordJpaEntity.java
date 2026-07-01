package com.semtex.infrastructure.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad JPA de {@code financial_records}.
 *
 * {@code row_data} es JSONB nativo de PostgreSQL, mapeado por Hibernate 6 con
 * {@code @JdbcTypeCode(SqlTypes.JSON)} (sin hypersistence-utils). Indexado con GIN.
 */
@Entity
@Table(name = "financial_records")
@Filter(name = TenantFilters.TENANT_FILTER)
public class FinancialRecordJpaEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "sheet_name")
    private String sheetName;

    @Column(name = "row_index")
    private Integer rowIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "row_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> rowData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FinancialRecordJpaEntity() {}

    public FinancialRecordJpaEntity(UUID id, UUID documentId, UUID organizationId,
                                    String sheetName, Integer rowIndex,
                                    Map<String, Object> rowData, LocalDateTime createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.organizationId = organizationId;
        this.sheetName = sheetName;
        this.rowIndex = rowIndex;
        this.rowData = rowData;
        this.createdAt = createdAt;
    }

    public UUID getId()                     { return id; }
    public UUID getDocumentId()             { return documentId; }
    public UUID getOrganizationId()         { return organizationId; }
    public String getSheetName()            { return sheetName; }
    public Integer getRowIndex()            { return rowIndex; }
    public Map<String, Object> getRowData() { return rowData; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
}
