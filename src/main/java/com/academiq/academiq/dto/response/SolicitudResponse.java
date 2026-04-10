package com.academiq.academiq.dto.response;

import com.academiq.academiq.domain.enums.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SolicitudResponse {
    private UUID id;
    private TipoSolicitud tipo;
    private String descripcion;
    private CanalOrigen canal;
    private Prioridad prioridad;
    private String justificacion;
    private EstadoSolicitud estado;
    private LocalDateTime fechaRegistro;
    private String observacionCierre;
    private UsuarioResumen solicitante;
    private UsuarioResumen responsable;
}