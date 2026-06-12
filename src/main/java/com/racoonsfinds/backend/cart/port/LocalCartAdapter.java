package com.racoonsfinds.backend.cart.port;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.cart.repository.CartRepository;
import com.racoonsfinds.backend.cart.port.CartItemSnapshot;
import com.racoonsfinds.backend.cart.port.CartPort;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador local: accede al dominio Cart via DB directa.
 * Reemplazar por HttpCartAdapter al extraer cart-service.
 */
@Component
@RequiredArgsConstructor
public class LocalCartAdapter implements CartPort {

    private final CartRepository cartRepository;

    @Override
    public List<CartItemSnapshot> itemsOf(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(c -> new CartItemSnapshot(c.getProduct().getId(), c.getAmount()))
                .toList();
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}
