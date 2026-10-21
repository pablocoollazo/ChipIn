package com.chipin.usuario;

public class EmailDuplicadoException extends Exception {
    private final String email;
    public EmailDuplicadoException(String email){
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}
