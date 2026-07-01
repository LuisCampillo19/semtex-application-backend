package com.semtex.infrastructure.out.persistence.repository;

import com.semtex.infrastructure.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    boolean existsByEmail(String email);
    List<UserJpaEntity> findByOrganizationIdOrderByCreatedAtAsc(UUID organizationId);
}
