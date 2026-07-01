package com.semtex.infrastructure.tenant;

import com.semtex.infrastructure.out.persistence.TenantFilters;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Habilita el filtro Hibernate de tenant en la sesión de la request.
 *
 * Corre como {@link HandlerInterceptor} (después de que Spring vincula el EntityManager por
 * Open-Session-In-View y después de que la cadena de seguridad autenticó la request), de modo que
 * todas las queries de la request quedan acotadas a {@code organization_id = :tenantId} sin que
 * los repositorios tengan que recordar pasar el tenant. Aislamiento por defecto, no por convención.
 */
@Component
public class TenantPersistenceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantPersistenceInterceptor.class);

    private final EntityManager entityManager;

    public TenantPersistenceInterceptor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!TenantContext.isPresent()) {
            return true; // ruta pública / sin tenant: no se habilita el filtro
        }
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter(TenantFilters.TENANT_FILTER)
                .setParameter(TenantFilters.TENANT_PARAM, TenantContext.requireOrganizationId());
        log.debug("Filtro de tenant habilitado para organización {}", TenantContext.currentOrganizationId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter(TenantFilters.TENANT_FILTER);
        } catch (RuntimeException ignored) {
            // la sesión puede estar ya cerrada; nada que limpiar
        }
    }
}
