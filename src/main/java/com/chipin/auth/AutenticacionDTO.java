package com.chipin.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AutenticacionDTO(
        @NotBlank @Email String email, @NotBlank String password) {}
