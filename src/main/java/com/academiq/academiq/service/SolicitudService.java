package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.HistorialSolicitud;
import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.*;
import com.academiq.academiq.exception.RecursoNoEncontradoException;
import com.academiq.academiq.exception.TransicionEstadoInvalidaException;
import com.academiq.academiq.repository.HistorialSolicitudRepository;
import com.academiq.academiq.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final ReglaPrioridadService reglaPrioridadService;
    private final UsuarioService usuarioService;

    /**
     * Registra una nueva solicitud en el sistema.
     * 
     * @param tipo El tipo de la solicitud (ej. HOMOLOGACION).
     * @param descripcion Detalles provistos por el solicitante.
     * @param canal El medio por el cual ingresó la solicitud.
     * @param solicitanteId ID del usuario que requiere el servicio.
     * @return La solicitud creada en estado REGISTRADA.
     */
    // ── RF-01 Registrar ──────────────────────────────────────
    @Transactional
    public Solicitud registrar(TipoSolicitud tipo,
                               String descripcion,
                               CanalOrigen canal,
                               UUID solicitanteId) {

        Usuario solicitante = usuarioService.obtenerActivo(solicitanteId);

        Solicitud solicitud = Solicitud.builder()
                .tipo(tipo)
                .descripcion(descripcion)
                .canal(canal)
                .estado(EstadoSolicitud.REGISTRADA)
                .fechaRegistro(LocalDateTime.now())
                .solicitante(solicitante)
                .build();

        solicitudRepository.save(solicitud);
        registrarHistorial(solicitud, solicitante, "REGISTRADA",
                "Solicitud creada desde canal " + canal);
        return solicitud;
    }

    /**
     * Clasifica una solicitud en estado REGISTRADA, aplicando reglas de prioridad
     * y preparándola para su asignación.
     * 
     * @param solicitudId ID de la solicitud a clasificar.
     * @param tipo Confirmamos/ajustamos el tipo de solicitud.
     * @param impacto Nivel de impacto académico (ALTO, MEDIO, BAJO).
     * @param fechaLimite Fecha de solución esperada.
     * @param justificacion Explicación técnica de la prioridad.
     * @param responsableId ID del funcionario que clasifica.
     * @return La solicitud actualizada a estado CLASIFICADA.
     * @throws TransicionEstadoInvalidaException si la solicitud no estaba en estado REGISTRADA.
     */
    // ── RF-02, RF-03 Clasificar ───────────────────────────────
    @Transactional
    public Solicitud clasificar(UUID solicitudId,
                                TipoSolicitud tipo,
                                ImpactoAcademico impacto,
                                LocalDate fechaLimite,
                                String justificacion,
                                UUID responsableId) {

        Solicitud solicitud = obtenerSolicitud(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.REGISTRADA) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede clasificar una solicitud en estado REGISTRADA. " +
                            "Estado actual: " + solicitud.getEstado());
        }

        // Validación de regla de negocio: SLA máximo permitido institucional (ej. 30 días)
        if (fechaLimite.isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("La fecha límite no puede exceder el límite institucional de 30 días calendario.");
        }

        Prioridad prioridad = reglaPrioridadService
                .calcular(tipo, impacto, fechaLimite);

        solicitud.setTipo(tipo);
        solicitud.setPrioridad(prioridad);
        solicitud.setJustificacion(justificacion);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);

        solicitudRepository.save(solicitud);

        Usuario responsable = usuarioService.obtenerActivo(responsableId);
        registrarHistorial(solicitud, responsable, "CLASIFICADA",
                "Prioridad calculada: " + prioridad + " | " + justificacion);

        return solicitud;
    }

    // ── RF-05 Asignar responsable ─────────────────────────────
    @Transactional
    public Solicitud asignarResponsable(UUID solicitudId, UUID responsableId) {

        Solicitud solicitud = obtenerSolicitud(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.CLASIFICADA) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede asignar responsable a una solicitud CLASIFICADA. " +
                            "Estado actual: " + solicitud.getEstado());
        }

        Usuario responsable = usuarioService.obtenerActivo(responsableId);

        solicitud.setResponsable(responsable);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);
        solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, responsable, "EN_ATENCION",
                "Responsable asignado: " + responsable.getNombre());

        return solicitud;
    }

    // ── RF-04 Atender ─────────────────────────────────────────
    @Transactional
    public Solicitud atender(UUID solicitudId,
                             String observacion,
                             UUID usuarioId) {

        Solicitud solicitud = obtenerSolicitud(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.EN_ATENCION) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede atender una solicitud EN_ATENCION. " +
                            "Estado actual: " + solicitud.getEstado());
        }

        solicitud.setEstado(EstadoSolicitud.ATENDIDA);
        solicitudRepository.save(solicitud);

        Usuario usuario = usuarioService.obtenerActivo(usuarioId);
        registrarHistorial(solicitud, usuario, "ATENDIDA", observacion);

        return solicitud;
    }

    // ── RF-08 Cerrar ──────────────────────────────────────────
    @Transactional
    public Solicitud cerrar(UUID solicitudId,
                            String observacionCierre,
                            UUID usuarioId) {

        Solicitud solicitud = obtenerSolicitud(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.ATENDIDA) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se puede cerrar una solicitud ATENDIDA. " +
                            "Estado actual: " + solicitud.getEstado());
        }

        solicitud.setEstado(EstadoSolicitud.CERRADA);
        solicitud.setObservacionCierre(observacionCierre);
        solicitudRepository.save(solicitud);

        Usuario usuario = usuarioService.obtenerActivo(usuarioId);
        registrarHistorial(solicitud, usuario, "CERRADA", observacionCierre);

        return solicitud;
    }

    // ── Simulación de Prioridad ───────────────────────────────
    public Prioridad simularPrioridad(TipoSolicitud tipo,
                                      ImpactoAcademico impacto,
                                      LocalDate fechaLimite) {
        if (fechaLimite != null && fechaLimite.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha límite no puede ser en el pasado.");
        }
        if (fechaLimite != null && fechaLimite.isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("La fecha límite no puede exceder el límite institucional de 30 días calendario.");
        }
        return reglaPrioridadService.calcular(tipo, impacto, fechaLimite);
    }

    // ── RF-07 Consultar ───────────────────────────────────────
    public Page<Solicitud> listar(EstadoSolicitud estado,
                                  TipoSolicitud tipo,
                                  Prioridad prioridad,
                                  UUID responsableId,
                                  Pageable pageable) {
        if (estado != null)
            return solicitudRepository.findByEstado(estado, pageable);
        if (tipo != null)
            return solicitudRepository.findByTipo(tipo, pageable);
        if (prioridad != null)
            return solicitudRepository.findByPrioridad(prioridad, pageable);
        if (responsableId != null)
            return solicitudRepository.findByResponsableId(responsableId, pageable);
        return solicitudRepository.findAll(pageable);
    }

    public Solicitud obtenerPorId(UUID id) {
        return obtenerSolicitud(id);
    }

    // ── RF-06 Historial ───────────────────────────────────────
    public List<HistorialSolicitud> obtenerHistorial(UUID solicitudId) {
        obtenerSolicitud(solicitudId);
        return historialRepository
                .findBySolicitudIdOrderByFechaAccionAsc(solicitudId);
    }

    // ── Métodos privados de soporte ───────────────────────────
    private Solicitud obtenerSolicitud(UUID id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Solicitud no encontrada: " + id));
    }

    private void registrarHistorial(Solicitud solicitud,
                                    Usuario usuario,
                                    String accion,
                                    String observacion) {
        HistorialSolicitud historial = HistorialSolicitud.builder()
                .solicitud(solicitud)
                .usuario(usuario)
                .accion(accion)
                .observacion(observacion)
                .fechaAccion(LocalDateTime.now())
                .build();
        historialRepository.save(historial);
    }
}
