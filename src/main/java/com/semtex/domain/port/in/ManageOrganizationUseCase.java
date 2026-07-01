package com.semtex.domain.port.in;

import com.semtex.domain.model.Organization;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (driving): gestión de organizaciones.
 */
public interface ManageOrganizationUseCase {

    Organization create(CreateOrganizationCommand command);

    Organization rename(UUID id, String newName);

    void deactivate(UUID id);

    Organization get(UUID id);

    List<Organization> list();

    record CreateOrganizationCommand(String name, String slug) {}
}
