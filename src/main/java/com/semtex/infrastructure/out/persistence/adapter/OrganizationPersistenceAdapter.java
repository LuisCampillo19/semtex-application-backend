package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.Organization;
import com.semtex.domain.port.out.OrganizationRepositoryPort;
import com.semtex.infrastructure.out.persistence.mapper.OrganizationPersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA del puerto de organizaciones. */
@Component
public class OrganizationPersistenceAdapter implements OrganizationRepositoryPort {

    private final OrganizationJpaRepository jpa;
    private final OrganizationPersistenceMapper mapper;

    public OrganizationPersistenceAdapter(OrganizationJpaRepository jpa, OrganizationPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Organization save(Organization organization) {
        return mapper.toDomain(jpa.save(mapper.toEntity(organization)));
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Organization> findVisible() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }
}
