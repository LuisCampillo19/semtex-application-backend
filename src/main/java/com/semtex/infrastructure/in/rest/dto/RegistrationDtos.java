package com.semtex.infrastructure.in.rest.dto;

import com.semtex.domain.model.Role;
import com.semtex.domain.port.in.RegisterOrganizationUseCase.Registration;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** DTOs REST del alta de empresa (bootstrap). */
public final class RegistrationDtos {

    private RegistrationDtos() {}

    public record Request(
            @NotBlank(message = "El nombre de la organización es obligatorio") @Size(max = 255) String organizationName,
            @NotBlank(message = "El slug es obligatorio")
            @Pattern(regexp = "^[a-z0-9-]{2,100}$", message = "El slug solo admite minúsculas, números y guiones") String slug,
            @NotBlank(message = "El email del administrador es obligatorio")
            @Email(message = "El email no es válido") String adminEmail,
            UUID adminUserId
    ) {}

    public record Response(OrganizationInfo organization, AdminInfo admin) {

        public static Response from(Registration r) {
            return new Response(
                    new OrganizationInfo(r.organization().getId(), r.organization().getName(), r.organization().getSlug()),
                    new AdminInfo(r.admin().getId(), r.admin().getEmail(), r.admin().getRole()));
        }
    }

    public record OrganizationInfo(UUID id, String name, String slug) {}

    public record AdminInfo(UUID id, String email, Role role) {}
}
