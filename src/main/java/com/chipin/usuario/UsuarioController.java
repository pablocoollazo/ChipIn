package com.chipin.usuario;

import com.chipin.auth.SinPermisoException;
import com.chipin.auth.UsuarioActual;
import com.chipin.patch.OperacionPatch;
import com.chipin.patch.PatchInvalidoException;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;

@NullMarked
@RestController
@RequestMapping("usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UsuarioDTO>> get(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort) {
        return ResponseEntity.ok(usuarioService.get(PageRequest.of(
                page,
                size,
                Sort.by(sort.stream()
                        .map(key -> key.startsWith("-") ? Sort.Order.desc(key.substring(1)) : Sort.Order.asc(key))
                        .toList()))));
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> get(@PathVariable("id") Long id) throws UsuarioNoEncontradoException {
        return ResponseEntity.ok(usuarioService.get(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> create(@RequestBody @Valid UsuarioDTO usuario) throws EmailDuplicadoException {
        UsuarioDTO creado = usuarioService.create(usuario);
        return ResponseEntity.created(
                        MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "get", creado.id())
                                .build()
                                .toUri())
                .body(creado);
    }

    @PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> update(
            @PathVariable("id") Long id, @RequestBody @Valid UsuarioDTO usuarioDTO, @UsuarioActual Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, EmailDuplicadoException {
        return ResponseEntity.ok(usuarioService.update(id, usuarioDTO, usuarioActual));
    }

    @PatchMapping(path = "{id}", consumes = "application/json-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> patch(
            @PathVariable("id") Long id, @RequestBody List<OperacionPatch> operaciones, @UsuarioActual Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, PatchInvalidoException, EmailDuplicadoException {
        return ResponseEntity.ok(usuarioService.patch(id, operaciones, usuarioActual));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @UsuarioActual Long usuarioActual)
            throws UsuarioNoEncontradoException, SinPermisoException, UsuarioConGruposException {
        usuarioService.delete(id, usuarioActual);
        return ResponseEntity.noContent().build();
    }
}
