package com.chipin.usuario;

import com.chipin.auth.SinPermisoException;
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
        return usuarioRepository
                .findById(id)
                .map(UsuarioDTO::from)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public UsuarioDTO create(UsuarioDTO usuario) throws EmailDuplicadoException {
        if (usuarioRepository.existsByEmail(usuario.email())) {
            throw new EmailDuplicadoException(usuario.email());
        }
        return UsuarioDTO.from(usuarioRepository.save(Usuario.from(usuario, passwordEncoder)));
    }

    public UsuarioDTO update(Long id, UsuarioDTO dto, Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, EmailDuplicadoException {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        if (!id.equals(usuarioActual)) {
            throw new SinPermisoException("Solo puedes modificar tu propio usuario");
        }

        if (usuarioRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new EmailDuplicadoException(dto.email());
        }

        usuario.setEmail(dto.email());
        usuario.setNombre(dto.nombre());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }
}
