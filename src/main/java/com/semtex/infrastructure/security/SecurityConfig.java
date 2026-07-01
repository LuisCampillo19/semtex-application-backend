package com.semtex.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security como <b>resource server</b> stateless validando el JWT de Supabase.
 *
 * <ul>
 *   <li>El rol viaja en un claim del token y se mapea a {@code ROLE_*} para {@code @PreAuthorize}.</li>
 *   <li>{@link TenantContextFilter} corre tras la autenticación y deja el tenant resuelto.</li>
 *   <li>El aislamiento por organización lo aplica el filtro Hibernate, no este componente.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(TenantContextFilter tenantContextFilter) {
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        return http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Organizaciones: solo ADMIN crea/elimina
                        .requestMatchers(HttpMethod.POST, "/api/organizations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizations/**").hasRole("ADMIN")

                        // Usuarios: solo ADMIN gestiona
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Documentos: ADMIN/OPERATOR suben; ADMIN elimina
                        .requestMatchers(HttpMethod.POST, "/api/documents/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/**").hasRole("ADMIN")

                        // Chat: ADMIN/OPERATOR
                        .requestMatchers("/api/chat/**").hasAnyRole("ADMIN", "OPERATOR")

                        // Auditoría: ADMIN/AUDITOR (solo lectura)
                        .requestMatchers(HttpMethod.GET, "/api/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    /** Mapea el claim de rol (ADMIN/OPERATOR/AUDITOR) a una authority {@code ROLE_*}. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${semtex.jwt.role-claim:role}") String roleClaim) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = readRole(jwt, roleClaim);
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        });
        return converter;
    }

    private static String readRole(Jwt jwt, String roleClaim) {
        Object value = jwt.getClaim(roleClaim);
        if (value instanceof String s) return s;
        if (value instanceof List<?> list && !list.isEmpty()) return String.valueOf(list.get(0));
        return null;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${semtex.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
            String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(o -> !o.isBlank()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Location"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
