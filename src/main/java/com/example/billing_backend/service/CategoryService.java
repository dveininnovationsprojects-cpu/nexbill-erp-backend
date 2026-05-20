package com.example.billing_backend.service;

import com.example.billing_backend.model.Category;
import java.util.List;

public interface CategoryService {

    // Create operation
    Category createCategory(Category category);

    // Read operations
    List<Category> getAllCategories();
    Category getCategoryById(Long id);

    // Update operation
    Category updateCategory(Long id, Category category);

    // Delete operation
    void deleteCategory(Long id);
}