package com.chipin.grupo;

public class GrupoNoEncontradoException extends Exception {
    // Almacenamos el id del grupo que se quiso buscar
    private final Long grupoId;

    public GrupoNoEncontradoException(Long id) {
        super("No se ha encontrado el grupo con id: " + id);
        this.grupoId = id;
    }

    public Long getGrupoId() {
        return grupoId;
    }
}
