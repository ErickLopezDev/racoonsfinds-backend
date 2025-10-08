package com.racoonsfinds.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.racoonsfinds.backend.dto.products.ProductRequestDto;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.model.Category;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.repository.CategoryRepository;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private S3Service s3Service;

    // Create a new product with optional image
    public ProductResponseDto createProduct(ProductRequestDto productRequestDTO, MultipartFile image) {
        // Validate category
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + productRequestDTO.getCategoryId()));

        // Map DTO to entity
        Product product = MapperUtil.map(productRequestDTO, Product.class);
        product.setCategory(category);
        product.setEliminado(false);
        
        // Set current date if not provided
        if (product.getCreatedDate() == null) {
            product.setCreatedDate(LocalDate.now());
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image);
            product.setImage(imageUrl);
        }

        // Save product
        Product savedProduct = productRepository.save(product);

        // Map entity to response DTO
        ProductResponseDto responseDTO = MapperUtil.map(savedProduct, ProductResponseDto.class);
        responseDTO.setCategoryName(category.getName());
        responseDTO.setImage(savedProduct.getImage()); // Ensure image URL is set in DTO
        return responseDTO;
    }

    // Get a product by ID
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getEliminado()) // Exclude soft-deleted products
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        ProductResponseDto responseDTO = MapperUtil.map(product, ProductResponseDto.class);
        responseDTO.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        responseDTO.setImage(product.getImage()); // Include image URL
        return responseDTO;
    }

    // Get all products
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAllByEliminadoFalse();
        return MapperUtil.mapList(products, ProductResponseDto.class).stream()
                .map(dto -> {
                    Optional<Product> productOpt = products.stream()
                            .filter(p -> p.getId().equals(dto.getId()))
                            .findFirst();
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
                        dto.setImage(product.getImage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Update a product with optional new image
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDTO, MultipartFile image) {
        // Find existing product
        Product product = productRepository.findById(id)
                .filter(p -> !p.getEliminado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Validate category
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + productRequestDTO.getCategoryId()));

        // Map DTO to existing entity
        MapperUtil.map(productRequestDTO, product);
        product.setCategory(category);

        // Handle image update
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (product.getImage() != null) {
                s3Service.deleteFile(product.getImage());
            }
            // Upload new image
            String newImageUrl = s3Service.uploadFile(image);
            product.setImage(newImageUrl);
        }

        // Save updated product
        Product updatedProduct = productRepository.save(product);

        // Map entity to response DTO
        ProductResponseDto responseDTO = MapperUtil.map(updatedProduct, ProductResponseDto.class);
        responseDTO.setCategoryName(category.getName());
        responseDTO.setImage(updatedProduct.getImage());
        return responseDTO;
    }

    // Soft delete a product and remove image from S3
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getEliminado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));
        
        // Delete image from S3 if exists
        if (product.getImage() != null) {
            s3Service.deleteFile(product.getImage());
        }
        
        product.setEliminado(true);
        productRepository.save(product);
    }
}