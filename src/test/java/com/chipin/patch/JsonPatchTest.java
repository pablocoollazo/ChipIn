package com.chipin.patch;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonPatchTest {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static ObjectNode documento() {
        ObjectNode documento = MAPPER.createObjectNode();
        documento.put("nombre", "John");
        documento.put("email", "john@test.com");
        return documento;
    }

    private static OperacionPatch op(TipoOperacionPatch tipo, String path, String valor) {
        return new OperacionPatch(tipo, path, valor == null ? null : StringNode.valueOf(valor));
    }

    @Test
    void replaceSustituyeElCampo() throws Exception {
        ObjectNode resultado = JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.REPLACE, "/nombre", "Juan")), documento());

        assertEquals("Juan", resultado.get("nombre").asString());
        assertEquals("john@test.com", resultado.get("email").asString());
    }

    @Test
    void addCreaElCampoSiNoExiste() throws Exception {
        ObjectNode resultado = JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.ADD, "/password", "secreta")), documento());

        assertEquals("secreta", resultado.get("password").asString());
    }

    @Test
    void removeQuitaElCampo() throws Exception {
        ObjectNode resultado = JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.REMOVE, "/email", null)), documento());

        assertFalse(resultado.has("email"));
    }

    @Test
    void lasOperacionesSeAplicanEnOrden() throws Exception {
        ObjectNode resultado = JsonPatch.aplicar(List.of(
                op(TipoOperacionPatch.REPLACE, "/nombre", "Juan"),
                op(TipoOperacionPatch.REPLACE, "/nombre", "Juanito")), documento());

        assertEquals("Juanito", resultado.get("nombre").asString());
    }

    @Test
    void replaceDeCampoInexistenteFalla() {
        assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.REPLACE, "/telefono", "600")), documento()));
    }

    @Test
    void removeDeCampoInexistenteFalla() {
        assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.REMOVE, "/telefono", null)), documento()));
    }

    @Test
    void addOReplaceSinValueFalla() {
        assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.ADD, "/nombre", null)), documento()));
        assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                List.of(op(TipoOperacionPatch.REPLACE, "/nombre", null)), documento()));
    }

    @Test
    void rutasNoValidasFallan() {
        for (String path : new String[]{null, "", "/", "nombre", "/a/b"}) {
            assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                    List.of(op(TipoOperacionPatch.REPLACE, path, "x")), documento()), "Ruta: " + path);
        }
    }

    @Test
    void faltaOpFalla() {
        assertThrows(PatchInvalidoException.class, () -> JsonPatch.aplicar(
                List.of(op(null, "/nombre", "x")), documento()));
    }

    @Test
    void opSeLeeSinDistinguirMayusculas() {
        OperacionPatch operacion = MAPPER.readValue(
                "{\"op\":\"replace\",\"path\":\"/nombre\",\"value\":\"Juan\"}", OperacionPatch.class);

        assertEquals(TipoOperacionPatch.REPLACE, operacion.op());
    }
}
