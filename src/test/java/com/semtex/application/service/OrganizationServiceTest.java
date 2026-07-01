package com.semtex.application.service;

import com.semtex.domain.model.Organization;
import com.semtex.domain.port.in.ManageOrganizationUseCase.CreateOrganizationCommand;
import com.semtex.domain.port.out.OrganizationRepositoryPort;
import com.semtex.domain.exception.DuplicateResourceException;
import com.semtex.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationServiceTest {

    private final OrganizationRepositoryPort repository = mock(OrganizationRepositoryPort.class);
    private final OrganizationService service = new OrganizationService(repository);

    @Test
    void crea_una_organizacion_cuando_el_slug_es_unico() {
        when(repository.existsBySlug("ferreteria-lopez")).thenReturn(false);
        when(repository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = service.create(new CreateOrganizationCommand("Ferretería López", "ferreteria-lopez"));

        assertThat(result.getName()).isEqualTo("Ferretería López");
        assertThat(result.getSlug()).isEqualTo("ferreteria-lopez");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void rechaza_un_slug_duplicado() {
        when(repository.existsBySlug("dup")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateOrganizationCommand("X", "dup")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void lanza_not_found_si_la_organizacion_no_existe() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
