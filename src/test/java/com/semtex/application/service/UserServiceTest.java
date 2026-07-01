package com.semtex.application.service;

import com.semtex.domain.model.Role;
import com.semtex.domain.model.User;
import com.semtex.domain.port.in.ManageUserUseCase.CreateUserCommand;
import com.semtex.domain.port.out.UserRepositoryPort;
import com.semtex.domain.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepositoryPort repository = mock(UserRepositoryPort.class);
    private final UserService service = new UserService(repository);

    @Test
    void crea_un_usuario_en_la_organizacion_del_token() {
        UUID org = UUID.randomUUID();
        when(repository.existsByEmail("op@empresa.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.create(new CreateUserCommand("op@empresa.com", Role.OPERATOR, org));

        assertThat(result.getEmail()).isEqualTo("op@empresa.com");
        assertThat(result.getRole()).isEqualTo(Role.OPERATOR);
        assertThat(result.getOrganizationId()).isEqualTo(org);
    }

    @Test
    void rechaza_un_email_duplicado() {
        when(repository.existsByEmail("dup@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new CreateUserCommand("dup@empresa.com", Role.AUDITOR, UUID.randomUUID())))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void cambia_el_rol_de_un_usuario_existente() {
        UUID id = UUID.randomUUID();
        User existing = User.create("u@empresa.com", Role.OPERATOR, UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.changeRole(id, Role.ADMIN);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }
}
