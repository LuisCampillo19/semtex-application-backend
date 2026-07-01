package com.semtex.application.service;

import com.semtex.domain.model.ChatMessage;
import com.semtex.domain.port.in.ChatUseCase.ChatResult;
import com.semtex.domain.port.in.ChatUseCase.SendMessageCommand;
import com.semtex.domain.port.out.AiGatewayPort;
import com.semtex.domain.port.out.ChatMessageRepositoryPort;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatMessageRepositoryPort chatRepo = mock(ChatMessageRepositoryPort.class);
    private final QueryFinancialRecordsUseCase financialQuery = mock(QueryFinancialRecordsUseCase.class);
    private final AiGatewayPort aiGateway = mock(AiGatewayPort.class);
    private final ChatService service = new ChatService(chatRepo, financialQuery, aiGateway, 200);

    @Test
    void responde_persiste_ambos_mensajes_y_no_consulta_finanzas_sin_documento() {
        when(aiGateway.ask(any())).thenReturn(new AiGatewayPort.AiAnswer("El total fue 1000.", 42));
        when(chatRepo.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatResult result = service.sendMessage(new SendMessageCommand(
                "¿Cuál fue el total de ventas?", null, UUID.randomUUID(), UUID.randomUUID()));

        assertThat(result.agentResponse()).isEqualTo("El total fue 1000.");
        assertThat(result.relevantRecords()).isEmpty();
        verify(chatRepo, times(2)).save(any(ChatMessage.class)); // mensaje del usuario + del agente
        verify(aiGateway).ask(any());
        verifyNoInteractions(financialQuery); // sin documentId no se carga contexto
    }

    @Test
    void carga_contexto_financiero_cuando_hay_documento() {
        UUID org = UUID.randomUUID();
        UUID doc = UUID.randomUUID();
        when(financialQuery.query(any())).thenReturn(java.util.List.of());
        when(aiGateway.ask(any())).thenReturn(new AiGatewayPort.AiAnswer("ok", null));
        when(chatRepo.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sendMessage(new SendMessageCommand("hola", doc, org, UUID.randomUUID()));

        verify(financialQuery).query(any());
    }
}
