package com.racoonsfinds.backend.catalog.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.catalog.dto.PagedResponse;
import com.racoonsfinds.backend.catalog.dto.ProductRequestDto;
import com.racoonsfinds.backend.catalog.dto.ProductResponseDto;

public interface ProductService {
  ProductResponseDto createProduct(MultipartFile file, ProductRequestDto req) throws IOException;
  PagedResponse<ProductResponseDto> findAllPagedByUserId(
            int page,
            int size,
            Long categoryId,
            String search,
            String sortBy,
            String sortDir
    );
  PagedResponse<ProductResponseDto> findAllPaged(
            int page,
            int size,
            Long categoryId,
            String search,
            String sortBy,
            String sortDir
    );
  ProductResponseDto getById(Long id);
  ProductResponseDto updateProduct(Long id, MultipartFile file, ProductRequestDto req) throws IOException;
  void delete(Long id);
}
