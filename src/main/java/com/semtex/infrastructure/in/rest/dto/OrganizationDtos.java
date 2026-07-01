package com.semtex.infrastructure.in.rest.dto;

import com.semtex.domain.model.Organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTOs REST del contexto identity — organizaciones. */
public final class OrganizationDtos {

    private OrganizationDtos() {}

    public record CreateRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 255) String name,
            @NotBlank(message = "El slug es obligatorio")
            @Pattern(regexp = "^[a-z0-9-]{2,100}$", message = "El slug solo admite minúsculas, números y guiones") String slug
    ) {}

    public record RenameRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 255) String name
    ) {}

    public record Response(
            UUID id, String name, String slug, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        public static Response from(Organization o) {
            return new Response(o.getId(), o.getName(), o.getSlug(), o.isActive(),
                    o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
