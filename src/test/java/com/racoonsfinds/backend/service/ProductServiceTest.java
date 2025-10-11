package com.racoonsfinds.backend.service;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3Service s3Service;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setupAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @AfterEach
    void clearAuth() { SecurityContextHolder.clearContext(); }

    @Test
    void createProduct_ShouldPersist_WithImageAndRelations() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(eq(file), eq("products"))).thenReturn("products/x-a.png");

        ProductRequestDto req = new ProductRequestDto();
        req.setName("N");
        req.setStock(5);
        req.setPrice(new BigDecimal("10.50"));
        req.setDescription("D");
        req.setCategoryId(2L);

        when(objectMapper.readValue(anyString(), eq(ProductRequestDto.class))).thenReturn(req);

        User user = new User(); user.setId(1L); user.setUsername("u");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Category cat = new Category(); cat.setId(2L); cat.setName("C");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));

        Product saved = new Product();
        saved.setId(10L); saved.setName("N"); saved.setStock(5);
        saved.setPrice(new BigDecimal("10.50")); saved.setDescription("D");
        saved.setCategory(cat); saved.setUser(user); saved.setImage("products/x-a.png");
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(s3Service.getFileUrl("products/x-a.png")).thenReturn("URL");

        ProductResponseDto dto = productService.createProduct(file, "{json}");

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("N", dto.getName());
        assertEquals("URL", dto.getImage());
        assertEquals(2L, dto.getCategoryId());
        assertEquals("C", dto.getCategoryName());
        assertEquals(1L, dto.getUserId());
        assertEquals("u", dto.getUserName());
    }

    @Test
    void findAllPaged_ShouldCallFindAll_WhenNoFilters() {
        Product p = new Product(); p.setId(1L); p.setName("A");
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        var resp = productService.findAllPaged(0, 10, null, null, "id", "asc");

        assertNotNull(resp);
        assertEquals(1, resp.getTotalItems());
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void getById_ShouldMapDto() {
        Product p = new Product(); p.setId(5L); p.setName("X");
        User u = new User(); u.setId(1L); u.setUsername("u");
        Category c = new Category(); c.setId(2L); c.setName("C");
        p.setUser(u); p.setCategory(c); p.setImage("k");
        when(s3Service.getFileUrl("k")).thenReturn("URL");
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));

        var dto = productService.getById(5L);
        assertEquals(5L, dto.getId());
        assertEquals("URL", dto.getImage());
        assertEquals(2L, dto.getCategoryId());
        assertEquals("C", dto.getCategoryName());
        assertEquals(1L, dto.getUserId());
        assertEquals("u", dto.getUserName());
    }

    @Test
    void update_ShouldReplaceFields_AndImageWhenProvided() throws IOException {
        Product existing = new Product(); existing.setId(5L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));

        Category c = new Category(); c.setId(2L); c.setName("C");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(c));

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(eq(file), eq("products"))).thenReturn("products/new.png");
        
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("N"); req.setStock(10); req.setPrice(new BigDecimal("3.50"));
        req.setDescription("D"); req.setEliminado(false); req.setCategoryId(2L);
        when(objectMapper.readValue(anyString(), eq(ProductUpdateRequest.class))).thenReturn(req);

        Product saved = new Product(); saved.setId(5L); saved.setName("N"); saved.setStock(10);
        saved.setPrice(new BigDecimal("3.50")); saved.setDescription("D"); saved.setEliminado(false);
        saved.setCategory(c); saved.setImage("products/new.png");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        var dto = productService.updateProduct(5L, file, "{json}");
        assertEquals("N", dto.getName());
        assertEquals(10, dto.getStock());
        assertEquals(2L, dto.getCategoryId());
    }

    @Test
    void delete_ShouldSoftDelete_WhenNotDeleted() {
        Product p = new Product(); p.setId(1L); p.setEliminado(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        productService.delete(1L);
        assertTrue(p.getEliminado());
        verify(productRepository).save(p);
    }

    @Test
    void delete_ShouldDoNothing_WhenAlreadyDeleted() {
        Product p = new Product(); p.setId(1L); p.setEliminado(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        productService.delete(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_ShouldThrow_WhenNoAuthenticatedUser() throws Exception {
        // clear authentication to simulate no user
        SecurityContextHolder.clearContext();

        when(objectMapper.readValue(anyString(), eq(ProductRequestDto.class)))
                .thenReturn(new ProductRequestDto());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(null, "{}"));
    }
}
