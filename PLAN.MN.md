# Plan del Monolito: Observabilidad + Containerización + Deploy a AWS

Backend de racoonsfinds (Spring Boot 3.5.6, Java 21, Maven, PostgreSQL, S3, Resend).
Objetivo: dejar el **monolito actual** observable, dockerizado y desplegado en AWS
con ECS + Fargate, gestionado por Terraform. Independiente del plan de microservicios
(ver `PLAN.MS.md`); de hecho es prerrequisito sano: no se separa lo que no se observa.

Orden recomendado: **Observabilidad → Docker (local) → Terraform/AWS → CI/CD**.

---

## Fase 1 — Observabilidad (Prometheus + Grafana)

El `pom.xml` ya incluye `spring-boot-starter-actuator` y
`micrometer-registry-prometheus`, así que la instrumentación base existe.

- [ ] **Fix de versiones:** quitar las versiones explícitas de `actuator` (3.3.2) y
      `micrometer-registry-prometheus` (1.13.6) en `pom.xml` — el `spring-boot-starter-parent`
      3.5.6 ya las gestiona vía BOM. Pinearlas a versiones viejas puede causar
      incompatibilidades.
- [ ] Exponer el endpoint en `application.yaml`:
      ```yaml
      management:
        endpoints:
          web:
            exposure:
              include: health,info,prometheus
        endpoint:
          health:
            probes:
              enabled: true   # liveness/readiness para ECS
        metrics:
          tags:
            application: ${spring.application.name}
      ```
- [ ] Verificar `GET /actuator/prometheus` localmente (formato de scrape).
- [ ] Definir métricas de negocio con Micrometer (`@Timed`, `Counter`): compras
      creadas, fallos de pago, latencia de S3, etc. (las técnicas — JVM, HTTP, HikariCP —
      vienen gratis).
- [ ] Asegurar que `/actuator/**` quede accesible para el scrape pero protegido del
      exterior en `SecurityConfig` (permitir solo desde la red interna / sin auth solo
      para health+prometheus).

**Entregable:** la app emite métricas en formato Prometheus.

---

## Fase 2 — Docker (entorno local reproducible)

- [ ] **Dockerfile multi-stage** (build con Maven + JDK 21, runtime con JRE slim):
      ```dockerfile
      FROM eclipse-temurin:21-jdk AS build
      WORKDIR /app
      COPY .mvn/ .mvn/
      COPY mvnw pom.xml ./
      RUN ./mvnw dependency:go-offline -B
      COPY src ./src
      RUN ./mvnw clean package -DskipTests

      FROM eclipse-temurin:21-jre AS runtime
      WORKDIR /app
      COPY --from=build /app/target/*.jar app.jar
      EXPOSE 8080
      ENTRYPOINT ["java","-jar","app.jar"]
      ```
- [ ] `.dockerignore` (target/, .git/, *.md, .env).
- [ ] **docker-compose.yml** para desarrollo local con todo el stack:
      `app` + `postgres` + `prometheus` + `grafana`.
      (Nota: ya existe `spring-boot-docker-compose` como dependencia runtime —
      decidir si se usa esa integración o un compose manual.)
- [ ] `prometheus.yml` con un scrape job apuntando a `app:8080/actuator/prometheus`.
- [ ] Provisionar Grafana: datasource Prometheus + dashboards (JVM Micrometer +
      Spring Boot Statistics, IDs 4701 / 6756) como provisioning-as-code.
- [ ] Externalizar config por variables de entorno (ya está: `POSTGRES_*`, `JWT_SECRET`,
      `AWS_*`, `RESEND_*`). Nunca hornear secretos en la imagen.

**Entregable:** `docker compose up` levanta app + DB + Prometheus + Grafana local.

---

## Fase 3 — Infraestructura AWS con Terraform (ECS + Fargate)

Estructura Terraform sugerida:

