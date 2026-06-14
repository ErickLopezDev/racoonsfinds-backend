# 03 · Datos y persistencia

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Cómo se modelan y guardan los datos sin que la base se convierta en un punto de acoplamiento.

## Qué va aquí

- **JPA / Hibernate sobre PostgreSQL**: por qué Postgres como motor (paridad dev/prod, tipos, agregaciones).
- **Datos por módulo**: cada módulo es dueño de sus tablas; no se hacen joins cruzando boundaries.
- **Desacople de FKs cross-module a IDs planos (`Long`)**: en vez de una relación JPA `@ManyToOne` hacia la entidad de otro módulo, se guarda el ID plano y se resuelve el nombre/datos vía puerto. Esto es lo que permite extraer un módulo sin reescribir el modelo.
- **Denormalización deliberada**: `Product` guarda `averageRating`/`reviewCount` (datos derivados del módulo review) para lecturas locales. Trade-off de duplicación vs acoplamiento de lectura.
- **Esquema y migraciones**: estado actual (`ddl-auto` en tests vs estrategia para prod), y la deuda hacia migraciones versionadas (Flyway/Liquibase).
- **Transacciones**: dónde están los límites transaccionales y por qué los eventos cross-module son after-commit.

## Artefactos en el repo

- Entidades `catalog/domain/Product`, `review/domain/Review`
- Repositorios JPA por módulo

## Enlaces

- Por qué los eventos son after-commit → [02-domain-events.md](02-domain-events.md)
- Tests contra Postgres real → [04-testing-strategy.md](04-testing-strategy.md)
