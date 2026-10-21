package com.chipin.grupo;

import java.math.BigDecimal;
import java.time.Instant;

// Record para hacer un DTO de MiembroGrupo, para enviar la información de un miembro sin enviar el objeto completo
public record MiembroDTO(Long usuarioId,
                         String nombre,
                         BigDecimal cuotaAsignada,
                         Instant fechaUnion) {

    // Metodo para convertir un objeto MiembroGrupo a MiembroDTO
    public static MiembroDTO from (MiembroGrupo miembro) {
        return new MiembroDTO(
                miembro.getUsuario().getId(),
                miembro.getUsuario().getNombre(),
                miembro.getCuotaAsignada(),
                miembro.getFechaUnion()
        );
    }
}
