package com.racoonsfinds.backend.identity.port;

/**
 * Puerto hacia el dominio Identity.
 * Implementación actual: LocalUserDirectoryAdapter (DB directa).
 * En microservicios: HttpUserDirectoryAdapter (REST al identity-service).
 */
public interface UserDirectoryPort {

    UserSnapshot findById(Long userId);
}
