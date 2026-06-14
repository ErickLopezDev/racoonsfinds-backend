# Racoonsfinds Backend

Backend de un marketplace construido como **monolito modular** en **Spring Boot 3.5 / Java 21**, con límites entre dominios verificados por tests, comunicación por eventos y un camino planificado hacia microservicios.

No es un CRUD: es un ejercicio de **diseño de sistemas backend / distribuidos** donde cada decisión de arquitectura es explícita, justificada y verificable.

---

## TL;DR

| | |
|---|---|
| **Stack** | Spring Boot 3.5.6, Java 21, Maven, PostgreSQL 16 |
| **Arquitectura** | Monolito modular (Spring Modulith 1.4), boundaries ejecutables |
| **Comunicación** | Eventos de dominio + puertos/adaptadores (ports & adapters) |
| **Persistencia** | JPA/Hibernate, FKs cross-module desacopladas a IDs planos |
| **Testing** | JUnit 5, Testcontainers (Postgres real), tests de arquitectura |
| **Observabilidad** | Actuator + Micrometer + Prometheus + Grafana (provisioning as code) |
| **Empaquetado** | Docker multi-stage (layered jars, non-root), `docker compose` |
| **Seguridad** | JWT (access/refresh) sobre Spring Security |

---

## Por qué este proyecto

El objetivo no es "hacer funcionar un marketplace", sino **demostrar criterio de ingeniería de sistemas distribuidos**:

- Empezar por **monolito modular** para descubrir los acoplamientos reales antes de pagar el costo operacional de la red.
- Hacer los **límites entre dominios ejecutables**: si un módulo accede a las internals de otro o se crea un ciclo, el build falla.
- **Desacoplar por eventos** lo que el día de mañana se extrae a un servicio, sin reescribir.
- Tratar **observabilidad, contenedores y testing contra dependencias reales** como parte del diseño, no como un extra.

La discusión completa de planificación, arquitectura, diagramas y trade-offs vive en [`docs/`](docs/).

---

## Arquitectura de un vistazo

Monolito modular: un solo deployable, dividido internamente en módulos por dominio con límites estrictos. Las relaciones cross-module no son FKs JPA, sino **IDs planos** resueltos vía **puertos**; los efectos secundarios cross-module viajan como **eventos de dominio** (async, after-commit).

```
                 ┌────────────────────────────────────────────┐
                 │            Racoonsfinds (1 deployable)       │
                 │                                              │
  HTTP / REST ──▶│  identity   catalog   cart   order   review │
                 │  notification   wishlist                     │
                 │                                              │
                 │  shared (eventos, contratos)  ·  platform    │
                 │                                              │
                 │   ── boundaries verificados por Modulith ──  │
                 └────────────────────────────────────────────┘
                          │ eventos de dominio (async)
                          ▼
              review ──ReviewStatsChangedEvent──▶ catalog
                       (denormaliza rating sobre Product)
```

Diagramas C4/PlantUML reales se **generan desde el código** (`ModularityTests`) en `target/spring-modulith-docs`. Ver [`docs/01-architecture.md`](docs/01-architecture.md).

---

## Quickstart

### Levantar el stack completo

```bash
docker compose up
```

Levanta: app (`:8080`) + PostgreSQL + Prometheus (`:9090`) + Grafana (`:3000`), con el dashboard cargado solo.

### Correr los tests

```bash
mvn test
```

Incluye verificación de arquitectura y tests de integración contra Postgres real (Testcontainers). Ver [`docs/04-testing-strategy.md`](docs/04-testing-strategy.md).

---

## Documentación (`docs/`)

La documentación está organizada como un recorrido desde la planificación hasta los trade-offs. Cada parte es autocontenida.

| # | Documento | De qué trata |
|---|-----------|--------------|
| 00 | [Visión y dominio](docs/00-overview.md) | Problema, alcance, objetivos y por qué monolito modular |
| 01 | [Arquitectura](docs/01-architecture.md) | Módulos, boundaries, diagramas C4, estructura de paquetes |
| 02 | [Eventos y desacoplamiento](docs/02-domain-events.md) | Ports & adapters, eventos de dominio, denormalización, consistencia eventual |
| 03 | [Datos y persistencia](docs/03-data-persistence.md) | JPA, Postgres, schema por módulo, desacople de FKs a IDs planos |
| 04 | [Estrategia de testing](docs/04-testing-strategy.md) | Tests de arquitectura, Testcontainers, tests de módulo en aislamiento |
| 05 | [Observabilidad](docs/05-observability.md) | Actuator/Micrometer/Prometheus/Grafana, métricas, provisioning |
| 06 | [Contenedores y dev local](docs/06-containers.md) | Dockerfile multi-stage, layered jars, compose, healthchecks |
| 07 | [Seguridad](docs/07-security.md) | JWT, Spring Security, manejo de secretos |
| 08 | [Roadmap a microservicios](docs/08-roadmap-microservices.md) | Fases, sagas, AWS/Terraform/ECS, arquitectura híbrida |
| 09 | [Decisiones y trade-offs](docs/09-decisions.md) | Bitácora de decisiones (estilo ADR) con su justificación |

> Estado de la documentación: estructura e índice creados. El contenido se completa por partes.
