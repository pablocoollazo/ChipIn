package com.chipin.usuario;

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
            @RequestParam(value = "sort", required = false, defaultValue = "") List<String> sort){
        return ResponseEntity.ok(usuarioService.get(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(sort.stream()
                                .map(key -> key.startsWith("-") ? Sort.Order.desc(key.substring(1)) : Sort.Order.asc(key))
                                .toList())
                )
        ));
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> get(@PathVariable("id")  Long id) throws UsuarioNoEncontradoException {
        return ResponseEntity.ok(usuarioService.get(id));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UsuarioDTO> create(@RequestBody @Valid UsuarioDTO usuario) throws EmailDuplicadoException {
        UsuarioDTO creado = usuarioService.create(usuario);
        return ResponseEntity
                .created(MvcUriComponentsBuilder.fromMethodName(UsuarioController.class, "get", creado.id()).build().toUri())
                .body(creado);
    }
}
