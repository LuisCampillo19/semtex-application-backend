package com.semtex.infrastructure.out.persistence.repository;

import com.semtex.infrastructure.out.persistence.entity.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, UUID> {
    boolean existsBySlug(String slug);
}
