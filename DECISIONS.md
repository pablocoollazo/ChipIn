# Decisiones de proyecto — Chip In

Documento vivo con las decisiones definitivas de diseño y arquitectura, para que quede constancia de por qué está hecho así (útil también para la memoria de la asignatura).

## Contexto

Proyecto de una asignatura de 4,5 créditos, hecho en pareja. No es un TFG: alcance ajustado, sin necesidad de justificar novedad académica.

## Arquitectura

- **Gradle** (no Maven), **Java 25 (LTS)**, Spring Boot 4. Inicialmente era Java 26, pero no es LTS, ya no tiene soporte y desaparece de los repos al salir la siguiente versión. La 25 es LTS y es la que usa el proyecto de ejemplo de la asignatura.
- **Package-by-feature**, no por capas: `usuario/`, `grupo/`, `regalo/`, `aportacion/`, cada uno con su entidad, repositorio, servicio y controller juntos. Elegido por ser 2 personas trabajando en paralelo — minimiza conflictos de merge y mantiene cohesionado el código de cada funcionalidad.
- Postgres vía Docker Compose en local.
- DTOs desde el principio en las respuestas del controller (evita `StackOverflowError` por relaciones bidireccionales al serializar con Jackson).
- Paquetes transversales, fuera de las features:
  - `auth/`: todo lo de "quién hace la petición" (`@UsuarioActual`, `UsuarioActualResolver`, `NoAutenticadoException`, `SinPermisoException`). Separado para que, cuando llegue JWT, solo cambie este paquete.
  - `config/`: solo configuración de Spring (`SeguridadConfig`, `WebConfig`, `ErrorController`).
- Errores: cada feature tiene su propio `@RestControllerAdvice` en su paquete (`UsuarioErrorHandler`, `GrupoErrorHandler`…), para no editar los dos el mismo fichero. `ErrorController` se queda con el `401`/`403` y con los errores propios de Spring (validación, JSON mal formado, `405`, `415`…), y es el único que hereda de `ResponseEntityExceptionHandler`; si lo heredaran varios, competirían por los mismos errores.
- Spring Security/JWT se añade cuando lo dé la asignatura, no antes. Las contraseñas sí se guardan hasheadas con BCrypt desde el principio (`usuario.password_hash` no nulo), usando solo `spring-security-crypto` (`BCryptPasswordEncoder`), que no activa la seguridad de los endpoints. Así no hay que migrar datos ni cambiar el esquema cuando llegue Security.

## Convención de nombres

Entidades y columnas **en español**, siguiendo el diagrama de base de datos acordado en equipo: `Usuario`, `Grupo`, `MiembroGrupo`, `Regalo`, `HistorialPrecio`, `AlertaPrecio`, `Aportacion`, `EventoPago`.

## Modelo de datos

