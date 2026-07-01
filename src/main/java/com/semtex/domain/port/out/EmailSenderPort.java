package com.semtex.domain.port.out;

/**
 * Puerto de salida: envío de correos (frontera tecnológica: SMTP / proveedor de email).
 */
public interface EmailSenderPort {

    void send(String toAddress, String subject, String htmlBody);
}
