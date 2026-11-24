package com.chipin.usuario;

import com.chipin.auth.SinPermisoException;
import com.chipin.grupo.GrupoRepository;
import com.chipin.grupo.MiembroGrupoRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@NullMarked
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GrupoRepository grupoRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;

    @Autowired
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            GrupoRepository grupoRepository,
            MiembroGrupoRepository miembroGrupoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.grupoRepository = grupoRepository;
        this.miembroGrupoRepository = miembroGrupoRepository;
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

    @Transactional
    public void delete(Long id, Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, UsuarioConGruposException {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNoEncontradoException(id);
        }

        if (!id.equals(usuarioActual)) {
            throw new SinPermisoException("Solo puedes borrar tu propio usuario");
        }

        if (grupoRepository.existsByCreadorId(id)) {
            throw new UsuarioConGruposException(id);
        }

        miembroGrupoRepository.deleteByUsuarioId(id);
        usuarioRepository.deleteById(id);
    }
}
