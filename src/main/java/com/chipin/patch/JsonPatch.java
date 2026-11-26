package com.chipin.patch;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Aplica un JSON Patch (RFC 6902) sobre un objeto JSON plano.
 * Solo admite add, remove y replace sobre campos de primer nivel ("/nombre"), que es lo que
 * necesitan los recursos de ChipIn. Rutas anidadas, arrays, move, copy y test no se admiten.
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    public static ObjectNode aplicar(List<OperacionPatch> operaciones, ObjectNode documento)
            throws PatchInvalidoException {
        for (OperacionPatch operacion : operaciones) {
            if (operacion.op() == null) {
                throw new PatchInvalidoException("Falta la operación (op)");
            }
            String campo = campo(operacion.path());

            switch (operacion.op()) {
                case ADD -> documento.set(campo, valor(operacion));
                case REPLACE -> {
                    comprobarQueExiste(documento, campo);
                    documento.set(campo, valor(operacion));
                }
                case REMOVE -> {
                    comprobarQueExiste(documento, campo);
                    documento.remove(campo);
                }
            }
        }
        return documento;
    }

    /** "/nombre" → "nombre". Rechaza rutas vacías o anidadas ("/a/b"). */
    private static String campo(String path) throws PatchInvalidoException {
        if (path == null || !path.startsWith("/") || path.length() == 1 || path.indexOf('/', 1) != -1) {
            throw new PatchInvalidoException("Ruta no válida: " + path + ". Solo se admiten campos de primer nivel, como /nombre");
        }
        return path.substring(1);
    }

    private static void comprobarQueExiste(ObjectNode documento, String campo) throws PatchInvalidoException {
        if (!documento.has(campo)) {
            throw new PatchInvalidoException("El campo /" + campo + " no existe");
        }
    }

    /** add y replace necesitan "value" (puede ser null en JSON, pero tiene que venir). */
    private static JsonNode valor(OperacionPatch operacion) throws PatchInvalidoException {
        if (operacion.value() == null) {
            throw new PatchInvalidoException("Falta value en la operación " + operacion.op().name().toLowerCase() + " de " + operacion.path());
        }
        return operacion.value();
    }
}
