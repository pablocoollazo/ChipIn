package com.chipin.config;

import com.chipin.grupo.CodigoInvitacionNoValidoException;
import com.chipin.grupo.GrupoNoAbiertoException;
import com.chipin.grupo.GrupoNoEncontradoException;
import com.chipin.grupo.YaEsMiembroException;
import com.chipin.usuario.EmailDuplicadoException;
import com.chipin.usuario.UsuarioNoEncontradoException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@NullMarked
public class ErrorController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(UsuarioNoEncontradoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Usuario no encontrado");
        problemDetail.setDetail("No existe ningún usuario con id " + ex.getId());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class).pathSegment("error", "usuario-no-encontrado").build().toUri());

        return ErrorResponse.builder(ex,  problemDetail).build();
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(EmailDuplicadoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Email duplicado");
        problemDetail.setDetail("Existe usuario con el email " + ex.getEmail());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class).pathSegment("error", "email-duplicado").build().toUri());

        return ErrorResponse.builder(ex,  problemDetail).build();
    }

    @ExceptionHandler(GrupoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(GrupoNoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Grupo no encontrado");
        problemDetail.setDetail("No existe ningún grupo con id " + ex.getGrupoId());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "grupo-no-encontrado").build().toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(CodigoInvitacionNoValidoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(CodigoInvitacionNoValidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Código de invitación inválido");
        problemDetail.setDetail("El código de invitación " + ex.getCodigoInvitacion() + " no es válido.");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "codigo-invitacion-invalido").build().toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(GrupoNoAbiertoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(GrupoNoAbiertoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Grupo no abierto");
        problemDetail.setDetail("El grupo de id " + ex.getGrupoId() + " no está abierto.");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "grupo-no-abierto").build().toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(YaEsMiembroException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(YaEsMiembroException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Usuario ya miembro");
        problemDetail.setDetail("El usuario de id " + ex.getUsuarioId() +
                " ya es miembro del grupo " + ex.getGrupoId() + ".");
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "ya-es-miembro").build().toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }
}
