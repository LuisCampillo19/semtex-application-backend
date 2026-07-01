package com.semtex.infrastructure.out.email;

import com.semtex.domain.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Adaptador SMTP (Spring Mail). En dev apunta a un SMTP local (p. ej. MailHog en :1025);
 * en prod a un proveedor SMTP/Resend.
 */
@Component
public class SmtpEmailAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailAdapter(JavaMailSender mailSender,
                            @Value("${semtex.email.from:noreply@semtex.app}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String toAddress, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailDeliveryException("No se pudo enviar el correo a " + toAddress, e);
        }
    }

    /** Fallo de entrega de correo. */
    public static class EmailDeliveryException extends RuntimeException {
        public EmailDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
