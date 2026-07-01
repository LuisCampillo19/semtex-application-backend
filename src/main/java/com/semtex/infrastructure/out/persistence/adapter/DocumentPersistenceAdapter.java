package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.Document;
import com.semtex.domain.port.out.DocumentRepositoryPort;
import com.semtex.infrastructure.out.persistence.mapper.DocumentPersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.DocumentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA del puerto de documentos. */
@Component
public class DocumentPersistenceAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository jpa;
    private final DocumentPersistenceMapper mapper;

    public DocumentPersistenceAdapter(DocumentJpaRepository jpa, DocumentPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Document save(Document document) {
        return mapper.toDomain(jpa.save(mapper.toEntity(document)));
    }

    @Override
    public Optional<Document> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Document> findByOrganization(UUID organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
