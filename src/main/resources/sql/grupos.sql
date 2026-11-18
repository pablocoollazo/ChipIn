INSERT INTO grupo (nombre, descripcion, importe_objetivo, creador_id, codigo_invitacion, estado, fecha_limite,
                   fecha_creacion)
VALUES ('Regalo cumple Ana', 'Entre todos', 120.00, 1, 'DEMO2026', 'ABIERTO', '2026-12-20', now());

-- John (creador) y Jane son miembros; Alice queda fuera para probar a unirse con DEMO2026
INSERT INTO miembro_grupo (grupo_id, usuario_id, fecha_union)
VALUES (1, 1, now()),
       (1, 2, now());