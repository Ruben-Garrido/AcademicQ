package com.academiq.academiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//Son objetos que separan lo que el cliente envía/recibe de las entidades internas
@Data
public class AtenderRequest {

    @NotBlank(message = "La observación es obligatoria")
    @Size(min = 5, max = 1000)
    private String observacion;
}