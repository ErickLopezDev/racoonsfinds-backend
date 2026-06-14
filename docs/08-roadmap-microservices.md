# 08 · Roadmap a microservicios

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

La visión: evolución por fases, cada una dejando el sistema desplegable.

## Qué va aquí

- **Fases**: monolito modular → observabilidad → contenedores → infra como código (Terraform + ECS/Fargate) → extracción de servicios.
- **Criterio de extracción**: un módulo se separa cuando justifica escalarse solo; ya está aislado (puertos + eventos) y listo para salir sin reescribir.
- **De evento in-process a mensajería**: los `@ApplicationModuleListener` y eventos de `shared/event` se externalizan a RabbitMQ/Kafka; los adaptadores `Local*` de los puertos pasan a `Http*`.
- **El nudo `order ↔ payment ↔ stock`**: por qué necesita una **saga** (no se puede una transacción ACID distribuida sin acoplar). Coordinación con compensaciones (si el pago falla tras reservar stock, se libera el stock).
- **Arquitectura destino híbrida**: REST síncrono en el borde + eventos asíncronos entre dominios.
- **Infra AWS**: ECR, ECS/Fargate detrás de ALB, RDS Postgres, autoscaling, logs a CloudWatch.
- **Observabilidad en AWS**: self-managed (Prometheus/Grafana en ECS) vs AMP + AMG (managed). Trade-offs.
- **CI/CD**: GitHub Actions (test → build → push ECR → deploy ECS), Terraform plan/apply, auth por OIDC.

## Enlaces

- El desacople que habilita esto → [02-domain-events.md](02-domain-events.md)
- Decisiones pendientes → [09-decisions.md](09-decisions.md)
