package com.example.billing_backend.service;

import com.example.billing_backend.model.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    List<Category> getAllCategories();

    // Missing aana indha 3 puthu methods:
    Category getCategoryById(Long id);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}