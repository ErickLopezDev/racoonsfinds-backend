# 09 - Decisiones y trade-offs

Este documento es una bitacora de las decisiones de diseno, al estilo de un registro ligero de decisiones de arquitectura (ADR). La idea la tome de la practica de dejar por escrito no solo que se decidio, sino por que y que se descarto, para que dentro de unos meses (o quien lea el repo) entienda el razonamiento y no solo el resultado.

Cada entrada sigue el mismo formato: contexto, decision, alternativa descartada y el trade-off que acepte.

## ADR-01 - Monolito modular antes que microservicios

- Contexto: el sistema tiene varios dominios que interactuan y eventualmente podrian escalar por separado.
- Decision: empezar por un monolito modular con limites estrictos.
- Alternativa descartada: microservicios desde el dia uno.
- Trade-off: renuncio al despliegue y escalado independiente por ahora, a cambio de no pagar latencia de red, consistencia distribuida ni complejidad operacional antes de necesitarla. Es la idea de DDIA de no introducir complejidad distribuida hasta que un problema real la justifique.

## ADR-02 - Limites verificados por Spring Modulith

- Contexto: los limites entre modulos se erosionan si nada los hace cumplir.
- Decision: verificar la estructura modular en un test que rompe el build ante violaciones.
- Alternativa descartada: confiar en la convencion y la revision de codigo.
- Trade-off: acoplo el build a una herramienta, a cambio de limites ejecutables que no dependen de la disciplina humana.

## ADR-03 - Comunicacion cross-module por puertos y eventos

- Contexto: los modulos necesitan datos y reaccionar a hechos de otros modulos.
- Decision: para datos sincronos, puertos (interfaces); para hechos, eventos de dominio en un lugar neutro (shared).
- Alternativa descartada: llamar directamente a los servicios de otros modulos.
- Trade-off: una capa de indireccion mas, a cambio de desacople real y de costuras claras por donde extraer servicios.

## ADR-04 - FKs cross-module como IDs planos

- Contexto: las relaciones JPA hacia entidades de otros modulos generan foreign keys cruzadas y acoplan los esquemas.
- Decision: guardar el id plano (Long) y resolver el resto via puerto.
- Alternativa descartada: relaciones `@ManyToOne` entre entidades de distintos modulos.
- Trade-off: un poco mas de codigo para resolver nombres, a cambio de poder separar el modulo sin desarmar el modelo de datos.

## ADR-05 - Denormalizar el rating sobre Product via evento

- Contexto: el catalogo necesita mostrar el rating, que nace en review.
- Decision: mantener una copia agregada (averageRating, reviewCount) sobre Product, actualizada por un listener de evento.
- Alternativa descartada: hacer un join o llamar a review en cada lectura del catalogo.
- Trade-off: duplicacion y consistencia eventual, a cambio de lecturas locales y rapidas que no dependen de review. Es el patron de dato derivado de DDIA.

## ADR-06 - Testcontainers con Postgres real, no H2

- Contexto: los tests de integracion deben dar confianza sobre el comportamiento en produccion.
- Decision: probar contra un Postgres real levantado en Docker.
- Alternativa descartada: H2 en memoria.
- Trade-off: tests mas lentos y dependencia de Docker, a cambio de paridad real con produccion (dialecto, agregaciones, transacciones).

## ADR-07 - Runtime Docker sobre JDK, no JRE slim

- Contexto: la imagen de runtime podria ser mas chica con una base JRE.
- Decision: mantener `eclipse-temurin:21-jdk-jammy` por ahora.
- Alternativa descartada: pasar a una imagen JRE slim.
- Trade-off: imagen mas pesada, a cambio de simplicidad. Es una optimizacion disponible, no un olvido.

## ADR-08 - Saga para orden/pago/stock (futuro)

- Contexto: confirmar una compra cruza tres dominios que en microservicios estarian separados.
- Decision (planeada): coordinar la operacion con una saga de pasos compensables.
- Alternativa descartada: una transaccion distribuida (2PC).
- Trade-off: complejidad de compensaciones, a cambio de no acoplar los servicios con una transaccion global.

## Decisiones aun abiertas

- Migraciones de esquema: introducir Flyway o Liquibase en vez de derivar el esquema con Hibernate.
- Endurecer Actuator: limitar endpoints y/o puerto de management separado.
- Observabilidad en AWS: self-managed (Prometheus/Grafana en ECS) vs managed (AMP + AMG).
- Base en la nube: RDS estandar vs Aurora Serverless v2.
- Estrategia de despliegue: rolling update vs blue/green.
- Imagen base: si se concreta el paso a JRE slim (ADR-07).

## Para seguir

- El contexto de cada decision esta repartido en [01](01-architecture.md), [02](02-domain-events.md) y [08](08-roadmap-microservices.md).
