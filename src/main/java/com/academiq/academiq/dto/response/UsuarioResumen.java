package com.academiq.academiq.dto.response;

import com.academiq.academiq.domain.enums.Rol;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
//solo vamos a responder lo necesario
public class UsuarioResumen {
    private UUID id;
    private String nombre;
    private Rol rol;
}