# Auditoría y guía de migración a monolito modular

> **Para el agente (Claude) en sesiones futuras:** este archivo es tu contexto de
> trabajo para la separación modular. Léelo completo antes de mover código. Es el
> estado real auditado del repo al 2026-06-11. Verificá contra el código actual
> antes de asumir que sigue vigente (`git log`, grep). Documentos hermanos:
> `PLAN.MS.md` (estrategia monolito→microservicios) y `PLAN.MN.md` (deploy/observabilidad).

---

## 1. Estado actual del desacoplamiento

El repo **ya empezó** la separación con el patrón **Port & Adapter**:

| Port | Adapter (impl actual) | Dueño | Consumidores |
|------|----------------------|-------|--------------|
| `service.port.ProductCatalogPort` | `service.adapter.LocalProductCatalogAdapter` (→ `ProductRepository`) | catalog | cart, wishlist, order |
| `service.port.NotificationPort` | `service.adapter.LocalNotificationAdapter` (→ `NotificationService`) | notification | order |
| `service.port.ProductSnapshot` (record) | — proyección de Product que cruza el borde | catalog | cart, wishlist, order |

Esto es exactamente la dirección de las Fases 2-4 de `PLAN.MS.md`: el día de mañana
`LocalProductCatalogAdapter` se reemplaza por `HttpProductCatalogAdapter` (REST al
catalog-service) sin tocar a los consumidores.

---

## 2. Mapa de dependencias entre services (nivel código)

Leyenda: ✓ = dependencia propia del módulo · ⚠️ = acoplamiento cruzado a otro dominio

| Service | Dominio | Dependencias | Cruces a romper |
|---------|---------|--------------|-----------------|
| `AuthServiceImpl` | identity | UserRepository ✓, RefreshTokenService ✓, EmailService (platform), UserTransactionService ✓ | — limpio |
| `UserServiceImpl` | identity | UserRepository ✓, S3Service (platform) | — limpio |
| `UserTransactionService` | identity | UserRepository ✓ | — limpio |
| `RefreshTokenService` | identity | RefreshTokenRepository ✓ | — limpio |
| `CategoryService` | catalog | CategoryRepository ✓ | — limpio |
| `ProductServiceImpl` | catalog | ProductRepository ✓, CategoryRepository ✓, S3Service (platform), **UserRepository ⚠️**, **ReviewRepository ⚠️** | identity, review |
| `ReviewServiceImpl` | review | ReviewRepository ✓, **ProductRepository ⚠️** | catalog |
| `CartServiceImpl` | cart | CartRepository ✓, ProductCatalogPort ✓(port), S3Service (platform) | — ya por port |
| `WishlistServiceImpl` | wishlist | WishlistRepository ✓, ProductCatalogPort ✓(port), S3Service (platform) | — ya por port |
| `PurchaseServiceImpl` | order | PurchaseRepository ✓, PurchaseDetailRepository ✓, ProductCatalogPort ✓(port), NotificationPort ✓(port), **CartRepository ⚠️** | cart |
| `PaymentServiceImpl` | order | PurchaseRepository ✓ | — limpio |
| `NotificationServiceImpl` | notification | NotificationRepository ✓ | — limpio |

### Cruces directos pendientes (los 4 que importan)

1. `ProductServiceImpl → UserRepository` — catalog mete mano en identity (setear `product.user` / vendedor). **Fix:** introducir `UserDirectoryPort` (identity) con un `UserSnapshot`.
2. `ProductServiceImpl → ReviewRepository` — catalog agrega rating/conteo desde review. **Fix:** `ReviewStatsPort` (review). ⚠️ Genera dependencia casi circular con el #3.
3. `ReviewServiceImpl → ProductRepository` — review valida/lee producto desde catalog. **Fix:** usar el `ProductCatalogPort` ya existente.
4. `PurchaseServiceImpl → CartRepository` — order lee/vacía el carrito directo. **Fix:** `CartPort` (cart) con operaciones `findItemsByUser` / `clear`.

