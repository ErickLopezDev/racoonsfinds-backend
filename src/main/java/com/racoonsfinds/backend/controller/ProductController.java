package com.racoonsfinds.backend.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Crear producto:
     * - multipart/form-data con:
     *    - file: imagen (opcional)
     *    - product: JSON string (ProductCreateRequest)
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> create(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("product") String productJson) throws IOException {

        ProductResponseDto dto = productService.createProduct(file, productJson);
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("product") String productJson) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, file, productJson));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<?> all(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "createdDate") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(productService.findAllPaged(page, size, categoryId, search, sortBy, sortDir));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
