package com.semtex.application.service;

import com.semtex.domain.model.Document;
import com.semtex.domain.model.ParsedSpreadsheet;
import com.semtex.domain.port.in.QueryDocumentsUseCase;
import com.semtex.domain.port.in.UploadDocumentUseCase;
import com.semtex.domain.port.out.DocumentRepositoryPort;
import com.semtex.domain.port.out.FileStoragePort;
import com.semtex.domain.port.out.SpreadsheetParserPort;
import com.semtex.domain.port.in.IngestFinancialRecordsUseCase;
import com.semtex.domain.port.in.IngestFinancialRecordsUseCase.IngestCommand;
import com.semtex.domain.port.in.IngestFinancialRecordsUseCase.SheetData;
import com.semtex.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: carga de documentos. Orquesta storage (binario original), parseo e ingesta de filas
 * en el contexto financial (a través de su puerto de entrada publicado, sin acoplar dominios).
 */
@Service
@Transactional
public class DocumentService implements UploadDocumentUseCase, QueryDocumentsUseCase {

    private final DocumentRepositoryPort documentRepository;
    private final FileStoragePort fileStorage;
    private final SpreadsheetParserPort parser;
    private final IngestFinancialRecordsUseCase ingestFinancialRecords;

    public DocumentService(DocumentRepositoryPort documentRepository,
                           FileStoragePort fileStorage,
                           SpreadsheetParserPort parser,
                           IngestFinancialRecordsUseCase ingestFinancialRecords) {
        this.documentRepository = documentRepository;
        this.fileStorage = fileStorage;
        this.parser = parser;
        this.ingestFinancialRecords = ingestFinancialRecords;
    }

    @Override
    public Document upload(UploadCommand command) {
        // 1. Guardar el binario original en el storage y obtener su clave real.
        String storagePath = fileStorage.store(new FileStoragePort.StoreFileCommand(
                command.organizationId(), command.originalFilename(),
                command.contentType(), command.content()));

        // 2. Persistir los metadatos del documento.
        Document saved = documentRepository.save(Document.create(
                command.originalFilename(), storagePath, command.contentType(),
                (long) command.content().length, command.organizationId(), command.uploadedBy()));

        // 3. Parsear el contenido e ingerir las filas como registros financieros.
        ParsedSpreadsheet parsed = parser.parse(
                command.content(), command.contentType(), command.originalFilename());
        List<SheetData> sheets = parsed.rowsBySheet().entrySet().stream()
                .map(e -> new SheetData(e.getKey(), e.getValue()))
                .toList();
        ingestFinancialRecords.ingest(new IngestCommand(saved.getId(), command.organizationId(), sheets));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> listByOrganization(UUID organizationId) {
        return documentRepository.findByOrganization(organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Document get(UUID id) {
        return require(id);
    }

    @Override
    public void delete(UUID id) {
        Document document = require(id);
        fileStorage.delete(document.getStoragePath());
        documentRepository.deleteById(id); // financial_records se eliminan por ON DELETE CASCADE
    }

    private Document require(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado: " + id));
    }
}
