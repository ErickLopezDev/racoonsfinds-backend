# Microservicios — Plan de Arquitectura y Guía de Implementación
> racoonsfinds-backend · cuenta AWS Free Tier

---

## 1. Visión General

```
Cliente (Frontend)
        │
        ▼
┌───────────────────┐
│   API Gateway     │  Spring Cloud Gateway — valida JWT, rutea requests
│   puerto 8080     │
└────────┬──────────┘
         │
    ┌────┴─────────────────────────────────┐
    │                                      │
    ▼                                      ▼
auth-service                        catalog-service
Spring Boot · puerto 8081           Spring Boot · puerto 8082
DB: auth_db                         DB: catalog_db
                                    Cache: Redis
    │                                      │
    └──────────────┬───────────────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
         ▼                   ▼
  commerce-service     payment-service
  Spring Boot          Spring Boot + Stripe
  puerto 8083          puerto 8084
  DB: commerce_db      DB: payment_db

         │                   │
         └─────────┬─────────┘
                   │ RabbitMQ events
                   ▼
        notification-service
        AWS Lambda (serverless)
        Trigger: SQS
        Email: Amazon SES (free tier)
        DB: notification_db
```

---

## 2. Separación de Dominios

### 2.1 auth-service
**Responsabilidad:** identidad, sesiones, verificación de email

| Capa | Contenido actual |
|------|-----------------|
| Models | `User`, `RefreshToken` |
| Services | `AuthServiceImpl`, `RefreshTokenService`, `UserTransactionService`, `UserServiceImpl` |
| Services (infra) | `EmailServiceImpl` |
| Controllers | `AuthController`, `UserController` |
| DB schema | `auth_db` |

**Expone:**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/verify`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET  /api/user/me`
- `PUT  /api/user`

---

### 2.2 catalog-service
**Responsabilidad:** qué se vende, reseñas, categorías

| Capa | Contenido actual |
|------|-----------------|
| Models | `Product`, `Category`, `Review` |
| Services | `ProductServiceImpl`, `CategoryService`, `ReviewServiceImpl`, `S3Service` |
| Controllers | `ProductController`, `CategoryController`, `ReviewController` |
| DB schema | `catalog_db` |
| Cache | Redis (productos individuales, TTL 5 min) |

**Expone:**
- `GET/POST/PUT/DELETE /api/products`
- `GET /api/categories`
- `GET/POST /api/reviews`

**Cambio importante:** `ProductServiceImpl.mapToDto()` ya no llama a `ReviewRepository` para el rating.
El rating se guarda desnormalizado en `products.average_rating` y se actualiza cuando llega el evento `review.created`.

---

### 2.3 commerce-service
**Responsabilidad:** carrito, wishlist, órdenes de compra

| Capa | Contenido actual |
|------|-----------------|
| Models | `Cart`, `Wishlist`, `Purchase`, `PurchaseDetail` |
| Services | `CartServiceImpl`, `WishlistServiceImpl`, `PurchaseServiceImpl` |
| Controllers | `CartController`, `WishlistController`, `PurchaseController`, `SalesController` |
| DB schema | `commerce_db` |
| Port | `ProductCatalogPort` → HTTP call a catalog-service |
| Port | `NotificationPort` → RabbitMQ event |

**Cambio en Purchase:** cuando el usuario compra, la purchase queda en `PENDING`.
El stock **no se descuenta** aquí — se descuenta cuando `payment-service` confirma el pago vía evento.

**Expone:**
- `GET/POST/DELETE /api/cart`
- `GET/POST/DELETE /api/wishlist`
- `POST /api/purchases/from-cart`
- `GET  /api/purchases`
- `GET  /api/sales`

---

### 2.4 payment-service
**Responsabilidad:** integración real con Stripe

| Capa | Contenido |
|------|-----------|
| Models | `PaymentRecord` (nuevo — guarda historial de pagos) |
| Services | `StripePaymentService` |
| Controllers | `PaymentController` |
| DB schema | `payment_db` |
| External | Stripe API, Stripe Webhooks |

**Flujo Stripe:**
```
1. Frontend → POST /api/payments/intent { purchaseId }
2. payment-service crea PaymentIntent en Stripe
3. Devuelve { clientSecret } al frontend
4. Frontend usa Stripe.js para capturar tarjeta (nunca toca tu servidor)
5. Stripe llama POST /api/payments/webhook
6. payment-service valida firma (Stripe-Signature header)
7. Publica payment.success o payment.failed a RabbitMQ
```

