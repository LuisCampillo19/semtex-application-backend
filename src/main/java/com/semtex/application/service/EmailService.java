package com.semtex.application.service;

import com.semtex.domain.port.in.SendEmailUseCase;
import com.semtex.domain.port.out.EmailSenderPort;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: enviar correo. La auditoría (éxito/fallo) la añade el aspecto AOP del contexto audit.
 */
@Service
public class EmailService implements SendEmailUseCase {

    private final EmailSenderPort emailSender;

    public EmailService(EmailSenderPort emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void send(SendEmailCommand command) {
        emailSender.send(command.toAddress(), command.subject(), command.body());
    }
}
