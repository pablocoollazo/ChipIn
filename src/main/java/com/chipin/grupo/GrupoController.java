package com.chipin.grupo;

import com.chipin.usuario.UsuarioNoEncontradoException;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;

// Controller creado para evitar que, si nos uniésemos por /grupos/{id}/miembros, tuviésemos que buscar el id
@NullMarked
@RestController
@RequestMapping("grupos")
public class GrupoController {
    private final GrupoService grupoService;

    @Autowired
    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    // Endpoint para el get de /grupos/{id}. Con el id proporcionado, se pasa al service y con el metodo
    // ok creamos la respuesta con código 200 y con lo pasado por argumentos en el body
    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GrupoDTO> get(@PathVariable("id") Long grupoId) throws GrupoNoEncontradoException {
        return ResponseEntity.ok(grupoService.get(grupoId));
    }

    // Endpoint para la creación de un grupo
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GrupoDTO> create(@RequestBody @Valid GrupoDTO grupo) throws UsuarioNoEncontradoException {
        GrupoDTO grupoCreado = grupoService.create(grupo);
        // MvcUriComponentsBuilder usa fromMethodName para buscar en GrupoController el metodo get para crear su URL.
        // Además, rellena el 1er parámetro (el id) con el id proporcionado
        return ResponseEntity.created(MvcUriComponentsBuilder.
                        fromMethodName(GrupoController.class, "get", grupoCreado.id()).build()
                        .toUri()).body(grupoCreado);
    }

    // Endpoint que nos permite ver los miembros del grupo con id {id}
    @GetMapping(path = "{id}/miembros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MiembroDTO>> getMiembros(@PathVariable("id") Long id) throws GrupoNoEncontradoException {
        return ResponseEntity.ok(grupoService.getMiembros(id));
    }
}
