package com.semtex.domain.port.in;

import com.semtex.domain.model.Document;

import java.util.UUID;

/**
 * Puerto de entrada (driving): carga de un Excel/CSV, almacenamiento del original e ingesta de filas.
 */
public interface UploadDocumentUseCase {

    Document upload(UploadCommand command);

    record UploadCommand(String originalFilename, String contentType, byte[] content,
                         UUID organizationId, UUID uploadedBy) {}
}
