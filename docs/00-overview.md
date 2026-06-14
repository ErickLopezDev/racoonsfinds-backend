# 00 · Visión y dominio

> **Estado:** esqueleto — plan de contenido. Pendiente de redactar.

Punto de entrada conceptual: qué problema resuelve el sistema y por qué está construido así.

## Qué va aquí

- **El dominio**: marketplace — actores (compradores, vendedores), capacidades (catálogo, carrito, órdenes, reseñas, wishlist, notificaciones).
- **Alcance y no-alcance**: qué cubre el proyecto y qué deliberadamente queda fuera (pagos reales, envíos, etc.).
- **Objetivo de ingeniería**: por qué este repo existe como ejercicio de *backend / sistemas distribuidos*, no como producto.
- **Por qué monolito modular (no microservicios desde el día 1)**: el argumento de descubrir acoplamientos reales antes de pagar latencia de red, consistencia eventual y complejidad de despliegue.
- **Mapa de módulos**: una frase por módulo (identity, catalog, cart, order, review, notification, wishlist, platform, shared).
- **Principios rectores**: boundaries ejecutables, desacople por eventos, testing contra dependencias reales, observabilidad como parte del diseño.

## Enlaces

- Detalle de arquitectura → [01-architecture.md](01-architecture.md)
- Justificación de cada decisión → [09-decisions.md](09-decisions.md)
