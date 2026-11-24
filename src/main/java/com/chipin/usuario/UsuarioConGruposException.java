package com.chipin.usuario;

public class UsuarioConGruposException extends Exception {
    private final Long id;

    public UsuarioConGruposException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
