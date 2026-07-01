package com.semtex.infrastructure.in.rest.dto;

import com.semtex.domain.model.FinancialRecord;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** DTOs REST del contexto financial. */
public final class FinancialRecordDtos {

    private FinancialRecordDtos() {}

    public record RecordResponse(
            UUID id, UUID documentId, String sheetName, Integer rowIndex,
            Map<String, Object> rowData, LocalDateTime createdAt
    ) {
        public static RecordResponse from(FinancialRecord r) {
            return new RecordResponse(r.getId(), r.getDocumentId(), r.getSheetName(),
                    r.getRowIndex(), r.getRowData(), r.getCreatedAt());
        }
    }
}
