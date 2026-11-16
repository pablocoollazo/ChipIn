package com.chipin.auth;

import com.chipin.usuario.UsuarioRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UsuarioActualResolver implements HandlerMethodArgumentResolver {

    private final UsuarioRepository usuarioRepository;
    private static final String CABECERA = "X-Usuario-Id";

    public UsuarioActualResolver(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UsuarioActual.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public @Nullable Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory)
            throws Exception {
        String valorCabecera = webRequest.getHeader(CABECERA);
        if (valorCabecera == null || valorCabecera.isBlank()) {
            throw new NoAutenticadoException("Falta la cabecera X-Usuario-Id");
        }
        Long id;
        try {
            id = Long.valueOf(valorCabecera);
        } catch (NumberFormatException e) {
            throw new NoAutenticadoException("El ID de usuario no es válido");
        }

        if (!usuarioRepository.existsById(id)) {
            throw new NoAutenticadoException("El usuario con ID " + id + " no existe");
        }

        return id;
    }
}
