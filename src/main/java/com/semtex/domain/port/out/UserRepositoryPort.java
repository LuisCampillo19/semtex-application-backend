package com.semtex.domain.port.out;

import com.semtex.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de usuarios.
 * Las lecturas quedan acotadas al tenant actual por el filtro Hibernate.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findByOrganizationId(UUID organizationId);

    boolean existsByEmail(String email);
}
