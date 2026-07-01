package com.semtex.infrastructure.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Contexto de tenant resuelto del JWT y disponible durante toda la request.
 *
 * Se puebla en la capa de infraestructura inbound (interceptor de seguridad) a partir del
 * token validado; el dominio y los use cases reciben el {@code organizationId} ya resuelto y
 * NUNCA leen el JWT. El aislamiento de lectura lo aplica el filtro Hibernate de persistencia.
 *
 * Implementado con {@link ThreadLocal}: una request = un hilo en el modelo MVC bloqueante.
 */
public final class TenantContext {

    /** Datos mínimos del actor autenticado. El rol viaja como String para no acoplar shared a identity. */
    public record TenantPrincipal(UUID userId, String email, UUID organizationId, String role) {}

    private static final ThreadLocal<TenantPrincipal> HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(TenantPrincipal principal) {
        HOLDER.set(principal);
    }

    public static Optional<TenantPrincipal> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    /** Tenant actual o {@code null} si la request no está autenticada (rutas públicas). */
    public static UUID currentOrganizationId() {
        TenantPrincipal p = HOLDER.get();
        return p == null ? null : p.organizationId();
    }

    public static UUID currentUserId() {
        TenantPrincipal p = HOLDER.get();
        return p == null ? null : p.userId();
    }

    /** Tenant actual; lanza si no hay tenant resuelto (uso desde código que lo exige). */
    public static UUID requireOrganizationId() {
        UUID orgId = currentOrganizationId();
        if (orgId == null) {
            throw new IllegalStateException("No hay tenant resuelto en el contexto de la request.");
        }
        return orgId;
    }

    public static boolean isPresent() {
        return HOLDER.get() != null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
