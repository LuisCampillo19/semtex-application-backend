package com.semtex.domain.model;

/**
 * Rol del usuario dentro de una organización (RBAC).
 * Espejo exacto del ENUM {@code user_role} de PostgreSQL.
 */
public enum Role {
    ADMIN,    // Control total: usuarios, APIs, archivos
    OPERATOR, // Carga Excels, chat, ordena correos
    AUDITOR   // Solo lectura de reportes e historial
}
