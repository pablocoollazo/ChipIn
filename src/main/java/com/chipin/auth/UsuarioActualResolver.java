package com.chipin.auth;

import com.chipin.usuario.UsuarioRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UsuarioActualResolver implements HandlerMethodArgumentResolver {

    private final UsuarioRepository usuarioRepository;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new NoAutenticadoException("No hay usuario autenticado");
        }
        if (!(authentication.getPrincipal() instanceof Long id)) {
            throw new NoAutenticadoException("El token no contiene un usuario válido");
        }
        if (!usuarioRepository.existsById(id)) {
            throw new NoAutenticadoException("El usuario con ID " + id + " no existe");
        }

        return id;
    }
}
