package com.chipin.grupo;

public class CodigoInvitacionNoValidoException extends Exception {
    private final String codigoInvitacion;

    public CodigoInvitacionNoValidoException(String codigoInvitacion) {
        super("El código de invitación " + codigoInvitacion + " no es válido.");
        this.codigoInvitacion = codigoInvitacion;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }
}
