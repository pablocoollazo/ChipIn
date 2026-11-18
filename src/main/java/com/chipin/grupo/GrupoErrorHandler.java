package com.chipin.grupo;

import com.chipin.config.ErrorController;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestControllerAdvice
@NullMarked
public class GrupoErrorHandler {
    @ExceptionHandler(GrupoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(GrupoNoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Grupo no encontrado");
        problemDetail.setDetail("No existe ningún grupo con id " + ex.getGrupoId());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "grupo-no-encontrado")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(CodigoInvitacionNoValidoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(CodigoInvitacionNoValidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Código de invitación inválido");
        problemDetail.setDetail("El código de invitación " + ex.getCodigoInvitacion() + " no es válido.");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "codigo-invitacion-invalido")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(GrupoNoAbiertoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(GrupoNoAbiertoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Grupo no abierto");
        problemDetail.setDetail("El grupo de id " + ex.getGrupoId() + " no está abierto.");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "grupo-no-abierto")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(YaEsMiembroException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(YaEsMiembroException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Usuario ya miembro");
        problemDetail.setDetail(
                "El usuario de id " + ex.getUsuarioId() + " ya es miembro del grupo " + ex.getGrupoId() + ".");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "ya-es-miembro")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }
}
