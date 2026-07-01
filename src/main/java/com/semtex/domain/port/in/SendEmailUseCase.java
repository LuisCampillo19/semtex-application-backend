package com.semtex.domain.port.in;

import java.util.UUID;

/**
 * Puerto de entrada (driving) publicado: enviar un correo.
 * Lo consume el agente (tool {@code enviarCorreo}) y posibles acciones administrativas.
 */
public interface SendEmailUseCase {

    void send(SendEmailCommand command);

    record SendEmailCommand(String toAddress, String subject, String body,
                            UUID organizationId, UUID requestedByUserId) {}
}
