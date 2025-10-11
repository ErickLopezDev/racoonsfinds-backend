package com.racoonsfinds.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racoonsfinds.backend.dto.category.CategoryRequestDto;
import com.racoonsfinds.backend.dto.category.CategoryResponseDto;
import com.racoonsfinds.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private CategoryService categoryService;
    @InjectMocks private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void create_ShouldReturnCreated() throws Exception {
        CategoryRequestDto req = new CategoryRequestDto(); req.setName("Cat");
        CategoryResponseDto resp = new CategoryResponseDto(); resp.setId(1); resp.setName("Cat");
        when(categoryService.create(any(CategoryRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Cat"));
    }

    @Test
    void update_ShouldReturnOk() throws Exception {
        CategoryRequestDto req = new CategoryRequestDto(); req.setName("New");
        CategoryResponseDto resp = new CategoryResponseDto(); resp.setId(2); resp.setName("New");
        when(categoryService.update(eq(2L), any(CategoryRequestDto.class))).thenReturn(resp);

        mockMvc.perform(put("/api/categories/{id}", 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("New"));
    }

    @Test
    void delete_ShouldReturnOk() throws Exception {
        doNothing().when(categoryService).delete(3L);
        mockMvc.perform(delete("/api/categories/{id}", 3))
            .andExpect(status().isOk());
        verify(categoryService).delete(3L);
    }

    @Test
    void all_ShouldReturnList() throws Exception {
        CategoryResponseDto a = new CategoryResponseDto(); a.setId(1); a.setName("A");
        when(categoryService.findAll()).thenReturn(List.of(a));
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("A"));
    }

    @Test
    void get_ShouldReturnOne() throws Exception {
        CategoryResponseDto a = new CategoryResponseDto(); a.setId(5); a.setName("X");
        when(categoryService.findById(5L)).thenReturn(a);
        mockMvc.perform(get("/api/categories/{id}", 5))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("X"));
    }
}

