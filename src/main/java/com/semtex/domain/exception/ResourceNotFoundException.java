package com.semtex.domain.exception;

/** El recurso solicitado no existe (o no es visible para el tenant actual). HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
