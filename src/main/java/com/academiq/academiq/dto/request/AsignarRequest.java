package com.academiq.academiq.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;
//Son objetos que separan lo que el cliente envía/recibe de las entidades internas
@Data
public class AsignarRequest {

    @NotNull(message = "El ID del responsable es obligatorio")
    private UUID responsableId;
}