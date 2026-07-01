package com.semtex.application.service;

import com.semtex.domain.model.ChatMessage;
import com.semtex.domain.port.in.ChatUseCase;
import com.semtex.domain.port.out.AiGatewayPort;
import com.semtex.domain.port.out.ChatMessageRepositoryPort;
import com.semtex.domain.model.FinancialRecord;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase.QueryCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: chat con el agente. <b>No</b> es transaccional: la inferencia (potencialmente lenta)
 * no debe retener una conexión del pool. Las lecturas/escrituras puntuales abren su propia transacción.
 */
@Service
public class ChatService implements ChatUseCase {

    private final ChatMessageRepositoryPort chatRepository;
    private final QueryFinancialRecordsUseCase financialQuery;
    private final AiGatewayPort aiGateway;
    private final int maxContextRecords;

    public ChatService(ChatMessageRepositoryPort chatRepository,
                       QueryFinancialRecordsUseCase financialQuery,
                       AiGatewayPort aiGateway,
                       @Value("${semtex.ai.max-context-records:200}") int maxContextRecords) {
        this.chatRepository = chatRepository;
        this.financialQuery = financialQuery;
        this.aiGateway = aiGateway;
        this.maxContextRecords = maxContextRecords;
    }

    @Override
    public ChatResult sendMessage(SendMessageCommand command) {
        // 1. Contexto financiero acotado (solo si el chat referencia un documento).
        List<FinancialRecord> relevant = command.documentId() == null
                ? List.of()
                : financialQuery.query(new QueryCommand(
                command.organizationId(), command.documentId(), null, null, maxContextRecords));

        // 2. Persistir el mensaje del usuario.
        chatRepository.save(ChatMessage.user(
                command.content(), command.organizationId(), command.userId(), command.documentId()));

        // 3. Inferencia (fuera de transacción). El agente puede invocar herramientas internamente.
        AiGatewayPort.AiAnswer answer = aiGateway.ask(
                new AiGatewayPort.AiRequest(command.content(), serialize(relevant)));

        // 4. Persistir la respuesta del agente.
        chatRepository.save(ChatMessage.agent(answer.text(), command.organizationId(),
                command.userId(), command.documentId(), answer.tokensUsed()));

        return new ChatResult(answer.text(), relevant);
    }

    private String serialize(List<FinancialRecord> records) {
        if (records.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (FinancialRecord r : records) {
            sb.append("Fila ").append(r.getRowIndex()).append(": ").append(r.getRowData()).append('\n');
        }
        return sb.toString();
    }
}
