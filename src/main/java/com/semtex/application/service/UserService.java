package com.semtex.application.service;

import com.semtex.domain.model.Role;
import com.semtex.domain.model.User;
import com.semtex.domain.port.in.ManageUserUseCase;
import com.semtex.domain.port.out.UserRepositoryPort;
import com.semtex.domain.exception.DuplicateResourceException;
import com.semtex.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: gestión de usuarios y RBAC. Depende solo de puertos del dominio.
 */
@Service
@Transactional
public class UserService implements ManageUserUseCase {

    private final UserRepositoryPort userRepository;

    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + command.email() + "'.");
        }
        return userRepository.save(User.create(command.email(), command.role(), command.organizationId()));
    }

    @Override
    public User changeRole(UUID userId, Role newRole) {
        User user = require(userId);
        user.changeRole(newRole);
        return userRepository.save(user);
    }

    @Override
    public void deactivate(UUID userId) {
        User user = require(userId);
        user.deactivate();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(UUID userId) {
        return require(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listByOrganization(UUID organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    private User require(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
