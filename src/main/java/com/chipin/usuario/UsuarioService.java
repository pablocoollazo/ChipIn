package com.chipin.usuario;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UsuarioDTO> get(PageRequest page) {
        return usuarioRepository.findAll(page).map(UsuarioDTO::from);
    }

    public UsuarioDTO get(Long id) throws UsuarioNoEncontradoException {
        return usuarioRepository.findById(id).map(UsuarioDTO::from).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public UsuarioDTO create(UsuarioDTO usuario) throws EmailDuplicadoException {
        if (usuarioRepository.existsByEmail(usuario.email())) {
            throw new EmailDuplicadoException(usuario.email());
        }
        return UsuarioDTO.from(usuarioRepository.save(Usuario.from(usuario, passwordEncoder)));
    }

}
