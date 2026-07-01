package com.semtex.application.service;

import com.semtex.domain.model.ChatMessage;
import com.semtex.domain.port.in.QueryChatHistoryUseCase;
import com.semtex.domain.port.out.ChatMessageRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Caso de uso: lectura del historial de chat. */
@Service
@Transactional(readOnly = true)
public class ChatHistoryService implements QueryChatHistoryUseCase {

    private final ChatMessageRepositoryPort chatRepository;

    public ChatHistoryService(ChatMessageRepositoryPort chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public List<ChatMessage> history(UUID organizationId, UUID userId, UUID documentId, int limit) {
        return chatRepository.findHistory(organizationId, userId, documentId, limit);
    }
}
