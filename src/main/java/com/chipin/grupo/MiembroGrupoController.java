package com.chipin.grupo;

import com.chipin.usuario.UsuarioNoEncontradoException;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@NullMarked
@RestController
@RequestMapping("miembros")
public class MiembroGrupoController {
    private final GrupoService grupoService;

    @Autowired
    public MiembroGrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MiembroDTO> join(@RequestBody @Valid UnirseGrupoDTO peticion)
            throws CodigoInvitacionNoValidoException, GrupoNoAbiertoException,
            YaEsMiembroException, UsuarioNoEncontradoException {
        MiembroDTO miembro = grupoService.join(peticion.codigoInvitacion(), peticion.usuarioId());
        return ResponseEntity
                .created(MvcUriComponentsBuilder.fromMethodName(GrupoController.class, "getMiembros", miembro.grupoId()).build().toUri())
                .body(miembro);
    }
}
