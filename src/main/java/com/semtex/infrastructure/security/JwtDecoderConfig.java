package com.semtex.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Decodificador de JWT con dos modos:
 *
 * <ul>
 *   <li><b>Producción (Supabase real):</b> {@code semtex.security.mode != hs256}. Spring Boot
 *       autoconfigura el {@link JwtDecoder} desde {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}
 *       (firma asimétrica RS256/ES256 validada contra el JWKS).</li>
 *   <li><b>Dev/tests:</b> {@code semtex.security.mode = hs256}. Se valida el token con un secreto
 *       simétrico HS256 (>= 32 bytes), sin depender de la nube.</li>
 * </ul>
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    @ConditionalOnProperty(name = "semtex.security.mode", havingValue = "hs256")
    public JwtDecoder hs256JwtDecoder(@Value("${semtex.jwt.hs256-secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
