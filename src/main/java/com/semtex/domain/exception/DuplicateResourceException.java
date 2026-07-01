package com.semtex.domain.exception;

/** Conflicto: el recurso ya existe (ej. slug o email duplicado). HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
