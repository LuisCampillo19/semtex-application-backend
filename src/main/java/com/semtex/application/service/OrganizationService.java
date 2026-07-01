package com.semtex.application.service;

import com.semtex.domain.model.Organization;
import com.semtex.domain.port.in.ManageOrganizationUseCase;
import com.semtex.domain.port.out.OrganizationRepositoryPort;
import com.semtex.domain.exception.DuplicateResourceException;
import com.semtex.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: gestión de organizaciones. Depende solo de puertos del dominio.
 */
@Service
@Transactional
public class OrganizationService implements ManageOrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepository;

    public OrganizationService(OrganizationRepositoryPort organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public Organization create(CreateOrganizationCommand command) {
        if (organizationRepository.existsBySlug(command.slug())) {
            throw new DuplicateResourceException("Ya existe una organización con el slug '" + command.slug() + "'.");
        }
        return organizationRepository.save(Organization.create(command.name(), command.slug()));
    }

    @Override
    public Organization rename(UUID id, String newName) {
        Organization org = require(id);
        org.rename(newName);
        return organizationRepository.save(org);
    }

    @Override
    public void deactivate(UUID id) {
        Organization org = require(id);
        org.deactivate();
        organizationRepository.save(org);
    }

    @Override
    @Transactional(readOnly = true)
    public Organization get(UUID id) {
        return require(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> list() {
        return organizationRepository.findVisible();
    }

    private Organization require(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada: " + id));
    }
}
