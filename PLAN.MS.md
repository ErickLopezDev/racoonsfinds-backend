# Plan de migración: Monolito → Monolito Modular → Microservicios

Backend de racoonsfinds (Spring Boot, Java 21, Maven). Este plan describe una
migración **incremental y reversible**: cada fase deja el sistema funcionando y
deployable. No se salta a microservicios de golpe.

## Arquitectura destino

**Híbrida**, no pura:

- **REST síncrono en el borde** para comandos/consultas que el usuario espera
  resueltos al instante (auth, navegar catálogo, carrito, consultar orden).
  Consistencia fuerte.
- **Event-driven asíncrono entre dominios** para efectos secundarios que deben
  estar desacoplados y ser resilientes (orden creada → descontar stock, notificar,
  actualizar ventas, confirmar pago).
- **Sagas** para el flujo `order ↔ payment ↔ stock`, que es donde se pierden las
  transacciones ACID al separar la base de datos.

Por qué no los extremos: REST puro acopla disponibilidad (una falla en cadena
tumba todo el flujo); event-driven puro mete consistencia eventual hasta en
operaciones que el usuario espera inmediatas.

---

## Mapa de dominios y acoplamientos actuales

Bounded contexts detectados en el código:

| Dominio        | Entidades / clases clave                                  |
|----------------|-----------------------------------------------------------|
| `identity`     | User, RefreshToken, Auth, JWT, UserStatus                 |
| `catalog`      | Product, Category                                         |
| `cart`         | Cart, Wishlist                                            |
| `order`        | Purchase, PurchaseDetail, Payment, Sales                  |
| `review`       | Review                                                    |
| `notification` | Notification                                              |
| `platform`     | S3Service (storage), EmailService                         |

**Hubs de acoplamiento:** `User` y `Product`. Casi todos los dominios los
referencian por `@ManyToOne` (Cart→User+Product, Purchase→User, Product→User+Category,
Review/Wishlist→User+Product). Estos son los bordes que más van a doler al separar
la base de datos.

**Candidatos a extraer primero (bajo acoplamiento):** `notification`, `identity`.
**Extraer al final (nudo transaccional):** `order` + `payment` + stock de `catalog`.

---

## Fase 0 — Guardrails (proteger el borde desde el día uno)

Sin algo que lo verifique, los imports cruzados vuelven en una semana.

- [ ] Agregar **ArchUnit** como dependencia de test, o adoptar **Spring Modulith**
      (hecho exactamente para esto: define módulos, verifica que solo se comuniquen
      por APIs públicas y eventos, y documenta dependencias).
- [ ] Escribir un test que falle el build si un módulo importa el `repository` o
      las clases internas de otro módulo.
- [ ] Decisión a tomar: **ArchUnit** (ligero, solo reglas) vs **Spring Modulith**
      (más completo: eventos, docs, test slices). Recomendado: Spring Modulith,
      porque ya prepara el terreno para la Fase 3 (eventos).

**Entregable:** build que rechaza acoplamiento prohibido. Riesgo bajo, reversible.

---

## Fase 1 — Reorganizar a paquetes por dominio (boundaries suaves)

Mover de estructura por capa técnica (`controller/`, `service/`, `repository/`)
a estructura por dominio. Mismo `.jar`, mismo deploy.

```
com.racoonsfinds.backend
├── shared/        (exception, utils, dto comunes, config)
├── platform/      (storage/S3, email)
├── identity/      (api, domain, repository, service, dto)
├── catalog/
├── cart/
├── order/
├── review/
└── notification/
```

Cada módulo: `api/` (controllers) · `domain/` (entidades) · `repository/` ·
`service/` (interfaz en API pública + impl interna) · `dto/`.

- [ ] Mover clases dominio por dominio (un commit por módulo).
- [ ] Mantener las relaciones JPA `@ManyToOne` como están **por ahora**.
- [ ] Regla nueva: un módulo solo accede a otro vía su **interfaz de service
      pública** (`catalog.ProductService`), nunca su repositorio.

**Entregable:** mismo comportamiento, código organizado por dominio. Casi puro
mover archivos → bajo riesgo, alta reversibilidad.

---

