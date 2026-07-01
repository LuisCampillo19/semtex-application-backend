package com.semtex.infrastructure.in.rest;

import com.semtex.domain.port.in.QueryAuditLogUseCase;
import com.semtex.infrastructure.in.rest.dto.AuditLogDtos.Response;
import com.semtex.infrastructure.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final QueryAuditLogUseCase useCase;

    public AuditLogController(QueryAuditLogUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public List<Response> list(@RequestParam(defaultValue = "100") int limit) {
        UUID organizationId = TenantContext.requireOrganizationId();
        return useCase.listByOrganization(organizationId, limit).stream().map(Response::from).toList();
    }
}
