package com.chipin.grupo;

import com.chipin.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "miembro_grupo")
@Getter
@Setter
@NoArgsConstructor
public class MiembroGrupo {
    @EmbeddedId // Declaramos que es una clave primaria compuesta (funciona de la mano con Embeddable)
    private MiembroGrupoId id;

    @MapsId("grupoId") // Declaramos que el id del grupo es el mismo que el de la clase MiembroGrupoId
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Muchos miembros pueden pertenecer a un grupo
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @MapsId("usuarioId") // Declaramos que el id del usuario es el mismo que el de la clase MiembroGrupoId
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Muchos miembros pueden pertenecer a un usuario
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant fechaUnion;

    @Column(precision = 10, scale = 2)
    private BigDecimal cuotaAsignada;

    public MiembroGrupo(Grupo grupo, Usuario usuario) {
        this.grupo = grupo;
        this.usuario = usuario;
        this.id = new MiembroGrupoId(grupo.getId(), usuario.getId());
    }
}
