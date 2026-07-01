package com.semtex.application.service;

import com.semtex.domain.exception.DuplicateResourceException;
import com.semtex.domain.model.Organization;
import com.semtex.domain.model.Role;
import com.semtex.domain.model.User;
import com.semtex.domain.port.in.RegisterOrganizationUseCase;
import com.semtex.domain.port.out.OrganizationRepositoryPort;
import com.semtex.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de bootstrap: crea una organización y su primer usuario ADMIN en una sola transacción.
 * Depende solo de puertos del dominio. Pensado para un endpoint público (alta de empresa).
 */
@Service
@Transactional
public class RegistrationService implements RegisterOrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepository;
    private final UserRepositoryPort userRepository;

    public RegistrationService(OrganizationRepositoryPort organizationRepository,
                               UserRepositoryPort userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Registration register(RegisterCommand command) {
        if (organizationRepository.existsBySlug(command.slug())) {
            throw new DuplicateResourceException("Ya existe una organización con el slug '" + command.slug() + "'.");
        }
        if (userRepository.existsByEmail(command.adminEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + command.adminEmail() + "'.");
        }

        Organization organization =
                organizationRepository.save(Organization.create(command.organizationName(), command.slug()));

        User admin = command.adminUserId() != null
                ? User.create(command.adminUserId(), command.adminEmail(), Role.ADMIN, organization.getId())
                : User.create(command.adminEmail(), Role.ADMIN, organization.getId());

        return new Registration(organization, userRepository.save(admin));
    }
}
