# 04 · Estrategia de testing

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

La arquitectura es verificable, no aspiracional. Tres niveles de prueba.

## Qué va aquí

- **Nivel 1 — Tests de arquitectura**: `ModularityTests.verifiesModularStructure()` → `modules.verify()` falla el build si se viola un boundary o se crea un ciclo.
- **Nivel 2 — Tests de módulo en aislamiento**: `@ApplicationModuleTest` levanta un solo módulo, mockea los puertos hacia los demás y verifica su contrato. Ej.: `ReviewEventPublicationTest` comprueba que crear una reseña publica `ReviewStatsChangedEvent` (con `PublishedEvents`).
- **Nivel 3 — Integración con Testcontainers**: contra **Postgres real** (no H2, no mocks de DB). `ReviewStatsDenormalizationTest` usa la API `Scenario` de Modulith (`publish(...).andWaitForStateChange(...)`) para probar el flujo async cross-module end-to-end.
- **Por qué Testcontainers y no H2**: H2 simula SQL pero no es Postgres (tipos, agregaciones, locking, transacciones). Probar contra el motor de prod atrapa bugs de dialecto/migración.
- **Patrón de contenedor singleton**: `PostgresTestContainer` arranca un Postgres por JVM y lo reutiliza; `@DynamicPropertySource` inyecta la datasource.
- **Estado**: 47 tests verdes.
- **Nota de entorno / CI**: correr Testcontainers en Windows local requirió un workaround (engine Docker más viejo por el choque docker-java 1.32 vs API mínima 1.40); en Linux CI corre nativo. Documentar para reproducibilidad.

## Artefactos en el repo

- `ModularityTests`
- `review/ReviewEventPublicationTest`
- `catalog/ReviewStatsDenormalizationTest`
- `support/PostgresTestContainer`

## Enlaces

- Los eventos que se prueban → [02-domain-events.md](02-domain-events.md)
- Los boundaries que se verifican → [01-architecture.md](01-architecture.md)