> **Nota sobre catalog↔review (circular):** review necesita Product (validar), y catalog
> necesita stats de review (rating). Romper con DOS ports unidireccionales
> (`ProductCatalogPort` consumido por review, `ReviewStatsPort` consumido por catalog).
> Si se vuelve molesto, la alternativa es que catalog **escuche eventos** de review
> (`ReviewCreatedEvent`) y denormalice `averageRating`/`reviewCount` en Product — esto
> ya es Fase 3 de `PLAN.MS.md` (eventos de dominio). Preferir eventos a la larga.

---

## 3. Acoplamiento a nivel de entidad (JPA @ManyToOne)

Esto NO bloquea el monolito modular, pero SÍ la separación de base de datos (Fase 4 de `PLAN.MS.md`).

| Entidad | Referencia a | Tipo | Borde |
|---------|-------------|------|-------|
| Product | Category | @ManyToOne | catalog-interno ✓ |
| Product | **User** | @ManyToOne | catalog → identity ⚠️ |
| Cart | **User**, **Product** | @ManyToOne | cart → identity, catalog ⚠️ |
| Wishlist | **User**, **Product** | @ManyToOne | wishlist → identity, catalog ⚠️ |
| Review | **User**, **Product** | @ManyToOne | review → identity, catalog ⚠️ |
| Purchase | **User** | @ManyToOne | order → identity ⚠️ |
| Purchase | PurchaseDetail | @OneToMany | order-interno ✓ |
| PurchaseDetail | Purchase | @ManyToOne | order-interno ✓ |
| PurchaseDetail | **Product** | @ManyToOne | order → catalog ⚠️ |
| Notification | **User** | @ManyToOne | notification → identity ⚠️ |
| RefreshToken | **User** | @ManyToOne | identity-interno ✓ |

**Hubs:** `User` lo referencian 7 entidades (Product, Cart, Wishlist, Review, Purchase, Notification, RefreshToken). `Product` lo referencian 4 (Cart, Wishlist, Review, PurchaseDetail). Estos dos son los que más duelen al partir la DB; en el monolito modular se quedan como relaciones JPA.

---

## 4. Layout objetivo de paquetes

```
com.racoonsfinds.backend
├── shared/        exception/, utils/ (MapperUtil, AuthUtil, ResponseUtil), dto/ApiResponse, config/
├── platform/      storage/ (S3Service), email/ (EmailService, EmailServiceImpl)
├── identity/      User, RefreshToken, Auth*, User*, UserTransactionService, JwtUtil, JwtAuthenticationFilter
├── catalog/       Product, Category, ProductService*, CategoryService, LocalProductCatalogAdapter
├── review/        Review, ReviewService*
├── cart/          Cart, CartService*
├── wishlist/      Wishlist, WishlistService*
├── order/         Purchase, PurchaseDetail, PurchaseService*, PaymentService*, Sales
└── notification/  Notification, NotificationService*, LocalNotificationAdapter
```

Cada módulo: `api/` (controllers) · `domain/` (entidades) · `repository/` · `service/` (interfaz pública + impl interna) · `dto/` · `port/`+`adapter/` cuando exponga/consuma puertos.

---

## 5. Orden de migración (por readiness, menos acoplado primero)

