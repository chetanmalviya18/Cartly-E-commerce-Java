package com.cartly.service;

import java.util.List;

import com.cartly.dao.CategoryDAO;
import com.cartly.dto.CategoryResponse;
import com.cartly.entity.Category;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public CategoryResponse createCategory(String name, String description) {

        validateName(name);

        name = name.trim();

        Category existingCategory = categoryDAO.findByName(name);

        if (existingCategory != null) {
            throw new IllegalArgumentException(
                    "Category already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        categoryDAO.save(category);

        return toResponse(category);
    }

    public CategoryResponse getCategoryById(Long id) {

        validateId(id);

        Category category = categoryDAO.findById(id);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found");
        }

        return toResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryDAO.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse updateCategory(
            Long id,
            String name,
            String description) {

        validateId(id);
        validateName(name);

        Category category = categoryDAO.findById(id);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found");
        }

        name = name.trim();

        Category existingCategory = categoryDAO.findByName(name);

        if (existingCategory != null
                && !existingCategory.getId().equals(id)) {

            throw new IllegalArgumentException(
                    "Category already exists");
        }

        category.setName(name);
        category.setDescription(description);

        categoryDAO.update(category);

        return toResponse(category);
    }

    private void validateName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Category name is required");
        }
    }

    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid category ID");
        }
    }

    private CategoryResponse toResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        return response;
    }
}