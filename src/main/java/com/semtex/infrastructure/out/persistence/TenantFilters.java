package com.semtex.infrastructure.out.persistence;

/**
 * Nombres del filtro Hibernate multi-inquilino aplicado en lecturas.
 *
 * El {@code @FilterDef} se declara una vez (ver {@code OrganizationJpaEntity}); cada entidad
 * con datos de tenant lleva {@code @Filter(name = TENANT_FILTER)}. El filtro se habilita por
 * request en {@link com.semtex.infrastructure.tenant.TenantPersistenceInterceptor} con el tenant actual.
 */
public final class TenantFilters {

    public static final String TENANT_FILTER = "tenantFilter";
    public static final String TENANT_PARAM = "tenantId";

    /** Condición por defecto para entidades con columna {@code organization_id}. */
    public static final String BY_ORGANIZATION = "organization_id = :tenantId";

    /** Condición para la propia organización (su id ES el tenant). */
    public static final String BY_ID = "id = :tenantId";

    private TenantFilters() {}
}
