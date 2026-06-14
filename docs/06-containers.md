# 06 · Contenedores y entorno local

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Build/deploy entendido: imágenes optimizadas y entorno reproducible.

## Qué va aquí

- **Dockerfile multi-stage**: una etapa compila con Maven (cacheando dependencias aparte del código), otra extrae las **capas de Spring Boot** (`jarmode=tools extract --layers`) con nombre de jar estable.
- **Por qué layered jars**: las dependencias cambian poco y el código mucho; separarlas en capas hace que un cambio de una línea solo recopie la capa de aplicación, no cientos de MB de deps.
- **Runtime non-root**: usuario `spring` dedicado, principio de mínimo privilegio.
- **Decisión de imagen base**: se mantiene `eclipse-temurin:21-jdk-jammy` en runtime (preferencia consciente); nota sobre la optimización a JRE slim si se quisiera reducir tamaño.
- **`.dockerignore`**: contexto de build limpio, no filtrar secretos (`.env`), excluir `target/`, `.git/`, apuntes.
- **`docker compose`**: orquesta app + Postgres + Prometheus + Grafana. **Healthcheck** en la DB (`pg_isready`) + `depends_on: condition: service_healthy` para evitar el race de arranque (la app esperaba a una DB no lista → "Unable to determine Dialect").
- **Configuración por entorno**: variables (`POSTGRES_*`, `JWT_SECRET`, `AWS_*`), nunca horneadas en la imagen.

## Artefactos en el repo

- `Dockerfile` (multi-stage, layered, non-root)
- `.dockerignore`
- `compose.yaml` (healthcheck + service_healthy)

## Enlaces

- El stack de observabilidad que orquesta → [05-observability.md](05-observability.md)
- Secretos en local vs prod → [07-security.md](07-security.md)
