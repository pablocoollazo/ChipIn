# Chip In

Shared gift fund for groups of friends, with price tracking. A group opens a fund for a gift or shared expense, members join with an invite code and pay their share, and if the fund is linked to a product, its price is tracked to show when to buy.

Course project for Service Engineering at USC, built by a team of two. Work in progress.

## Status

| Area | State |
|---|---|
| User API (`GET /usuarios`, `GET /usuarios/{id}`, `POST /usuarios`) | Done |
| Group data model | Done |
| Group API | In progress |
| Gifts, contributions, price alerts, Stripe test payments | Planned |
| Authentication, frontend | Planned |

## Tech stack

Java 25, Spring Boot 4, Spring Data JPA, PostgreSQL, Docker Compose, Gradle.

Errors follow RFC 7807 Problem Details, and passwords are stored hashed with BCrypt.

## Architecture

```
Client → Controller → Service → Repository → PostgreSQL
```

Controllers exchange DTOs rather than entities, and `ErrorController` turns exceptions into Problem Details responses.

## Running locally

Requires JDK 25 and Docker with Docker Compose.

```bash
docker compose up -d   # start PostgreSQL
./gradlew bootRun      # start the API on localhost:8080
```

The credentials in `docker-compose.yml` are for local development only.

## Project structure

Code is organised by feature, not by layer: each package under `src/main/java/com/chipin/` holds the entity, repository, service and controller of one feature.

## Testing

The automated suite is currently a Spring context test. `src/test/queries.http` holds example requests for the user API. Unit and integration tests are planned.

## Design decisions

Architecture, data model and REST conventions are documented in Spanish in [DECISIONS.md](./DECISIONS.md). The full schema is in [docs/modelo-datos.dbml](./docs/modelo-datos.dbml).

## Contributing

Work happens on feature branches, reviewed through pull requests. Commits follow [Conventional Commits](https://www.conventionalcommits.org/).
