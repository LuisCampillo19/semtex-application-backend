package com.semtex.domain.model;

/**
 * Tipo de acción registrada en el log de auditoría.
 * Espejo exacto del ENUM {@code audit_action} de PostgreSQL.
 */
public enum AuditAction {
    DOCUMENT_UPLOADED,
    FINANCIAL_QUERY,
    EMAIL_SENT,
    EMAIL_FAILED,
    USER_LOGIN,
    USER_CREATED,
    USER_DEACTIVATED,
    ROLE_CHANGED
}
