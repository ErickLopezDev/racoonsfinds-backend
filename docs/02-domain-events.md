# 02 · Eventos de dominio y desacoplamiento

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

El corazón del enfoque distribuido: cómo se comunican los módulos sin acoplarse.

## Qué va aquí

- **Ports & adapters**: los módulos se hablan vía interfaces (`ProductCatalogPort`, `UserDirectoryPort`) con adaptadores `Local*` hoy e `Http*` el día que se separen. El consumidor no conoce la implementación.
- **Eventos de dominio**: efectos secundarios cross-module viajan como eventos, no como llamadas directas. Contrato neutro en `shared/event` para no acoplar consumidor→productor.
- **Caso de estudio — denormalización del rating**: `review` publica `ReviewStatsChangedEvent`; `catalog` lo consume con `@ApplicationModuleListener` (async, after-commit) y denormaliza `averageRating` + `reviewCount` sobre `Product`. Las lecturas de catálogo quedan locales.
- **Consistencia eventual**: el trade-off explícito (el rating se actualiza tras el commit, no en la misma transacción) y por qué es aceptable aquí.
- **Por qué async after-commit**: no bloquear la escritura de la reseña; no propagar un rollback parcial.
- **Camino a externalizar**: el mismo evento se reenruta a RabbitMQ/Kafka cuando `review` se extraiga, sin tocar la lógica de negocio.

## Artefactos en el repo

- `shared/event/ReviewStatsChangedEvent`
- `catalog/event/ReviewStatsListener` (`@ApplicationModuleListener`)
- `catalog/port/ProductCatalogPort`, `identity/port/UserDirectoryPort`

## Enlaces

- Cómo se prueba el flujo de eventos end-to-end → [04-testing-strategy.md](04-testing-strategy.md)
- Cómo evoluciona a mensajería real → [08-roadmap-microservices.md](08-roadmap-microservices.md)
