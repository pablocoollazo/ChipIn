package com.chipin.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UsuarioDTO(Long id,
                         @NotBlank @Size(max = 100) String nombre,
                         @NotBlank @Email @Size(max = 150) String email,
                         @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Size(max = 72) String password,
                         Instant fechaRegistro) {
    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                null,
                usuario.getFechaRegistro()
        );
    }
}
