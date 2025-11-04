package com.racoonsfinds.backend.controller;

import java.io.IOException;
import java.math.BigDecimal;

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
import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.service.int_.ProductService;
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
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "stock", required = false) String stock,
            @RequestPart(value = "price", required = false) String price,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "categoryId", required = false) String categoryId,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        // Crear el DTO y asignar los valores de las partes
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name(name)
                .stock(stock != null ? Integer.parseInt(stock) : null)
                .price(price != null ? new BigDecimal(price) : null)
                .description(description)
                .categoryId(categoryId != null ? Long.parseLong(categoryId) : null)
                .build();

        ProductResponseDto dto = productService.createProduct(file, productRequestDto);
        return ResponseUtil.created("Producto creado correctamente", dto);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponseDto>> update(
            @PathVariable Long id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "stock", required = false) String stock,
            @RequestPart(value = "price", required = false) String price,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "categoryId", required = false) String categoryId,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        // Crear el DTO como en create()
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name(name)
                .stock(stock != null ? Integer.parseInt(stock) : null)
                .price(price != null ? new BigDecimal(price) : null)
                .description(description)
                .categoryId(categoryId != null ? Long.parseLong(categoryId) : null)
                .build();

        ProductResponseDto dto = productService.updateProduct(id, file, productRequestDto);
        return ResponseUtil.ok("Producto actualizado correctamente", dto);
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


   @GetMapping("/user")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDto>>> getProductsByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseUtil.ok(
            "Productos del usuario obtenidos correctamente",
            productService.findAllPagedByUserId(page, size, categoryId, search, sortBy, sortDir)
        );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseUtil.ok("Producto eliminado correctamente");
    }
}
