package com.racoonsfinds.backend.controller;

import com.racoonsfinds.backend.dto.products.PagedResponse;
import com.racoonsfinds.backend.dto.products.ProductResponseDto;
import com.racoonsfinds.backend.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private ProductServiceImpl productService;
    @InjectMocks private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    // @Test
    // void create_ShouldReturnCreated() throws Exception {
    //     MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1,2});
    //     MockMultipartFile json = new MockMultipartFile("product", "", "application/json", "{\"name\":\"N\"}".getBytes());

    //     ProductResponseDto dto = ProductResponseDto.builder().id(1L).name("N").build();
    //     when(productService.createProduct(any(), anyString())).thenReturn(dto);

    //     mockMvc.perform(multipart("/api/products").file(file).file(json))
    //         .andExpect(status().isCreated())
    //         .andExpect(jsonPath("$.data.name").value("N"));
    // }

    // @Test
    // void update_ShouldReturnOk() throws Exception {
    //     MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
    //     MockMultipartFile json = new MockMultipartFile("product", "", "application/json", "{\"name\":\"X\"}".getBytes());

    //     ProductResponseDto dto = ProductResponseDto.builder().id(2L).name("X").build();
    //     when(productService.updateProduct(eq(2L), any(), anyString())).thenReturn(dto);

    //     mockMvc.perform(multipart("/api/products/{id}", 2L)
    //             .file(file).file(json)
    //             .with(request -> { request.setMethod("PUT"); return request; }))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.data.id").value(2))
    //         .andExpect(jsonPath("$.data.name").value("X"));
    // }

    @Test
    void get_ShouldReturnOne() throws Exception {
        ProductResponseDto dto = ProductResponseDto.builder().id(5L).name("A").build();
        when(productService.getById(5L)).thenReturn(dto);
        mockMvc.perform(get("/api/products/{id}", 5))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("A"));
    }

    @Test
    void all_ShouldReturnPaged() throws Exception {
        PagedResponse<ProductResponseDto> page = new PagedResponse<>(List.of(), 0, 0, 0, 10);
        when(productService.findAllPaged(anyInt(), anyInt(), any(), any(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    void delete_ShouldReturnOk() throws Exception {
        doNothing().when(productService).delete(3L);
        mockMvc.perform(delete("/api/products/{id}", 3))
            .andExpect(status().isOk());
        verify(productService).delete(3L);
    }
}

