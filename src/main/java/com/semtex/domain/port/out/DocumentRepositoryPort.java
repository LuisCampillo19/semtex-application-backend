package com.semtex.domain.port.out;

import com.semtex.domain.model.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: persistencia de metadatos de documentos. */
public interface DocumentRepositoryPort {

    Document save(Document document);

    Optional<Document> findById(UUID id);

    List<Document> findByOrganization(UUID organizationId);

    void deleteById(UUID id);
}
