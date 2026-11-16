package com.chipin.auth;

public class NoAutenticadoException extends Exception {
    public NoAutenticadoException(String message) {
        super(message);
    }
}
