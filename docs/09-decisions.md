# 09 · Decisiones y trade-offs

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Bitácora de decisiones (estilo ADR ligero): qué se decidió, por qué, y qué se descartó.

## Formato sugerido por entrada

Para cada decisión: **Contexto** · **Decisión** · **Alternativas descartadas** · **Trade-off aceptado** · **Estado**.

## Decisiones a documentar

- **ADR-01 — Monolito modular en vez de microservicios desde el día 1.** Trade-off: 80% del beneficio (límites claros, extracción futura) con 20% del costo (sin latencia de red ni consistencia distribuida todavía).
- **ADR-02 — Boundaries verificados por Spring Modulith.** Alternativa: convención + revisión humana. Trade-off: build acoplado a la herramienta a cambio de límites ejecutables.
- **ADR-03 — Comunicación cross-module por eventos + puertos.** Alternativa: llamadas directas a servicios de otros módulos. Trade-off: indirección a cambio de desacople real.
- **ADR-04 — FKs cross-module como IDs planos, no relaciones JPA.** Trade-off: resolver nombres vía puerto (más código) a cambio de poder extraer el módulo.
- **ADR-05 — Denormalizar rating sobre Product vía evento.** Trade-off: consistencia eventual + duplicación a cambio de lecturas locales.
- **ADR-06 — Testcontainers (Postgres real) en vez de H2.** Trade-off: tests más lentos + Docker requerido a cambio de paridad con prod.
- **ADR-07 — Runtime Docker sobre `jdk-jammy` (no JRE slim).** Trade-off: imagen más pesada, decisión consciente del autor.
- **ADR-08 — Saga para `order/payment/stock` (futuro).** Alternativa: 2PC. Trade-off: complejidad de compensaciones a cambio de no acoplar servicios.

## Decisiones aún abiertas

- Observabilidad en AWS: self-managed vs AMP+AMG.
- DB: RDS estándar vs Aurora Serverless v2.
- Deploy: rolling vs blue/green.
- Migraciones: introducir Flyway/Liquibase.

## Enlaces

- Contexto de cada una repartido en → [01](01-architecture.md), [02](02-domain-events.md), [08](08-roadmap-microservices.md)
