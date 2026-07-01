package com.semtex.infrastructure.out.persistence;

import com.semtex.infrastructure.out.persistence.entity.DocumentJpaEntity;
import com.semtex.infrastructure.out.persistence.repository.DocumentJpaRepository;
import com.semtex.infrastructure.out.persistence.entity.FinancialRecordJpaEntity;
import com.semtex.infrastructure.out.persistence.repository.FinancialRecordJpaRepository;
import com.semtex.infrastructure.out.persistence.entity.OrganizationJpaEntity;
import com.semtex.infrastructure.out.persistence.entity.UserJpaEntity;
import com.semtex.infrastructure.out.persistence.repository.OrganizationJpaRepository;
import com.semtex.infrastructure.out.persistence.repository.UserJpaRepository;
import com.semtex.infrastructure.out.persistence.TenantFilters;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba el aislamiento multi-inquilino aplicado por el filtro Hibernate (el punto bloqueante del
 * audit) y el round-trip de JSONB, contra un PostgreSQL real vía Testcontainers.
 */
@SpringBootTest
@Testcontainers
@Transactional
class FinancialRecordTenantIsolationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired EntityManager em;
    @Autowired OrganizationJpaRepository orgRepo;
    @Autowired UserJpaRepository userRepo;
    @Autowired DocumentJpaRepository docRepo;
    @Autowired FinancialRecordJpaRepository financialRepo;

    @Test
    void el_filtro_de_tenant_oculta_las_filas_de_otra_organizacion() {
        UUID orgA = seedOrganization("Empresa A", "empresa-a");
        UUID orgB = seedOrganization("Empresa B", "empresa-b");
        UUID docA = seedDocument(orgA);
        UUID docB = seedDocument(orgB);
        financialRepo.save(record(docA, orgA, Map.of("categoria", "Ventas", "ingreso", 1000)));
        financialRepo.save(record(docB, orgB, Map.of("categoria", "Ventas", "ingreso", 2000)));
        em.flush();
        em.clear();

        enableTenantFilter(orgA);
        List<FinancialRecordJpaEntity> visible = financialRepo.findAll();

        assertThat(visible).hasSize(1);
        assertThat(visible.get(0).getOrganizationId()).isEqualTo(orgA);
        assertThat(orgRepo.findAll()).extracting(OrganizationJpaEntity::getId).containsExactly(orgA);
    }

    @Test
    void el_jsonb_hace_round_trip_y_la_busqueda_por_campo_matchea_numeros() {
        UUID orgA = seedOrganization("Empresa C", "empresa-c");
        UUID docA = seedDocument(orgA);
        financialRepo.save(record(docA, orgA, Map.of("categoria", "Ventas", "ingreso", 1500)));
        em.flush();
        em.clear();

        // Búsqueda nativa por valor numérico (corrige el match solo-string).
        List<FinancialRecordJpaEntity> byNumber =
                financialRepo.findByOrganizationIdAndJsonField(orgA, "ingreso", "1500", 10);
        assertThat(byNumber).hasSize(1);
        assertThat(byNumber.get(0).getRowData()).containsEntry("categoria", "Ventas");

        List<FinancialRecordJpaEntity> byString =
                financialRepo.findByOrganizationIdAndJsonField(orgA, "categoria", "Ventas", 10);
        assertThat(byString).hasSize(1);
    }

    // ---- helpers ----

    private void enableTenantFilter(UUID tenantId) {
        Session session = em.unwrap(Session.class);
        session.enableFilter(TenantFilters.TENANT_FILTER).setParameter(TenantFilters.TENANT_PARAM, tenantId);
    }

    private UUID seedOrganization(String name, String slug) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        orgRepo.save(new OrganizationJpaEntity(id, name, slug, true, now, now));
        return id;
    }

    private UUID seedDocument(UUID orgId) {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        userRepo.save(new UserJpaEntity(userId, "u-" + userId + "@x.com",
                UserJpaEntity.RoleJpa.ADMIN, orgId, true, null, now, now));
        UUID docId = UUID.randomUUID();
        docRepo.save(new DocumentJpaEntity(docId, "balance.csv", orgId + "/balance.csv",
                "text/csv", 123L, orgId, userId, now));
        return docId;
    }

    private FinancialRecordJpaEntity record(UUID docId, UUID orgId, Map<String, Object> rowData) {
        return new FinancialRecordJpaEntity(UUID.randomUUID(), docId, orgId, "default", 1,
                rowData, LocalDateTime.now());
    }
}
