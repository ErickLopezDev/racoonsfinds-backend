# 03 - Datos y persistencia

Aqui explico como se guardan los datos sin que la base se convierta en el punto donde todos los modulos se vuelven a acoplar. Es facil tener modulos limpios en el codigo y una base de datos que los amarra a todos con foreign keys cruzadas.

## Postgres como motor

Uso PostgreSQL, el mismo motor en desarrollo, en los tests y en la idea de produccion. Esto es a propos: muchas diferencias sutiles (tipos, funciones de agregacion, comportamiento de transacciones, locking optimista) no aparecen si testeo contra una base distinta a la de produccion. Mas sobre esto en [04-testing-strategy.md](04-testing-strategy.md).

## Cada modulo es dueno de sus datos

La regla es que cada modulo manda sobre sus propias tablas y nadie hace joins cruzando un limite. Si catalog necesita un dato de review, no lo busca en las tablas de review: lo recibe por evento o lo pide por un puerto. Esto replica, dentro de una sola base, la disciplina que tendria si cada modulo tuviera su propia base, que es justamente la situacion a la que apunto si algun dia se extraen servicios.

## El cambio clave: FKs cross-module como IDs planos

Esta es la decision de persistencia mas importante del proyecto. En un modelo JPA tipico, una resena tendria una relacion hacia el producto asi:

```java
// lo que NO hago
@ManyToOne
private Product product;
```

Eso es comodo pero acopla la entidad Review a la entidad Product de otro modulo: para compilar review necesita la clase de catalog, y a nivel base aparece una foreign key entre tablas de dos dominios. Imposible separar.

En su lugar guardo el identificador plano:

```java
// lo que si hago
private Long productId;
```

review guarda solo el id del producto. Cuando necesita mostrar el nombre del producto o validar que existe, lo resuelve a traves de `ProductCatalogPort`. La relacion sigue existiendo conceptualmente, pero ya no es una FK rigida entre esquemas de distintos modulos: es una referencia blanda que se resuelve en la capa de aplicacion.

```mermaid
flowchart LR
    subgraph antes[Antes: FK cross-module]
        R1[Review] -->|@ManyToOne| P1[Product]
    end
    subgraph despues[Despues: id plano + puerto]
        R2[Review<br/>productId: Long] -.resuelve via.-> Port[ProductCatalogPort]
        Port --> P2[Product]
    end
```

El costo es un poco mas de codigo (resolver nombres por puerto en vez de navegar la relacion). El beneficio es que el dia que review se vuelva un servicio, no hay que reescribir el modelo ni desarmar foreign keys entre dos bases.

## Denormalizacion deliberada

Como conte en [02-domain-events.md](02-domain-events.md), Product guarda `averageRating` y `reviewCount`, que son datos que en realidad nacen en review. Es duplicacion a propos. En DDIA esto se llama dato derivado: una vista materializada que mantengo cerca de quien lee para no tener que recalcular ni cruzar el limite en cada lectura.

El trade-off es el tipico de la denormalizacion: gano lecturas rapidas y locales, pago con tener que mantener la copia sincronizada (lo hace el listener) y con consistencia eventual. Para datos que se leen mucho mas de lo que cambian, como el rating de un producto en una pagina de catalogo, el intercambio vale la pena.

## Transacciones y limites

El limite transaccional vive dentro de cada modulo. Cuando review crea una resena, esa escritura es una transaccion. La actualizacion del rating en catalog es otra transaccion distinta, que corre en el listener despues del commit de la primera. No hay una transaccion que abarque los dos modulos, y eso es intencional: una transaccion distribuida volveria a acoplarlos. Acepto consistencia eventual entre ambos a cambio de mantenerlos independientes.

## Deuda conocida: migraciones

Hoy el esquema lo deriva Hibernate a partir de las entidades. En los tests uso `ddl-auto=create-drop` para levantar y tirar el esquema en cada corrida. Para un sistema que apunta a produccion esto no alcanza: el siguiente paso natural es introducir migraciones versionadas con Flyway o Liquibase, para que los cambios de esquema sean explicitos, ordenados y reproducibles. Lo dejo registrado como decision abierta en [09-decisions.md](09-decisions.md).

## Para seguir

- Por que los eventos cross-module son despues del commit: [02-domain-events.md](02-domain-events.md)
- Como se prueba todo esto contra Postgres real: [04-testing-strategy.md](04-testing-strategy.md)
