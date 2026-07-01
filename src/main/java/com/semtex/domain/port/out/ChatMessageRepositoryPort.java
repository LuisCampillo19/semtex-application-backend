package com.semtex.domain.port.out;

import com.semtex.domain.model.ChatMessage;

import java.util.List;
import java.util.UUID;

/** Puerto de salida: persistencia e historial de mensajes de chat. */
public interface ChatMessageRepositoryPort {

    ChatMessage save(ChatMessage message);

    /** Historial cronológico del par (organización, usuario), opcionalmente acotado a un documento. */
    List<ChatMessage> findHistory(UUID organizationId, UUID userId, UUID documentId, int limit);
}
