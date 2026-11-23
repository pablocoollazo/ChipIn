package com.chipin.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final PrivateKey privateKey;
    private final JwtParser parser;
    private final Duration durationToken;

    public JwtService(KeyPair keyPair, @Value("${chipin.jwt.access-ttl}") Duration durationToken) {
        this.privateKey = keyPair.getPrivate();
        this.parser = Jwts.parser().verifyWith(keyPair.getPublic()).build();
        this.durationToken = durationToken;
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(durationToken);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(privateKey)
                .compact();
    }

    public Long getUserId(String token) throws NoAutenticadoException {
        try {
            return Long.valueOf(parser.parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new NoAutenticadoException("Token inválido o expirado");
        }
    }
}
