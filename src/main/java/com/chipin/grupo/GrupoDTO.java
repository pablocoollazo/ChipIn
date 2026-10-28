package com.chipin.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// DTO para enviar la información de un grupo sin enviar el objeto
public record GrupoDTO(@JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
                       @NotBlank @Size(max = 100) String nombre,
                       @Size(max = 1000) String descripcion,
                       @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal importeObjetivo,
                       @NotNull Long creadorId,
                       @JsonProperty(access = JsonProperty.Access.READ_ONLY) String codigoInvitacion,
                       EstadoGrupo estado,
                       @JsonProperty(access = JsonProperty.Access.READ_ONLY) @FutureOrPresent LocalDate fechaLimite,
                       Instant fechaCreacion) {

    // Metodo para convertir un objeto Grupo a GrupoDTO
    public static GrupoDTO from (Grupo grupo) {
        return new GrupoDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getImporteObjetivo(),
                grupo.getCreador().getId(),
                grupo.getCodigoInvitacion(),
                grupo.getEstado(),
                grupo.getFechaLimite(),
                grupo.getFechaCreacion()
        );
    }
}
