package com.chipin.grupo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnirseGrupoDTO(
        @NotBlank @Size(max = 12) String codigoInvitacion,
        @NotNull Long usuarioId
) {

}
