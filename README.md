# Chip In

Bote compartido entre amigos, con seguimiento de precio incluido.

Un grupo crea un bote para un regalo o gasto conjunto, invita gente con un código, cada uno aporta su parte (con tarjeta o marcando manualmente un Bizum/efectivo), y si el bote está ligado a un artículo con enlace, se sigue su precio para saber cuándo es buen momento de comprar.

## Stack

- **Backend:** Spring Boot 4 (Java 25, Gradle), Spring Data JPA, PostgreSQL.
- **Frontend:** pendiente (React, más adelante).

Ver [DECISIONS.md](./DECISIONS.md) para el detalle de arquitectura, modelo de datos y decisiones de diseño.

## Requisitos

- JDK 25 (en Arch: `jdk25-openjdk`)
- Docker + Docker Compose (para la base de datos local)

## Arrancar en local

```bash
docker compose up -d        # levanta Postgres
./gradlew bootRun           # arranca la API en localhost:8080
```

## Estructura

Organizado por feature (no por capas): cada paquete bajo `src/main/java/com/chipin/` agrupa entidad, repositorio, servicio y controller de una misma funcionalidad (`usuario/`, `grupo/`, `regalo/`, `aportacion/`...).

## Commits

Seguimos [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`.
