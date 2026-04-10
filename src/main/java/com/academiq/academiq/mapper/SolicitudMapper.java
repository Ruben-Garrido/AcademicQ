package com.academiq.academiq.mapper;

import com.academiq.academiq.domain.entity.HistorialSolicitud;
import com.academiq.academiq.domain.entity.Solicitud;
import com.academiq.academiq.domain.entity.Usuario;
import com.academiq.academiq.dto.response.HistorialResponse;
import com.academiq.academiq.dto.response.SolicitudResponse;
import com.academiq.academiq.dto.response.UsuarioResumen;
import com.academiq.academiq.dto.response.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    public SolicitudResponse toResponse(Solicitud s) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .tipo(s.getTipo())
                .descripcion(s.getDescripcion())
                .canal(s.getCanal())
                .prioridad(s.getPrioridad())
                .justificacion(s.getJustificacion())
                .estado(s.getEstado())
                .fechaRegistro(s.getFechaRegistro())
                .observacionCierre(s.getObservacionCierre())
                .solicitante(toUsuarioResumen(s.getSolicitante()))
                .responsable(s.getResponsable() != null ?
                        toUsuarioResumen(s.getResponsable()) : null)
                .build();
    }

    public HistorialResponse toHistorialResponse(HistorialSolicitud h) {
        return HistorialResponse.builder()
                .id(h.getId())
                .accion(h.getAccion())
                .fechaAccion(h.getFechaAccion())
                .observacion(h.getObservacion())
                .usuario(toUsuarioResumen(h.getUsuario()))
                .solicitudId(h.getSolicitud().getId())
                .build();
    }

    public UsuarioResumen toUsuarioResumen(Usuario u) {
        return UsuarioResumen.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .rol(u.getRol())
                .build();
    }

    public UsuarioResponse toUsuarioResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .build();
    }
}