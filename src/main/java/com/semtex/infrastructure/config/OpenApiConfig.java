package com.semtex.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger (springdoc).
 *
 * Declara el esquema de seguridad Bearer JWT para que Swagger UI permita autenticar las llamadas,
 * y describe la API. Los endpoints se documentan automáticamente desde los controllers.
 *
 * Swagger UI: {@code /swagger-ui.html} · OpenAPI JSON: {@code /v3/api-docs}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Semtex API",
                version = "1.0.0",
                description = "Copiloto administrativo/financiero con IA para PyMEs. "
                        + "Multi-inquilino: el tenant se deriva del JWT (claim org_id)."),
        servers = @Server(url = "/", description = "Servidor local"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
