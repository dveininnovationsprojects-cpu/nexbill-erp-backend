package com.example.billing_backend.service; // Fixed: Matches your current folder structure

import com.example.billing_backend.model.Category;
import com.example.billing_backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with this name already exists!");
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // --- NEWLY ADDED CRUD METHODS BELOW ---

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public Category updateCategory(Long id, Category categoryDetails) {
        // First, check if the old category exists in the database
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update with the newly provided details
        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());

        // Save the updated data
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        // Check if the category exists before trying to delete it
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}