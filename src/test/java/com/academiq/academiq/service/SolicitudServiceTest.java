package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.*;
import com.academiq.academiq.exception.TransicionEstadoInvalidaException;
import com.academiq.academiq.repository.HistorialSolicitudRepository;
import com.academiq.academiq.repository.SolicitudRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ReglaPrioridadService reglaPrioridadService;

    @Mock
    private HistorialSolicitudRepository historialRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    @Test
    @DisplayName("✅ PASA: registrar() debe crear solicitud con estado REGISTRADA")
    void test_registrar_cuando_datosValidos_deberia_crearSolicitud() {
        // Arrange
        UUID solicitanteId = UUID.randomUUID();
        Usuario solicitante = new Usuario();
        solicitante.setId(solicitanteId);
        solicitante.setRol(Rol.ESTUDIANTE);

        when(usuarioService.obtenerActivo(solicitanteId)).thenReturn(solicitante);

        Solicitud solicitudGuardada = new Solicitud();
        solicitudGuardada.setId(UUID.randomUUID());
        solicitudGuardada.setTipo(TipoSolicitud.REGISTRO_ASIGNATURA);
        solicitudGuardada.setEstado(EstadoSolicitud.REGISTRADA);
        solicitudGuardada.setCanal(CanalOrigen.SAC);

        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);

        // Act
        Solicitud resultado = solicitudService.registrar(
                TipoSolicitud.REGISTRO_ASIGNATURA,
                "Necesito apoyo con inscripción",
                CanalOrigen.SAC,
                solicitanteId
        );

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        
        // Verificamos que se guarde en base de datos la solicitud y el historial
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
        verify(historialRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("❌ FALLA (Esperado): clasificar() lanza error si la solicitud no está REGISTRADA")
    void test_clasificar_cuando_estadoNoEsRegistrada_deberia_lanzarExcepcion() {
        // Arrange
        UUID solicitudId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario usuarioLogueado = new Usuario();
        usuarioLogueado.setId(usuarioId);
        Solicitud solicitudExistente = new Solicitud();
        solicitudExistente.setId(solicitudId);
        // PONEMOS ESTADO INCORRECTO para que falle la regla: No se puede clasificar algo ya cerrado o en atencion
        solicitudExistente.setEstado(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitudExistente));

        // Act & Assert
        assertThatThrownBy(() -> solicitudService.clasificar(
                solicitudId,
                TipoSolicitud.REGISTRO_ASIGNATURA,
                ImpactoAcademico.ALTO,
                LocalDate.now().plusDays(5),
                "Afecta la graduación",
                usuarioId
        )).isInstanceOf(TransicionEstadoInvalidaException.class); // Nombre esperado de error
    }

    @Test
    @DisplayName("✅ PASA: clasificar() cambia la solicitud a CLASIFICADA")
    void test_clasificar_cuando_estadoRegistrada_deberia_clasificar() {
        // Arrange
        UUID solicitudId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario responsable = new Usuario();
        responsable.setId(usuarioId);
        responsable.setActivo(true);
        responsable.setRol(Rol.RESPONSABLE);
        when(usuarioService.obtenerActivo(usuarioId)).thenReturn(responsable);

        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.REGISTRADA);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(reglaPrioridadService.calcular(any(), any(), any())).thenReturn(Prioridad.ALTA);
        // Act
        Solicitud resultado = solicitudService.clasificar(
                solicitudId, TipoSolicitud.REGISTRO_ASIGNATURA, ImpactoAcademico.ALTO,
                LocalDate.now().plusDays(5), "Urgente", usuarioId
        );

        // Assert
        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        assertThat(resultado.getPrioridad()).isEqualTo(Prioridad.ALTA);
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    @DisplayName("✅ PASA: asignarResponsable() cambia solicitud a EN_ATENCION")
    void test_asignarResponsable_cuando_estadoClasificada_deberia_asignar() {
        // Arrange
        UUID solicitudId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        Usuario responsable = new Usuario();
        responsable.setId(responsableId);
        responsable.setActivo(true);
        responsable.setRol(Rol.RESPONSABLE);
        when(usuarioService.obtenerActivo(responsableId)).thenReturn(responsable);

        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act
        Solicitud resultado = solicitudService.asignarResponsable(solicitudId, responsableId);

        // Assert
        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.EN_ATENCION);
        assertThat(resultado.getResponsable()).isEqualTo(responsable);
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    @DisplayName("✅ PASA: atender() cambia solicitud a ATENDIDA")
    void test_atender_cuando_estadoEnAtencion_deberia_atender() {
        UUID solicitudId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario responsable = new Usuario();
        responsable.setId(usuarioId);
        responsable.setActivo(true);

        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);
        solicitud.setResponsable(responsable);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioService.obtenerActivo(usuarioId)).thenReturn(responsable);

        Solicitud resultado = solicitudService.atender(solicitudId, "Atendido con exito", usuarioId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);
    }

    @Test
    @DisplayName("✅ PASA: cerrar() cambia solicitud a CERRADA")
    void test_cerrar_cuando_estadoAtendida_deberia_cerrar() {
        UUID solicitudId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario responsable = new Usuario();
        responsable.setId(usuarioId);
        responsable.setActivo(true);
        when(usuarioService.obtenerActivo(usuarioId)).thenReturn(responsable);

        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.ATENDIDA);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        Solicitud resultado = solicitudService.cerrar(solicitudId, "Cerrado con exito", usuarioId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
    }
}