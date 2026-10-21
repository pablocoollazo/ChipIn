package com.chipin.grupo;

// Enum para establecer los estados posibles de un grupo.
// ABIERTO: El grupo está abierto y aceptando nuevos miembros.
// CERRADO: El grupo está cerrado y no acepta nuevos miembros. Terminó.
// COMPLETADO: El grupo ha completado su objetivo o actividad, pero no terminó.
// CANCELADO: El grupo ha sido cancelado y no se llevará a cabo.
public enum EstadoGrupo {
    ABIERTO,
    CERRADO,
    COMPLETADO,
    CANCELADO
}
