package com.chipin.grupo;

import com.chipin.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "grupo")
@Getter
@Setter
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importeObjetivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @Column(nullable = false, unique = true, length = 12)
    private String codigoInvitacion;

    @Enumerated(EnumType.STRING) // Para guardar en la BD que es un enum y que se almacene como Str
    @Column(nullable = false, length = 20)
    private EstadoGrupo estado = EstadoGrupo.ABIERTO;

    @Column()
    private LocalDate fechaLimite;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant fechaCreacion;

}
