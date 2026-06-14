# 05 · Observabilidad

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Observabilidad como parte del diseño, reproducible con un comando.

## Qué va aquí

- **Instrumentación**: Actuator + Micrometer exponen métricas en formato Prometheus (`/actuator/prometheus`), con histogramas de latencia de requests HTTP (`distribution.percentiles-histogram`).
- **Stack reproducible**: Prometheus + Grafana levantan con `docker compose up`, con **provisioning as code** — datasource y dashboard se cargan solos, sin clickear.
- **Métricas técnicas (gratis)**: JVM/GC, HTTP server requests (rate, p99), pool HikariCP.
- **Métricas de negocio (pendiente)**: `Counter` de compras creadas, fallos de pago, `@Timed` en subida a S3. Qué medir y por qué.
- **Health checks**: liveness/readiness (`health.probes.enabled`) listos para orquestadores (ECS).
- **Hardening de actuator (pendiente)**: limitar `/actuator/**` a `health,info,prometheus`; considerar puerto de management aparte o restricción por red/ALB.
- **Qué vigilar en prod**: error rate, p99 latencia, saturación de Hikari, memoria/GC, métricas de negocio. Alertas básicas.
- **Screenshot del dashboard** (para la sección visible en una revisión).

## Artefactos en el repo

- `monitoring/prometheus.yml`
- `monitoring/grafana/provisioning/` (datasource + dashboard "Racoonsfinds — Overview")
- `application.yaml` (`management.endpoints`, métricas)

## Enlaces

- Cómo se levanta junto al resto → [06-containers.md](06-containers.md)
- Observabilidad en AWS → [08-roadmap-microservices.md](08-roadmap-microservices.md)
