package com.chipin.usuario;

public class UsuarioNoEncontradoException extends Exception {
    private final Long id;
    public UsuarioNoEncontradoException(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
}
