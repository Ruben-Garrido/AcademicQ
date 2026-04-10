package com.academiq.academiq.dto.response;

import com.academiq.academiq.domain.enums.Rol;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UsuarioResponse {
    private UUID id;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;
}