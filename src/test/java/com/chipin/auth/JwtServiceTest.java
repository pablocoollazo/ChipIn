package com.chipin.auth;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    private static KeyPair generarClaves() throws Exception {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        return generador.generateKeyPair();
    }

    @Test
    void tokenGeneradoDevuelveElMismoUsuario() throws Exception {
        JwtService servicio = new JwtService(generarClaves(), QUINCE_MINUTOS);

        String token = servicio.generateToken(42L);

        assertEquals(42L, servicio.getUserId(token));
    }

    @Test
    void tokenManipuladoSeRechaza() throws Exception {
        JwtService servicio = new JwtService(generarClaves(), QUINCE_MINUTOS);
        String token = servicio.generateToken(42L);

        // Cambia un carácter en mitad del payload (entre el primer y el segundo punto)
        int inicioPayload = token.indexOf('.') + 1;
        int finPayload = token.lastIndexOf('.');
        int posicion = (inicioPayload + finPayload) / 2;
        char original = token.charAt(posicion);
        StringBuilder manipulado = new StringBuilder(token);
        manipulado.setCharAt(posicion, original == 'A' ? 'B' : 'A');

        assertThrows(NoAutenticadoException.class, () -> servicio.getUserId(manipulado.toString()));
    }

    @Test
    void tokenCaducadoSeRechaza() throws Exception {
        JwtService servicio = new JwtService(generarClaves(), Duration.ofMinutes(-1));
        String token = servicio.generateToken(42L);

        assertThrows(NoAutenticadoException.class, () -> servicio.getUserId(token));
    }

    @Test
    void tokenFirmadoConOtraClaveSeRechaza() throws Exception {
        JwtService emisor = new JwtService(generarClaves(), QUINCE_MINUTOS);
        JwtService otro = new JwtService(generarClaves(), QUINCE_MINUTOS);
        String token = emisor.generateToken(42L);

        assertThrows(NoAutenticadoException.class, () -> otro.getUserId(token));
    }

    @Test
    void tokenMalFormadoOVacioSeRechaza() throws Exception {
        JwtService servicio = new JwtService(generarClaves(), QUINCE_MINUTOS);

        assertThrows(NoAutenticadoException.class, () -> servicio.getUserId("esto-no-es-un-jwt"));
        assertThrows(NoAutenticadoException.class, () -> servicio.getUserId(null));
    }
}
