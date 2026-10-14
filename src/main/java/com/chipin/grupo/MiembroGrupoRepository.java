package com.chipin.grupo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, MiembroGrupoId> {
    // EntityGraph evita las N+1 querys (N consultas para N meimbros + 1 para o grupo).
    // attributePaths indica que queremos traer el usuario asociado a cada miembro del grupo.
    @EntityGraph(attributePaths = {"usuario"})
    List<MiembroGrupo> findByGroupId(Long grupoId);
}
