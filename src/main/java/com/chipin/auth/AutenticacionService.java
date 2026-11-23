package com.chipin.auth;

import com.chipin.usuario.Usuario;
import com.chipin.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService {
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public AutenticacionService(
            UsuarioRepository usuarioRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(AutenticacionDTO autenticacionDTO) throws NoAutenticadoException {
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(autenticacionDTO.email(), autenticacionDTO.password());
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (BadCredentialsException e) {
            throw new NoAutenticadoException("Email o contraseña incorrectos");
        }
        Usuario usuario = usuarioRepository
                .findByEmail(autenticacionDTO.email())
                .orElseThrow(() -> new NoAutenticadoException("Email o contraseña incorrectos"));
        return jwtService.generateToken(usuario.getId());
    }
}
