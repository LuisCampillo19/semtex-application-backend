package com.semtex.infrastructure.in.rest;

import com.semtex.domain.port.in.QueryFinancialRecordsUseCase;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase.QueryCommand;
import com.semtex.infrastructure.in.rest.dto.FinancialRecordDtos.RecordResponse;
import com.semtex.infrastructure.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial-records")
public class FinancialRecordController {

    private final QueryFinancialRecordsUseCase useCase;

    public FinancialRecordController(QueryFinancialRecordsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<RecordResponse> list(
            @RequestParam(required = false) UUID documentId,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String value,
            @RequestParam(defaultValue = "100") int limit) {
        UUID organizationId = TenantContext.requireOrganizationId();
        return useCase.query(new QueryCommand(organizationId, documentId, fieldName, value, limit))
                .stream().map(RecordResponse::from).toList();
    }
}
