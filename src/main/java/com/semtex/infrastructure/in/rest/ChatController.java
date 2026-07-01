package com.semtex.infrastructure.in.rest;

import com.semtex.domain.port.in.ChatUseCase;
import com.semtex.domain.port.in.ChatUseCase.ChatResult;
import com.semtex.domain.port.in.ChatUseCase.SendMessageCommand;
import com.semtex.domain.port.in.QueryChatHistoryUseCase;
import com.semtex.infrastructure.in.rest.dto.ChatDtos.MessageResponse;
import com.semtex.infrastructure.in.rest.dto.ChatDtos.SendRequest;
import com.semtex.infrastructure.in.rest.dto.ChatDtos.SendResponse;
import com.semtex.infrastructure.in.rest.dto.FinancialRecordDtos.RecordResponse;
import com.semtex.infrastructure.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final QueryChatHistoryUseCase chatHistory;

    public ChatController(ChatUseCase chatUseCase, QueryChatHistoryUseCase chatHistory) {
        this.chatUseCase = chatUseCase;
        this.chatHistory = chatHistory;
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public SendResponse send(@Valid @RequestBody SendRequest request) {
        UUID organizationId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.currentUserId();
        ChatResult result = chatUseCase.sendMessage(
                new SendMessageCommand(request.content(), request.documentId(), organizationId, userId));
        return new SendResponse(
                result.agentResponse(),
                result.relevantRecords().stream().map(RecordResponse::from).toList());
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public List<MessageResponse> history(@RequestParam(required = false) UUID documentId,
                                         @RequestParam(defaultValue = "100") int limit) {
        UUID organizationId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.currentUserId();
        return chatHistory.history(organizationId, userId, documentId, limit).stream()
                .map(MessageResponse::from).toList();
    }
}