| # | Módulo | Readiness | Bloqueantes antes de extraer |
|---|--------|-----------|------------------------------|
| 1 | **notification** | Alta — solo NotificationRepository, ya tiene `NotificationPort` + adapter | ninguno (solo FK Notification→User a nivel DB) |
| 2 | **identity** | Alta — services self-contained, no depende de otros dominios | definir API pública (`UserDirectoryPort` para consumo externo) |
| 3 | **review** | Media — chico, pero acoplado a catalog en ambos sentidos | cruce #3 (usar ProductCatalogPort) y #2 (exponer ReviewStatsPort) |
| 4 | **catalog** | Media — 2º hub, ya expone ProductCatalogPort | cruces #1 (UserDirectoryPort) y #2 (consumir ReviewStatsPort) |
| 5 | **cart** / **wishlist** | Media-alta — ya consumen ProductCatalogPort | exponer `CartPort` para order (cruce #4) |
| 6 | **order** (purchase+payment) | Baja — nudo transaccional | cruce #4 (CartPort); a futuro sagas/outbox (Fase 6 PLAN.MS) |

---

## 6. Receta de ejecución por módulo (checklist repetible)

Para CADA módulo, en este orden, **un PR/commit por módulo**:

1. **Romper cruces de salida**: por cada dependencia ⚠️ que el módulo tenga hacia
   otro dominio, introducir/consumir un port (interfaz en `port/` + `*Snapshot`
   record para los datos que cruzan). Reemplazar el repositorio/service ajeno por el port.
2. **Adapter local**: implementar el port con un `Local*Adapter` en el módulo dueño
   de los datos (igual que `LocalProductCatalogAdapter`).
3. **Mover archivos** al paquete del dominio (`domain/`, `repository/`, `service/`,
   `dto/`, `api/`). Mantener relaciones JPA como están.
4. **Marcar API pública**: lo que otros módulos NO deben usar pasa a package-private
   o queda fuera de la API. Solo interfaces de `service/` y `port/` son públicas.
5. **Regla ArchUnit**: agregar test que prohíba a otros módulos importar
   `<modulo>.repository..` y `<modulo>.domain..` (ver §7).
6. **Verificar**: `./mvnw.cmd test` en verde antes de cerrar el módulo.

### Plan específico por cruce
- **#3 review→catalog:** en `ReviewServiceImpl` cambiar `ProductRepository` por `ProductCatalogPort.findById(...)`. Cero adapters nuevos (el port ya existe).
- **#1 catalog→identity:** crear `UserDirectoryPort { UserSnapshot findById(Long) }` + `LocalUserDirectoryAdapter` en identity. `ProductServiceImpl` lo usa para resolver el vendedor.
- **#2 catalog→review:** crear `ReviewStatsPort { Double averageRating(Long); long count(Long) }` + adapter en review. `ProductServiceImpl` lo usa en el mapeo de DTO (hoy `reviewRepository.findAverageRatingByProductId` / `countByProductId`). **Alternativa preferida a futuro:** evento `ReviewCreatedEvent` + denormalización.
- **#4 order→cart:** crear `CartPort { List<CartItemSnapshot> itemsOf(Long userId); void clear(Long userId) }` + adapter en cart. `PurchaseServiceImpl` lo usa en vez de `CartRepository`.

---

## 7. Guardrails (ArchUnit) — instalar en Fase 0, antes de mover nada

Dependencia de test (gestionada por el parent BOM si aplica, si no fijar versión):
`com.tngtech.archunit:archunit-junit5`.

Reglas mínimas a codificar como `@ArchTest`:
- Ningún módulo importa `..<otro>.repository..` ni `..<otro>.domain..` de otro módulo.
- La comunicación entre módulos solo via `..port..` o interfaces de `..service..`.
- `shared` y `platform` no dependen de ningún módulo de dominio.
- Los controllers (`api/`) no acceden a repositorios directamente.

Alternativa más completa: **Spring Modulith** (define módulos, verifica bordes, genera
docs y habilita eventos in-process para la Fase 3 de PLAN.MS). Decisión pendiente.

---

## 8. Convenciones del proyecto (respetar SIEMPRE al migrar)

Estas reglas vienen del refactor de junio 2026 y deben mantenerse intactas durante la migración:

1. **Services nunca devuelven `ResponseEntity`** — solo tipos de dominio/DTO. El
   `ResponseUtil` solo se usa en controllers.
2. **Excepciones de la jerarquía `ApiException`** (`NotFoundException`,
   `BadRequestException`, `ConflictException`, `ForbiddenException`,
   `UnauthorizedException`). Nunca `RuntimeException` cruda ni `ResourceNotFoundException`
   (esa no extiende ApiException → caería como 500). El `GlobalExceptionHandler`
   mapea la jerarquía a su status HTTP.
3. **Inyección con `@RequiredArgsConstructor` + campos `final`**. Nunca `@AllArgsConstructor`
   en services ni constructores a mano. (Excepción tolerada hoy: `SecurityConfig` usa
   `@AllArgsConstructor` con campos final — al migrarlo a `identity/config`, pasarlo a
   `@RequiredArgsConstructor`.)
4. **DTOs de respuesta** construidos en services usan `@Builder`, no setters campo a campo
   (criterio; conviven setters en código legacy).
5. **`MapperUtil`** centraliza ModelMapper. Ojo: los typeMaps que skipean una relación
   con FK implícita (ej. `categoryId`→`category.id`) deben usar `emptyTypeMap(...).addMappings(skips).implicitMappings()`,
   no `typeMap(...)`, o el bloque estático lanza `ConfigurationException` al init.

---

## 9. Tareas (servicios a migrar)

- [ ] **Fase 0 — Guardrails:** instalar ArchUnit (o Spring Modulith) + reglas de §7. Decidir cuál.
- [ ] **Romper cruce #3** — `ReviewServiceImpl` usa `ProductCatalogPort` en vez de `ProductRepository`.
- [ ] **Romper cruce #1** — `UserDirectoryPort` + adapter (identity); `ProductServiceImpl` lo consume.
- [ ] **Romper cruce #2** — `ReviewStatsPort` + adapter (review); `ProductServiceImpl` lo consume.
- [ ] **Romper cruce #4** — `CartPort` + adapter (cart); `PurchaseServiceImpl` lo consume.
- [ ] **Migrar `notification`** (módulo 1) — mover a paquete + ArchUnit + tests verdes.
- [ ] **Migrar `identity`** (módulo 2) — mover + API pública + tests.
- [ ] **Migrar `review`** (módulo 3).
- [ ] **Migrar `catalog`** (módulo 4).
- [ ] **Migrar `cart` y `wishlist`** (módulo 5).
- [ ] **Migrar `order`** (módulo 6, último).
- [ ] **(Diferido, Fase 3 PLAN.MS)** reemplazar cruces por eventos de dominio donde aplique (catalog↔review).

> Cada tarea deja el sistema compilando y con la suite (`./mvnw.cmd test`) en verde.
> No extraer a microservicio ninguna pieza hasta que su acoplamiento de datos esté resuelto.

---

## 10. Estimación de esfuerzo y paralelización

> Alcance de esta estimación: **monolito modular** (puertos + repaquetado + guardrails).
> NO incluye extracción física a microservicios (DBs separadas, despliegues
> independientes, sagas/outbox) — eso es `PLAN.MS.md` Fases 5-6, otra liga.

Trabajo solo (un dev) + agente:

| Fase | Trabajo | Esfuerzo |
|------|---------|----------|
| F0 | ArchUnit + reglas de frontera (§7) | ~0.5 día |
| Cruce #1 | `UserDirectoryPort` + adapter + tests | ~0.5 día |
| Cruce #2 | `ReviewStatsPort` + adapter | ~0.5 día |
| Cruce #3 | `ReviewServiceImpl → ProductCatalogPort` | ~0.5 día |
| Cruce #4 | `CartPort` + adapter | ~0.5 día |
| Repaquetado | notification / identity / review (chicos) | ~1 día |
| Repaquetado | catalog + order (grandes) | ~1.5 días |

**Total: ~1 a 1.5 semanas** de trabajo enfocado. Es repackaging + puertos + arreglar
imports, no reescritura de lógica → bajo riesgo (los tests verdes protegen cada paso).

### ¿Paralelizar?

Se gana **poco**. Restricciones reales:
1. **F0 va primero sí o sí** — sin las reglas ArchUnit, paralelizar es trabajar a ciegas.
2. **`User` y `Product` son cuellos de botella** — referenciados por 7 y 4 entidades (§3).
   Dos personas tocando esas entidades/paquetes a la vez = conflictos de merge → serializa
   catalog e identity de facto.
3. **catalog y order dependen de que los puertos ya existan** — no se mueven limpios hasta
   tener `UserDirectoryPort`, `ReviewStatsPort` y `CartPort`.

Lo único paralelizable sin fricción: las 4 extracciones de puerto (#1-#4) son archivos y
módulos distintos → caben 2 frentes a la vez, recortando esos ~2 días a ~1.

**Realista:** con buena paralelización bajas de ~1.5 semanas a **~1 semana**. No baja a la
mitad porque la mitad del trabajo es inherentemente secuencial. Para un repo de un dev,
**no compensa** meter varias personas (el overhead de coordinación supera la ganancia);
mejor secuencial con el agente, un módulo por commit, verde entre cada uno.
