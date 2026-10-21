package com.chipin.grupo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    // JpaRepository<Entidad (que es la tabla (@Entity) donde se hace la consulta), Tipo de la clave primaria>

    // Optional puede contener UN valor o estar vacío. No usa null, a diferencia de usar 'Grupo' cuando no encuentra
    Optional<Grupo> findByCodigoInvitacion(String codigoInvitacion);

    boolean existsByCodigoInvitacion(String codigoInvitacion);
}
