package com.racoonsfinds.backend.catalog.port;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.catalog.repository.ProductRepository;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador local: accede al dominio Catalog via DB directa.
 * Reemplazar por HttpProductCatalogAdapter al extraer catalog-service.
 */
@Component
@RequiredArgsConstructor
public class LocalProductCatalogAdapter implements ProductCatalogPort {

    private final ProductRepository productRepository;

    @Override
    public ProductSnapshot findById(Long productId) {
        return toSnapshot(productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado")));
    }

    @Override
    public List<ProductSnapshot> findAllByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    @Transactional
    public void decrementStock(Long productId, int amount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        int newStock = product.getStock() - amount;
        if (newStock < 0)
            throw new BadRequestException("Stock insuficiente para: " + product.getName());
        product.setStock(newStock);
        productRepository.save(product);
    }

    private ProductSnapshot toSnapshot(Product p) {
        return new ProductSnapshot(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getImage(),
                p.getStock(),
                p.getUserId()
        );
    }
}