Esquema completo (tablas, columnas, tipos y restricciones) en [`docs/modelo-datos.dbml`](./docs/modelo-datos.dbml), que es la fuente de verdad. Se puede visualizar pegándolo en [dbdiagram.io](https://dbdiagram.io).

```
Usuario ──< MiembroGrupo >── Grupo ──(1:1 opcional)── Regalo ──< HistorialPrecio
                                │                          └──< AlertaPrecio >── Usuario
                                └──< Aportacion >── Usuario
                                              └──< EventoPago
```

- **Grupo** es una campaña de bote concreta (tiene su propio `estado`, `fecha_limite`, `codigo_invitacion`), no un círculo de amigos persistente reutilizable — decisión tomada explícitamente al revisar el diagrama, simplifica el modelo.
- **Grupo.importe_objetivo** (obligatorio): la meta del bote. Se guarda en el grupo y no se toma de `Regalo.precio_actual`, porque el regalo es opcional y su precio cambia; así todo grupo tiene meta, tenga regalo o no.
- **Grupo.creador_id** identifica al organizador: es quien confirma o rechaza las aportaciones manuales.
- **MiembroGrupo**: entidad propia (no `@ManyToMany`), por el atributo `cuota_asignada`. Clave primaria compuesta `(grupo_id, usuario_id)`.
- **Regalo**: 1:1 opcional con `Grupo`; el enlace/precio de lo que se quiere comprar.
- **HistorialPrecio** + **AlertaPrecio**: seguimiento de precio del `Regalo`, con aviso configurable por umbral. `AlertaPrecio` guarda `precio_umbral`, si está `activa` y `fecha_disparo`; como máximo una alerta por usuario y regalo.
- **Aportacion**: núcleo transaccional, con estado dual según método de pago:
  - Manual (Bizum/efectivo): `PENDIENTE → MARCADA_PAGADA → CONFIRMADA/RECHAZADA` (autoreporte del pagador + confirmación del organizador).
  - Automático (Stripe, modo test): `PENDIENTE → PROCESANDO → COMPLETADA/FALLIDA/REEMBOLSADA` (confirmación vía webhook).
  - `clave_idempotencia` (única): guarda el header `Idempotency-Key` del `POST`. Si el cliente reintenta con la misma clave, se devuelve la aportación existente en vez de crear otra.
  - El bote de un grupo es la suma de `importe` de sus aportaciones en estado `CONFIRMADA` o `COMPLETADA`. No se guarda como columna.
- **EventoPago**: log de webhooks de Stripe con `id_evento_externo` único, para no procesar el mismo pago dos veces (idempotencia).

### Por qué no Stripe Connect

El caso de uso real implica que organizadores distintos (en grupos distintos) deberían cobrar cada uno a su propia cuenta — eso es exactamente lo que resuelve Stripe Connect, pero implementarlo (onboarding, verificación de identidad por usuario) es desproporcionado para 4,5 créditos. Se documenta como limitación conocida / trabajo futuro; en su lugar, Stripe se implementa en modo test contra una única cuenta como demostración técnica, y el flujo manual (Bizum/efectivo) es la vía real de uso.

## Reglas de diseño REST (Tema 3 de la asignatura)

- URLs en minúsculas, con guiones, sin verbos, sin barra final. Colecciones en plural (`/grupos`, `/aportaciones`); filtros en query string, nunca en el path.
- Verbos HTTP con semántica correcta: `GET` seguro/idempotente; `POST` crea (no idempotente, con idempotency key para operaciones sensibles como aportaciones); `PUT` reemplazo completo; `PATCH` con formato JSON Patch para cambios parciales; `DELETE` idempotente.
- Códigos de estado específicos: `201` + header `Location` al crear; `204` tras `DELETE`; `404` solo si el recurso no existe (una búsqueda sin resultados es `200` + `[]`, nunca `404`); `401` (no autenticado) vs `403` (sin permiso) diferenciados.
- Errores en formato **RFC 7807** ("Problem Details") en el `GlobalExceptionHandler`.

## Flujo de trabajo en equipo

- Commits en formato **Conventional Commits** (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`).
- Núcleo (`usuario/`, `grupo/`) se construye primero, junto, porque lo necesita todo lo demás.
- A partir de ahí, ramas en paralelo por feature (`feature/regalo`, `feature/aportacion`), cada una cerrada con un Pull Request revisado por el otro antes de mergear a `main`.

## Reparto de la fase 2

Repartido según quién conoce mejor el código del que depende cada rama, y equilibrado con la ampliación de la fase 4.

| Rama | Responsable | Contenido | Ampliación (fase 4) |
|---|---|---|---|
| `feature/aportacion` | Pablo Mouriño (`Pablomou6`) | `Aportacion`, `MetodoPago`, `EstadoAportacion`; `POST /grupos/{id}/aportaciones` con `Idempotency-Key`; flujo manual por `PATCH` con transiciones validadas; total del bote calculado | Stripe Checkout en modo test, webhooks y `EventoPago` |
| `feature/regalo` | Pablo Collazo (`pablocoollazo`) | `Regalo` (`PUT/GET/DELETE /grupos/{id}/regalo`), `HistorialPrecio` (`GET .../regalo/historial-precios`), `AlertaPrecio` (CRUD, una por usuario y regalo) | Scheduler que actualiza precios y dispara alertas |

El autor de grupo hace aportación porque esta depende de `Grupo`/`MiembroGrupo` (pertenencia, estado `ABIERTO`, creador como confirmador). Regalo solo necesita que el grupo exista.

Cada rama incluye su fichero `.http` y tests de integración de su servicio.

### Paso previo: convenciones comunes (Pablo Collazo) ✅ Hecho

- **Usuario que hace la petición**: hasta que llegue Spring Security, se identifica con la cabecera `X-Usuario-Id` (en vez de mandar `usuarioId`/`creadorId` en el body). Se sustituirá por el JWT cambiando solo `UsuarioActualResolver`.
  - Uso: en un endpoint que necesite saber quién llama, se añade un parámetro `@UsuarioActual Long usuarioId`. Si el endpoint no lo necesita, no se pone.
  - `401` si falta la cabecera, no es un número o el usuario no existe. El `403` lo lanza cada servicio con `SinPermisoException` (p. ej. alguien que no es el creador intenta confirmar una aportación).
- **Errores**: un `@RestControllerAdvice` por feature (ver Arquitectura). Las ramas nuevas añaden el suyo: `regalo/RegaloErrorHandler`, `aportacion/AportacionErrorHandler`.
- **Datos de prueba**: `src/main/resources/sql/` (`usuarios.sql`, `grupos.sql`), cargados en cada arranque tras vaciar las tablas. Usuarios 1–3 con contraseña `1234`; grupo 1 con código `DEMO2026`, miembros 1 y 2 (el 3 queda libre para probar a unirse). Cada feature añade su fichero y lo registra en `spring.sql.init.data-locations`, en orden de dependencias.
- **Peticiones de ejemplo**: `src/test/http/`, un `.http` por feature.

### Flecos de la fase 1

| Responsable | Pendiente |
|---|---|
| Pablo Mouriño | Grupo: `GET /grupos?usuarioId=` (mis grupos), `PATCH /grupos/{id}` (JSON Patch, incluido cambio de `estado`), `DELETE /grupos/{id}/miembros/{usuarioId}` (salir del grupo), asignar `cuota_asignada`; el `Location` de unirse debe apuntar al miembro creado, no a la colección; usar `@UsuarioActual` en `POST /grupos` y `POST /miembros` (quitar `creadorId`/`usuarioId` del body); issue #7 (`fechaUnion` a `null` al unirse) |
| Pablo Collazo | Usuario: `PUT`, `PATCH` (JSON Patch) y `DELETE /usuarios/{id}` |

### Resto de fases

| Fase | Pablo Mouriño | Pablo Collazo |
|---|---|---|
| 3. Tests | Grupo y aportación | Usuario, regalo y `UsuarioActualResolver` (401 sin cabecera / id inválido / usuario inexistente, 200 con usuario válido) |
| 4. Ampliación | Stripe Checkout test, webhooks, `EventoPago` | Scheduler de precios y alertas |
| 5. Memoria | Grupo, aportación y pagos | Arquitectura, decisiones, usuario y regalo |

## Roadmap

| Fase | Contenido | Estado |
|---|---|---|
| 0. Base | Proyecto Spring Boot + Postgres | ✅ Hecho |
| 1. Núcleo | `Usuario`, `Grupo`, `EstadoGrupo`, `MiembroGrupo` | ✅ Mergeado (PRs #2–#6); flecos pendientes repartidos en fase 2 |
| 2. Regalo + Aportación | En paralelo, 2 ramas (ver reparto) | 🔲 Pendiente |
| 3. Testing y pulido | Tests unitarios e integración | 🔲 Pendiente |
| 4. Ampliación | Scheduler de precios, Stripe Checkout test | 🔲 Pendiente |
| 5. Documentación de cierre | Memoria de la asignatura | 🔲 Pendiente |
