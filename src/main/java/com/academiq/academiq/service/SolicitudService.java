package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.HistorialSolicitud;
import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.domain.enums.*;
import com.academiq.academiq.exception.RecursoNoEncontradoException;
import com.academiq.academiq.exception.TransicionEstadoInvalidaException;
import com.academiq.academiq.exception.UsuarioInactivoException;
import com.academiq.academiq.repository.HistorialSolicitudRepository;
import com.academiq.academiq.repository.SolicitudRepository;
import com.academiq.academiq.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final ReglaPrioridadService reglaPrioridadService;

    // ── RF-01 Registrar ──────────────────────────────────────
    @Transactional
    public Solicitud registrar(TipoSolicitud tipo,
                               String descripcion,
                               CanalOrigen canal,
                               UUID solicitanteId) {

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + solicitanteId));

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

        Prioridad prioridad = reglaPrioridadService
                .calcular(tipo, impacto, fechaLimite);

        solicitud.setTipo(tipo);
        solicitud.setPrioridad(prioridad);
        solicitud.setJustificacion(justificacion);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);

        solicitudRepository.save(solicitud);

        Usuario responsable = obtenerUsuarioActivo(responsableId);
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

        Usuario responsable = obtenerUsuarioActivo(responsableId);

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

        Usuario usuario = solicitud.getResponsable();
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

        Usuario usuario = obtenerUsuarioActivo(usuarioId);
        registrarHistorial(solicitud, usuario, "CERRADA", observacionCierre);

        return solicitud;
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

    private Usuario obtenerUsuarioActivo(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + id));
        if (!usuario.getActivo()) {
            throw new UsuarioInactivoException(
                    "El usuario " + usuario.getNombre() + " no está activo");
        }
        return usuario;
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
