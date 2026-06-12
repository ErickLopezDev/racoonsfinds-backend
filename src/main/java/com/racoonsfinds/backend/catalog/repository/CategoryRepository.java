package com.racoonsfinds.backend.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.catalog.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
