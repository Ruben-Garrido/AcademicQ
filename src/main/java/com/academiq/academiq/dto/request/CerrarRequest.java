package com.academiq.academiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CerrarRequest {

    @NotBlank(message = "La observación de cierre es obligatoria")
    @Size(min = 5, max = 1000)
    private String observacionCierre;
}