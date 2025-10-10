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
    public ProductResponseDto createProduct(MultipartFile file, String productJson) throws IOException {
        ProductRequestDto req = objectMapper.readValue(productJson, ProductRequestDto.class);

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
        return mapToDto(saved);
    }


    public PagedResponse<ProductResponseDto> findAllPaged(
            int page,
            int size,
            Long categoryId,
            String search,
            String sortBy,
            String sortDir) {

        // Límite de tamaño máximo por seguridad
        if (size > 50) size = 50;

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;

        if (categoryId != null && search != null && !search.isEmpty()) {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, search, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (search != null && !search.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        // Mapear resultados a DTO
        List<ProductResponseDto> dtoList = products
                .map(p -> MapperUtil.map(p, ProductResponseDto.class))
                .getContent();

        // Retornar estructura con metadata
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
    public ProductResponseDto updateProduct(Long id, MultipartFile file, String productJson) throws IOException {
        ProductUpdateRequest req = objectMapper.readValue(productJson, ProductUpdateRequest.class);

        // El ID de la ruta siempre manda
        if (req.getId() == null) {
            req.setId(id);
        }

        Product existing = productRepository.findById(req.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + req.getId()));

        // ✅ Actualizamos manualmente los campos simples (sin tocar relaciones ni ID)
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getStock() != null) existing.setStock(req.getStock());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getEliminado() != null) existing.setEliminado(req.getEliminado());

        // ✅ Actualizamos categoría solo si viene un ID válido
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + req.getCategoryId()));
            existing.setCategory(cat);
        }

        // ✅ Si se envía un nuevo archivo, se reemplaza la imagen
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            existing.setImage(key);
        }

        // Guardamos los cambios
        Product saved = productRepository.save(existing);
        return mapToDto(saved);
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
