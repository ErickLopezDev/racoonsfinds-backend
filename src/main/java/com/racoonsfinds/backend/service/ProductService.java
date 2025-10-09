package com.racoonsfinds.backend.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.dto.products.ProductUpdateRequest;
import com.racoonsfinds.backend.model.Category;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.CategoryRepository;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.shared.exception.ResourceNotFoundException;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    /**
     * Crea un producto desde multipart: recibe 'file' y 'product' (json string)
     */
    @Transactional
    public ProductResponseDto createProduct(MultipartFile file, String productJson) throws IOException {
        ProductRequestDto req = objectMapper.readValue(productJson, ProductRequestDto.class);

        Product product = new Product();
        // Mapea campos simples desde request a entity usando MapperUtil
        MapperUtil.map(req, product);

        // Set fechas / defaults
        product.setCreatedDate(LocalDate.now());
        product.setEliminado(false);

        // Asigna category si existe
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found " + req.getCategoryId()));
            product.setCategory(category);
        }

        // Asigna user si existe
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found " + req.getUserId()));
            product.setUser(user);
        }

        // Si viene archivo lo subimos a S3 y guardamos la key
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            product.setImage(key);
        }

        Product saved = productRepository.save(product);
        ProductResponseDto dto = MapperUtil.map(saved, ProductResponseDto.class);

        // Convertimos 'image' key a URL accesible (si quieres)
        if (saved.getImage() != null) {
            dto.setImage(s3Service.getFileUrl(saved.getImage()));
        }

        // Seteamos categoryId y userId en DTO
        if (saved.getCategory() != null) dto.setCategoryId(saved.getCategory().getId());
        if (saved.getUser() != null) dto.setUserId(saved.getUser().getId());

        return dto;
    }

    public ProductResponseDto getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found " + id));
        ProductResponseDto dto = MapperUtil.map(p, ProductResponseDto.class);
        if (p.getImage() != null) dto.setImage(s3Service.getFileUrl(p.getImage()));
        if (p.getCategory() != null) dto.setCategoryId(p.getCategory().getId());
        if (p.getUser() != null) dto.setUserId(p.getUser().getId());
        return dto;
    }

    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream().map(p -> {
            ProductResponseDto d = MapperUtil.map(p, ProductResponseDto.class);
            if (p.getImage() != null) d.setImage(s3Service.getFileUrl(p.getImage()));
            if (p.getCategory() != null) d.setCategoryId(p.getCategory().getId());
            if (p.getUser() != null) d.setUserId(p.getUser().getId());
            return d;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto updateProduct(MultipartFile file, String productJson) throws IOException {
        ProductUpdateRequest req = objectMapper.readValue(productJson, ProductUpdateRequest.class);

        Product existing = productRepository.findById(req.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found " + req.getId()));

        // map fields from request to entity
        MapperUtil.map(req, existing);

        // check category change
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found " + req.getCategoryId()));
            existing.setCategory(cat);
        }

        // if new file -> upload and set new key
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            existing.setImage(key);
        }

        Product saved = productRepository.save(existing);
        ProductResponseDto dto = MapperUtil.map(saved, ProductResponseDto.class);
        if (saved.getImage() != null) dto.setImage(s3Service.getFileUrl(saved.getImage()));
        if (saved.getCategory() != null) dto.setCategoryId(saved.getCategory().getId());
        if (saved.getUser() != null) dto.setUserId(saved.getUser().getId());
        return dto;
    }

    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found " + id));
        productRepository.delete(p);
    }
}
