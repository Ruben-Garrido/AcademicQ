package com.academiq.academiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResumenResponse {
    private String resumen;
    private String modelo;
    private String advertencia;
}
