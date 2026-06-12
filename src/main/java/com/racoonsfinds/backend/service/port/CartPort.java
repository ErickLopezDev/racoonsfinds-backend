package com.racoonsfinds.backend.service.port;

import java.util.List;

/**
 * Puerto hacia el dominio Cart.
 * Implementación actual: LocalCartAdapter (DB directa).
 * En microservicios: HttpCartAdapter (REST al cart-service).
 */
public interface CartPort {

    List<CartItemSnapshot> itemsOf(Long userId);

    void clear(Long userId);
}
