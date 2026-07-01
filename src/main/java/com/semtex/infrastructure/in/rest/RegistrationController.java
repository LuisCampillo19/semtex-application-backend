package com.semtex.infrastructure.in.rest;

import com.semtex.domain.port.in.RegisterOrganizationUseCase;
import com.semtex.domain.port.in.RegisterOrganizationUseCase.RegisterCommand;
import com.semtex.domain.port.in.RegisterOrganizationUseCase.Registration;
import com.semtex.infrastructure.in.rest.dto.RegistrationDtos.Request;
import com.semtex.infrastructure.in.rest.dto.RegistrationDtos.Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Alta de empresa (bootstrap): crea una organización y su primer ADMIN. Endpoint <b>público</b>
 * (sin token) — es el punto de entrada antes de existir cualquier usuario.
 */
@RestController
@RequestMapping("/api/register")
public class RegistrationController {

    private final RegisterOrganizationUseCase useCase;

    public RegistrationController(RegisterOrganizationUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<Response> register(@Valid @RequestBody Request request) {
        Registration registration = useCase.register(new RegisterCommand(
                request.organizationName(), request.slug(), request.adminEmail(), request.adminUserId()));
        return ResponseEntity
                .created(URI.create("/api/organizations/" + registration.organization().getId()))
                .body(Response.from(registration));
    }
}
