package com.example.billing_backend.controller;

import com.example.billing_backend.model.Category;
import com.example.billing_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*") // React frontend connect aaga
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. CREATE - Add new category
    @PostMapping("/add")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    // 2. READ - Get all categories (For Dropdowns)
    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // 3. READ - Get single category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // 4. UPDATE - Edit existing category
    @PutMapping("/update/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    // 5. DELETE - Remove a category
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully!");
    }
}