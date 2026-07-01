package com.semtex.domain.port.in;

import com.semtex.domain.model.Organization;
import com.semtex.domain.model.User;

import java.util.UUID;

/**
 * Puerto de entrada (driving): alta de una nueva empresa con su primer usuario ADMIN.
 *
 * Es el punto de arranque del sistema (bootstrap): resuelve el problema del huevo y la gallina, ya
 * que crear usuarios/organizaciones por la API normal exige ser ADMIN. Es público y crea la
 * organización y su administrador de forma atómica.
 */
public interface RegisterOrganizationUseCase {

    Registration register(RegisterCommand command);

    /**
     * @param adminUserId opcional. En producción se pasa el {@code sub} del JWT de Supabase para que
     *                    el id del usuario coincida con el de Auth; si es {@code null} se genera uno.
     */
    record RegisterCommand(String organizationName, String slug, String adminEmail, UUID adminUserId) {}

    record Registration(Organization organization, User admin) {}
}
