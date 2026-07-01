package com.semtex.infrastructure.in.rest.dto;

import com.semtex.domain.model.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTOs REST del contexto document. */
public final class DocumentDtos {

    private DocumentDtos() {}

    public record Response(
            UUID id, String name, String storagePath, String mimeType, Long fileSizeBytes,
            UUID organizationId, UUID uploadedBy, LocalDateTime createdAt
    ) {
        public static Response from(Document d) {
            return new Response(d.getId(), d.getName(), d.getStoragePath(), d.getMimeType(),
                    d.getFileSizeBytes(), d.getOrganizationId(), d.getUploadedBy(), d.getCreatedAt());
        }
    }
}
