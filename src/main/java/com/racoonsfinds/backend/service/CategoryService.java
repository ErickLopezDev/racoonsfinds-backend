package com.racoonsfinds.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.dto.category.CategoryResponseDto;
import com.racoonsfinds.backend.repository.CategoryRepository;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> findAll() {
        return MapperUtil.mapList(categoryRepository.findAll(), CategoryResponseDto.class);
    }
}
