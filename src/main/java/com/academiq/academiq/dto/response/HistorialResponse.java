package com.academiq.academiq.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HistorialResponse {
    private UUID id;
    private String accion;
    private LocalDateTime fechaAccion;
    private String observacion;
    private UsuarioResumen usuario;
    private UUID solicitudId;
}