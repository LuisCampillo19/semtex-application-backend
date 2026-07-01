package com.semtex.domain.exception;

/** Petición inválida por reglas de negocio (no de validación de campos). HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
