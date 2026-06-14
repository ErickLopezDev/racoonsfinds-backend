# 01 · Arquitectura

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Cómo está estructurado el sistema por dentro y cómo se hacen cumplir los límites.

## Qué va aquí

- **Monolito modular, definición operativa**: un deployable, módulos por dominio, API pública vía `package-info.java`, el resto interno.
- **Spring Modulith**: cómo `modules.verify()` rompe el build ante violaciones de boundary o ciclos de dependencia. Los límites son una regla ejecutable, no un diagrama que se pudre.
- **Estructura de paquetes**: convención `com.racoonsfinds.backend.<modulo>.{api,service,domain,repository,port,event}`.
- **Diagramas C4 / PlantUML**: generados desde el código por `ModularityTests.writesDocumentationSnippets()` → `target/spring-modulith-docs`. Incrustar aquí el diagrama de módulos y un par de "module canvases".
- **Reglas de dependencia entre módulos**: qué puede depender de qué; rol de `shared` (contratos/eventos neutros) y `platform` (infra transversal: storage S3, etc.).
- **Flujo de una request típica**: REST → service → repositorio, y dónde se emiten eventos.

## Artefactos en el repo

- `ModularityTests` (`src/test/java/.../ModularityTests.java`)
- `package-info.java` por módulo

## Enlaces

- Cómo se comunican los módulos → [02-domain-events.md](02-domain-events.md)
- Cómo se verifican estos límites → [04-testing-strategy.md](04-testing-strategy.md)
