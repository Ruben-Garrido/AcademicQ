package com.academiq.academiq.repository;

import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.CanalOrigen;
import com.academiq.academiq.domain.enums.EstadoSolicitud;
import com.academiq.academiq.domain.enums.Rol;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SolicitudRepositoryTest {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("✅ PASA: findByEstado debe traer las solicitudes con el estado exacto")
    void test_findByEstado_cuando_haySolicitudes_deberia_retornarPage() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Estudiante");
        usuario.setEmail("estudiante@uni.edu");
        usuario.setPassword("123");
        usuario.setRol(Rol.ESTUDIANTE);
        usuario.setActivo(true);
        usuario = entityManager.persist(usuario);

        Solicitud solicitud = new Solicitud();
        solicitud.setTipo(TipoSolicitud.REGISTRO_ASIGNATURA);
        solicitud.setDescripcion("Test Descripción");
        solicitud.setCanal(CanalOrigen.SAC);
        solicitud.setEstado(EstadoSolicitud.REGISTRADA);
        solicitud.setFechaRegistro(LocalDateTime.now());
        solicitud.setSolicitante(usuario);
        entityManager.persistAndFlush(solicitud);

        // Act
        Page<Solicitud> resultado = solicitudRepository.findByEstado(EstadoSolicitud.REGISTRADA, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("❌ FALLA (Esperado): findByEstado debe retornar vacío si no hay de ese estado")
    void test_findByEstado_cuando_noHayDeEseEstado_deberia_retornarPageVacio() {
        // Arrange: No guardamos nada
        
        // Act
        Page<Solicitud> resultado = solicitudRepository.findByEstado(EstadoSolicitud.CERRADA, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }
}