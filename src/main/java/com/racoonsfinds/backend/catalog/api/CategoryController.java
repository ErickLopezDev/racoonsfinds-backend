package com.racoonsfinds.backend.catalog.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.shared.dto.ApiResponse;
import com.racoonsfinds.backend.catalog.dto.*;
import com.racoonsfinds.backend.catalog.service.CategoryService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> all() {
        return ResponseUtil.ok("Categorías obtenidas correctamente", categoryService.findAll());
    }
}