**Stripe Free:** cuenta de test tiene tarjetas de prueba, sin costo real.
Clave de test: empieza con `sk_test_...`

---

### 2.5 notification-service (AWS Lambda)
**Responsabilidad:** emails y notificaciones in-app

| Aspecto | Detalle |
|---------|---------|
| Runtime | Java 21 o Node.js 20 |
| Trigger | SQS queue (`notifications-queue`) |
| Email | Amazon SES (free tier: 62,000 emails/mes desde EC2/Lambda) |
| DB | `notification_db` vía RDS (guarda historial) |
| Deploy | AWS Lambda — free tier: 1M requests/mes |

**Por qué Lambda aquí:**
- Las notificaciones llegan en ráfagas (muchas compras a la vez)
- No necesita estado ni servidor corriendo 24/7
- Free tier de Lambda cubre perfectamente la escala de un proyecto portfolio

---

## 3. Mensajería — RabbitMQ

### Proveedor recomendado (free tier)
**CloudAMQP** — plan "Little Lemur" gratuito:
- 1 millón de mensajes/mes
- 3 conexiones concurrentes
- Suficiente para desarrollo y demo

URL: https://www.cloudamqp.com (signup gratis, no requiere tarjeta)

### Exchanges y Queues

```
Exchange: purchase.exchange (fanout)
  └── purchase.completed.payment.queue  → payment-service consumer
  └── purchase.completed.notify.queue   → notification-service (via SQS bridge)

Exchange: payment.exchange (topic)
  ├── payment.success
  │     └── payment.success.commerce.queue   → commerce-service (actualiza status)
  │     └── payment.success.catalog.queue    → catalog-service (descuenta stock)
  │     └── payment.success.notify.queue     → notification-service
  │
  └── payment.failed
        └── payment.failed.commerce.queue    → commerce-service (marca FAILED)
        └── payment.failed.catalog.queue     → catalog-service (revierte stock)
        └── payment.failed.notify.queue      → notification-service
```

### Eventos (payload JSON)

```json
// purchase.completed
{
  "purchaseId": 42,
  "buyerId": 7,
  "items": [
    { "productId": 1, "sellerId": 3, "amount": 2, "price": 25.00 }
  ],
  "total": 50.00
}

// payment.success
{
  "purchaseId": 42,
  "stripePaymentIntentId": "pi_xxx",
  "amount": 50.00,
  "buyerId": 7
}

// payment.failed
{
  "purchaseId": 42,
  "reason": "insufficient_funds",
  "buyerId": 7
}
```

---

## 4. Base de Datos

### Estrategia — Free Tier AWS RDS
- **1 instancia RDS** `db.t3.micro` (750 horas/mes gratis, 20 GB storage)
- PostgreSQL con **schemas separados** por servicio
- En prod real serían instancias separadas, pero para portfolio/demo esto es suficiente

```sql
-- Cada servicio se conecta a su propio schema
-- auth-service:
spring.datasource.url=jdbc:postgresql://host:5432/racoonsfinds?currentSchema=auth_db

-- catalog-service:
spring.datasource.url=jdbc:postgresql://host:5432/racoonsfinds?currentSchema=catalog_db

-- commerce-service:
spring.datasource.url=jdbc:postgresql://host:5432/racoonsfinds?currentSchema=commerce_db

-- payment-service:
spring.datasource.url=jdbc:postgresql://host:5432/racoonsfinds?currentSchema=payment_db

-- notification-service:
spring.datasource.url=jdbc:postgresql://host:5432/racoonsfinds?currentSchema=notification_db
```

### Datos duplicados (normal en microservicios)
`commerce_db.purchase_details` guarda `product_id` (Long) + `product_name` + `price` en el momento de la compra.
No hay FK al catalog. Si el producto se elimina después, la compra sigue teniendo el historial correcto.

---

## 5. AWS Free Tier — Qué usar y límites

| Servicio AWS | Uso | Límite free |
|---|---|---|
| **Lambda** | notification-service | 1M requests/mes · 400K GB-seg compute |
| **SQS** | Cola de notificaciones | 1M requests/mes |
| **SES** | Emails | 62K emails/mes (desde Lambda) |
| **RDS** | Base de datos (db.t3.micro) | 750 horas/mes · 20 GB |
| **S3** | Imágenes (ya existe) | 5 GB · 20K GET · 2K PUT |
| **API Gateway** | Exponer Lambda si se necesita HTTP | 1M calls/mes |
| **CloudWatch** | Logs de Lambda | 5 GB logs/mes |
| **EC2 t3.micro** | Correr los Spring Boot services | 750 horas/mes |

