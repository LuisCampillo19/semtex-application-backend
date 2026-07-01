package com.semtex.domain.port.out;

import com.semtex.domain.model.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de organizaciones.
 * Las lecturas quedan acotadas al tenant actual por el filtro Hibernate.
 */
public interface OrganizationRepositoryPort {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);

    List<Organization> findVisible();

    boolean existsBySlug(String slug);
}
