package com.chipin.config;

import com.chipin.auth.JwtAuthenticationEntryPoint;
import com.chipin.auth.JwtAuthenticationFilter;
import com.chipin.auth.JwtService;
import com.chipin.usuario.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Configuration
public class SeguridadConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Para el login: el "username" de Spring Security es el email.
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return email -> usuarioRepository
                .findByEmail(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getPasswordHash())
                        .authorities(List.of())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    // Valida email + contraseña en el login (userDetailsService + BCrypt)
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(userDetailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(proveedor);
    }

    // Par RSA para firmar los JWT, generado en memoria al arrancar: no hay ningun secreto en el repo.
    // Al reiniciar la app los tokens emitidos dejan de ser validos (aceptable en desarrollo).
    @Bean
    public KeyPair keyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        return generador.generateKeyPair();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtService jwtService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/usuarios", "/tokens")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
