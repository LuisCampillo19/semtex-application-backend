package com.semtex.domain.port.in;

import com.semtex.domain.model.FinancialRecord;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (driving): conversación con el agente Semtex.
 */
public interface ChatUseCase {

    ChatResult sendMessage(SendMessageCommand command);

    record SendMessageCommand(String content, UUID documentId, UUID organizationId, UUID userId) {}

    record ChatResult(String agentResponse, List<FinancialRecord> relevantRecords) {}
}
