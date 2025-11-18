package com.racoonsfinds.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
