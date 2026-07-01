package com.semtex.infrastructure.in.rest;

import com.semtex.domain.model.Organization;
import com.semtex.domain.port.in.ManageOrganizationUseCase;
import com.semtex.infrastructure.in.rest.dto.OrganizationDtos.CreateRequest;
import com.semtex.infrastructure.in.rest.dto.OrganizationDtos.RenameRequest;
import com.semtex.infrastructure.in.rest.dto.OrganizationDtos.Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final ManageOrganizationUseCase useCase;

    public OrganizationController(ManageOrganizationUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
        Organization created = useCase.create(
                new ManageOrganizationUseCase.CreateOrganizationCommand(request.name(), request.slug()));
        return ResponseEntity.created(URI.create("/api/organizations/" + created.getId()))
                .body(Response.from(created));
    }

    /** Devuelve la organización del token (el filtro de tenant limita a la propia). */
    @GetMapping
    public List<Response> list() {
        return useCase.list().stream().map(Response::from).toList();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable UUID id) {
        return Response.from(useCase.get(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Response rename(@PathVariable UUID id, @Valid @RequestBody RenameRequest request) {
        return Response.from(useCase.rename(id, request.name()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        useCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
