package com.semtex.application.service;

import com.semtex.domain.exception.DuplicateResourceException;
import com.semtex.domain.model.Organization;
import com.semtex.domain.model.Role;
import com.semtex.domain.model.User;
import com.semtex.domain.port.in.RegisterOrganizationUseCase.RegisterCommand;
import com.semtex.domain.port.in.RegisterOrganizationUseCase.Registration;
import com.semtex.domain.port.out.OrganizationRepositoryPort;
import com.semtex.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    private final OrganizationRepositoryPort orgRepo = mock(OrganizationRepositoryPort.class);
    private final UserRepositoryPort userRepo = mock(UserRepositoryPort.class);
    private final RegistrationService service = new RegistrationService(orgRepo, userRepo);

    @Test
    void crea_la_organizacion_y_su_primer_admin() {
        when(orgRepo.existsBySlug("acme")).thenReturn(false);
        when(userRepo.existsByEmail("owner@acme.com")).thenReturn(false);
        when(orgRepo.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration reg = service.register(new RegisterCommand("ACME", "acme", "owner@acme.com", null));

        assertThat(reg.organization().getSlug()).isEqualTo("acme");
        assertThat(reg.admin().getRole()).isEqualTo(Role.ADMIN);
        assertThat(reg.admin().getOrganizationId()).isEqualTo(reg.organization().getId());
    }

    @Test
    void usa_el_id_de_supabase_cuando_se_provee() {
        UUID supabaseUid = UUID.randomUUID();
        when(orgRepo.existsBySlug(any())).thenReturn(false);
        when(userRepo.existsByEmail(any())).thenReturn(false);
        when(orgRepo.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration reg = service.register(new RegisterCommand("ACME", "acme", "owner@acme.com", supabaseUid));

        assertThat(reg.admin().getId()).isEqualTo(supabaseUid);
    }

    @Test
    void rechaza_un_slug_duplicado_sin_crear_usuario() {
        when(orgRepo.existsBySlug("acme")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterCommand("ACME", "acme", "owner@acme.com", null)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepo, never()).save(any());
        verify(orgRepo, never()).save(any());
    }
}