**Lo que NO es free:**
- Amazon MQ (RabbitMQ managed) — usar CloudAMQP gratis en su lugar
- ECS/Fargate — usar EC2 con Docker directamente
- ElastiCache (Redis) — usar Upstash Redis gratis (10K commands/día)

**Redis alternativa gratis:** Upstash — https://upstash.com (plan free: 10K requests/día, suficiente para demo)

---

## 6. Estructura del Proyecto — Maven Multi-Module

```
racoonsfinds/
├── pom.xml                        ← parent POM
├── microservices.plan.md
├── docker-compose.yml             ← local dev: todos los servicios
├── api-gateway/
│   ├── pom.xml
│   └── src/
├── auth-service/
│   ├── pom.xml
│   └── src/
├── catalog-service/
│   ├── pom.xml
│   └── src/
├── commerce-service/
│   ├── pom.xml
│   └── src/
├── payment-service/
│   ├── pom.xml
│   └── src/
├── notification-lambda/
│   ├── pom.xml                    ← packaging: jar (para Lambda)
│   └── src/
└── shared-lib/                    ← DTOs de eventos compartidos
    ├── pom.xml
    └── src/
        └── events/
            ├── PurchaseCompletedEvent.java
            └── PaymentResultEvent.java
```

---

## 7. Docker Compose — Desarrollo Local

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: racoonsfinds
      POSTGRES_USER: racoon
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - ./init-schemas.sql:/docker-entrypoint-initdb.d/init.sql

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"  # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: racoon
      RABBITMQ_DEFAULT_PASS: secret

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on: [auth-service, catalog-service, commerce-service]

  auth-service:
    build: ./auth-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/racoonsfinds?currentSchema=auth_db

  catalog-service:
    build: ./catalog-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/racoonsfinds?currentSchema=catalog_db
      REDIS_HOST: redis

  commerce-service:
    build: ./commerce-service
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/racoonsfinds?currentSchema=commerce_db
      CATALOG_SERVICE_URL: http://catalog-service:8082
      RABBITMQ_HOST: rabbitmq

  payment-service:
    build: ./payment-service
    ports:
      - "8084:8084"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/racoonsfinds?currentSchema=payment_db
      STRIPE_SECRET_KEY: sk_test_...
      STRIPE_WEBHOOK_SECRET: whsec_...
      RABBITMQ_HOST: rabbitmq
