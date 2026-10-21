package com.chipin.grupo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable // Declaramos que es una clase que se puede usar como clave primaria compuesta, sin necesidad de tener su propia tabla en la BD
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Para usar la clave para saber si dos objetos son la misma fila
public class MiembroGrupoId implements Serializable {
    private Long grupoId;
    private Long usuarioId;
}
