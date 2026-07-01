package com.semtex.infrastructure.aspect;

import com.semtex.application.service.AuditRecorder;
import com.semtex.domain.model.AuditLog;
import com.semtex.domain.port.in.ChatUseCase.SendMessageCommand;
import com.semtex.domain.model.Document;
import com.semtex.domain.port.in.SendEmailUseCase.SendEmailCommand;
import com.semtex.domain.model.User;
import com.semtex.infrastructure.tenant.TenantContext;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Auditoría transversal por AOP: registra las acciones relevantes (del agente y de gestión) sin
 * que cada servicio tenga que llamar a la auditoría manualmente. Vive en el contexto audit como
 * observador de los demás contextos.
 */
@Aspect
@Component
public class AgentAuditAspect {

    private final AuditRecorder recorder;

    public AgentAuditAspect(AuditRecorder recorder) {
        this.recorder = recorder;
    }

    @AfterReturning("execution(* com.semtex.application.service.EmailService.send(..)) && args(command)")
    public void onEmailSent(SendEmailCommand command) {
        recorder.record(AuditLog.emailSent(
                command.organizationId(), command.requestedByUserId(), command.toAddress()));
    }

    @AfterThrowing(pointcut = "execution(* com.semtex.application.service.EmailService.send(..)) && args(command)",
            throwing = "ex")
    public void onEmailFailed(SendEmailCommand command, Throwable ex) {
        recorder.record(AuditLog.emailFailed(
                command.organizationId(), command.requestedByUserId(), command.toAddress(), ex.getMessage()));
    }

    @AfterReturning(pointcut = "execution(* com.semtex.application.service.DocumentService.upload(..))",
            returning = "document")
    public void onDocumentUploaded(Document document) {
        recorder.record(AuditLog.documentUploaded(
                document.getOrganizationId(), document.getUploadedBy(), document.getName()));
    }

    @AfterReturning("execution(* com.semtex.application.service.ChatService.sendMessage(..)) && args(command)")
    public void onChatQuery(SendMessageCommand command) {
        recorder.record(AuditLog.financialQuery(
                command.organizationId(), command.userId(), command.content()));
    }

    @AfterReturning(pointcut = "execution(* com.semtex.application.service.UserService.create(..))",
            returning = "user")
    public void onUserCreated(User user) {
        recorder.record(AuditLog.userCreated(
                user.getOrganizationId(), TenantContext.currentUserId(), user.getEmail()));
    }
}
