# 07 · Seguridad

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Seguridad básica bien hecha + conciencia de hardening.

## Qué va aquí

- **Autenticación JWT**: access + refresh tokens sobre Spring Security. Flujo de emisión/validación.
- **Fortaleza de clave**: HMAC-SHA con clave ≥ 256 bits; qué pasa si es más corta (`WeakKeyException`) y por qué se valida.
- **Autorización**: cómo se protegen los endpoints, cómo se resuelve el principal (user id) en los servicios.
- **Superficie de actuator**: acotar `/actuator/**` a los endpoints públicos necesarios; no exponer `env`, `beans`, `heapdump`.
- **Manejo de secretos**: hoy por variables de entorno para dev local; nunca commiteados ni horneados en la imagen. En prod → Secrets Manager / SSM, inyectados como `secrets` en la task def (no `environment`), acceso a S3 vía IAM task role en vez de claves estáticas.

## Artefactos en el repo

- `identity/security/JwtUtil`
- `config`/`SecurityConfig`
- `shared/utils/AuthUtil`

## Enlaces

- Dónde viven los secretos en el deploy → [08-roadmap-microservices.md](08-roadmap-microservices.md)
- Inyección por entorno en contenedores → [06-containers.md](06-containers.md)
