package com.racoonsfinds.backend.catalog.service;

import com.racoonsfinds.backend.platform.storage.S3Service;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.catalog.dto.PagedResponse;
import com.racoonsfinds.backend.catalog.dto.ProductRequestDto;
import com.racoonsfinds.backend.catalog.dto.ProductResponseDto;
import com.racoonsfinds.backend.catalog.domain.Category;
import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.catalog.repository.CategoryRepository;
import com.racoonsfinds.backend.catalog.repository.ProductRepository;
import com.racoonsfinds.backend.catalog.service.ProductService;
import com.racoonsfinds.backend.identity.port.UserDirectoryPort;
import com.racoonsfinds.backend.identity.port.UserSnapshot;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.catalog.mapper.CatalogMapper;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserDirectoryPort userDirectoryPort;
    private final S3Service s3Service;

    public static final String PRODUCT_ID_NOT_FOUND = "Product not found with ID ";

    @Transactional
    public ProductResponseDto createProduct(MultipartFile file, ProductRequestDto req) throws IOException {
        Product product = CatalogMapper.map(req, Product.class);
        product.setCreatedDate(LocalDate.now());
        product.setEliminado(false);

        // === Usuario autenticado ===
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) {
            throw new NotFoundException("Authenticated user not found");
        }

        UserSnapshot userSnapshot = userDirectoryPort.findById(userId);
        product.setUserId(userSnapshot.id());

        // === Categoría ===
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with ID " + req.getCategoryId()));
            product.setCategory(category);
        }

        // === Imagen S3 ===
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            product.setImage(key);
        }

        Product saved = productRepository.save(product);
        return CatalogMapper.map(saved, ProductResponseDto.class);
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
            throw new NotFoundException("Usuario no autenticado");
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
        Page<Product> products;
        if (searchTerm == null && categoryId == null) {
            products = productRepository.findAllByUserId(userId, pageable);
        } else if (searchTerm == null) {
            products = productRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        } else {
            products = productRepository.searchProductsByUserAndText(userId, categoryId, searchTerm, pageable);
        }

        // Mapear a DTO
        List<ProductResponseDto> dtoList = mapToDtoList(products.getContent());

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
        List<ProductResponseDto> dtoList = mapToDtoList(products.getContent());

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
                .orElseThrow(() -> new NotFoundException(PRODUCT_ID_NOT_FOUND + id));
        return mapToDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, MultipartFile file, ProductRequestDto req) throws IOException {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_ID_NOT_FOUND + id));

        // Actualizamos los campos si vienen valores
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getStock() != null) existing.setStock(req.getStock());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());

        // Actualizar categoría si se envía
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with ID " + req.getCategoryId()));
            existing.setCategory(cat);
        }

        // Actualizar imagen solo si llega una nueva
        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "products");
            existing.setImage(key);
        }

        Product saved = productRepository.save(existing);
        return CatalogMapper.map(saved, ProductResponseDto.class);
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_ID_NOT_FOUND + id));

        // Si ya está eliminado, no hacemos nada
        if (Boolean.TRUE.equals(p.getEliminado())) {
            return;
        }

        // Marcamos como eliminado (borrado lógico)
        p.setEliminado(true);
        productRepository.save(p);
    }


    // === PRIVATE MAPPERS ===
    private List<ProductResponseDto> mapToDtoList(List<Product> products) {
        List<Long> userIds = products.stream()
                .map(Product::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        java.util.Map<Long, String> userNames = userDirectoryPort.findAllByIds(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(UserSnapshot::id, UserSnapshot::username));

        return products.stream()
                .map(p -> mapToDto(p, userNames.get(p.getUserId())))
                .toList();
    }

    private ProductResponseDto mapToDto(Product p) {
        String userName = p.getUserId() != null
                ? userDirectoryPort.findById(p.getUserId()).username()
                : null;
        return mapToDto(p, userName);
    }

    private ProductResponseDto mapToDto(Product p, String userName) {
        ProductResponseDto dto = CatalogMapper.map(p, ProductResponseDto.class);

        if (p.getImage() != null)
            dto.setImage(s3Service.getFileUrl(p.getImage()));

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }

        dto.setUserName(userName);

        dto.setAverageRating(p.getAverageRating() != null ? p.getAverageRating() : 0.0);
        dto.setReviewCount(p.getReviewCount() != null ? p.getReviewCount() : 0L);

        return dto;
    }
}
