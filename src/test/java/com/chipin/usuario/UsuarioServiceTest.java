package com.chipin.usuario;

import com.chipin.auth.SinPermisoException;
import com.chipin.grupo.MiembroGrupoId;
import com.chipin.grupo.MiembroGrupoRepository;
import com.chipin.patch.OperacionPatch;
import com.chipin.patch.PatchInvalidoException;
import com.chipin.patch.TipoOperacionPatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de integración contra el Postgres local (docker compose up -d).
 * Parten de los datos de src/main/resources/sql: John (1) es creador del grupo 1 y miembro;
 * Jane (2) es solo miembro; Alice (3) no está en ningún grupo. Contraseña de los tres: 1234.
 * Cada test va en una transacción que se deshace al terminar, así que no se pisan entre ellos.
 */
@SpringBootTest
@Transactional
class UsuarioServiceTest {
    private static final Long JOHN = 1L;
    private static final Long JANE = 2L;
    private static final Long ALICE = 3L;
    private static final Long INEXISTENTE = 999L;

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private MiembroGrupoRepository miembroGrupoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static UsuarioDTO datos(String nombre, String email, String password) {
        return new UsuarioDTO(null, nombre, email, password, null);
    }

    // --- get ---

    @Test
    void getDevuelveElUsuarioSinPassword() throws Exception {
        UsuarioDTO usuario = usuarioService.get(JOHN);

        assertEquals("john.doe@example.com", usuario.email());
        assertNull(usuario.password());
    }

