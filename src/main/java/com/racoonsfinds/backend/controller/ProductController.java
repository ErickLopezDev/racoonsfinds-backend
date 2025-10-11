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

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.products.PagedResponse;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.service.ProductService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Crear producto:
     * - multipart/form-data con:
     *    - file: imagen (opcional)
     *    - product: JSON string (ProductCreateRequest)
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponseDto>> create(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("product") String productJson) throws IOException {

        ProductResponseDto dto = productService.createProduct(file, productJson);
        return ResponseUtil.created("Producto creado correctamente", dto);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponseDto>> update(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("product") String productJson) throws IOException {
        return ResponseUtil.ok("Producto actualizado correctamente", productService.updateProduct(id, file, productJson));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> get(@PathVariable Long id) {
        return ResponseUtil.ok("Producto obtenido correctamente", productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDto>>> all(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "createdDate") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        return ResponseUtil.ok("Productos obtenidos correctamente",
            productService.findAllPaged(page, size, categoryId, search, sortBy, sortDir));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseUtil.ok("Producto eliminado correctamente");
    }
}
