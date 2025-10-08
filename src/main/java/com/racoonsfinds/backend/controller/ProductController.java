package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Create a new product with image
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestPart("product") ProductRequestDto productRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponseDto responseDTO = productService.createProduct(productRequestDTO, image);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Get a product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto responseDTO = productService.getProductById(id);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Update a product with optional new image
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequestDto productRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponseDto responseDTO = productService.updateProduct(id, productRequestDTO, image);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    // Delete a product (soft delete, but delete image from S3)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}