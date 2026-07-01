package com.semtex.application.service;

import com.semtex.domain.model.FinancialRecord;
import com.semtex.domain.port.in.IngestFinancialRecordsUseCase;
import com.semtex.domain.port.out.FinancialRecordRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso publicado: ingesta de filas extraídas de un documento como registros financieros.
 */
@Service
@Transactional
public class FinancialRecordIngestionService implements IngestFinancialRecordsUseCase {

    private final FinancialRecordRepositoryPort repository;

    public FinancialRecordIngestionService(FinancialRecordRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void ingest(IngestCommand command) {
        List<FinancialRecord> records = new ArrayList<>();
        for (SheetData sheet : command.sheets()) {
            List<Map<String, Object>> rows = sheet.rows();
            for (int i = 0; i < rows.size(); i++) {
                records.add(FinancialRecord.create(
                        command.documentId(),
                        command.organizationId(),
                        sheet.sheetName(),
                        i + 1,
                        rows.get(i)));
            }
        }
        if (!records.isEmpty()) {
            repository.saveAll(records);
        }
    }
}
