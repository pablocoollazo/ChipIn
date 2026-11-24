package com.chipin.config;

import com.chipin.auth.NoAutenticadoException;
import com.chipin.auth.SinPermisoException;
import com.chipin.patch.PatchInvalidoException;
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

    @ExceptionHandler(NoAutenticadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handle(NoAutenticadoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("No autenticado");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "no-autenticado")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(SinPermisoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(SinPermisoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Sin permiso");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "sin-permiso")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }

    @ExceptionHandler(PatchInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(PatchInvalidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Patch no válido");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setType(MvcUriComponentsBuilder.fromController(ErrorController.class)
                .pathSegment("error", "patch-invalido")
                .build()
                .toUri());
        return ErrorResponse.builder(ex, problemDetail).build();
    }
}
