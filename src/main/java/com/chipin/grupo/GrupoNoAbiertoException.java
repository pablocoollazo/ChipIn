package com.chipin.grupo;

public class GrupoNoAbiertoException extends Exception {
    private final Long grupoId;

    public GrupoNoAbiertoException(Long grupoId) {
        super("El grupo, con id " + grupoId + ", no está abierto.");
        this.grupoId = grupoId;
    }

    public Long getGrupoId() {
        return grupoId;
    }
}
