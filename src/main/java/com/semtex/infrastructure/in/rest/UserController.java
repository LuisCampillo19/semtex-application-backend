package com.semtex.infrastructure.in.rest;

import com.semtex.domain.model.User;
import com.semtex.domain.port.in.ManageUserUseCase;
import com.semtex.infrastructure.in.rest.dto.UserDtos.ChangeRoleRequest;
import com.semtex.infrastructure.in.rest.dto.UserDtos.CreateRequest;
import com.semtex.infrastructure.in.rest.dto.UserDtos.Response;
import com.semtex.infrastructure.tenant.TenantContext;
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
@RequestMapping("/api/users")
public class UserController {

    private final ManageUserUseCase useCase;

    public UserController(ManageUserUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
        UUID organizationId = TenantContext.requireOrganizationId();
        User created = useCase.create(
                new ManageUserUseCase.CreateUserCommand(request.email(), request.role(), organizationId));
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(Response.from(created));
    }

    /** Lista los usuarios de la organización del token. */
    @GetMapping
    public List<Response> list() {
        UUID organizationId = TenantContext.requireOrganizationId();
        return useCase.listByOrganization(organizationId).stream().map(Response::from).toList();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable UUID id) {
        return Response.from(useCase.get(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Response changeRole(@PathVariable UUID id, @Valid @RequestBody ChangeRoleRequest request) {
        return Response.from(useCase.changeRole(id, request.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        useCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
