package com.racoonsfinds.backend.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racoonsfinds.backend.dto.products.PagedResponse;
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
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil; 
import org.springframework.data.domain.Pageable;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProductResponseDto createProduct(MultipartFile file, ProductRequestDto req) throws IOException {
        Product product = MapperUtil.map(req, Product.class);
        product.setCreatedDate(LocalDate.now());
        product.setEliminado(false);

        // === Usuario autenticado ===
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + userId));
        product.setUser(user);

        // === Categoría ===
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + req.getCategoryId()));
            product.setCategory(category);
        }

        // === Imagen S3 ===
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            product.setImage(key);
        }

        Product saved = productRepository.save(product);
        return MapperUtil.map(saved, ProductResponseDto.class);
    }

    public PagedResponse<ProductResponseDto> findAllPagedByUserId(
            int page,
            int size,
            Long categoryId,
            String search,
            String sortBy,
            String sortDir
    ) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }

        // Seguridad: limitar tamaño máximo
        size = Math.min(size, 50);
        page = Math.max(page, 0);

        // Configurar orden dinámico
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Sanitizar búsqueda
        String searchTerm = (search != null) ? search.trim() : null;

        // Repositorio con filtro dinámico (usando @Query)
        Page<Product> products = productRepository.searchProductsByUser(
                userId, categoryId, searchTerm, pageable
        );

        // Mapear a DTO
        List<ProductResponseDto> dtoList = products
                .stream()
                .map(this::mapToDto)
                .toList();

        return new PagedResponse<>(
                dtoList,
                products.getNumber(),
                products.getTotalPages(),
                products.getTotalElements(),
                products.getSize()
        );
    }


    public PagedResponse<ProductResponseDto> findAllPaged(
            int page,
            int size,
            Long categoryId,
            String search,
            String sortBy,
            String sortDir
    ) {
        // Seguridad: limitar tamaño máximo
        size = Math.min(size, 50);
        page = Math.max(page, 0);

        // Configurar orden dinámico
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Sanitizar búsqueda
        String searchTerm = (search != null) ? search.trim() : null;

        Page<Product> products;

        // Filtro combinado flexible
        if (categoryId != null && searchTerm != null && !searchTerm.isEmpty()) {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCaseOrCategoryIdAndDescriptionContainingIgnoreCase(
                    categoryId, searchTerm, categoryId, searchTerm, pageable
            );
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    searchTerm, searchTerm, pageable
            );
        } else {
            products = productRepository.findAll(pageable);
        }

        // Mapear resultados
        List<ProductResponseDto> dtoList = products
                .stream()
                .map(this::mapToDto)
                .toList();

        // Estructura de respuesta
        return new PagedResponse<>(
                dtoList,
                products.getNumber(),
                products.getTotalPages(),
                products.getTotalElements(),
                products.getSize()
        );
    }


    public ProductResponseDto getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));
        return mapToDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, MultipartFile file, ProductRequestDto req) throws IOException {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));

        // Actualizamos los campos si vienen valores
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getStock() != null) existing.setStock(req.getStock());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());

        // Actualizar categoría si se envía
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + req.getCategoryId()));
            existing.setCategory(cat);
        }

        // Actualizar imagen solo si llega una nueva
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            existing.setImage(key);
        }

        Product saved = productRepository.save(existing);
        return MapperUtil.map(saved, ProductResponseDto.class);
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));

        // Si ya está eliminado, no hacemos nada
        if (Boolean.TRUE.equals(p.getEliminado())) {
            return;
        }

        // Marcamos como eliminado (borrado lógico)
        p.setEliminado(true);
        productRepository.save(p);
    }


    // === PRIVATE MAPPER ===
    private ProductResponseDto mapToDto(Product p) {
        ProductResponseDto dto = MapperUtil.map(p, ProductResponseDto.class);

        if (p.getImage() != null)
            dto.setImage(s3Service.getFileUrl(p.getImage()));

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }

        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            dto.setUserName(p.getUser().getUsername());
        }

        return dto;
    }
}
