package com.chipin.usuario;

import com.chipin.auth.SinPermisoException;
import com.chipin.grupo.GrupoRepository;
import com.chipin.grupo.MiembroGrupoRepository;
import com.chipin.patch.JsonPatch;
import com.chipin.patch.OperacionPatch;
import com.chipin.patch.PatchInvalidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@NullMarked
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GrupoRepository grupoRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    /** Campos que un PATCH puede tocar. id y fechaRegistro los pone el servidor. */
    private static final Set<String> CAMPOS_MODIFICABLES = Set.of("/nombre", "/email", "/password");

    @Autowired
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            GrupoRepository grupoRepository,
            MiembroGrupoRepository miembroGrupoRepository,
            JsonMapper jsonMapper,
            Validator validator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.grupoRepository = grupoRepository;
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
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

    public UsuarioDTO patch(Long id, List<OperacionPatch> operaciones, Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, PatchInvalidoException, EmailDuplicadoException {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        if (!id.equals(usuarioActual)) {
            throw new SinPermisoException("Solo puedes modificar tu propio usuario");
        }
        for (OperacionPatch operacion : operaciones) {
            if (operacion.path() == null) {
                throw new PatchInvalidoException("Falta la ruta (path)");
            }
            if (!CAMPOS_MODIFICABLES.contains(operacion.path())) {
                throw new PatchInvalidoException("No se puede modificar " + operacion.path()
                        + ". Campos modificables: /nombre, /email, /password");
            }
        }

        // El JSON del usuario no lleva password (WRITE_ONLY): solo aparece si el patch la añade
        ObjectNode documento = jsonMapper.valueToTree(UsuarioDTO.from(usuario));
        UsuarioDTO resultado;
        try {
            resultado = jsonMapper.treeToValue(JsonPatch.aplicar(operaciones, documento), UsuarioDTO.class);
        } catch (JacksonException e) {
            throw new PatchInvalidoException(
                    "El resultado del patch no es un usuario válido: " + e.getOriginalMessage());
        }
        validar(resultado);

        if (usuarioRepository.existsByEmailAndIdNot(resultado.email(), id)) {
            throw new EmailDuplicadoException(resultado.email());
        }

        usuario.setNombre(resultado.nombre());
        usuario.setEmail(resultado.email());
        if (resultado.password() != null) {
            usuario.setPasswordHash(passwordEncoder.encode(resultado.password()));
        }
        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }

    /**
     * Aplica a mano las validaciones del DTO, porque en un PATCH no hay @Valid sobre el body.
     * La password solo se valida si el patch la trae; si no, se mantiene la actual.
     */
    private void validar(UsuarioDTO usuario) throws PatchInvalidoException {
        Set<ConstraintViolation<UsuarioDTO>> errores = new HashSet<>();
        errores.addAll(validator.validateProperty(usuario, "nombre"));
        errores.addAll(validator.validateProperty(usuario, "email"));
        if (usuario.password() != null) {
            errores.addAll(validator.validateProperty(usuario, "password"));
        }
        if (!errores.isEmpty()) {
            throw new PatchInvalidoException(errores.stream()
                    .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
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
