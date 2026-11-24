package com.chipin.patch;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoOperacionPatch {
    ADD,
    REMOVE,
    REPLACE;

    @JsonCreator
    public static TipoOperacionPatch desdeTexto(String texto) {
        return valueOf(texto.toUpperCase());
    }
}
