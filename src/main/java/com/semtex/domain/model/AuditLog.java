package com.semtex.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Modelo de dominio: AuditLog — registro INMUTABLE de acciones (solo INSERT).
 *
 * REGLA HEXAGONAL: Java puro, sin anotaciones externas.
 */
public class AuditLog {

    private final UUID id;
    private final AuditAction action;
    private final String description;
    private final Map<String, Object> metadata;
    private final UUID organizationId;
    private final UUID performedBy;  // null si fue acción del sistema
    private final LocalDateTime createdAt;

    /** Constructor canónico (también usado por el mapper de persistencia). */
    public AuditLog(UUID id, AuditAction action, String description, Map<String, Object> metadata,
                    UUID organizationId, UUID performedBy, LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.description = description;
        this.metadata = metadata;
        this.organizationId = organizationId;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    // ---- Factories de dominio (verbos de negocio) ----

    private static AuditLog of(AuditAction action, String description, Map<String, Object> metadata,
                              UUID organizationId, UUID performedBy) {
        return new AuditLog(UUID.randomUUID(), action, description, metadata,
                organizationId, performedBy, LocalDateTime.now());
    }

    public static AuditLog financialQuery(UUID organizationId, UUID userId, String query) {
        return of(AuditAction.FINANCIAL_QUERY, "Consulta financiera: " + query,
                Map.of("query", query), organizationId, userId);
    }

    public static AuditLog emailSent(UUID organizationId, UUID userId, String emailTo) {
        return of(AuditAction.EMAIL_SENT, "Email enviado a: " + emailTo,
                Map.of("email_to", emailTo), organizationId, userId);
    }

    public static AuditLog emailFailed(UUID organizationId, UUID userId, String emailTo, String reason) {
        return of(AuditAction.EMAIL_FAILED, "Fallo al enviar email a: " + emailTo,
                Map.of("email_to", emailTo, "reason", reason == null ? "" : reason), organizationId, userId);
    }

    public static AuditLog documentUploaded(UUID organizationId, UUID userId, String fileName) {
        return of(AuditAction.DOCUMENT_UPLOADED, "Documento subido: " + fileName,
                Map.of("file_name", fileName), organizationId, userId);
    }

    public static AuditLog userCreated(UUID organizationId, UUID performedBy, String email) {
        return of(AuditAction.USER_CREATED, "Usuario creado: " + email,
                Map.of("email", email), organizationId, performedBy);
    }

    public static AuditLog userDeactivated(UUID organizationId, UUID performedBy, String email) {
        return of(AuditAction.USER_DEACTIVATED, "Usuario desactivado: " + email,
                Map.of("email", email), organizationId, performedBy);
    }

    public static AuditLog roleChanged(UUID organizationId, UUID performedBy, String email, String newRole) {
        return of(AuditAction.ROLE_CHANGED, "Rol cambiado para " + email + " a " + newRole,
                Map.of("email", email, "new_role", newRole), organizationId, performedBy);
    }

    public static AuditLog userLogin(UUID organizationId, UUID userId, String email) {
        return of(AuditAction.USER_LOGIN, "Login de usuario: " + email,
                Map.of("email", email), organizationId, userId);
    }

    public UUID getId()                      { return id; }
    public AuditAction getAction()           { return action; }
    public String getDescription()           { return description; }
    public Map<String, Object> getMetadata() { return metadata; }
    public UUID getOrganizationId()          { return organizationId; }
    public UUID getPerformedBy()             { return performedBy; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
}
