package com.racoonsfinds.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.dto.category.*;
import com.racoonsfinds.backend.shared.exception.ResourceNotFoundException;
import com.racoonsfinds.backend.model.Category;
import com.racoonsfinds.backend.repository.CategoryRepository;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDto create(CategoryRequestDto dto) {
        Category entity = MapperUtil.map(dto, Category.class);
        Category saved = categoryRepository.save(entity);
        return MapperUtil.map(saved, CategoryResponseDto.class);
    }

    public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found " + id));
        // map fields from dto to entity
        MapperUtil.map(dto, existing);
        existing.setId(id); // aseguramos id
        Category saved = categoryRepository.save(existing);
        return MapperUtil.map(saved, CategoryResponseDto.class);
    }

    public void delete(Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found " + id));
        categoryRepository.delete(existing);
    }

    public List<CategoryResponseDto> findAll() {
        return MapperUtil.mapList(categoryRepository.findAll(), CategoryResponseDto.class);
    }

    public CategoryResponseDto findById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found " + id));
        return MapperUtil.map(c, CategoryResponseDto.class);
    }
}
