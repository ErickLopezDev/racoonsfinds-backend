package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.category.CategoryRequestDto;
import com.racoonsfinds.backend.dto.category.CategoryResponseDto;
import com.racoonsfinds.backend.model.Category;
import com.racoonsfinds.backend.repository.CategoryRepository;
import com.racoonsfinds.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_ShouldSaveAndMap() {
        CategoryRequestDto req = new CategoryRequestDto();
        req.setName("Cat");

        Category saved = new Category(); saved.setId(1L); saved.setName("Cat");
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponseDto resp = categoryService.create(req);
        assertNotNull(resp);
        assertEquals("Cat", resp.getName());
    }

    @Test
    void update_ShouldFindAndSave() {
        Category existing = new Category(); existing.setId(2L); existing.setName("Old");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));

        Category updated = new Category(); updated.setId(2L); updated.setName("New");
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryRequestDto req = new CategoryRequestDto(); req.setName("New");
        var resp = categoryService.update(2L, req);
        assertEquals("New", resp.getName());
    }

    @Test
    void delete_ShouldRemove() {
        Category existing = new Category(); existing.setId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        categoryService.delete(3L);
        verify(categoryRepository).delete(existing);
    }

    @Test
    void findAll_ShouldMapList() {
        Category c1 = new Category(); c1.setId(1L); c1.setName("A");
        Category c2 = new Category(); c2.setId(2L); c2.setName("B");
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));
        var list = categoryService.findAll();
        assertEquals(2, list.size());
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(9L));
    }
}

