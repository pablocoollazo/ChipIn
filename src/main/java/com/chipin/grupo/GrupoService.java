package com.chipin.grupo;

import com.chipin.usuario.Usuario;
import com.chipin.usuario.UsuarioNoEncontradoException;
import com.chipin.usuario.UsuarioRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@NullMarked
public class GrupoService {
    // Vamos a especificar el alfabeto para eliminar I/1 y 0/O
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 8;
    // SecureRandom para generar el código único de forma segura
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GrupoRepository grupoRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final UsuarioRepository usuarioRepository;

    // Constructor del servicio
    @Autowired
    public GrupoService(GrupoRepository grupoRepository,
                        MiembroGrupoRepository miembroGrupoRepository,
                        UsuarioRepository usuarioRepository) {
        this.grupoRepository = grupoRepository;
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Buscamos un usuario por el id
    private Usuario buscarUsuario(Long id) throws UsuarioNoEncontradoException {
        return usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    // Función que genera el código único de invitación
    private String generarCodigoUnico() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                sb.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (grupoRepository.existsByCodigoInvitacion(codigo));
        return codigo;
    }

    // Get de un DTO del grupo a partir del id del mismo
    @Transactional(readOnly = true)
    public GrupoDTO get(Long idGrupo) throws GrupoNoEncontradoException {
        // El objeto grupo devuelto por findById se introduce como argmento en el from de GrupoDTO
        return grupoRepository.findById(idGrupo).map(GrupoDTO::from)
                .orElseThrow(() -> new GrupoNoEncontradoException(idGrupo));
    }

    // Función para la creación del grupo
    @Transactional(rollbackFor = Exception.class)
    public GrupoDTO create(GrupoDTO dto) throws UsuarioNoEncontradoException {
        Usuario creador = buscarUsuario(dto.creadorId());

        Grupo grupo = new Grupo();
        grupo.setNombre(dto.nombre());
        grupo.setDescripcion(dto.descripcion());
        grupo.setImporteObjetivo(dto.importeObjetivo());
        grupo.setFechaLimite(dto.fechaLimite());
        grupo.setCreador(creador);
        grupo.setCodigoInvitacion(generarCodigoUnico());

        // Guardamos el grupo en la BD para asignarle un id y unimos ya el primer miembro (el creador)
        grupo = grupoRepository.save(grupo);
        miembroGrupoRepository.save(new MiembroGrupo(grupo, creador));
        return GrupoDTO.from(grupo);
    }

    // Función con la lógica para unirse a un grupo
    @Transactional(rollbackFor = Exception.class)
    public MiembroDTO join(String codigoInvitacion, Long usuarioId)
            throws CodigoInvitacionNoValidoException, GrupoNoAbiertoException,
            YaEsMiembroException, UsuarioNoEncontradoException {
        Grupo grupo = grupoRepository.findByCodigoInvitacion(codigoInvitacion)
                .orElseThrow(() -> new CodigoInvitacionNoValidoException(codigoInvitacion));

        if(grupo.getEstado() != EstadoGrupo.ABIERTO) {
            throw new GrupoNoAbiertoException(grupo.getId());
        }

        MiembroGrupoId miembroGrupoId = new MiembroGrupoId(grupo.getId(), usuarioId);
        if(miembroGrupoRepository.existsById(miembroGrupoId)) {
            throw new YaEsMiembroException(grupo.getId(), usuarioId);
        }

        Usuario usuario = buscarUsuario(usuarioId);
        MiembroGrupo miembroGrupo = new MiembroGrupo(grupo, usuario);
        miembroGrupo = miembroGrupoRepository.save(miembroGrupo);
        return MiembroDTO.from(miembroGrupo);
    }

    // Función para recuperar los miembros ed un grupo, a partir del id de este
    @Transactional(readOnly = true)
    public List<MiembroDTO> getMiembros(Long grupoId) throws GrupoNoEncontradoException {
        if(!grupoRepository.existsById(grupoId)) {
            throw new GrupoNoEncontradoException(grupoId);
        }

        return miembroGrupoRepository.findByGrupoId(grupoId).stream().map(MiembroDTO::from).toList();
    }
}
