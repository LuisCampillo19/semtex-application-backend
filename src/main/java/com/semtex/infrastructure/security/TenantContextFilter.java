package com.semtex.infrastructure.security;

import com.semtex.infrastructure.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Puebla {@link TenantContext} a partir del JWT ya validado por el resource server.
 *
 * Se ejecuta después de la autenticación: lee {@code sub}, {@code email}, el claim de organización
 * y el de rol, y deja el {@code organizationId} resuelto para que los use cases no toquen el token.
 * Limpia el contexto al finalizar la request (evita fugas entre hilos reutilizados del pool).
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private final String orgClaim;
    private final String roleClaim;

    public TenantContextFilter(@Value("${semtex.jwt.org-claim:org_id}") String orgClaim,
                               @Value("${semtex.jwt.role-claim:role}") String roleClaim) {
        this.orgClaim = orgClaim;
        this.roleClaim = roleClaim;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth) {
                Jwt jwt = auth.getToken();
                UUID userId = parseUuid(jwt.getSubject());
                UUID orgId = parseUuid(jwt.getClaimAsString(orgClaim));
                String email = jwt.getClaimAsString("email");
                String role = jwt.getClaimAsString(roleClaim);
                if (orgId != null) {
                    TenantContext.set(new TenantContext.TenantPrincipal(userId, email, orgId, role));
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
