package com.semtex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de Semtex — copiloto administrativo/financiero con IA para PyMEs.
 *
 * Arquitectura hexagonal (puertos y adaptadores) en monolito, organizada por capas:
 *   domain · application · infrastructure
 *
 * Regla de dependencia: infrastructure -> application -> domain.
 * El dominio es POJO puro (sin Spring/JPA). Verificado con ArchUnit.
 */
@SpringBootApplication
public class SemtexApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemtexApplication.class, args);
    }
}
