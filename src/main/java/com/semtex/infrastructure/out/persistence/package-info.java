/**
 * Infraestructura de persistencia compartida.
 *
 * Declara a nivel de paquete el {@code @FilterDef} global de tenant para que quede registrado
 * de forma independiente al orden en que Hibernate enlaza las entidades. Si se declara sobre una
 * entidad concreta (p. ej. {@code OrganizationJpaEntity}, contexto {@code identity}), una entidad
 * de otro contexto procesada antes alfabéticamente ({@code audit}, {@code chat}, ...) referencia
 * el filtro con {@code @Filter} cuando aún no existe y la EntityManagerFactory falla con
 * "undefined filter named 'tenantFilter'".
 */
@FilterDef(
        name = TenantFilters.TENANT_FILTER,
        parameters = @ParamDef(name = TenantFilters.TENANT_PARAM, type = UUID.class),
        defaultCondition = TenantFilters.BY_ORGANIZATION
)
package com.semtex.infrastructure.out.persistence;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;
