package com.semtex.infrastructure.in.rest.dto;

import com.semtex.domain.model.Role;
import com.semtex.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTOs REST del contexto identity — usuarios. */
public final class UserDtos {

    private UserDtos() {}

    public record CreateRequest(
            @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es válido") String email,
            @NotNull(message = "El rol es obligatorio") Role role
    ) {}

    public record ChangeRoleRequest(
            @NotNull(message = "El rol es obligatorio") Role role
    ) {}

    public record Response(
            UUID id, String email, Role role, boolean active,
            LocalDateTime lastLoginAt, LocalDateTime createdAt
    ) {
        public static Response from(User u) {
            return new Response(u.getId(), u.getEmail(), u.getRole(), u.isActive(),
                    u.getLastLoginAt(), u.getCreatedAt());
        }
    }
}
