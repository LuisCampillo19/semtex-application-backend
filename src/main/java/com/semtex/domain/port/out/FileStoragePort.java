package com.semtex.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida: almacenamiento del binario original del documento.
 *
 * Frontera tecnológica real (S3/MinIO en dev, Supabase Storage en prod). El dominio solo conoce
 * esta interfaz y trabaja con la clave/ruta del objeto, nunca con el SDK concreto.
 */
public interface FileStoragePort {

    /** Sube el binario y devuelve la clave del objeto almacenada (storage path). */
    String store(StoreFileCommand command);

    /** Descarga el binario original por su clave, si existe. */
    Optional<byte[]> load(String storagePath);

    /** Elimina el objeto (idempotente). */
    void delete(String storagePath);

    record StoreFileCommand(UUID organizationId, String originalFilename,
                            String contentType, byte[] content) {}
}
