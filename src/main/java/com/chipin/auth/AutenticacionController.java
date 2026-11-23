package com.chipin.auth;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tokens")
public class AutenticacionController {
    private final AutenticacionService autenticacionService;

    @Autowired
    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody @Valid AutenticacionDTO credenciales)
            throws NoAutenticadoException {
        String token = autenticacionService.login(credenciales);
        return ResponseEntity.noContent()
                .headers(headers -> headers.setBearerAuth(token))
                .build();
    }
}
