package com.academiq.academiq.dto.response;

import com.academiq.academiq.domain.enums.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioridadResponse {
    private Prioridad prioridad;
    private String mensaje;
}
