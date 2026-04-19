package com.academiq.academiq.dto.request;

import com.academiq.academiq.domain.enums.ImpactoAcademico;
import com.academiq.academiq.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClasificarRequest {

    @NotNull(message = "El tipo es obligatorio")
    private TipoSolicitud tipo;

    @NotNull(message = "El impacto académico es obligatorio")
    private ImpactoAcademico impactoAcademico;

    @NotNull(message = "La fecha límite es obligatoria")
    private LocalDate fechaLimite;

    @NotBlank(message = "La justificación es obligatoria")
    @Size(min = 5, max = 500)
    private String justificacion;
}