## Fase 2 — Aislar la comunicación entre módulos

- [ ] Definir la **API pública** de cada módulo (interfaces de service + DTOs que
      expone). Todo lo demás pasa a package-private / interno.
- [ ] Reemplazar accesos directos a repositorios ajenos por llamadas a la API
      pública del módulo dueño.
- [ ] Verificar con los guardrails de Fase 0 que no quedan fugas.

**Entregable:** módulos que solo se hablan por contratos explícitos. Sigue siendo
un solo deploy.

---

## Fase 3 — Introducir eventos de dominio internos (semilla event-driven)

Antes de separar procesos, desacoplar los efectos secundarios *dentro* del monolito
usando `ApplicationEventPublisher` de Spring (o eventos de Spring Modulith).

- [ ] Identificar side-effects cross-domain. Ejemplo: al crear una `Purchase`,
      hoy probablemente se descuenta stock, se notifica y se registra venta de forma
      acoplada.
- [ ] Publicar eventos de dominio (`PurchaseCreatedEvent`) y mover esos efectos a
      listeners en `catalog` (stock), `notification`, etc.
- [ ] Estos eventos hoy son in-process; mañana se reemplazan por mensajes de un
      broker sin cambiar la lógica de negocio.

**Entregable:** el flujo de compra ya es event-driven *conceptualmente*, aunque
todo corra en el mismo proceso. Este es el cambio de diseño más importante.

---

## Fase 4 — Romper el acoplamiento de datos (preparar la separación de DB)

Solo en los bordes de los módulos que se van a extraer.

- [ ] Reemplazar `@ManyToOne Product product` por `Long productId` (+ resolución
      vía `ProductService`) donde corresponda (empezar por `cart`, `review`).
- [ ] Identificar qué `JOIN`/queries se rompen y reescribirlos.
- [ ] Separar el esquema lógicamente: cada módulo dueño de sus tablas, sin FKs
      cruzando bordes de módulo.

**Entregable:** base de datos lista para particionarse por servicio. Acá empieza
el costo real; hacerlo módulo por módulo.

---

## Fase 5 — Extraer el primer microservicio (prueba de concepto)

Candidato: **notification** (o **identity**). Bajo acoplamiento de datos, borde claro.

- [ ] Sacar el módulo a su propio servicio deployable.
- [ ] Introducir un **message broker** (RabbitMQ o Kafka) — los eventos de la
      Fase 3 ahora viajan por la red.
- [ ] Comunicación: REST para queries síncronas, eventos para lo asíncrono.
- [ ] Sumar infra de microservicios: API Gateway, service discovery,
      observabilidad distribuida (trazas), config centralizada.
- [ ] Base de datos propia para el servicio extraído.

**Entregable:** primer servicio independiente en producción + el patrón replicable.

---

## Fase 6 — Descomponer el núcleo transaccional (order / payment / stock)

El trabajo más difícil, al final y con todo lo anterior ya probado.

- [ ] Diseñar la **Saga** para `order → payment → stock` (orquestada recomendada
      para un flujo crítico y auditable).
- [ ] Manejar compensaciones (pago falla → liberar stock reservado → cancelar orden).
- [ ] Patrón **Outbox** para garantizar publicación de eventos consistente con la
      transacción local.
- [ ] Extraer `order`, `payment` y el stock de `catalog` a servicios separados.

**Entregable:** ecommerce distribuido con consistencia gestionada por sagas.

---

## Principios transversales

- Cada fase deja el sistema **deployable y funcionando**.
- Un commit/PR por unidad pequeña (un módulo, un evento), no big-bang.
- No extraer a microservicio nada que no esté ya aislado como módulo y con su
  acoplamiento de datos resuelto.
- Medir antes de extraer: si un módulo no necesita escalar/deployarse aparte,
  puede quedarse en el monolito modular indefinidamente. Microservicios es un
  medio, no la meta.

## Decisiones pendientes

1. Guardrail: **ArchUnit** vs **Spring Modulith** (Fase 0).
2. Broker: **RabbitMQ** (más simple, colas) vs **Kafka** (log de eventos, replay).
3. Saga: **orquestada** (orquestador central) vs **coreografiada** (solo eventos).
