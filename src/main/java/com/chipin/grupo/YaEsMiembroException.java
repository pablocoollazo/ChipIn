package com.chipin.grupo;

public class YaEsMiembroException extends Exception {
    private final Long grupoId;
    private final Long usuarioId;

    public YaEsMiembroException(Long grupoId, Long usuarioId) {
        super("El usuario de id " + usuarioId + " no puede unirse al grupo "
                + grupoId + " ya que ya forma parte del mismo");
        this.grupoId = grupoId;
        this.usuarioId = usuarioId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getGrupoId() {
        return grupoId;
    }
}
