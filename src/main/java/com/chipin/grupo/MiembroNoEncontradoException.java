package com.chipin.grupo;

public class MiembroNoEncontradoException extends Exception {
    private final Long grupoId;
    private final Long usuarioId;

    public MiembroNoEncontradoException(Long grupoId, Long usuarioId) {
        super("El usuario de id " + usuarioId + " no es miembro del grupo " + grupoId);
        this.grupoId = grupoId;
        this.usuarioId = usuarioId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}