```

---

## 8. Fases de Implementación

### Fase 1 — Setup del proyecto multi-module (3-4 días)
- [ ] Crear repositorio nuevo `racoonsfinds` (monorepo)
- [ ] Configurar `pom.xml` parent con módulos
- [ ] Crear `shared-lib` con los records de eventos (`PurchaseCompletedEvent`, `PaymentResultEvent`)
- [ ] Configurar `docker-compose.yml` con Postgres, RabbitMQ, Redis
- [ ] Crear `init-schemas.sql` que crea los 5 schemas en Postgres

### Fase 2 — auth-service (3-4 días)
- [ ] Copiar código de auth del monolito
- [ ] Ajustar `application.yaml` para schema `auth_db`
- [ ] Verificar que el JWT generado funcione (mismo secret que usarán los demás servicios)
- [ ] Dockerizar

### Fase 3 — catalog-service (4-5 días)
- [ ] Copiar código de catalog del monolito
- [ ] Agregar `spring-boot-starter-data-redis` y cachear `GET /products/{id}`
- [ ] Agregar columna `average_rating` en `products` table
- [ ] Crear consumer de `review.created` que actualiza el rating
- [ ] Dockerizar

### Fase 4 — commerce-service (4-5 días)
- [ ] Copiar código de commerce del monolito
- [ ] Cambiar `LocalProductCatalogAdapter` → `HttpProductCatalogAdapter` con `WebClient`
- [ ] Cambiar `LocalNotificationAdapter` → `RabbitMQNotificationAdapter`
- [ ] Cambiar `purchaseFromCart` para no decrementar stock — solo publicar `purchase.completed`
- [ ] Agregar consumer de `payment.success` → actualizar purchase status + confirmar stock decrement
- [ ] Agregar consumer de `payment.failed` → revertir purchase
- [ ] Dockerizar

### Fase 5 — payment-service con Stripe (5-6 días)
- [ ] Crear proyecto nuevo, agregar dependencia `stripe-java`
- [ ] Crear cuenta Stripe test en https://dashboard.stripe.com
- [ ] Implementar `POST /payments/intent` → crea `PaymentIntent` en Stripe
- [ ] Implementar `POST /payments/webhook` → valida firma con `Stripe-Signature`
- [ ] Publicar `payment.success` o `payment.failed` a RabbitMQ según resultado
- [ ] Guardar registro en `payment_db.payment_records`
- [ ] Probar con tarjeta de test Stripe: `4242 4242 4242 4242`
- [ ] Probar pago fallido con: `4000 0000 0000 0002`
- [ ] Dockerizar

### Fase 6 — API Gateway (2-3 días)
- [ ] Agregar `spring-cloud-starter-gateway`
- [ ] Configurar rutas en `application.yaml`
- [ ] Agregar `GlobalFilter` que valida el JWT antes de rutear
- [ ] Los servicios downstream confían en el header `X-User-Id` que el gateway agrega
- [ ] Dockerizar

```yaml
# api-gateway/src/main/resources/application.yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**, /api/user/**
        - id: catalog
          uri: http://catalog-service:8082
          predicates:
            - Path=/api/products/**, /api/categories/**, /api/reviews/**
        - id: commerce
          uri: http://commerce-service:8083
          predicates:
            - Path=/api/cart/**, /api/wishlist/**, /api/purchases/**, /api/sales/**
        - id: payment
          uri: http://payment-service:8084
          predicates:
            - Path=/api/payments/**
```

### Fase 7 — notification-service Lambda (4-5 días)
- [ ] Crear cuenta AWS (free tier)
- [ ] Crear función Lambda en Java 21 (o Node.js si preferís algo más liviano)
- [ ] Crear SQS queue `notifications-queue` en AWS
- [ ] Conectar CloudAMQP con SQS usando el plugin Shovel de RabbitMQ (reenvía mensajes a SQS)
- [ ] Configurar trigger SQS → Lambda en la consola de AWS
- [ ] Verificar email en Amazon SES (sandbox mode gratis)
- [ ] Implementar lógica: recibe evento SQS → parsea tipo → envía email via SES
- [ ] Guardar notificación en `notification_db` via RDS

**Lambda handler básico:**
```java
public class NotificationHandler implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            NotificationEvent evt = parseEvent(message.getBody());
            sesClient.sendEmail(buildEmail(evt));
            saveToDb(evt);
        }
        return null;
    }
}
```

---

## 9. Configuración Stripe paso a paso

1. Ir a https://dashboard.stripe.com → crear cuenta
2. En el dashboard ir a **Developers → API Keys**
3. Copiar `Publishable key` (para el frontend) y `Secret key` (para el backend)
4. Para webhooks locales instalar Stripe CLI:
   ```bash
   stripe listen --forward-to localhost:8084/api/payments/webhook
   ```
   Esto da el `webhook secret` para desarrollo local
5. En producción: crear webhook en el dashboard apuntando a tu URL real

**Dependencia Maven:**
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>26.3.0</version>
</dependency>
```

---

## 10. Qué demostrar en el CV / README

```
✅ Arquitectura de microservicios con 5 servicios independientes
✅ Comunicación async con RabbitMQ (eventos: purchase, payment)
✅ Serverless con AWS Lambda + SQS trigger
✅ Integración real de pagos con Stripe + Webhooks
✅ API Gateway centralizado con validación de JWT
✅ Cache con Redis (catalog-service)
✅ Pattern Port/Adapter para desacoplamiento entre dominios
✅ Base de datos aislada por dominio (PostgreSQL multi-schema)
✅ Docker Compose para desarrollo local completo
✅ CI/CD con GitHub Actions (ya existe en el repo)
```

---

## 11. Orden de commits sugerido para el CV

Hacer el trabajo en un monorepo nuevo con commits descriptivos:
```
feat: initialize multi-module Maven project structure
feat(auth): extract auth-service from monolith
feat(catalog): extract catalog-service with Redis caching
feat(commerce): wire HttpProductCatalogAdapter replacing direct DB access
feat(commerce): publish purchase.completed event via RabbitMQ
feat(payment): integrate Stripe PaymentIntent flow
feat(payment): add Stripe webhook handler with signature validation
feat(gateway): add Spring Cloud Gateway with JWT filter
feat(notification): deploy notification Lambda with SQS trigger
feat(notification): send transactional emails via Amazon SES
```
