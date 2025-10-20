package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.model.Category;
import com.racoonsfinds.backend.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAll_ShouldMapList() {
        Category c1 = new Category(); c1.setId(1L); c1.setName("A");
        Category c2 = new Category(); c2.setId(2L); c2.setName("B");
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));
        var list = categoryService.findAll();
        assertEquals(2, list.size());
    }
}

