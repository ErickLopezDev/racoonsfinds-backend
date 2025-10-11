package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.category.*;
import com.racoonsfinds.backend.service.CategoryService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> create(@RequestBody CategoryRequestDto dto) {
        return ResponseUtil.created("Categoría creada correctamente", categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> update(@PathVariable Long id, @RequestBody CategoryRequestDto dto) {
        return ResponseUtil.ok("Categoría actualizada correctamente", categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseUtil.ok("Categoría eliminada correctamente");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> all() {
        return ResponseUtil.ok("Categorías obtenidas correctamente", categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> get(@PathVariable Long id) {
        return ResponseUtil.ok("Categoría obtenida correctamente", categoryService.findById(id));
    }
}
