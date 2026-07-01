package com.semtex.infrastructure.out.persistence.adapter;

import com.semtex.domain.model.User;
import com.semtex.domain.port.out.UserRepositoryPort;
import com.semtex.infrastructure.out.persistence.mapper.UserPersistenceMapper;
import com.semtex.infrastructure.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador JPA del puerto de usuarios. */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository jpa, UserPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpa.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<User> findByOrganizationId(UUID organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtAsc(organizationId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }
}