    @Test
    void getDeUsuarioInexistenteLanza404() {
        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.get(INEXISTENTE));
    }

    // --- create ---

    @Test
    void createGuardaElUsuarioConLaPasswordHasheada() throws Exception {
        UsuarioDTO creado = usuarioService.create(datos("Luis", "luis@test.com", "secreta"));

        assertNotNull(creado.id());
        assertNotNull(creado.fechaRegistro());
        Usuario guardado = usuarioRepository.findById(creado.id()).orElseThrow();
        assertTrue(passwordEncoder.matches("secreta", guardado.getPasswordHash()));
    }

    @Test
    void createConEmailDuplicadoLanza409() {
        assertThrows(EmailDuplicadoException.class,
                () -> usuarioService.create(datos("Otro John", "john.doe@example.com", "1234")));
    }

    // --- update ---

    @Test
    void updateSustituyeNombreEmailYPassword() throws Exception {
        UsuarioDTO actualizado = usuarioService.update(
                JOHN, datos("John Nuevo", "john.nuevo@example.com", "nueva"), JOHN);

        assertEquals(JOHN, actualizado.id());
        assertEquals("John Nuevo", actualizado.nombre());
        assertEquals("john.nuevo@example.com", actualizado.email());
        Usuario guardado = usuarioRepository.findById(JOHN).orElseThrow();
        assertTrue(passwordEncoder.matches("nueva", guardado.getPasswordHash()));
    }

    @Test
    void updateManteniendoSuPropioEmailNoEsDuplicado() throws Exception {
        UsuarioDTO actualizado = usuarioService.update(
                JOHN, datos("John Doe", "john.doe@example.com", "1234"), JOHN);

        assertEquals("john.doe@example.com", actualizado.email());
    }

    @Test
    void updateDeOtroUsuarioLanza403() {
        assertThrows(SinPermisoException.class, () -> usuarioService.update(
                JANE, datos("Jane", "jane.smith@example.com", "1234"), JOHN));
    }

    @Test
    void updateDeUsuarioInexistenteLanza404() {
        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.update(
                INEXISTENTE, datos("Nadie", "nadie@test.com", "1234"), INEXISTENTE));
    }

    @Test
    void updateConEmailDeOtroUsuarioLanza409() {
        assertThrows(EmailDuplicadoException.class, () -> usuarioService.update(
                JOHN, datos("John Doe", "jane.smith@example.com", "1234"), JOHN));
    }

    // --- patch ---

    private static OperacionPatch replace(String path, String valor) {
        return new OperacionPatch(TipoOperacionPatch.REPLACE, path, StringNode.valueOf(valor));
    }

    private static OperacionPatch add(String path, String valor) {
        return new OperacionPatch(TipoOperacionPatch.ADD, path, StringNode.valueOf(valor));
    }

    @Test
    void patchCambiaSoloElCampoIndicadoYMantieneLaPassword() throws Exception {
        String hashAntes = usuarioRepository.findById(JOHN).orElseThrow().getPasswordHash();

        UsuarioDTO resultado = usuarioService.patch(JOHN, List.of(replace("/nombre", "Johnny")), JOHN);

        assertEquals("Johnny", resultado.nombre());
        assertEquals("john.doe@example.com", resultado.email());
        assertEquals(hashAntes, usuarioRepository.findById(JOHN).orElseThrow().getPasswordHash());
    }

    @Test
    void patchConPasswordLaGuardaHasheada() throws Exception {
        usuarioService.patch(JOHN, List.of(add("/password", "nueva")), JOHN);

        assertTrue(passwordEncoder.matches("nueva", usuarioRepository.findById(JOHN).orElseThrow().getPasswordHash()));
    }

    @Test
    void patchDeIdOFechaRegistroLanza400() {
        assertThrows(PatchInvalidoException.class,
                () -> usuarioService.patch(JOHN, List.of(replace("/id", "5")), JOHN));
        assertThrows(PatchInvalidoException.class,
                () -> usuarioService.patch(JOHN, List.of(replace("/fechaRegistro", "2020-01-01T00:00:00Z")), JOHN));
    }

    @Test
    void patchQueDejaElUsuarioInvalidoLanza400() {
        assertThrows(PatchInvalidoException.class,
                () -> usuarioService.patch(JOHN, List.of(replace("/email", "noesunemail")), JOHN));
        assertThrows(PatchInvalidoException.class, () -> usuarioService.patch(
                JOHN, List.of(new OperacionPatch(TipoOperacionPatch.REMOVE, "/nombre", null)), JOHN));
    }

    @Test
    void patchConEmailDeOtroUsuarioLanza409() {
        assertThrows(EmailDuplicadoException.class,
                () -> usuarioService.patch(JOHN, List.of(replace("/email", "jane.smith@example.com")), JOHN));
    }

    @Test
    void patchDeOtroUsuarioLanza403() {
        assertThrows(SinPermisoException.class,
                () -> usuarioService.patch(JANE, List.of(replace("/nombre", "X")), JOHN));
    }

    @Test
    void patchDeUsuarioInexistenteLanza404() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.patch(INEXISTENTE, List.of(replace("/nombre", "X")), INEXISTENTE));
    }

    // --- delete ---

    @Test
    void deleteDeUsuarioSinGruposLoBorra() throws Exception {
        usuarioService.delete(ALICE, ALICE);

        assertFalse(usuarioRepository.existsById(ALICE));
    }

    @Test
    void deleteDeUsuarioSoloMiembroBorraTambienSusMembresias() throws Exception {
        assertTrue(miembroGrupoRepository.existsById(new MiembroGrupoId(1L, JANE)));

        usuarioService.delete(JANE, JANE);

        assertFalse(usuarioRepository.existsById(JANE));
        assertFalse(miembroGrupoRepository.existsById(new MiembroGrupoId(1L, JANE)));
        // Los demás miembros del grupo no se tocan
        assertTrue(miembroGrupoRepository.existsById(new MiembroGrupoId(1L, JOHN)));
    }

    @Test
    void deleteDeCreadorDeGrupoLanza409YNoBorraNada() {
        assertThrows(UsuarioConGruposException.class, () -> usuarioService.delete(JOHN, JOHN));

        assertTrue(usuarioRepository.existsById(JOHN));
        assertTrue(miembroGrupoRepository.existsById(new MiembroGrupoId(1L, JOHN)));
    }

    @Test
    void deleteDeOtroUsuarioLanza403() {
        assertThrows(SinPermisoException.class, () -> usuarioService.delete(JANE, JOHN));

        assertTrue(usuarioRepository.existsById(JANE));
    }

    @Test
    void deleteDeUsuarioInexistenteLanza404() {
        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.delete(INEXISTENTE, INEXISTENTE));
    }
}
