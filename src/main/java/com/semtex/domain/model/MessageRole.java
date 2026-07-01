package com.semtex.domain.model;

/**
 * Rol del remitente en un mensaje de chat.
 * Espejo exacto del ENUM {@code message_role} de PostgreSQL.
 */
public enum MessageRole {
    USER,  // Mensaje del usuario humano
    AGENT  // Respuesta generada por Semtex IA
}
