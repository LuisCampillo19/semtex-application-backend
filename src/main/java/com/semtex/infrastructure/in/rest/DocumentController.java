package com.semtex.infrastructure.in.rest;

import com.semtex.domain.model.Document;
import com.semtex.domain.port.in.QueryDocumentsUseCase;
import com.semtex.domain.port.in.UploadDocumentUseCase;
import com.semtex.infrastructure.in.rest.dto.DocumentDtos.Response;
import com.semtex.infrastructure.tenant.TenantContext;
import com.semtex.domain.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final UploadDocumentUseCase uploadDocument;
    private final QueryDocumentsUseCase queryDocuments;

    public DocumentController(UploadDocumentUseCase uploadDocument, QueryDocumentsUseCase queryDocuments) {
        this.uploadDocument = uploadDocument;
        this.queryDocuments = queryDocuments;
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Response> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio y no puede estar vacío.");
        }
        UUID organizationId = TenantContext.requireOrganizationId();
        UUID uploadedBy = TenantContext.currentUserId();
        Document created = uploadDocument.upload(new UploadDocumentUseCase.UploadCommand(
                file.getOriginalFilename(), file.getContentType(), readBytes(file),
                organizationId, uploadedBy));
        return ResponseEntity.created(URI.create("/api/documents/" + created.getId()))
                .body(Response.from(created));
    }

    @GetMapping
    public List<Response> list() {
        UUID organizationId = TenantContext.requireOrganizationId();
        return queryDocuments.listByOrganization(organizationId).stream().map(Response::from).toList();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable UUID id) {
        return Response.from(queryDocuments.get(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        queryDocuments.delete(id);
        return ResponseEntity.noContent().build();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer el archivo subido.");
        }
    }
}
