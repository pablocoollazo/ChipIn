package com.chipin.config;

import com.chipin.usuario.EmailDuplicadoException;
import com.chipin.usuario.UsuarioController;
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
}