```
infra/
├── modules/
│   ├── network/      (VPC, subnets pública/privada, NAT, SG)
│   ├── ecr/          (repositorio de imágenes)
│   ├── rds/          (PostgreSQL)
│   ├── ecs/          (cluster, task def, service Fargate, ALB, target group)
│   ├── observability/(scrape + dashboards — ver decisión abajo)
│   └── secrets/      (Secrets Manager / SSM Parameter Store)
└── envs/
    ├── dev/
    └── prod/
```

- [ ] **State remoto:** backend S3 + lock en DynamoDB antes de aplicar nada.
- [ ] **network:** VPC con subnets privadas para ECS/RDS y públicas para el ALB.
- [ ] **ecr:** repo para la imagen; push desde CI.
- [ ] **rds:** PostgreSQL en subnet privada; sustituye al `localhost:5432` del yaml.
- [ ] **secrets:** mover `JWT_SECRET`, credenciales de RDS, `RESEND_API_KEY`, claves
      AWS a Secrets Manager; inyectarlas en la task def como `secrets` (no `environment`).
- [ ] **ecs/Fargate:**
  - Task definition: imagen de ECR, CPU/memoria, puerto 8080, logs a CloudWatch.
  - Service detrás de un **ALB**; health check del target group → `/actuator/health/readiness`.
  - Autoscaling por CPU/memoria (target tracking).
- [ ] **S3:** el bucket de imágenes ya existe; gestionarlo en Terraform (o `data` source
      si se mantiene fuera) y dar permisos vía **IAM task role** en vez de claves estáticas
      (`AWS_ACCESS_KEY_ID`/`SECRET`) — quitar esas keys del entorno cuando corra en ECS.

**Entregable:** `terraform apply` levanta toda la infra; la app corre en Fargate
detrás del ALB con RDS y secretos gestionados.

---

## Fase 4 — Observabilidad en AWS

Decisión clave (anotada abajo): cómo correr Prometheus/Grafana en la nube.

- **Opción A — Self-managed en ECS:** Prometheus y Grafana como servicios Fargate
  propios. Más control y barato, pero vos mantenés scrape, storage y HA.
  Scrape de tasks dinámicas vía ECS service discovery.
- **Opción B — AWS Managed (recomendada para empezar):** **AMP** (Amazon Managed
  Service for Prometheus) + **AMG** (Amazon Managed Grafana). Menos operación;
  usás el **ADOT collector** como sidecar para hacer remote-write a AMP.

- [ ] Elegir A o B.
- [ ] Configurar el scrape de `/actuator/prometheus` de las tasks de ECS.
- [ ] Recrear los dashboards de la Fase 2 en Grafana/AMG.
- [ ] Alertas básicas: error rate HTTP, p99 latencia, saturación de pool de conexiones,
      memoria JVM, health del target group.

**Entregable:** métricas del monolito visibles y con alertas en producción.

---

## Fase 5 — CI/CD

- [ ] Pipeline (GitHub Actions): test → build imagen → push a ECR → actualizar
      service de ECS (rolling o blue/green con CodeDeploy).
- [ ] `terraform plan` en PR, `apply` en merge a main (con aprobación manual para prod).
- [ ] Inyectar secretos del pipeline desde OIDC a AWS (sin claves estáticas en CI).

**Entregable:** push a main → deploy automatizado a Fargate.

---

## Decisiones pendientes

1. **Observabilidad en AWS:** self-managed en ECS (opción A) vs AMP + AMG (opción B).
2. **DB:** RDS estándar vs Aurora Serverless v2 (escala a cero en dev).
3. **Despliegue:** rolling update simple vs blue/green con CodeDeploy.
4. **docker-compose local:** usar la integración `spring-boot-docker-compose` ya
   presente vs compose manual independiente.

## Notas / deuda detectada

- `pom.xml`: versiones pineadas de actuator (3.3.2) y micrometer (1.13.6) divergen
  del parent 3.5.6 → quitar versiones explícitas (Fase 1).
- `application.yaml`: `logging.level.org.springframework.security: DEBUG` no debería
  ir a prod (ruidoso y filtra detalle de auth) → bajar a `INFO`/`WARN` por perfil.
- Credenciales AWS estáticas en el entorno → reemplazar por IAM task role en ECS.
