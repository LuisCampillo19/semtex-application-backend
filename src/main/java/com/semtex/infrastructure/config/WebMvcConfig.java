package com.semtex.infrastructure.config;

import com.semtex.infrastructure.tenant.TenantPersistenceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el {@link TenantPersistenceInterceptor} para que el filtro Hibernate de tenant
 * quede habilitado en cada request autenticada (el EntityManager ya está vinculado a este punto).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantPersistenceInterceptor tenantPersistenceInterceptor;

    public WebMvcConfig(TenantPersistenceInterceptor tenantPersistenceInterceptor) {
        this.tenantPersistenceInterceptor = tenantPersistenceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantPersistenceInterceptor).addPathPatterns("/api/**");
    }
}
